import { REFUND_STATUS_LABEL } from '@/constants/refundStatus'
import type { RefundStatus } from '@/types/refund'
import './RefundStatusBadge.css'

export function RefundStatusBadge({ status }: { status: RefundStatus }) {
  return <span className={`badge refund-status refund-status--${status.toLowerCase()}`}>{REFUND_STATUS_LABEL[status]}</span>
}
