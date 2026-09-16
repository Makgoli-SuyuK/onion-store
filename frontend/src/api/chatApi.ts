import { http, unwrap, unwrapPage, USE_MOCK } from './client'
import {
  mockCreateRoom,
  mockGetMessages,
  mockGetRoom,
  mockGetRooms,
  mockUpdateStatus,
} from '@/mocks/chatMock'
import type { PageResponse } from '@/types/common'
import type {
  ChatMessagePage,
  ChatRoomDetail,
  ChatRoomStatus,
  ChatRoomStatusUpdate,
  ChatRoomSummary,
} from '@/types/chat'

export const chatApi = {
  async createRoom(title: string): Promise<ChatRoomSummary> {
    if (USE_MOCK) return mockCreateRoom(title)
    const res = await http.post('/api/chats/rooms', { title })
    return unwrap(res)
  },

  async getRooms(status: ChatRoomStatus | undefined, page: number, size: number): Promise<PageResponse<ChatRoomSummary>> {
    if (USE_MOCK) return mockGetRooms(status, page, size)
    const res = await http.get('/api/chats/rooms', { params: { status, page, size } })
    return unwrapPage(res)
  },

  async getRoom(roomId: number): Promise<ChatRoomDetail> {
    if (USE_MOCK) return mockGetRoom(roomId)
    const res = await http.get(`/api/chats/rooms/${roomId}`)
    return unwrap(res)
  },

  async getMessages(roomId: number, lastMessageId: number | undefined, size = 30): Promise<ChatMessagePage> {
    if (USE_MOCK) return mockGetMessages(roomId, lastMessageId, size)
    const res = await http.get(`/api/chats/rooms/${roomId}/messages`, { params: { lastMessageId, size } })
    return unwrap(res)
  },

  // 관리자 전용
  async updateStatus(roomId: number, status: ChatRoomStatus): Promise<ChatRoomStatusUpdate> {
    if (USE_MOCK) return mockUpdateStatus(roomId, status)
    const res = await http.patch(`/api/admin/chats/rooms/${roomId}/status`, { status })
    return unwrap(res)
  },
}
