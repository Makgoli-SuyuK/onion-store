import type { RefundStatus } from '@/types/refund'

export const REFUND_STATUS_LABEL: Record<RefundStatus, string> = {
  PENDING_APPROVAL: '승인 대기',
  REQUESTED: '취소 처리 중',
  COMPLETED: '환불 완료',
  REJECTED: '환불 거절',
  FAILED: '환불 실패',
}
