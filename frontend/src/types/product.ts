// 판매 상태: 백엔드 ProductStatus enum과 동일 (SELLING/SOLD_OUT/HIDDEN)
export type ProductStatus = 'SELLING' | 'SOLD_OUT' | 'HIDDEN'

// 정렬 UI에서 쓰는 값. LATEST는 백엔드에 생성일 정렬이 없어 임시로 정렬 없음으로 처리한다.
export type SortOption = 'POPULAR' | 'PRICE_ASC' | 'LATEST'

// 목록(GET /api/products)이 내려주는 요약 정보. 재고/판매상태/설명 없음(백엔드 응답에 없음).
export interface ProductSummary {
  id: number
  categoryName: string
  name: string
  price: number
  likeCount: number
}

// 상품 상세의 실제 정보
export interface Product {
  id: number
  categoryName: string
  name: string
  description: string
  price: number
  stock: number
  likeCount: number
  status: ProductStatus
  createdAt: string
  updatedAt: string
}

// 상세 API는 상품 정보와 현재 사용자의 좋아요 여부를 함께 반환한다.
export interface ProductDetail {
  productInfo: Product
  liked: boolean
}

export interface ProductListQuery {
  name?: string
  category?: string
  sort?: SortOption
  page?: number
  size?: number
}
