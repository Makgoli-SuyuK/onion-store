import { delay } from './helpers'
import { getCurrentMockUser } from './authMock'
import type { PageResponse } from '@/types/common'
import type {
  ChatMessage,
  ChatMessagePage,
  ChatRoomDetail,
  ChatRoomStatus,
  ChatRoomStatusUpdate,
  ChatRoomSummary,
} from '@/types/chat'

interface MockRoom extends ChatRoomSummary {
  customerName: string
}

let rooms: MockRoom[] = [
  {
    roomId: 1,
    title: '주문한 양파즙이 안 와요',
    status: 'IN_PROGRESS',
    customerId: 1,
    customerName: 'tester',
    createdAt: '2026-09-10T09:00:00+09:00',
  },
  {
    roomId: 2,
    title: '자색양파 상품 관련 문의',
    status: 'COMPLETED',
    customerId: 1,
    customerName: 'tester',
    createdAt: '2026-09-05T14:00:00+09:00',
  },
]
let nextRoomId = 3

const messages = new Map<number, ChatMessage[]>([
  [
    1,
    [
      { messageId: 1, senderId: 1, senderName: 'tester', message: '안녕하세요, 주문한 상품이 아직 안 왔어요.', sentAt: '2026-09-10T09:00:10+09:00' },
      { messageId: 2, senderId: 9000, senderName: '어니언즈 관리자', message: '안녕하세요! 주문번호 확인해드릴게요, 잠시만요.', sentAt: '2026-09-10T09:02:00+09:00' },
    ],
  ],
  [
    2,
    [
      { messageId: 3, senderId: 1, senderName: 'tester', message: '자색양파 매운 정도가 어느정도인가요?', sentAt: '2026-09-05T14:00:20+09:00' },
      { messageId: 4, senderId: 9000, senderName: '어니언즈 관리자', message: '일반 양파보다 순한 편이에요!', sentAt: '2026-09-05T14:05:00+09:00' },
      { messageId: 5, senderId: 1, senderName: 'tester', message: '감사합니다 :)', sentAt: '2026-09-05T14:06:00+09:00' },
    ],
  ],
])
let nextMessageId = 6

function requireUser() {
  const user = getCurrentMockUser()
  if (!user) throw new Error('로그인이 필요한 서비스입니다.')
  return user
}

export async function mockCreateRoom(title: string): Promise<ChatRoomSummary> {
  await delay(250)
  const user = requireUser()
  if (user.role === 'ADMIN') throw new Error('관리자는 문의방을 생성할 수 없습니다.')

  const room: MockRoom = {
    roomId: nextRoomId++,
    title,
    status: 'WAITING',
    customerId: user.userId,
    customerName: user.name,
    createdAt: new Date().toISOString(),
  }
  rooms = [room, ...rooms]
  messages.set(room.roomId, [])
  return room
}

export async function mockGetRooms(
  status: ChatRoomStatus | undefined,
  page: number,
  size: number,
): Promise<PageResponse<ChatRoomSummary>> {
  await delay(250)
  const user = requireUser()
  const filtered =
    user.role === 'ADMIN'
      ? status
        ? rooms.filter((r) => r.status === status)
        : rooms
      : rooms.filter((r) => r.customerId === user.userId)

  const start = page * size
  const content = filtered.slice(start, start + size)
  return {
    content,
    totalElements: filtered.length,
    totalPages: Math.max(1, Math.ceil(filtered.length / size)),
    page,
    size,
  }
}

export async function mockGetRoom(roomId: number): Promise<ChatRoomDetail> {
  await delay(200)
  const user = requireUser()
  const room = rooms.find((r) => r.roomId === roomId)
  if (!room) throw new Error('채팅방을 찾을 수 없습니다.')
  if (user.role === 'CUSTOMER' && room.customerId !== user.userId) {
    throw new Error('해당 채팅방에 참여하지 않은 사용자입니다.')
  }
  return {
    roomId: room.roomId,
    title: room.title,
    status: room.status,
    customerId: room.customerId,
    customerName: room.customerName,
    createdDate: room.createdAt,
  }
}

export async function mockGetMessages(
  roomId: number,
  lastMessageId: number | undefined,
  size: number,
): Promise<ChatMessagePage> {
  await delay(200)
  requireUser()
  const all = (messages.get(roomId) ?? []).slice().sort((a, b) => b.messageId - a.messageId)
  const startIdx = lastMessageId ? all.findIndex((m) => m.messageId < lastMessageId) : 0
  const from = startIdx === -1 ? all.length : Math.max(startIdx, 0)
  const page = all.slice(from, from + size + 1)
  const hasNext = page.length > size
  const rows = hasNext ? page.slice(0, size) : page
  return {
    messages: rows.slice().reverse(),
    nextCursor: rows.length > 0 ? rows[rows.length - 1].messageId : null,
    hasNext,
  }
}

export async function mockUpdateStatus(roomId: number, status: ChatRoomStatus): Promise<ChatRoomStatusUpdate> {
  await delay(200)
  const user = requireUser()
  if (user.role !== 'ADMIN') throw new Error('관리자만 상태를 변경할 수 있습니다.')
  const room = rooms.find((r) => r.roomId === roomId)
  if (!room) throw new Error('채팅방을 찾을 수 없습니다.')
  if (room.status === 'COMPLETED' && status !== 'COMPLETED') {
    throw new Error('완료된 문의는 상태를 되돌릴 수 없습니다.')
  }
  room.status = status
  return { roomId: room.roomId, status: room.status, updateAt: new Date().toISOString() }
}

// mock 모드에서는 실제 WebSocket 없이 이 함수가 메시지 저장 + "브로드캐스트"를 흉내낸다.
export async function mockSendMessage(roomId: number, message: string): Promise<ChatMessage> {
  await delay(150)
  const user = requireUser()
  const room = rooms.find((r) => r.roomId === roomId)
  if (!room) throw new Error('채팅방을 찾을 수 없습니다.')
  if (room.status === 'COMPLETED') throw new Error('완료된 문의에는 메시지를 보낼 수 없습니다.')
  if (!message.trim()) throw new Error('메시지 내용이 비어있습니다.')

  const msg: ChatMessage = {
    messageId: nextMessageId++,
    senderId: user.userId,
    senderName: user.name,
    message: message.trim(),
    sentAt: new Date().toISOString(),
  }
  messages.get(roomId)?.push(msg)
  return msg
}
