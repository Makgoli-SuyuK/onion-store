export type ChatRoomStatus = 'WAITING' | 'IN_PROGRESS' | 'COMPLETED'

// GET /api/chats/rooms 목록 항목
export interface ChatRoomSummary {
  roomId: number
  title: string
  status: ChatRoomStatus
  customerId: number
  createdAt: string
}

// GET /api/chats/rooms/{id} 상세
export interface ChatRoomDetail {
  roomId: number
  title: string
  status: ChatRoomStatus
  customerId: number
  customerName: string
  createdDate: string
}

export interface ChatMessage {
  messageId: number
  senderId: number
  senderName: string
  message: string
  sentAt: string
}

export interface ChatRoomStatusUpdate {
  roomId: number
  status: ChatRoomStatus
  updateAt: string
}

export interface ChatMessagePage {
  messages: ChatMessage[]
  nextCursor: number | null
  hasNext: boolean
}
