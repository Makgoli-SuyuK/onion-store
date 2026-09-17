import { http, unwrap } from './client'
import type { Cart, CartItem } from '@/types/cart'

export const cartApi = {
  async getCart(): Promise<Cart> {
    const res = await http.get('/api/carts')
    return unwrap(res)
  },

  async addItem(productId: number, quantity: number): Promise<CartItem> {
    const res = await http.post('/api/carts/items', { productId, quantity })
    return unwrap(res)
  },

  async updateQuantity(cartItemId: number, quantity: number): Promise<CartItem> {
    const res = await http.patch(`/api/carts/items/${cartItemId}`, { quantity })
    return unwrap(res)
  },

  async removeItem(cartItemId: number): Promise<void> {
    const res = await http.delete(`/api/carts/items/${cartItemId}`)
    return unwrap(res)
  },
}
