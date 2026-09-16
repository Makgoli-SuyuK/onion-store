import { Link } from 'react-router-dom'
import { ProductImage } from '@/components/common/ProductImage'
import { formatCurrency } from '@/utils/currency'
import type { ProductSummary } from '@/types/product'
import './ProductCard.css'

// 목록 API에는 현재 사용자의 liked 여부가 없으므로, 좋아요 수는 상세 화면에서만 토글할 수 있다.
export function ProductCard({ product }: { product: ProductSummary }) {
  return (
    <Link to={`/products/${product.id}`} className="product-card card">
      <div className="product-card__pic">
        <ProductImage alt={product.name} />
      </div>
      <div className="product-card__body">
        <span className="badge badge--tag">{product.categoryName}</span>
        <p className="product-card__name">{product.name}</p>
        <p className="product-card__price">{formatCurrency(product.price)}</p>
        <span className="product-card__like" aria-label={`좋아요 ${product.likeCount}개`}>
          🧡 {product.likeCount}
        </span>
      </div>
    </Link>
  )
}
