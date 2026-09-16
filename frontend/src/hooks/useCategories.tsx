import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { categoryApi } from '@/api/categoryApi'
import type { Category } from '@/types/category'

interface CategoriesContextValue {
  categories: Category[]
  loading: boolean
}

const CategoriesContext = createContext<CategoriesContextValue>({ categories: [], loading: true })

export function CategoriesProvider({ children }: { children: ReactNode }) {
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    categoryApi
      .getCategories()
      .then(setCategories)
      .catch(() => setCategories([]))
      .finally(() => setLoading(false))
  }, [])

  return <CategoriesContext.Provider value={{ categories, loading }}>{children}</CategoriesContext.Provider>
}

export function useCategories(): CategoriesContextValue {
  return useContext(CategoriesContext)
}
