import { Link } from 'react-router-dom'
import { useState, type FormEvent } from 'react'
import { refundApi } from '@/api/refundApi'
import { RefundStatusBadge } from '@/components/refund/RefundStatusBadge'
import { EmptyState } from '@/components/common/EmptyState'
import { ErrorState } from '@/components/common/ErrorState'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { useApiRequest } from '@/hooks/useApiRequest'
import { formatCurrency } from '@/utils/currency'
import type { AdminRefundSearchCondition, RefundStatus } from '@/types/refund'
import './AdminRefundListPage.css'

const statusOptions: Array<{ value: RefundStatus | ''; label: string }> = [
  { value: '', label: '전체 상태' },
  { value: 'PENDING_APPROVAL', label: '승인 대기' },
  { value: 'REQUESTED', label: '취소 처리 중' },
  { value: 'COMPLETED', label: '환불 완료' },
  { value: 'REJECTED', label: '환불 거절' },
  { value: 'FAILED', label: '환불 실패' },
]

export function AdminRefundListPage() {
  const [condition, setCondition] = useState<AdminRefundSearchCondition>({ page: 1, size: 20 })
  const [draft, setDraft] = useState<AdminRefundSearchCondition>({ page: 1, size: 20 })
  const { data, loading, error, refetch } = useApiRequest(() => refundApi.getAdminRefunds(condition), [condition])

  const search = (event: FormEvent) => {
    event.preventDefault()
    setCondition({ ...draft, page: 1, size: 20 })
  }

  return (
    <div className="container admin-refunds">
      <h1 className="admin-refunds__title">환불 관리</h1>
      <form className="admin-refunds__filters card" onSubmit={search}>
        <select className="input" value={draft.status ?? ''} onChange={(e) => setDraft({ ...draft, status: (e.target.value || undefined) as RefundStatus | undefined })}>
          {statusOptions.map((option) => <option key={option.label} value={option.value}>{option.label}</option>)}
        </select>
        <input className="input" type="date" value={draft.from ?? ''} onChange={(e) => setDraft({ ...draft, from: e.target.value || undefined })} aria-label="시작일" />
        <input className="input" type="date" value={draft.to ?? ''} onChange={(e) => setDraft({ ...draft, to: e.target.value || undefined })} aria-label="종료일" />
        <input className="input" placeholder="주문번호 또는 고객명" value={draft.keyword ?? ''} onChange={(e) => setDraft({ ...draft, keyword: e.target.value })} />
        <button type="submit" className="btn btn--primary btn--sm">검색</button>
      </form>
      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={refetch} />}
      {!loading && !error && data?.content.length === 0 && <EmptyState emoji="↩️" message="조건에 맞는 환불 요청이 없어요." />}
      {!loading && !error && data && data.content.length > 0 && (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead><tr><th>환불 번호</th><th>주문번호</th><th>고객</th><th>요청 금액</th><th>요청 일시</th><th>상태</th><th></th></tr></thead>
            <tbody>
              {data.content.map((refund) => (
                <tr key={refund.refundId}>
                  <td>#{refund.refundId}</td><td>{refund.orderNumber}</td><td>{refund.customerName}</td><td>{formatCurrency(refund.requestedAmount)}</td><td>{new Date(refund.requestedAt).toLocaleDateString('ko-KR')}</td><td><RefundStatusBadge status={refund.status} /></td>
                  <td><Link to={`/admin/refunds/${refund.refundId}`} className="btn btn--outline btn--sm">상세</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
