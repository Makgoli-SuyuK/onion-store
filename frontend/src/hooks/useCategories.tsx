import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { categoryApi } from '@/api/categoryApi'
import type { Category } from '@/types/category'

interface CategoriesContextValue {
  categories: Category[]
  loading: boolean
  refresh: () => Promise<void>
}

const CategoriesContext = createContext<CategoriesContextValue>({
  categories: [],
  loading: true,
  refresh: async () => {},
})

export function CategoriesProvider({ children }: { children: ReactNode }) {
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async () => {
    setLoading(true)
    try {
      setCategories(await categoryApi.getCategories())
    } catch {
      setCategories([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  return <CategoriesContext.Provider value={{ categories, loading, refresh }}>{children}</CategoriesContext.Provider>
}

export function useCategories(): CategoriesContextValue {
  return useContext(CategoriesContext)
}
