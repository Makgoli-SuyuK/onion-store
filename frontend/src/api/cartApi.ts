import { http, unwrap, USE_MOCK } from './client'
import { mockAddItem, mockGetCart, mockRemoveItem, mockUpdateQuantity } from '@/mocks/cartMock'
import type { Cart, CartItem } from '@/types/cart'

export const cartApi = {
  async getCart(): Promise<Cart> {
    if (USE_MOCK) return mockGetCart()
    const res = await http.get('/api/carts')
    return unwrap(res)
  },

  async addItem(productId: number, quantity: number): Promise<CartItem> {
    if (USE_MOCK) return mockAddItem(productId, quantity)
    const res = await http.post('/api/carts/items', { productId, quantity })
    return unwrap(res)
  },

  async updateQuantity(cartItemId: number, quantity: number): Promise<CartItem> {
    if (USE_MOCK) return mockUpdateQuantity(cartItemId, quantity)
    const res = await http.patch(`/api/carts/items/${cartItemId}`, { quantity })
    return unwrap(res)
  },

  async removeItem(cartItemId: number): Promise<void> {
    if (USE_MOCK) return mockRemoveItem(cartItemId)
    const res = await http.delete(`/api/carts/items/${cartItemId}`)
    return unwrap(res)
  },
}
