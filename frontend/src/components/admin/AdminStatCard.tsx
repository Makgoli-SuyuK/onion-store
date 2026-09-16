export function AdminStatCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="card admin-stat-card">
      <p className="admin-stat-card__label">{label}</p>
      <p className="admin-stat-card__value">{value.toLocaleString('ko-KR')}</p>
    </div>
  )
}
