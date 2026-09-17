import { ProductCard } from './ProductCard'
import type { ProductSummary } from '@/types/product'
import './ProductGrid.css'

export function ProductGrid({ products }: { products: ProductSummary[] }) {
  return (
    <div className="product-grid">
      {products.map((p) => (
        <ProductCard key={p.id} product={p} />
      ))}
    </div>
  )
}
