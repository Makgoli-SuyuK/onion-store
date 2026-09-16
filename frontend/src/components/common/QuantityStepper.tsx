import './QuantityStepper.css'

export function QuantityStepper({
  value,
  max,
  min = 1,
  onChange,
}: {
  value: number
  max?: number
  min?: number
  onChange: (next: number) => void
}) {
  const decrease = () => onChange(Math.max(min, value - 1))
  const increase = () => onChange(max != null ? Math.min(max, value + 1) : value + 1)

  return (
    <div className="qty-stepper">
      <button type="button" aria-label="수량 감소" onClick={decrease} disabled={value <= min}>
        −
      </button>
      <span aria-live="polite">{value}</span>
      <button type="button" aria-label="수량 증가" onClick={increase} disabled={max != null && value >= max}>
        +
      </button>
    </div>
  )
}
