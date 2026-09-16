import { http, unwrap, unwrapPage, USE_MOCK } from './client'
import { mockGetProduct, mockGetProductDetail, mockGetProducts, mockToggleProductLike } from '@/mocks/productMock'
import type { PageResponse } from '@/types/common'
import type { Product, ProductDetail, ProductListQuery, ProductSummary, SortOption } from '@/types/product'

// UI 정렬 옵션 -> 백엔드 sortBy/sortOrder. 백엔드는 name/category/price/likeCount만 정렬 지원.
// LATEST(최신순)에 대응하는 생성일 정렬이 없어 정렬 파라미터를 비워 기본 순서로 조회한다.
function toSortParams(sort?: SortOption): { sortBy?: string; sortOrder?: string } {
  if (sort === 'PRICE_ASC') return { sortBy: 'price', sortOrder: 'asc' }
  if (sort === 'POPULAR') return { sortBy: 'likeCount', sortOrder: 'desc' }
  return {}
}

async function getProductDetail(productId: number): Promise<ProductDetail> {
  if (USE_MOCK) return mockGetProductDetail(productId)
  const res = await http.get(`/api/products/${productId}`)
  return unwrap(res)
}

export const productApi = {
  async getProducts(query: ProductListQuery): Promise<PageResponse<ProductSummary>> {
    if (USE_MOCK) return mockGetProducts(query)
    const { name, category, sort, page = 0, size = 12 } = query
    // 화면은 Spring Page 응답처럼 0부터 페이지를 다루지만, 현재 상품 검색 API의 요청 page는 1부터 시작한다.
    const res = await http.get('/api/products', {
      params: { name, category, page: page + 1, size, ...toSortParams(sort) },
    })
    return unwrapPage(res)
  },

  getProductDetail,

  // 관리자 목록처럼 상품 정보만 필요한 호출부는 상세 응답의 productInfo만 사용한다.
  async getProduct(productId: number): Promise<Product> {
    if (USE_MOCK) return mockGetProduct(productId)
    return (await getProductDetail(productId)).productInfo
  },

  async toggleLike(productId: number): Promise<void> {
    if (USE_MOCK) return mockToggleProductLike(productId)
    const res = await http.post(`/api/products/${productId}/like`)
    unwrap(res)
  },
}
