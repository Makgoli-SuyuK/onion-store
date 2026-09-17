import { http, unwrap } from './client'
import type { PaymentConfirmResponse } from '@/types/order'
import type { PortOneConfig } from '@/types/payment'

export const paymentApi = {
  async getPortOneConfig(): Promise<PortOneConfig> {
    const res = await http.get('/api/config/portone')
    return unwrap(res)
  },

  async confirmPayment(orderId: number, portonePaymentId: string): Promise<PaymentConfirmResponse> {
    const res = await http.post('/api/payments/confirm', { orderId, portonePaymentId })
    return unwrap(res)
  },
}
