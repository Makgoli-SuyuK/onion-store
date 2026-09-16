import { http, unwrap, unwrapPage, USE_MOCK } from './client'
import { mockCreateOrder, mockGetMyOrders, mockGetOrder } from '@/mocks/orderMock'
import type { PageResponse } from '@/types/common'
import type { Order, OrderCreateResponse, OrderSummary } from '@/types/order'

export const orderApi = {
  // cartItemIds가 비어있으면 장바구니 전체를 주문한다 (백엔드 규칙).
  // 가격/재고/판매상태 재확인은 별도 API가 아니라 이 호출 자체가 서버에서 원자적으로 처리한다
  // (재고 부족/품절이면 주문 생성 자체가 실패하고 에러 메시지가 내려온다).
  async createOrder(cartItemIds: number[]): Promise<OrderCreateResponse> {
    if (USE_MOCK) return mockCreateOrder(cartItemIds)
    const res = await http.post('/api/orders', { cartItemIds })
    return unwrap(res)
  },

  async getOrder(orderId: number): Promise<Order> {
    if (USE_MOCK) return mockGetOrder(orderId)
    const res = await http.get(`/api/orders/${orderId}`)
    return unwrap(res)
  },

  async getMyOrders(page: number, size: number, customerId?: number): Promise<PageResponse<OrderSummary>> {
    if (USE_MOCK) return mockGetMyOrders(page, size)
    const res = await http.get('/api/orders', { params: { page, size, customerId } })
    return unwrapPage(res)
  },
}
