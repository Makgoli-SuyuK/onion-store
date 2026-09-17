import { http, unwrap, unwrapPage } from './client'
import type { PageResponse } from '@/types/common'
import type { CancelOrderResponse, Order, OrderCreateResponse, OrderSummary } from '@/types/order'

export const orderApi = {
  // cartItemIds가 비어있으면 장바구니 전체를 주문한다 (백엔드 규칙).
  // 가격/재고/판매상태 재확인은 별도 API가 아니라 이 호출 자체가 서버에서 원자적으로 처리한다
  // (재고 부족/품절이면 주문 생성 자체가 실패하고 에러 메시지가 내려온다).
  async createOrder(cartItemIds: number[]): Promise<OrderCreateResponse> {
    const res = await http.post('/api/orders', { cartItemIds })
    return unwrap(res)
  },

  async getOrder(orderId: number): Promise<Order> {
    const res = await http.get(`/api/orders/${orderId}`)
    return unwrap(res)
  },

  async getMyOrders(page: number, size: number, customerId?: number): Promise<PageResponse<OrderSummary>> {
    const res = await http.get('/api/orders', { params: { page, size, customerId } })
    return unwrapPage(res)
  },

  async cancelOrder(orderId: number): Promise<CancelOrderResponse> {
    const res = await http.post(`/api/orders/${orderId}/cancel`)
    return unwrap(res)
  },
}
