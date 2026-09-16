import type { ChatRoomStatus } from '@/types/chat'

export const CHAT_STATUS_LABEL: Record<ChatRoomStatus, string> = {
  WAITING: '대기중',
  IN_PROGRESS: '상담중',
  COMPLETED: '완료',
}
