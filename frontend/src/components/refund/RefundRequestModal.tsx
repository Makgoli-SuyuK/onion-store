import { useMemo, useState } from 'react'
import { refundApi } from '@/api/refundApi'
import { Modal } from '@/components/common/Modal'
import { useToast } from '@/hooks/useToast'
import type { Order } from '@/types/order'
import './RefundRequestModal.css'

export function RefundRequestModal({
  orderId,
  order,
  onClose,
  onRequested,
}: {
  orderId: number
  order: Order
  onClose: () => void
  onRequested: () => void
}) {
  const { showToast } = useToast()
  const initialQuantities = useMemo(
    () => Object.fromEntries(order.orderItems.map((item) => [item.orderItemId, '0'])),
    [order.orderItems],
  )
  const [quantities, setQuantities] = useState<Record<number, string>>(initialQuantities)
  const [reason, setReason] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async () => {
    const items = order.orderItems
      .map((item) => ({ orderItemId: item.orderItemId, quantity: Number(quantities[item.orderItemId] ?? 0) }))
      .filter((item) => Number.isInteger(item.quantity) && item.quantity > 0)

    if (!reason.trim()) {
      showToast('환불 사유를 입력해 주세요.', 'error')
      return
    }
    if (items.length === 0) {
      showToast('환불할 상품과 수량을 선택해 주세요.', 'error')
      return
    }

    setSubmitting(true)
    try {
      await refundApi.requestRefund(orderId, { reason: reason.trim(), items })
      showToast('환불 요청을 접수했습니다.', 'success')
      onRequested()
      onClose()
    } catch (error) {
      showToast((error as Error).message, 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal title="환불 요청" onClose={onClose}>
      <p className="refund-request__guide">환불할 상품의 수량을 선택하고 사유를 입력해 주세요.</p>
      <div className="refund-request__items">
        {order.orderItems.map((item) => (
          <label key={item.orderItemId} className="refund-request__item">
            <span>
              <strong>{item.productName}</strong>
              <small>구매 수량 {item.quantity}개</small>
            </span>
            <input
              className="input refund-request__quantity"
              type="number"
              min="0"
              max={item.quantity}
              value={quantities[item.orderItemId] ?? '0'}
              onChange={(event) =>
                setQuantities((current) => ({ ...current, [item.orderItemId]: event.target.value }))
              }
            />
          </label>
        ))}
      </div>
      <label className="field">
        <span>환불 사유</span>
        <textarea
          className="input refund-request__reason"
          rows={4}
          maxLength={500}
          placeholder="환불 사유를 입력해 주세요."
          value={reason}
          onChange={(event) => setReason(event.target.value)}
        />
      </label>
      <button type="button" className="btn btn--primary btn--block" disabled={submitting} onClick={handleSubmit}>
        {submitting ? '환불 요청 처리 중...' : '환불 요청하기'}
      </button>
    </Modal>
  )
}
