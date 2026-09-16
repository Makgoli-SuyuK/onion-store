import { useState } from 'react'
import { adminApi } from '@/api/adminApi'
import { useApiRequest } from '@/hooks/useApiRequest'
import { useToast } from '@/hooks/useToast'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { EmptyState } from '@/components/common/EmptyState'
import { AdminStatCard } from '@/components/admin/AdminStatCard'
import { ProductFormModal, toCreateRequest, toEditRequest, type ProductFormValues } from '@/components/admin/ProductFormModal'
import { formatCurrency } from '@/utils/currency'
import type { Product } from '@/types/product'
import './AdminProductListPage.css'

const STATUS_LABEL: Record<Product['status'], string> = { SELLING: '판매중', SOLD_OUT: '품절', HIDDEN: '숨김' }

export function AdminProductListPage() {
  const { showToast } = useToast()
  const [name, setName] = useState('')
  const [nameInput, setNameInput] = useState('')
  const [editingProduct, setEditingProduct] = useState<Product | 'new' | null>(null)

  const stats = useApiRequest(() => adminApi.getStats(), [])
  const list = useApiRequest(() => adminApi.getProducts({ name, page: 0, size: 50 }), [name])

  const reload = () => {
    stats.refetch()
    list.refetch()
  }

  const handleSubmit = async (values: ProductFormValues) => {
    try {
      if (editingProduct && editingProduct !== 'new') {
        await adminApi.updateProduct(editingProduct.id, toEditRequest(values))
        showToast('상품을 수정했습니다.', 'success')
      } else {
        await adminApi.createProduct(toCreateRequest(values))
        showToast('상품을 등록했습니다.', 'success')
      }
      setEditingProduct(null)
      reload()
    } catch (err) {
      showToast((err as Error).message, 'error')
    }
  }

  return (
    <div className="container admin-page">
      <h1 className="admin-page__title">상품 관리</h1>

      <div className="admin-page__stats">
        {stats.data && (
          <>
            <AdminStatCard label="전체 상품 수" value={stats.data.totalCount} />
            <AdminStatCard label="판매 중" value={stats.data.sellingCount} />
            <AdminStatCard label="품절" value={stats.data.soldOutCount} />
          </>
        )}
      </div>

      <div className="admin-page__toolbar">
        <form
          className="admin-page__search"
          onSubmit={(e) => {
            e.preventDefault()
            setName(nameInput.trim())
          }}
        >
          <input
            className="input"
            placeholder="상품명 검색"
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
          />
          <button type="submit" className="btn btn--outline btn--sm">
            검색
          </button>
        </form>
        <button type="button" className="btn btn--primary btn--sm" onClick={() => setEditingProduct('new')}>
          + 상품 등록
        </button>
      </div>

      {list.loading && <LoadingSpinner />}
      {!list.loading && list.error && <ErrorState message={list.error} onRetry={list.refetch} />}
      {!list.loading && !list.error && list.data && list.data.content.length === 0 && (
        <EmptyState message="등록된 상품이 없어요." />
      )}
      {!list.loading && !list.error && list.data && list.data.content.length > 0 && (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>상품명</th>
                <th>카테고리</th>
                <th>가격</th>
                <th>재고</th>
                <th>판매 상태</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {list.data.content.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.categoryName}</td>
                  <td>{formatCurrency(p.price)}</td>
                  <td>{p.stock}</td>
                  <td>
                    <span className={`badge ${p.status === 'SELLING' ? 'badge--tag' : 'badge--soldout'}`}>
                      {STATUS_LABEL[p.status]}
                    </span>
                  </td>
                  <td>
                    <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditingProduct(p)}>
                      수정
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {editingProduct && (
        <ProductFormModal
          product={editingProduct === 'new' ? undefined : editingProduct}
          onClose={() => setEditingProduct(null)}
          onSubmit={handleSubmit}
        />
      )}
    </div>
  )
}
