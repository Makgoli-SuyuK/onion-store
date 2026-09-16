import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { orderApi } from '@/api/orderApi'
import { useApiRequest } from '@/hooks/useApiRequest'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { ProductImage } from '@/components/common/ProductImage'
import { RefundRequestModal } from '@/components/refund/RefundRequestModal'
import { ORDER_STATUS_LABEL } from '@/constants/orderStatus'
import { formatCurrency } from '@/utils/currency'
import './OrderDetailPage.css'

export function OrderDetailPage() {
  const { orderId } = useParams<{ orderId: string }>()
  const [requestingRefund, setRequestingRefund] = useState(false)
  const { data, loading, error, refetch } = useApiRequest(() => orderApi.getOrder(Number(orderId)), [orderId])

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorState message={error} onRetry={refetch} />
  if (!data) return null

  const canRequestRefund = data.orderStatus === 'PAID'
    && (data.paymentStatus === 'SUCCESS' || data.paymentStatus === 'PARTIALLY_CANCELLED')

  return (
    <div className="container order-detail">
      <div className="order-detail__header">
        <h1>주문 상세</h1>
        <span className="badge badge--tag">{ORDER_STATUS_LABEL[data.orderStatus]}</span>
      </div>

      <div className="summary-row">
        <span>주문번호</span>
        <span>{data.orderNumber}</span>
      </div>
      <div className="summary-row">
        <span>주문 일시</span>
        <span>{new Date(data.createdAt).toLocaleString('ko-KR')}</span>
      </div>
      {data.paidAt && (
        <div className="summary-row">
          <span>결제 일시</span>
          <span>{new Date(data.paidAt).toLocaleString('ko-KR')}</span>
        </div>
      )}

      <section className="order-detail__items">
        {data.orderItems.map((item) => (
          <div key={item.orderItemId} className="checkout-item">
            <div className="checkout-item__pic">
              <ProductImage alt={item.productName} />
            </div>
            <div className="checkout-item__info">
              <p className="checkout-item__name">{item.productName}</p>
              <p className="checkout-item__qty">
                {formatCurrency(item.productPrice)} × {item.quantity}개
              </p>
            </div>
            <p className="checkout-item__subtotal">{formatCurrency(item.productPrice * item.quantity)}</p>
          </div>
        ))}
      </section>

      <div className="card order-detail__summary">
        <div className="summary-row summary-row--total">
          <span>결제 금액</span>
          <span>{formatCurrency(data.totalPrice)}</span>
        </div>
      </div>

      {canRequestRefund && (
        <button type="button" className="btn btn--outline order-detail__refund" onClick={() => setRequestingRefund(true)}>
          환불 요청
        </button>
      )}

      {requestingRefund && (
        <RefundRequestModal
          orderId={Number(orderId)}
          order={data}
          onClose={() => setRequestingRefund(false)}
          onRequested={refetch}
        />
      )}
    </div>
  )
}
