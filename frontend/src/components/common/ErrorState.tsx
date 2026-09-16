export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="state-box">
      <span className="state-box__emoji" aria-hidden="true">
        😥
      </span>
      <p>{message}</p>
      {onRetry && (
        <button type="button" className="btn btn--outline btn--sm" onClick={onRetry}>
          다시 시도
        </button>
      )}
    </div>
  )
}
