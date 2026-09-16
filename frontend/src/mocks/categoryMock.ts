import { MOCK_CATEGORIES } from './categories'
import { delay } from './helpers'
import type { Category } from '@/types/category'

export async function mockGetCategories(): Promise<Category[]> {
  await delay(150)
  return MOCK_CATEGORIES
}
