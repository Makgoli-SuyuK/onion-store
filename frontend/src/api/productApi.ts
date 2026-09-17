import { http, unwrap, unwrapPage } from './client'
import type { PageResponse } from '@/types/common'
import type { Product, ProductDetail, ProductListQuery, ProductSummary, SortOption } from '@/types/product'

// Spring Pageable 형식: sort=필드,방향. 페이지 번호는 0부터 시작한다.
function toSortParams(sort?: SortOption): { sort: string } {
  if (sort === 'PRICE_ASC') return { sort: 'price,asc' }
  if (sort === 'POPULAR') return { sort: 'likeCount,desc' }
  return { sort: 'createdAt,desc' }
}

async function getProductDetail(productId: number): Promise<ProductDetail> {
  const res = await http.get(`/api/products/${productId}`)
  return unwrap(res)
}

export const productApi = {
  async getProducts(query: ProductListQuery): Promise<PageResponse<ProductSummary>> {
    const { name, category, sort, page = 0, size = 12 } = query
    const res = await http.get('/api/products', {
      params: { name, category, page, size, ...toSortParams(sort) },
    })
    return unwrapPage(res)
  },

  getProductDetail,

  // 관리자 목록처럼 상품 정보만 필요한 호출부는 상세 응답의 productInfo만 사용한다.
  async getProduct(productId: number): Promise<Product> {
    return (await getProductDetail(productId)).productInfo
  },

  async toggleLike(productId: number): Promise<void> {
    const res = await http.post(`/api/products/${productId}/like`)
    unwrap(res)
  },
}
