import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { orderApi } from '@/api/orderApi'
import { paymentApi } from '@/api/paymentApi'
import { requestPortOnePayment } from '@/payment/portone'
import { useAuth } from '@/hooks/useAuth'
import { useCart } from '@/hooks/useCart'
import { useToast } from '@/hooks/useToast'
import { EmptyState } from '@/components/common/EmptyState'
import { ProductImage } from '@/components/common/ProductImage'
import { formatCurrency } from '@/utils/currency'
import './CheckoutPage.css'

export function CheckoutPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { items, refresh: refreshCart } = useCart()
  const { showToast } = useToast()

  const cartItemIds = (location.state as { cartItemIds?: number[] } | null)?.cartItemIds ?? []
  const selectedItems = items.filter((i) => cartItemIds.includes(i.cartItemId))
  const [submitting, setSubmitting] = useState(false)

  if (selectedItems.length === 0) {
    return (
      <div className="container">
        <EmptyState message="주문할 상품을 먼저 장바구니에서 선택해 주세요." />
      </div>
    )
  }

  const productAmount = selectedItems.reduce((sum, i) => sum + i.subtotal, 0)

  const handlePay = async () => {
    if (!user) return
    setSubmitting(true)
    try {
      // 주문 생성 자체가 서버에서 가격/재고/판매상태를 원자적으로 재확인한다.
      // (재고 부족·품절이면 여기서 바로 실패하고 한글 메시지가 내려온다)
      const order = await orderApi.createOrder(cartItemIds)

      const config = await paymentApi.getPortOneConfig()
      const orderName =
        order.items.length > 1
          ? `${order.items[0].productName} 외 ${order.items.length - 1}건`
          : order.items[0].productName
      const paidPaymentId = await requestPortOnePayment({
        storeId: config.storeId,
        channelKey: config.channelKey,
        paymentId: order.portonePaymentId,
        orderName,
        totalAmount: order.totalPrice,
        customer: { fullName: user.name, phoneNumber: user.phoneNumber, email: user.email },
      })

      await paymentApi.confirmPayment(order.orderId, paidPaymentId)
      await refreshCart()
      navigate(`/orders/complete/${order.orderId}`, { replace: true })
    } catch (err) {
      showToast((err as Error).message, 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container checkout-page">
      <h1 className="checkout-page__title">주문/결제</h1>

      <section className="checkout-page__section">
        <h2>주문 상품</h2>
        <div className="checkout-page__items">
          {selectedItems.map((item) => (
            <div key={item.cartItemId} className="checkout-item">
              <div className="checkout-item__pic">
                <ProductImage alt={item.productName} />
              </div>
              <div className="checkout-item__info">
                <p className="checkout-item__name">{item.productName}</p>
                <p className="checkout-item__qty">
                  {formatCurrency(item.price)} × {item.quantity}개
                </p>
              </div>
              <p className="checkout-item__subtotal">{formatCurrency(item.subtotal)}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="checkout-page__section">
        <h2>주문자 정보</h2>
        <div className="checkout-page__orderer">
          <div className="summary-row">
            <span>이름</span>
            <span>{user?.name}</span>
          </div>
          <div className="summary-row">
            <span>연락처</span>
            <span>{user?.phoneNumber}</span>
          </div>
          <div className="summary-row">
            <span>이메일</span>
            <span>{user?.email}</span>
          </div>
        </div>
      </section>

      <section className="checkout-page__section card checkout-page__summary">
        <div className="summary-row">
          <span>상품 금액</span>
          <span>{formatCurrency(productAmount)}</span>
        </div>
        <div className="summary-row summary-row--total">
          <span>결제 금액</span>
          <span>{formatCurrency(productAmount)}</span>
        </div>
        <button type="button" className="btn btn--primary btn--block" disabled={submitting} onClick={handlePay}>
          {submitting ? '결제 처리 중...' : `${formatCurrency(productAmount)} 결제하기`}
        </button>
      </section>
    </div>
  )
}
