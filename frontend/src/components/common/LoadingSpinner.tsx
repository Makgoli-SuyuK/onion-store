export function LoadingSpinner({ label = '불러오는 중이에요...' }: { label?: string }) {
  return (
    <div className="state-box" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  )
}
