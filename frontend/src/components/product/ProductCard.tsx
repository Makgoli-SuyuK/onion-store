import { useState, type MouseEvent } from 'react'
import { Link } from 'react-router-dom'
import { ProductImage } from '@/components/common/ProductImage'
import { formatCurrency } from '@/utils/currency'
import type { ProductSummary } from '@/types/product'
import './ProductCard.css'

// 목록 API(ProductSummary)에는 재고/판매상태가 없어 목록 카드에는 품절 표시를 할 수 없다.
// TODO(백엔드): 목록 응답에 status가 추가되면 여기에 품절 배지를 다시 넣을 수 있음.
export function ProductCard({ product }: { product: ProductSummary }) {
  const [likeCount, setLikeCount] = useState(product.likeCount)

  // 백엔드에 좋아요 등록/취소 API가 아직 없어 화면에서만 증가시킨다(새로고침하면 초기화됨).
  const handleLike = (e: MouseEvent) => {
    e.preventDefault()
    setLikeCount((c) => c + 1)
  }

  return (
    <Link to={`/products/${product.id}`} className="product-card card">
      <div className="product-card__pic">
        <ProductImage alt={product.name} />
      </div>
      <div className="product-card__body">
        <span className="badge badge--tag">{product.categoryName}</span>
        <p className="product-card__name">{product.name}</p>
        <p className="product-card__price">{formatCurrency(product.price)}</p>
        <button
          type="button"
          className="product-card__like"
          onClick={handleLike}
          aria-label={`좋아요 ${likeCount}개, 좋아요 누르기`}
        >
          🧡 {likeCount}
        </button>
      </div>
    </Link>
  )
}
