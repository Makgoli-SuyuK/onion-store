import { createBrowserRouter } from 'react-router-dom'
import { Layout } from '@/components/layout/Layout'
import { ProtectedRoute } from './ProtectedRoute'
import { HomePage } from '@/pages/HomePage'
import { ProductListPage } from '@/pages/ProductListPage'
import { ProductDetailPage } from '@/pages/ProductDetailPage'
import { CartPage } from '@/pages/CartPage'
import { CheckoutPage } from '@/pages/CheckoutPage'
import { OrderCompletePage } from '@/pages/OrderCompletePage'
import { MyOrdersPage } from '@/pages/MyOrdersPage'
import { OrderDetailPage } from '@/pages/OrderDetailPage'
import { MyRefundsPage } from '@/pages/MyRefundsPage'
import { MyRefundDetailPage } from '@/pages/MyRefundDetailPage'
import { MyProfilePage } from '@/pages/MyProfilePage'
import { LoginPage } from '@/pages/LoginPage'
import { SignupPage } from '@/pages/SignupPage'
import { AdminProductListPage } from '@/pages/admin/AdminProductListPage'
import { AdminRefundListPage } from '@/pages/admin/AdminRefundListPage'
import { AdminRefundDetailPage } from '@/pages/admin/AdminRefundDetailPage'
import { ChatListPage } from '@/pages/chat/ChatListPage'
import { ChatRoomPage } from '@/pages/chat/ChatRoomPage'
import { NotFoundPage } from '@/pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'products', element: <ProductListPage /> },
      { path: 'products/:id', element: <ProductDetailPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      {
        path: 'cart',
        element: (
          <ProtectedRoute>
            <CartPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'checkout',
        element: (
          <ProtectedRoute>
            <CheckoutPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'orders/complete/:orderId',
        element: (
          <ProtectedRoute>
            <OrderCompletePage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'mypage/orders',
        element: (
          <ProtectedRoute>
            <MyOrdersPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'mypage/profile',
        element: (
          <ProtectedRoute>
            <MyProfilePage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'mypage/orders/:orderId',
        element: (
          <ProtectedRoute>
            <OrderDetailPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'mypage/refunds',
        element: (
          <ProtectedRoute>
            <MyRefundsPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'mypage/refunds/:refundId',
        element: (
          <ProtectedRoute>
            <MyRefundDetailPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'admin/products',
        element: (
          <ProtectedRoute adminOnly>
            <AdminProductListPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'admin/refunds',
        element: (
          <ProtectedRoute adminOnly>
            <AdminRefundListPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'admin/refunds/:refundId',
        element: (
          <ProtectedRoute adminOnly>
            <AdminRefundDetailPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'chat',
        element: (
          <ProtectedRoute>
            <ChatListPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'chat/rooms/:roomId',
        element: (
          <ProtectedRoute>
            <ChatRoomPage />
          </ProtectedRoute>
        ),
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
