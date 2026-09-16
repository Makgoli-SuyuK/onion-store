import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { chatApi } from '@/api/chatApi'
import { useApiRequest } from '@/hooks/useApiRequest'
import { useAuth } from '@/hooks/useAuth'
import { useToast } from '@/hooks/useToast'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { EmptyState } from '@/components/common/EmptyState'
import { CHAT_STATUS_LABEL } from '@/constants/chatStatus'
import type { ChatRoomStatus } from '@/types/chat'
import './ChatListPage.css'

const STATUS_TABS: { value: ChatRoomStatus | undefined; label: string }[] = [
  { value: undefined, label: '전체' },
  { value: 'WAITING', label: '대기중' },
  { value: 'IN_PROGRESS', label: '상담중' },
  { value: 'COMPLETED', label: '완료' },
]

export function ChatListPage() {
  const { isAdmin } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [status, setStatus] = useState<ChatRoomStatus | undefined>(undefined)
  const [creating, setCreating] = useState(false)
  const [title, setTitle] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const { data, loading, error, refetch } = useApiRequest(
    () => chatApi.getRooms(isAdmin ? status : undefined, 0, 30),
    [isAdmin, status],
  )

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault()
    if (!title.trim()) return
    setSubmitting(true)
    try {
      const room = await chatApi.createRoom(title.trim())
      navigate(`/chat/rooms/${room.roomId}`)
    } catch (err) {
      showToast((err as Error).message, 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container chat-list-page">
      <h1 className="chat-list-page__title">{isAdmin ? '문의 관리' : '1:1 문의'}</h1>

      {isAdmin && (
        <div className="chat-list-page__tabs">
          {STATUS_TABS.map((t) => (
            <button
              key={t.label}
              type="button"
              className={`chip ${status === t.value ? 'chip--active' : ''}`}
              onClick={() => setStatus(t.value)}
            >
              {t.label}
            </button>
          ))}
        </div>
      )}

      {!isAdmin && !creating && (
        <button type="button" className="btn btn--primary btn--sm" onClick={() => setCreating(true)}>
          + 새 문의하기
        </button>
      )}

      {!isAdmin && creating && (
        <form className="chat-list-page__create" onSubmit={handleCreate}>
          <input
            className="input"
            placeholder="문의 제목을 입력해 주세요 (1~50자)"
            maxLength={50}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            autoFocus
          />
          <button type="submit" className="btn btn--primary btn--sm" disabled={submitting}>
            {submitting ? '생성 중...' : '문의 시작'}
          </button>
          <button type="button" className="btn btn--ghost btn--sm" onClick={() => setCreating(false)}>
            취소
          </button>
        </form>
      )}

      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={refetch} />}
      {!loading && !error && data && data.content.length === 0 && (
        <EmptyState emoji="💬" message="문의 내역이 없어요." />
      )}
      {!loading && !error && data && data.content.length > 0 && (
        <div className="chat-list-page__list">
          {data.content.map((room) => (
            <Link key={room.roomId} to={`/chat/rooms/${room.roomId}`} className="card chat-room-item">
              <div>
                <p className="chat-room-item__title">{room.title}</p>
                <p className="chat-room-item__date">{new Date(room.createdAt).toLocaleString('ko-KR')}</p>
              </div>
              <span className="badge badge--tag">{CHAT_STATUS_LABEL[room.status]}</span>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
