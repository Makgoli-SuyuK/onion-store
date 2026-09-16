import type { OrderStatus } from '@/types/order'

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: '결제 대기',
  PAID: '결제 완료',
  CANCELLED: '주문 취소',
}
