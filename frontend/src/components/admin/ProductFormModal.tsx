import { useState, type FormEvent } from 'react'
import { Modal } from '@/components/common/Modal'
import { useCategories } from '@/hooks/useCategories'
import type { ProductCreateRequest, ProductEditRequest } from '@/types/admin'
import type { Product, ProductStatus } from '@/types/product'

export interface ProductFormValues {
  name: string
  category: string
  price: number
  stock: number
  status: ProductStatus
  description: string
}

export function ProductFormModal({
  product,
  onClose,
  onSubmit,
}: {
  product?: Product
  onClose: () => void
  // 등록/수정 요청 DTO 필드가 서로 달라(등록: category/productName, 수정: name/status) 호출부에서 매핑한다.
  onSubmit: (values: ProductFormValues) => Promise<void>
}) {
  const { categories } = useCategories()
  const [name, setName] = useState(product?.name ?? '')
  const [category, setCategory] = useState(product?.categoryName ?? categories[0]?.name ?? '')
  const [price, setPrice] = useState(product?.price?.toString() ?? '')
  const [stock, setStock] = useState(product?.stock?.toString() ?? '')
  const [status, setStatus] = useState<ProductStatus>(product?.status ?? 'SELLING')
  const [description, setDescription] = useState(product?.description ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  const validate = () => {
    const next: Record<string, string> = {}
    if (!name.trim()) next.name = '상품명을 입력해 주세요.'
    if (!product && !category) next.category = '카테고리를 선택해 주세요.'
    if (!description.trim()) next.description = '상품 설명을 입력해 주세요.'
    if (!price || Number(price) < 1) next.price = '가격은 1원 이상이어야 해요.'
    if (!stock || Number(stock) < 1) next.stock = '재고는 1개 이상이어야 해요.'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!validate()) return
    setSubmitting(true)
    try {
      await onSubmit({
        name: name.trim(),
        category,
        price: Number(price),
        stock: Number(stock),
        status,
        description: description.trim(),
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal title={product ? '상품 수정' : '상품 등록'} onClose={onClose}>
      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="pf-name">상품명</label>
          <input id="pf-name" className="input" value={name} onChange={(e) => setName(e.target.value)} />
          {errors.name && <span className="field-error">{errors.name}</span>}
        </div>

        <div className="field">
          <label htmlFor="pf-category">카테고리</label>
          {product ? (
            // 상품 수정 API는 카테고리 변경을 지원하지 않아 등록 시 카테고리를 그대로 보여준다.
            <input id="pf-category" className="input" value={category} disabled />
          ) : (
            <select id="pf-category" className="input" value={category} onChange={(e) => setCategory(e.target.value)}>
              {categories.length === 0 && <option value="">등록된 카테고리가 없어요</option>}
              {categories.map((c) => (
                <option key={c.id} value={c.name}>
                  {c.name}
                </option>
              ))}
            </select>
          )}
          {errors.category && <span className="field-error">{errors.category}</span>}
        </div>

        <div className="field">
          <label htmlFor="pf-price">가격 (원)</label>
          <input
            id="pf-price"
            type="number"
            min={1}
            className="input"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />
          {errors.price && <span className="field-error">{errors.price}</span>}
        </div>

        <div className="field">
          <label htmlFor="pf-stock">재고 (개)</label>
          <input
            id="pf-stock"
            type="number"
            min={1}
            className="input"
            value={stock}
            onChange={(e) => setStock(e.target.value)}
          />
          {errors.stock && <span className="field-error">{errors.stock}</span>}
        </div>

        {product && (
          <div className="field">
            <label htmlFor="pf-status">판매 상태</label>
            <select
              id="pf-status"
              className="input"
              value={status}
              onChange={(e) => setStatus(e.target.value as ProductStatus)}
            >
              <option value="SELLING">판매중</option>
              <option value="SOLD_OUT">품절</option>
              <option value="HIDDEN">숨김</option>
            </select>
          </div>
        )}

        <div className="field">
          <label htmlFor="pf-desc">상품 설명</label>
          <textarea
            id="pf-desc"
            className="input"
            rows={3}
            style={{ borderRadius: 16 }}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          {errors.description && <span className="field-error">{errors.description}</span>}
        </div>

        <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
          <button type="button" className="btn btn--ghost" onClick={onClose}>
            취소
          </button>
          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? '저장 중...' : '저장'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export function toCreateRequest(v: ProductFormValues): ProductCreateRequest {
  return { category: v.category, productName: v.name, description: v.description, price: v.price, stock: v.stock }
}

export function toEditRequest(v: ProductFormValues): ProductEditRequest {
  return { name: v.name, description: v.description, price: v.price, stock: v.stock, status: v.status }
}
