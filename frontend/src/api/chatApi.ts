import { http, unwrap, unwrapPage } from './client'
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
    const res = await http.post('/api/chats/rooms', { title })
    return unwrap(res)
  },

  async getRooms(status: ChatRoomStatus | undefined, page: number, size: number): Promise<PageResponse<ChatRoomSummary>> {
    const res = await http.get('/api/chats/rooms', { params: { status, page, size } })
    return unwrapPage(res)
  },

  async getRoom(roomId: number): Promise<ChatRoomDetail> {
    const res = await http.get(`/api/chats/rooms/${roomId}`)
    return unwrap(res)
  },

  async getMessages(roomId: number, lastMessageId: number | undefined, size = 30): Promise<ChatMessagePage> {
    const res = await http.get(`/api/chats/rooms/${roomId}/messages`, { params: { lastMessageId, size } })
    return unwrap(res)
  },

  // 관리자 전용
  async updateStatus(roomId: number, status: ChatRoomStatus): Promise<ChatRoomStatusUpdate> {
    const res = await http.patch(`/api/admin/chats/rooms/${roomId}/status`, { status })
    return unwrap(res)
  },
}
