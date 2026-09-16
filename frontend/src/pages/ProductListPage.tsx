import { useSearchParams } from 'react-router-dom'
import { productApi } from '@/api/productApi'
import { useApiRequest } from '@/hooks/useApiRequest'
import { useCategories } from '@/hooks/useCategories'
import { ProductGrid } from '@/components/product/ProductGrid'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { EmptyState } from '@/components/common/EmptyState'
import type { SortOption } from '@/types/product'
import './ProductListPage.css'

const SORT_OPTIONS: { value: SortOption; label: string }[] = [
  { value: 'POPULAR', label: '인기순' },
  { value: 'PRICE_ASC', label: '낮은 가격순' },
  { value: 'LATEST', label: '최신순' },
]

export function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const { categories } = useCategories()
  const name = searchParams.get('name') ?? ''
  const category = searchParams.get('category') ?? ''
  const sort = (searchParams.get('sort') as SortOption) || 'POPULAR'

  const { data, loading, error, refetch } = useApiRequest(
    () => productApi.getProducts({ name, category: category || undefined, sort, page: 0, size: 20 }),
    [name, category, sort],
  )

  const updateParams = (next: Record<string, string>) => {
    const params = new URLSearchParams(searchParams)
    Object.entries(next).forEach(([k, v]) => {
      if (v) params.set(k, v)
      else params.delete(k)
    })
    setSearchParams(params)
  }

  return (
    <div className="container product-list-page">
      <div className="product-list-page__filters">
        <div className="product-list-page__categories">
          <button
            type="button"
            className={`chip ${category === '' ? 'chip--active' : ''}`}
            onClick={() => updateParams({ category: '' })}
          >
            전체 상품
          </button>
          {categories.map((c) => (
            <button
              key={c.id}
              type="button"
              className={`chip ${category === c.name ? 'chip--active' : ''}`}
              onClick={() => updateParams({ category: c.name })}
            >
              {c.name}
            </button>
          ))}
        </div>

        <select
          className="input product-list-page__sort"
          value={sort}
          aria-label="정렬 기준"
          onChange={(e) => updateParams({ sort: e.target.value })}
        >
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>

      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={refetch} />}
      {!loading && !error && data && data.content.length === 0 && (
        <EmptyState message="검색 결과가 없어요." description="다른 검색어나 카테고리로 찾아보세요." />
      )}
      {!loading && !error && data && data.content.length > 0 && <ProductGrid products={data.content} />}
    </div>
  )
}
