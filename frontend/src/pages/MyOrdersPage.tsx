import { Link } from 'react-router-dom'
import { orderApi } from '@/api/orderApi'
import { useApiRequest } from '@/hooks/useApiRequest'
import { useAuth } from '@/hooks/useAuth'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { EmptyState } from '@/components/common/EmptyState'
import { formatCurrency } from '@/utils/currency'
import './MyOrdersPage.css'

export function MyOrdersPage() {
  const { user, isAdmin } = useAuth()
  const { data, loading, error, refetch } = useApiRequest(
    () => orderApi.getMyOrders(0, 20, isAdmin ? user?.userId : undefined),
    [isAdmin, user?.userId],
  )

  return (
    <div className="container my-orders">
      <h1 className="my-orders__title">주문 내역</h1>

      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={refetch} />}
      {!loading && !error && data && data.content.length === 0 && (
        <EmptyState emoji="📦" message="아직 주문 내역이 없어요." />
      )}

      {!loading && !error && data && data.content.length > 0 && (
        <div className="my-orders__list">
          {data.content.map((order) => (
            <div key={order.orderId} className="card my-orders__item">
              <div className="my-orders__item-main">
                <p className="my-orders__number">{order.orderNumber}</p>
                <p className="my-orders__date">{new Date(order.createdAt).toLocaleDateString('ko-KR')}</p>
                <p className="my-orders__product">
                  {order.items[0]?.productName}
                  {order.items.length > 1 ? ` 외 ${order.items.length - 1}건` : ''}
                </p>
              </div>
              <div className="my-orders__item-side">
                <span className="badge badge--tag">{order.paidAt ? '결제 완료' : '결제 대기'}</span>
                <p className="my-orders__amount">{formatCurrency(order.totalPrice)}</p>
                <Link to={`/mypage/orders/${order.orderId}`} className="btn btn--outline btn--sm">
                  상세 보기
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
