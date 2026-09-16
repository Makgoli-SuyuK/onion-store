import { MOCK_CATEGORIES } from './categories'
import { delay } from './helpers'
import type { Category } from '@/types/category'

export async function mockGetCategories(): Promise<Category[]> {
  await delay(150)
  return [...MOCK_CATEGORIES]
}

export async function mockCreateCategory(name: string): Promise<void> {
  await delay()
  if (MOCK_CATEGORIES.some((category) => category.name === name)) {
    throw new Error('이미 존재하는 카테고리입니다.')
  }
  const nextId = Math.max(0, ...MOCK_CATEGORIES.map((category) => category.id)) + 1
  MOCK_CATEGORIES.push({ id: nextId, name })
}

export async function mockUpdateCategory(categoryId: number, newName: string): Promise<void> {
  await delay()
  if (MOCK_CATEGORIES.some((category) => category.id !== categoryId && category.name === newName)) {
    throw new Error('이미 존재하는 카테고리입니다.')
  }
  const category = MOCK_CATEGORIES.find((item) => item.id === categoryId)
  if (!category) throw new Error('카테고리를 찾을 수 없습니다.')
  category.name = newName
}

export async function mockDeleteCategory(categoryId: number): Promise<void> {
  await delay()
  const index = MOCK_CATEGORIES.findIndex((category) => category.id === categoryId)
  if (index < 0) throw new Error('카테고리를 찾을 수 없습니다.')
  MOCK_CATEGORIES.splice(index, 1)
}
