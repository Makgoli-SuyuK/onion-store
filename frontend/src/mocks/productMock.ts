import { MOCK_PRODUCTS } from './products'
import { delay } from './helpers'
import type { PageResponse } from '@/types/common'
import type { Product, ProductDetail, ProductListQuery, ProductSummary } from '@/types/product'

const likedProductIds = new Set<number>()

function toSummary(p: Product): ProductSummary {
  return { id: p.id, categoryName: p.categoryName, name: p.name, price: p.price, likeCount: p.likeCount }
}

export async function mockGetProducts(query: ProductListQuery): Promise<PageResponse<ProductSummary>> {
  await delay()
  const { name, category, sort = 'POPULAR', page = 0, size = 12 } = query

  let filtered = MOCK_PRODUCTS.filter((p) => p.status !== 'HIDDEN')
  if (category) filtered = filtered.filter((p) => p.categoryName === category)
  if (name?.trim()) {
    const k = name.trim().toLowerCase()
    filtered = filtered.filter((p) => p.name.toLowerCase().includes(k))
  }

  filtered = [...filtered].sort((a, b) => {
    if (sort === 'PRICE_ASC') return a.price - b.price
    if (sort === 'LATEST') return b.id - a.id
    return b.likeCount - a.likeCount
  })

  const start = page * size
  const content = filtered.slice(start, start + size).map(toSummary)

  return {
    content,
    totalElements: filtered.length,
    totalPages: Math.max(1, Math.ceil(filtered.length / size)),
    page,
    size,
  }
}

export async function mockGetProduct(productId: number): Promise<Product> {
  await delay()
  const product = MOCK_PRODUCTS.find((p) => p.id === productId)
  if (!product) throw new Error('요청하신 상품 정보를 찾을 수 없습니다.')
  return product
}

export async function mockGetProductDetail(productId: number): Promise<ProductDetail> {
  return {
    productInfo: await mockGetProduct(productId),
    liked: likedProductIds.has(productId),
  }
}

export async function mockToggleProductLike(productId: number): Promise<void> {
  await delay()
  const product = MOCK_PRODUCTS.find((p) => p.id === productId)
  if (!product) throw new Error('요청하신 상품 정보를 찾을 수 없습니다.')

  if (likedProductIds.delete(productId)) {
    product.likeCount = Math.max(0, product.likeCount - 1)
    return
  }

  likedProductIds.add(productId)
  product.likeCount += 1
}
