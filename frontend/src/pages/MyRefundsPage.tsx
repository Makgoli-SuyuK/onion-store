import { Link } from 'react-router-dom'
import { refundApi } from '@/api/refundApi'
import { RefundStatusBadge } from '@/components/refund/RefundStatusBadge'
import { EmptyState } from '@/components/common/EmptyState'
import { ErrorState } from '@/components/common/ErrorState'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { useApiRequest } from '@/hooks/useApiRequest'
import { formatCurrency } from '@/utils/currency'
import './MyRefundsPage.css'

export function MyRefundsPage() {
  const { data, loading, error, refetch } = useApiRequest(() => refundApi.getMyRefunds(), [])

  return (
    <div className="container my-refunds">
      <h1 className="my-refunds__title">환불 내역</h1>
      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={refetch} />}
      {!loading && !error && data?.length === 0 && <EmptyState emoji="↩️" message="환불 요청 내역이 없어요." />}
      {!loading && !error && data && data.length > 0 && (
        <div className="my-refunds__list">
          {data.map((refund) => (
            <Link key={refund.refundId} to={`/mypage/refunds/${refund.refundId}`} className="card my-refunds__item">
              <div>
                <p className="my-refunds__number">환불 #{refund.refundId}</p>
                <p className="my-refunds__date">{new Date(refund.requestedAt).toLocaleString('ko-KR')}</p>
                <p className="my-refunds__amount">{formatCurrency(refund.requestedAmount)}</p>
              </div>
              <RefundStatusBadge status={refund.status} />
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
