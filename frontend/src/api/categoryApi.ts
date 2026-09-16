import { http, unwrap, USE_MOCK } from './client'
import { mockCreateCategory, mockDeleteCategory, mockGetCategories, mockUpdateCategory } from '@/mocks/categoryMock'
import type { Category } from '@/types/category'

export const categoryApi = {
  async getCategories(): Promise<Category[]> {
    if (USE_MOCK) return mockGetCategories()
    const res = await http.get('/api/categories')
    return unwrap(res)
  },

  async createCategory(name: string): Promise<void> {
    if (USE_MOCK) return mockCreateCategory(name)
    const res = await http.post('/api/categories', { name })
    unwrap(res)
  },

  async updateCategory(categoryId: number, newName: string): Promise<void> {
    if (USE_MOCK) return mockUpdateCategory(categoryId, newName)
    const res = await http.patch(`/api/categories/${categoryId}`, { newName })
    unwrap(res)
  },

  async deleteCategory(categoryId: number): Promise<void> {
    if (USE_MOCK) return mockDeleteCategory(categoryId)
    const res = await http.delete(`/api/categories/${categoryId}`)
    unwrap(res)
  },
}
