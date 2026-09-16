import { http, unwrap, unwrapPage, USE_MOCK } from './client'
import { mockGetProduct, mockGetProducts } from '@/mocks/productMock'
import type { PageResponse } from '@/types/common'
import type { Product, ProductListQuery, ProductSummary, SortOption } from '@/types/product'

// UI 정렬 옵션 -> 백엔드 sortBy/sortOrder. 백엔드는 name/category/price/likeCount만 정렬 지원.
// LATEST(최신순)에 대응하는 생성일 정렬이 없어 정렬 파라미터를 비워 기본 순서로 조회한다.
function toSortParams(sort?: SortOption): { sortBy?: string; sortOrder?: string } {
  if (sort === 'PRICE_ASC') return { sortBy: 'price', sortOrder: 'asc' }
  if (sort === 'POPULAR') return { sortBy: 'likeCount', sortOrder: 'desc' }
  return {}
}

export const productApi = {
  async getProducts(query: ProductListQuery): Promise<PageResponse<ProductSummary>> {
    if (USE_MOCK) return mockGetProducts(query)
    const { name, category, sort, page = 0, size = 12 } = query
    const res = await http.get('/api/products', {
      params: { name, category, page, size, ...toSortParams(sort) },
    })
    return unwrapPage(res)
  },

  async getProduct(productId: number): Promise<Product> {
    if (USE_MOCK) return mockGetProduct(productId)
    const res = await http.get(`/api/products/${productId}`)
    return unwrap(res)
  },
}
