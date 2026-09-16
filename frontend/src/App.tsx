import { RouterProvider } from 'react-router-dom'
import { router } from '@/routes/router'
import { ToastProvider } from '@/hooks/useToast'
import { AuthProvider } from '@/hooks/useAuth'
import { CartProvider } from '@/hooks/useCart'
import { CategoriesProvider } from '@/hooks/useCategories'

export function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <CartProvider>
          <CategoriesProvider>
            <RouterProvider router={router} />
          </CategoriesProvider>
        </CartProvider>
      </AuthProvider>
    </ToastProvider>
  )
}
