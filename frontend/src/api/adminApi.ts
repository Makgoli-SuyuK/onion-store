import { http, unwrap, USE_MOCK } from './client'
import { productApi } from './productApi'
import {
  mockCreateProduct,
  mockGetAdminProducts,
  mockGetStats,
  mockUpdateProduct,
} from '@/mocks/adminMock'
import type { AdminProductStats, ProductCreateRequest, ProductEditRequest } from '@/types/admin'
import type { PageResponse } from '@/types/common'
import type { Product, ProductListQuery } from '@/types/product'

// 백엔드 상품 목록 API(GET /api/products)는 재고/판매상태를 내려주지 않는다(요약 응답).
// 관리자 화면은 행마다 재고/상태를 보여줘야 해서, 목록을 받은 뒤 상세(GET /api/products/{id})를
// 하나씩 더 호출해 합친다. TODO(백엔드): 목록 응답에 stock/status를 포함해주면 이 부분을 걷어낼 수 있음.
async function fetchProductsWithDetail(query: ProductListQuery): Promise<PageResponse<Product>> {
  const summaryPage = await productApi.getProducts(query)
  const details = await Promise.all(summaryPage.content.map((p) => productApi.getProduct(p.id)))
  return { ...summaryPage, content: details }
}

export const adminApi = {
  async getStats(): Promise<AdminProductStats> {
    if (USE_MOCK) return mockGetStats()
    const page = await fetchProductsWithDetail({ page: 0, size: 200 })
    return {
      totalCount: page.totalElements,
      sellingCount: page.content.filter((p) => p.status === 'SELLING').length,
      soldOutCount: page.content.filter((p) => p.status === 'SOLD_OUT').length,
    }
  },

  async getProducts(query: ProductListQuery): Promise<PageResponse<Product>> {
    if (USE_MOCK) return mockGetAdminProducts(query)
    return fetchProductsWithDetail(query)
  },

  // 응답 데이터가 없어(ApiResponse<Void>) 생성 후에는 화면에서 목록을 다시 불러와야 한다.
  async createProduct(req: ProductCreateRequest): Promise<void> {
    if (USE_MOCK) return mockCreateProduct(req)
    const res = await http.post('/api/products', req)
    unwrap(res)
  },

  async updateProduct(productId: number, req: ProductEditRequest): Promise<Product> {
    if (USE_MOCK) return mockUpdateProduct(productId, req)
    const res = await http.patch(`/api/products/${productId}`, req)
    return unwrap(res)
  },
}
