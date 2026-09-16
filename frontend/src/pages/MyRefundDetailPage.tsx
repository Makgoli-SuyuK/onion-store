import { useParams } from 'react-router-dom'
import { refundApi } from '@/api/refundApi'
import { RefundStatusBadge } from '@/components/refund/RefundStatusBadge'
import { ErrorState } from '@/components/common/ErrorState'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { useApiRequest } from '@/hooks/useApiRequest'
import { formatCurrency } from '@/utils/currency'
import './RefundDetailPage.css'

export function MyRefundDetailPage() {
  const { refundId } = useParams<{ refundId: string }>()
  const { data, loading, error, refetch } = useApiRequest(
    () => refundApi.getMyRefundDetail(Number(refundId)),
    [refundId],
  )

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorState message={error} onRetry={refetch} />
  if (!data) return null

  return (
    <div className="container refund-detail">
      <div className="refund-detail__header">
        <h1>환불 상세</h1>
        <RefundStatusBadge status={data.refund.status} />
      </div>
      <section className="card refund-detail__section">
        <h2>주문 정보</h2>
        <div className="summary-row"><span>주문번호</span><span>{data.order.orderNumber}</span></div>
        <div className="summary-row"><span>주문 상품</span><span>{data.order.productSummaryName}</span></div>
        <div className="summary-row summary-row--total"><span>총 결제 금액</span><span>{formatCurrency(data.order.orderAmount)}</span></div>
      </section>
      <section className="card refund-detail__section">
        <h2>환불 요청</h2>
        <div className="summary-row"><span>요청 일시</span><span>{new Date(data.refund.requestedAt).toLocaleString('ko-KR')}</span></div>
        <div className="summary-row"><span>환불 금액</span><span>{formatCurrency(data.refund.requestedAmount)}</span></div>
        <p className="refund-detail__reason">{data.refund.reason}</p>
      </section>
      <section className="card refund-detail__section">
        <h2>환불 상품</h2>
        {data.refundItems.map((item) => (
          <div key={item.orderItemId} className="summary-row">
            <span>{item.productName}</span>
            <span>{item.requestedQuantity}개 / 구매 {item.orderedQuantity}개</span>
          </div>
        ))}
      </section>
      {data.refund.rejectionReason && <p className="refund-detail__rejection">거절 사유: {data.refund.rejectionReason}</p>}
    </div>
  )
}
