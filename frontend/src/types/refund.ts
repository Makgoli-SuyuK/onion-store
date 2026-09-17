export type RefundStatus = 'PENDING_APPROVAL' | 'REQUESTED' | 'COMPLETED' | 'REJECTED' | 'FAILED'
export type RefundInitiator = 'CUSTOMER' | 'SYSTEM'
export type RefundReasonType = 'CUSTOMER_REQUEST' | 'AMOUNT_MISMATCH'

export interface RefundItemRequest {
  orderItemId: number
  quantity: number
}

export interface CustomerRefundRequest {
  reason: string
  items: RefundItemRequest[]
}

export interface CustomerRefundSummary {
  refundId: number
  status: RefundStatus
  requestedAmount: number
  requestedAt: string
  reviewedAt: string | null
  rejectionReason: string | null
}

export interface RefundOrderInfo {
  orderNumber: string
  productSummaryName: string
  totalOrderQuantity: number
  orderAmount: number
}

export interface RefundItemDetail {
  orderItemId: number
  productName: string
  orderedQuantity: number
  requestedQuantity: number
}

export interface CustomerRefundDetail {
  order: RefundOrderInfo
  refund: CustomerRefundSummary & {
    initiator: RefundInitiator
    reasonType: RefundReasonType
    reason: string
  }
  refundItems: RefundItemDetail[]
}

export interface AdminRefundListItem {
  refundId: number
  orderNumber: string
  customerName: string
  requestedAt: string
  status: RefundStatus
  orderAmount: number
  requestedAmount: number
  initiator: RefundInitiator
}

export interface AdminRefundPage {
  content: AdminRefundListItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AdminRefundDetail {
  order: RefundOrderInfo
  customer: {
    name: string
    phoneNumber: string
    email: string
  }
  refund: CustomerRefundSummary & {
    initiator: RefundInitiator
    reasonType: RefundReasonType
    reason: string
    reviewerName: string | null
    portoneCancellationId: string | null
  }
  refundItems: RefundItemDetail[]
}

export interface RefundReviewResponse {
  refundId: number
  status: RefundStatus
  reviewedAt: string | null
  rejectionReason: string | null
}

export interface AdminRefundSearchCondition {
  status?: RefundStatus
  from?: string
  to?: string
  keyword?: string
  page?: number
  size?: number
}
