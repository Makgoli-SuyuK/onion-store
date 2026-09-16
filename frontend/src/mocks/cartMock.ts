import { MOCK_PRODUCTS } from './products'
import { delay } from './helpers'
import type { Cart, CartItem } from '@/types/cart'

let items: CartItem[] = []
let nextCartItemId = 1

function toCart(): Cart {
  return {
    cartId: 1,
    items,
    totalPrice: items.reduce((sum, i) => sum + i.subtotal, 0),
  }
}

export async function mockGetCart(): Promise<Cart> {
  await delay()
  return toCart()
}

export async function mockAddItem(productId: number, quantity: number): Promise<CartItem> {
  await delay(200)
  const product = MOCK_PRODUCTS.find((p) => p.id === productId)
  if (!product) throw new Error('요청하신 상품 정보를 찾을 수 없습니다.')
  if (product.status !== 'SELLING') throw new Error('현재 판매할 수 없는 상품입니다.')

  const existing = items.find((item) => item.productId === productId)
  if (existing) {
    const nextQuantity = existing.quantity + quantity
    if (nextQuantity > product.stock) throw new Error('선택한 수량보다 재고가 부족합니다.')
    existing.quantity = nextQuantity
    existing.subtotal = existing.price * existing.quantity
    return existing
  }

  if (quantity > product.stock) throw new Error('선택한 수량보다 재고가 부족합니다.')

  const item: CartItem = {
    cartItemId: nextCartItemId++,
    productId: product.id,
    productName: product.name,
    price: product.price,
    quantity,
    subtotal: product.price * quantity,
  }
  items.push(item)
  return item
}

export async function mockUpdateQuantity(cartItemId: number, quantity: number): Promise<CartItem> {
  await delay(150)
  const item = items.find((i) => i.cartItemId === cartItemId)
  if (!item) throw new Error('장바구니에서 해당 상품을 찾을 수 없습니다.')
  const product = MOCK_PRODUCTS.find((p) => p.id === item.productId)
  if (product && quantity > product.stock) throw new Error('선택한 수량보다 재고가 부족합니다.')
  item.quantity = Math.max(1, quantity)
  item.subtotal = item.price * item.quantity
  return item
}

export async function mockRemoveItem(cartItemId: number): Promise<void> {
  await delay(150)
  const before = items.length
  items = items.filter((i) => i.cartItemId !== cartItemId)
  if (items.length === before) throw new Error('장바구니에서 해당 상품을 찾을 수 없습니다.')
}

// 주문 생성 성공 후 결제된 항목을 장바구니에서 제거할 때 사용 (백엔드도 주문 생성 시 자동으로 비움)
export function mockRemoveItems(cartItemIds: number[]): void {
  items = items.filter((i) => !cartItemIds.includes(i.cartItemId))
}

export function mockFindCartItems(cartItemIds: number[]): CartItem[] {
  if (cartItemIds.length === 0) return items
  return items.filter((i) => cartItemIds.includes(i.cartItemId))
}
