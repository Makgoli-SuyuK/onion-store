import { http, unwrap, USE_MOCK } from './client'
import { mockConfirmPayment } from '@/mocks/orderMock'
import type { PaymentConfirmResponse } from '@/types/order'
import type { PortOneConfig } from '@/types/payment'

export const paymentApi = {
  async getPortOneConfig(): Promise<PortOneConfig> {
    if (USE_MOCK) return { storeId: 'mock-store', channelKey: 'mock-channel' }
    const res = await http.get('/api/config/portone')
    return unwrap(res)
  },

  async confirmPayment(orderId: number, portonePaymentId: string): Promise<PaymentConfirmResponse> {
    if (USE_MOCK) return mockConfirmPayment(orderId, portonePaymentId)
    const res = await http.post('/api/payments/confirm', { orderId, portonePaymentId })
    return unwrap(res)
  },
}
