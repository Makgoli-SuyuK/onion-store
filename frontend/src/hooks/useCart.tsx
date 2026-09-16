import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { cartApi } from '@/api/cartApi'
import { useAuth } from './useAuth'
import { useToast } from './useToast'
import { resolveErrorMessage } from '@/utils/errorMessage'
import type { CartItem } from '@/types/cart'

interface CartContextValue {
  items: CartItem[]
  totalPrice: number
  loading: boolean
  totalCount: number
  refresh: () => Promise<void>
  addItem: (productId: number, quantity: number) => Promise<CartItem>
  updateQuantity: (cartItemId: number, quantity: number) => Promise<void>
  removeItem: (cartItemId: number) => Promise<void>
}

const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const { showToast } = useToast()
  const [items, setItems] = useState<CartItem[]>([])
  const [totalPrice, setTotalPrice] = useState(0)
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setItems([])
      setTotalPrice(0)
      return
    }
    setLoading(true)
    try {
      const cart = await cartApi.getCart()
      setItems(cart.items)
      setTotalPrice(cart.totalPrice)
    } catch (err) {
      showToast(resolveErrorMessage(err), 'error')
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated, showToast])

  useEffect(() => {
    refresh()
  }, [refresh])

  const addItem = useCallback(
    async (productId: number, quantity: number) => {
      try {
        const item = await cartApi.addItem(productId, quantity)
        await refresh()
        showToast('장바구니에 담았습니다.', 'success')
        return item
      } catch (err) {
        showToast((err as Error).message || resolveErrorMessage(err), 'error')
        throw err
      }
    },
    [refresh, showToast],
  )

  const updateQuantity = useCallback(
    async (cartItemId: number, quantity: number) => {
      try {
        await cartApi.updateQuantity(cartItemId, quantity)
        await refresh()
      } catch (err) {
        showToast((err as Error).message || resolveErrorMessage(err), 'error')
      }
    },
    [refresh, showToast],
  )

  const removeItem = useCallback(
    async (cartItemId: number) => {
      try {
        await cartApi.removeItem(cartItemId)
        await refresh()
      } catch (err) {
        showToast((err as Error).message || resolveErrorMessage(err), 'error')
      }
    },
    [refresh, showToast],
  )

  const totalCount = items.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <CartContext.Provider
      value={{ items, totalPrice, loading, totalCount, refresh, addItem, updateQuantity, removeItem }}
    >
      {children}
    </CartContext.Provider>
  )
}

export function useCart(): CartContextValue {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart는 CartProvider 내부에서만 사용할 수 있습니다.')
  return ctx
}
