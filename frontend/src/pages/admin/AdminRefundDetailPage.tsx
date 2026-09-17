import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { refundApi } from '@/api/refundApi'
import { RefundStatusBadge } from '@/components/refund/RefundStatusBadge'
import { ErrorState } from '@/components/common/ErrorState'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { Modal } from '@/components/common/Modal'
import { useApiRequest } from '@/hooks/useApiRequest'
import { useToast } from '@/hooks/useToast'
import { formatCurrency } from '@/utils/currency'
import './AdminRefundDetailPage.css'

export function AdminRefundDetailPage() {
  const { refundId } = useParams<{ refundId: string }>()
  const navigate = useNavigate()
  const { showToast } = useToast()
  const { data, loading, error, refetch } = useApiRequest(
    () => refundApi.getAdminRefundDetail(Number(refundId)),
    [refundId],
  )
  const [rejecting, setRejecting] = useState(false)
  const [reason, setReason] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorState message={error} onRetry={refetch} />
  if (!data) return null

  const isPending = data.refund.status === 'PENDING_APPROVAL'

  const approve = async () => {
    setSubmitting(true)
    try {
      const result = await refundApi.approveRefund(data.refund.refundId)
      showToast(result.status === 'COMPLETED' ? '환불 처리가 완료되었습니다.' : '환불 취소 요청을 처리하고 있습니다.', 'success')
      refetch()
    } catch (error) {
      showToast((error as Error).message, 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const reject = async () => {
    if (!reason.trim()) {
      showToast('거절 사유를 입력해 주세요.', 'error')
      return
    }
    setSubmitting(true)
    try {
      await refundApi.rejectRefund(data.refund.refundId, reason.trim())
      showToast('환불 요청을 거절했습니다.', 'success')
      setRejecting(false)
      refetch()
    } catch (error) {
      showToast((error as Error).message, 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container admin-refund-detail">
      <button type="button" className="btn btn--ghost btn--sm" onClick={() => navigate('/admin/refunds')}>← 목록</button>
      <div className="admin-refund-detail__header"><h1>환불 #{data.refund.refundId}</h1><RefundStatusBadge status={data.refund.status} /></div>
      <section className="card admin-refund-detail__section">
        <h2>고객과 주문 정보</h2>
        <div className="summary-row"><span>고객</span><span>{data.customer.name}</span></div>
        <div className="summary-row"><span>연락처</span><span>{data.customer.phoneNumber}</span></div>
        <div className="summary-row"><span>이메일</span><span>{data.customer.email}</span></div>
        <div className="summary-row"><span>주문번호</span><span>{data.order.orderNumber}</span></div>
        <div className="summary-row summary-row--total"><span>총 결제 금액</span><span>{formatCurrency(data.order.orderAmount)}</span></div>
      </section>
      <section className="card admin-refund-detail__section">
        <h2>환불 요청</h2>
        <div className="summary-row"><span>환불 금액</span><span>{formatCurrency(data.refund.requestedAmount)}</span></div>
        <div className="summary-row"><span>요청 일시</span><span>{new Date(data.refund.requestedAt).toLocaleString('ko-KR')}</span></div>
        <p className="admin-refund-detail__reason">{data.refund.reason}</p>
      </section>
      <section className="card admin-refund-detail__section">
        <h2>환불 상품</h2>
        {data.refundItems.map((item) => <div key={item.orderItemId} className="summary-row"><span>{item.productName}</span><span>{item.requestedQuantity}개 / 구매 {item.orderedQuantity}개</span></div>)}
      </section>
      {isPending && <div className="admin-refund-detail__actions"><button type="button" className="btn btn--primary" disabled={submitting} onClick={approve}>승인하고 환불 처리</button><button type="button" className="btn btn--outline" disabled={submitting} onClick={() => setRejecting(true)}>환불 거절</button></div>}
      {data.refund.rejectionReason && <p className="admin-refund-detail__rejection">거절 사유: {data.refund.rejectionReason}</p>}
      {rejecting && <Modal title="환불 요청 거절" onClose={() => setRejecting(false)}><label className="field"><span>거절 사유</span><textarea className="input admin-refund-detail__textarea" rows={4} maxLength={500} value={reason} onChange={(e) => setReason(e.target.value)} /></label><button type="button" className="btn btn--primary btn--block" disabled={submitting} onClick={reject}>거절 처리</button></Modal>}
    </div>
  )
}
