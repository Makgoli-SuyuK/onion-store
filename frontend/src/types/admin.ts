import type { ProductStatus } from './product'

// POST /api/products 요청 (필드명이 수정과 다르다: category/productName)
export interface ProductCreateRequest {
  category: string
  productName: string
  description: string
  price: number
  stock: number
}

// PATCH /api/products/{id} 요청 (필드명이 등록과 다르다: name, status 포함)
export interface ProductEditRequest {
  name: string
  description: string
  price: number
  stock: number
  status: ProductStatus
}

// 백엔드에 통계 API가 없어 상품 목록을 직접 집계해 만든다 (TODO: 백엔드에 전용 API 생기면 교체)
export interface AdminProductStats {
  totalCount: number
  sellingCount: number
  soldOutCount: number
}
