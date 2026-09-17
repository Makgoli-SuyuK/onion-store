export function EmptyState({
  emoji = '🧅',
  message,
  description,
}: {
  emoji?: string
  message: string
  description?: string
}) {
  return (
    <div className="state-box">
      <span className="state-box__emoji" aria-hidden="true">
        {emoji}
      </span>
      <p>{message}</p>
      {description && <p style={{ fontSize: 13, color: 'var(--color-text-faint)' }}>{description}</p>}
    </div>
  )
}
