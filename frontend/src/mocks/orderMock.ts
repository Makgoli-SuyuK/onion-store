import { MOCK_PRODUCTS } from './products'
import { mockFindCartItems, mockRemoveItems } from './cartMock'
import { delay } from './helpers'
import type { PageResponse } from '@/types/common'
import type {
  Order,
  OrderCreateResponse,
  OrderItem,
  OrderStatus,
  OrderSummary,
  PaymentConfirmResponse,
} from '@/types/order'

interface MockOrder {
  orderId: number
  orderNumber: string
  createdAt: string
  paidAt: string | null
  status: OrderStatus
  portonePaymentId: string
  items: OrderItem[]
  totalPrice: number
}

let orders: MockOrder[] = [
  {
    orderId: 1001,
    orderNumber: 'ORD-20260901-0001',
    createdAt: '2026-09-01T10:32:00+09:00',
    paidAt: '2026-09-01T10:33:00+09:00',
    status: 'PAID',
    portonePaymentId: 'seed-payment-1',
    items: [
      { orderItemId: 1, productId: 3, productName: '전남 무안 햇양파 1.5kg', productPrice: 6900, quantity: 2 },
      { orderItemId: 2, productId: 9, productName: '양파즙 100포', productPrice: 32000, quantity: 1 },
    ],
    totalPrice: 6900 * 2 + 32000,
  },
  {
    orderId: 1002,
    orderNumber: 'ORD-20260910-0002',
    createdAt: '2026-09-10T14:05:00+09:00',
    paidAt: null,
    status: 'PENDING',
    portonePaymentId: 'seed-payment-2',
    items: [{ orderItemId: 3, productId: 1, productName: '국산 양파 1kg', productPrice: 4500, quantity: 2 }],
    totalPrice: 9000,
  },
]
let nextOrderId = 1003
let nextOrderItemId = 100

export async function mockCreateOrder(cartItemIds: number[]): Promise<OrderCreateResponse> {
  await delay(400)
  const items = mockFindCartItems(cartItemIds)
  if (items.length === 0) throw new Error('장바구니가 비어있습니다.')

  for (const cartItem of items) {
    const product = MOCK_PRODUCTS.find((p) => p.id === cartItem.productId)
    if (!product || product.status !== 'SELLING' || product.stock < cartItem.quantity) {
      throw new Error('선택한 수량보다 재고가 부족합니다.')
    }
  }

  items.forEach((cartItem) => {
    const product = MOCK_PRODUCTS.find((p) => p.id === cartItem.productId)!
    product.stock -= cartItem.quantity
    if (product.stock === 0) product.status = 'SOLD_OUT'
  })

  const totalPrice = items.reduce((sum, i) => sum + i.price * i.quantity, 0)
  const orderItems: OrderItem[] = items.map((i) => ({
    orderItemId: nextOrderItemId++,
    productId: i.productId,
    productName: i.productName,
    productPrice: i.price,
    quantity: i.quantity,
  }))

  const order: MockOrder = {
    orderId: nextOrderId++,
    orderNumber: `ORD-${Date.now()}`,
    createdAt: new Date().toISOString(),
    paidAt: null,
    status: 'PENDING',
    portonePaymentId: `mock-payment-${Date.now()}`,
    items: orderItems,
    totalPrice,
  }
  orders = [order, ...orders]
  mockRemoveItems(items.map((i) => i.cartItemId))

  return {
    orderId: order.orderId,
    orderNumber: order.orderNumber,
    totalPrice: order.totalPrice,
    createdAt: order.createdAt,
    status: order.status,
    portonePaymentId: order.portonePaymentId,
    items: orderItems.map((i) => ({ productName: i.productName, productPrice: i.productPrice, quantity: i.quantity })),
  }
}

export async function mockConfirmPayment(orderId: number, portonePaymentId: string): Promise<PaymentConfirmResponse> {
  await delay(300)
  const order = orders.find((o) => o.orderId === orderId)
  if (!order) throw new Error('요청하신 주문 정보를 찾을 수 없습니다.')
  order.status = 'PAID'
  order.paidAt = new Date().toISOString()
  order.portonePaymentId = portonePaymentId
  return { orderId, portonePaymentId, paymentStatus: 'SUCCESS', orderStatus: 'PAID' }
}

export async function mockGetOrder(orderId: number): Promise<Order> {
  await delay(200)
  const order = orders.find((o) => o.orderId === orderId)
  if (!order) throw new Error('요청하신 주문 정보를 찾을 수 없습니다.')
  return {
    orderNumber: order.orderNumber,
    totalPrice: order.totalPrice,
    createdAt: order.createdAt,
    paidAt: order.paidAt,
    orderStatus: order.status,
    paymentStatus: order.status === 'PAID' ? 'SUCCESS' : 'READY',
    orderItems: order.items,
  }
}

export async function mockGetMyOrders(page: number, size: number): Promise<PageResponse<OrderSummary>> {
  await delay(250)
  const summaries: OrderSummary[] = orders.map((o) => ({
    orderId: o.orderId,
    orderNumber: o.orderNumber,
    items: o.items.map((i) => ({ productName: i.productName, productPrice: i.productPrice, quantity: i.quantity })),
    totalPrice: o.totalPrice,
    createdAt: o.createdAt,
    paidAt: o.paidAt,
  }))
  const start = page * size
  const content = summaries.slice(start, start + size)
  return {
    content,
    totalElements: summaries.length,
    totalPages: Math.max(1, Math.ceil(summaries.length / size)),
    page,
    size,
  }
}
