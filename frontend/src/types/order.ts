// 백엔드 OrderStatus: PENDING(결제 전) -> PAID(결제 완료) / CANCELLED
export type OrderStatus = 'PENDING' | 'PAID' | 'CANCELLED'
export type PaymentStatus = 'READY' | 'SUCCESS' | 'FAILED' | 'PARTIALLY_CANCELLED' | 'CANCELLED'

export interface OrderListItem {
  productName: string
  productPrice: number
  quantity: number
}

export interface OrderItem {
  orderItemId: number
  productId: number
  productName: string
  productPrice: number
  quantity: number
}

// GET /api/orders (내 주문 목록)
export interface OrderSummary {
  orderId: number
  orderNumber: string
  items: OrderListItem[]
  totalPrice: number
  createdAt: string
  paidAt: string | null
}

// GET /api/orders/{id} (주문 상세)
export interface Order {
  orderNumber: string
  totalPrice: number
  createdAt: string
  paidAt: string | null
  orderStatus: OrderStatus
  paymentStatus: PaymentStatus
  orderItems: OrderItem[]
}

// POST /api/orders 응답: cartItemIds가 없으면(빈 배열) 장바구니 전체 주문
export interface OrderCreateResponse {
  orderId: number
  orderNumber: string
  totalPrice: number
  createdAt: string
  status: OrderStatus
  portonePaymentId: string
  items: OrderListItem[]
}

export interface PaymentConfirmResponse {
  orderId: number
  portonePaymentId: string
  paymentStatus: PaymentStatus
  orderStatus: OrderStatus
}

export interface CancelOrderResponse {
  orderId: number
  status: OrderStatus
  cancelledAt: string
}
