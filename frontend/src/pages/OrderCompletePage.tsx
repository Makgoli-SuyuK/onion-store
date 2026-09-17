import { Link, useParams } from 'react-router-dom'
import { orderApi } from '@/api/orderApi'
import { useApiRequest } from '@/hooks/useApiRequest'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { formatCurrency } from '@/utils/currency'
import './OrderCompletePage.css'

export function OrderCompletePage() {
  const { orderId } = useParams<{ orderId: string }>()
  const { data, loading, error } = useApiRequest(() => orderApi.getOrder(Number(orderId)), [orderId])

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorState message={error} />
  if (!data) return null

  return (
    <div className="container order-complete">
      <div className="order-complete__icon" aria-hidden="true">
        🎉
      </div>
      <h1>주문이 완료되었어요!</h1>
      <p className="order-complete__desc">양파 친구들이 정성껏 포장해서 보내드릴게요.</p>

      <div className="card order-complete__box">
        <div className="summary-row">
          <span>주문번호</span>
          <span>{data.orderNumber}</span>
        </div>
        <div className="summary-row">
          <span>주문 일시</span>
          <span>{new Date(data.createdAt).toLocaleString('ko-KR')}</span>
        </div>
        <div className="summary-row summary-row--total">
          <span>결제 금액</span>
          <span>{formatCurrency(data.totalPrice)}</span>
        </div>
      </div>

      <div className="order-complete__actions">
        <Link to="/mypage/orders" className="btn btn--outline">
          주문 내역 보기
        </Link>
        <Link to="/products" className="btn btn--primary">
          쇼핑 계속하기
        </Link>
      </div>
    </div>
  )
}
