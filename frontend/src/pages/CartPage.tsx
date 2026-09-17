import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCart } from '@/hooks/useCart'
import { CartItemRow } from '@/components/cart/CartItemRow'
import { EmptyState } from '@/components/common/EmptyState'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { formatCurrency } from '@/utils/currency'
import './CartPage.css'

export function CartPage() {
  const { items, loading, updateQuantity, removeItem } = useCart()
  const navigate = useNavigate()
  const [selected, setSelected] = useState<Set<number>>(new Set())

  useEffect(() => {
    setSelected((prev) => {
      if (prev.size === 0) return new Set(items.map((i) => i.cartItemId))
      const validIds = new Set(items.map((i) => i.cartItemId))
      return new Set(Array.from(prev).filter((id) => validIds.has(id)))
    })
  }, [items])

  const allSelected = items.length > 0 && items.every((i) => selected.has(i.cartItemId))

  const toggleAll = () => {
    setSelected(allSelected ? new Set() : new Set(items.map((i) => i.cartItemId)))
  }

  const toggleOne = (cartItemId: number) => {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(cartItemId)) next.delete(cartItemId)
      else next.add(cartItemId)
      return next
    })
  }

  const selectedItems = items.filter((i) => selected.has(i.cartItemId))
  const productAmount = selectedItems.reduce((sum, i) => sum + i.subtotal, 0)

  const handleOrder = () => {
    navigate('/checkout', { state: { cartItemIds: Array.from(selected) } })
  }

  if (loading) return <LoadingSpinner />

  if (items.length === 0) {
    return (
      <div className="container">
        <EmptyState emoji="🧺" message="장바구니가 비어 있어요." description="마음에 드는 양파를 담아보세요." />
      </div>
    )
  }

  return (
    <div className="container cart-page">
      <h1 className="cart-page__title">장바구니</h1>

      <div className="cart-page__select-all">
        <label>
          <input type="checkbox" checked={allSelected} onChange={toggleAll} /> 전체 선택 (
          {selected.size}/{items.length})
        </label>
      </div>

      <div className="cart-page__list">
        {items.map((item) => (
          <CartItemRow
            key={item.cartItemId}
            item={item}
            selected={selected.has(item.cartItemId)}
            onToggleSelect={() => toggleOne(item.cartItemId)}
            onQuantityChange={(q) => updateQuantity(item.cartItemId, q)}
            onRemove={() => removeItem(item.cartItemId)}
          />
        ))}
      </div>

      <div className="cart-page__summary card">
        <div className="summary-row">
          <span>상품 금액</span>
          <span>{formatCurrency(productAmount)}</span>
        </div>
        <div className="summary-row summary-row--total">
          <span>총 결제 예정 금액</span>
          <span>{formatCurrency(productAmount)}</span>
        </div>
        <button
          type="button"
          className="btn btn--primary btn--block"
          disabled={selected.size === 0}
          onClick={handleOrder}
        >
          선택한 상품 주문하기
        </button>
      </div>
    </div>
  )
}
