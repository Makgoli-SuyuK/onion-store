// 포트원(PortOne) V2 브라우저 SDK 연동. index.html에서 SDK 스크립트를 미리 로드해둔다.
// 주문서에서 PortOne 결제창을 열고, 완료된 paymentId를 결제 확인 API에 전달한다.

interface PortOneRequestPaymentInput {
  storeId: string
  channelKey: string
  paymentId: string
  orderName: string
  totalAmount: number
  customer: {
    fullName: string
    phoneNumber: string
    email: string
  }
}

interface PortOnePaymentResult {
  code?: string
  message?: string
  paymentId?: string
}

declare global {
  interface Window {
    PortOne?: {
      requestPayment: (input: Record<string, unknown>) => Promise<PortOnePaymentResult>
    }
  }
}

export async function requestPortOnePayment(input: PortOneRequestPaymentInput): Promise<string> {
  if (!window.PortOne) {
    throw new Error('결제 모듈을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.')
  }

  const result = await window.PortOne.requestPayment({
    storeId: input.storeId,
    channelKey: input.channelKey,
    paymentId: input.paymentId,
    orderName: input.orderName,
    totalAmount: input.totalAmount,
    currency: 'KRW',
    payMethod: 'CARD',
    customer: input.customer,
  })

  if (!result) {
    throw new Error('결제창 응답을 받지 못했습니다.')
  }
  if (result.code !== undefined) {
    throw new Error(result.message || '결제가 완료되지 않았습니다.')
  }
  return result.paymentId || input.paymentId
}
