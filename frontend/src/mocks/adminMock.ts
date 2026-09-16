import { MOCK_PRODUCTS } from './products'
import { delay } from './helpers'
import type { AdminProductStats, ProductCreateRequest, ProductEditRequest } from '@/types/admin'
import type { PageResponse } from '@/types/common'
import type { Product, ProductListQuery } from '@/types/product'

let nextProductId = Math.max(...MOCK_PRODUCTS.map((p) => p.id)) + 1

export async function mockGetStats(): Promise<AdminProductStats> {
  await delay(200)
  const visible = MOCK_PRODUCTS.filter((p) => p.status !== 'HIDDEN')
  return {
    totalCount: visible.length,
    sellingCount: visible.filter((p) => p.status === 'SELLING').length,
    soldOutCount: visible.filter((p) => p.status === 'SOLD_OUT').length,
  }
}

export async function mockGetAdminProducts(query: ProductListQuery): Promise<PageResponse<Product>> {
  await delay()
  const { name, category, page = 0, size = 50 } = query
  let filtered = MOCK_PRODUCTS.filter((p) => p.status !== 'HIDDEN')
  if (category) filtered = filtered.filter((p) => p.categoryName === category)
  if (name?.trim()) {
    const k = name.trim().toLowerCase()
    filtered = filtered.filter((p) => p.name.toLowerCase().includes(k))
  }
  const start = page * size
  const content = filtered.slice(start, start + size)
  return {
    content,
    totalElements: filtered.length,
    totalPages: Math.max(1, Math.ceil(filtered.length / size)),
    page,
    size,
  }
}

export async function mockCreateProduct(req: ProductCreateRequest): Promise<void> {
  await delay(300)
  const now = new Date().toISOString()
  MOCK_PRODUCTS.push({
    id: nextProductId++,
    categoryName: req.category,
    name: req.productName,
    description: req.description,
    price: req.price,
    stock: req.stock,
    likeCount: 0,
    status: req.stock === 0 ? 'SOLD_OUT' : 'SELLING',
    createdAt: now,
    updatedAt: now,
  })
}

export async function mockUpdateProduct(productId: number, req: ProductEditRequest): Promise<Product> {
  await delay(300)
  const product = MOCK_PRODUCTS.find((p) => p.id === productId)
  if (!product) throw new Error('요청하신 상품 정보를 찾을 수 없습니다.')
  product.name = req.name
  product.description = req.description
  product.price = req.price
  product.stock = req.stock
  product.status = req.status
  product.updatedAt = new Date().toISOString()
  return product
}
