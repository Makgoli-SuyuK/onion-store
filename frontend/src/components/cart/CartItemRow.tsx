import { Link } from 'react-router-dom'
import { ProductImage } from '@/components/common/ProductImage'
import { QuantityStepper } from '@/components/common/QuantityStepper'
import { formatCurrency } from '@/utils/currency'
import type { CartItem } from '@/types/cart'
import './CartItemRow.css'

// 백엔드 장바구니 응답에 재고/판매상태가 없어 수량 상한과 품절 표시는 하지 않는다.
// 재고를 초과하면 서버가 수량 변경/주문 시점에 오류를 내려준다.
export function CartItemRow({
  item,
  selected,
  onToggleSelect,
  onQuantityChange,
  onRemove,
}: {
  item: CartItem
  selected: boolean
  onToggleSelect: () => void
  onQuantityChange: (quantity: number) => void
  onRemove: () => void
}) {
  return (
    <div className="cart-row">
      <input type="checkbox" checked={selected} onChange={onToggleSelect} aria-label={`${item.productName} 선택`} />
      <div className="cart-row__pic">
        <ProductImage alt={item.productName} />
      </div>
      <div className="cart-row__info">
        <Link to={`/products/${item.productId}`} className="cart-row__name">
          {item.productName}
        </Link>
        <p className="cart-row__price">{formatCurrency(item.price)}</p>
      </div>
      <QuantityStepper value={item.quantity} onChange={onQuantityChange} />
      <p className="cart-row__subtotal">{formatCurrency(item.subtotal)}</p>
      <button
        type="button"
        className="cart-row__remove"
        aria-label={`${item.productName} 삭제`}
        onClick={onRemove}
      >
        ✕
      </button>
    </div>
  )
}
