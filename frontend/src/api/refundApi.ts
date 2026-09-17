import { http, unwrap } from './client'
import type {
  AdminRefundDetail,
  AdminRefundPage,
  AdminRefundSearchCondition,
  CustomerRefundDetail,
  CustomerRefundRequest,
  CustomerRefundSummary,
  RefundReviewResponse,
} from '@/types/refund'

export const refundApi = {
  async requestRefund(orderId: number, request: CustomerRefundRequest): Promise<CustomerRefundSummary> {
    const response = await http.post(`/api/orders/${orderId}/refunds`, request)
    return unwrap(response)
  },

  async getMyRefunds(): Promise<CustomerRefundSummary[]> {
    const response = await http.get('/api/refunds')
    return unwrap(response)
  },

  async getMyRefundDetail(refundId: number): Promise<CustomerRefundDetail> {
    const response = await http.get(`/api/refunds/${refundId}`)
    return unwrap(response)
  },

  async getAdminRefunds(condition: AdminRefundSearchCondition): Promise<AdminRefundPage> {
    const response = await http.get('/api/admin/refunds', { params: condition })
    return unwrap(response)
  },

  async getAdminRefundDetail(refundId: number): Promise<AdminRefundDetail> {
    const response = await http.get(`/api/admin/refunds/${refundId}`)
    return unwrap(response)
  },

  async approveRefund(refundId: number): Promise<RefundReviewResponse> {
    const response = await http.post(`/api/admin/refunds/${refundId}/approve`)
    return unwrap(response)
  },

  async rejectRefund(refundId: number, reason: string): Promise<RefundReviewResponse> {
    const response = await http.post(`/api/admin/refunds/${refundId}/reject`, { reason })
    return unwrap(response)
  },
}
