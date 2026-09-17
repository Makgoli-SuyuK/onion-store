import { http, unwrap } from './client'
import type { Category } from '@/types/category'

export const categoryApi = {
  async getCategories(): Promise<Category[]> {
    const res = await http.get('/api/categories')
    return unwrap(res)
  },

  async createCategory(name: string): Promise<void> {
    const res = await http.post('/api/categories', { name })
    unwrap(res)
  },

  async updateCategory(categoryId: number, newName: string): Promise<void> {
    const res = await http.patch(`/api/categories/${categoryId}`, { newName })
    unwrap(res)
  },

  async deleteCategory(categoryId: number): Promise<void> {
    const res = await http.delete(`/api/categories/${categoryId}`)
    unwrap(res)
  },
}
