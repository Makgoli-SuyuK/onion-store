const formatter = new Intl.NumberFormat('ko-KR', {
  style: 'currency',
  currency: 'KRW',
})

export function formatCurrency(amount: number): string {
  return formatter.format(amount)
}
