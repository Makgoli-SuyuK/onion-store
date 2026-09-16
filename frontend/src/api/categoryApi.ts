import { http, unwrap, USE_MOCK } from './client'
import { mockGetCategories } from '@/mocks/categoryMock'
import type { Category } from '@/types/category'

export const categoryApi = {
  async getCategories(): Promise<Category[]> {
    if (USE_MOCK) return mockGetCategories()
    const res = await http.get('/api/categories')
    return unwrap(res)
  },
}
