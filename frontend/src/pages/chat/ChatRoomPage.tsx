import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { chatApi } from '@/api/chatApi'
import { connectChatRoom } from '@/chat/stompClient'
import { useAuth } from '@/hooks/useAuth'
import { useToast } from '@/hooks/useToast'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { CHAT_STATUS_LABEL } from '@/constants/chatStatus'
import type { ChatMessage, ChatRoomDetail, ChatRoomStatus } from '@/types/chat'
import './ChatRoomPage.css'

const ADMIN_STATUS_OPTIONS: ChatRoomStatus[] = ['WAITING', 'IN_PROGRESS', 'COMPLETED']

interface ChatBroadcast {
  messageId: number
  senderId: number
  senderName: string
  message: string
  sentAt: string
}

export function ChatRoomPage() {
  const { roomId } = useParams<{ roomId: string }>()
  const id = Number(roomId)
  const navigate = useNavigate()
  const { user, isAdmin } = useAuth()
  const { showToast } = useToast()

  const [room, setRoom] = useState<ChatRoomDetail | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const listRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    Promise.all([chatApi.getRoom(id), chatApi.getMessages(id, undefined, 30)])
      .then(([roomDetail, messagePage]) => {
        if (cancelled) return
        setRoom(roomDetail)
        setMessages(messagePage.messages)
      })
      .catch((err) => {
        if (!cancelled) setError((err as Error).message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  const stompRef = useRef<ReturnType<typeof connectChatRoom> | null>(null)
  useEffect(() => {
    if (!room) return
    const conn = connectChatRoom(id, (raw) => {
      const data = raw as ChatBroadcast
      setMessages((prev) => [
        ...prev,
        { messageId: data.messageId, senderId: data.senderId, senderName: data.senderName, message: data.message, sentAt: data.sentAt },
      ])
    })
    stompRef.current = conn
    return () => {
      conn.disconnect()
      stompRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, !!room])

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight })
  }, [messages])

  const handleSend = async (e: FormEvent) => {
    e.preventDefault()
    const text = input.trim()
    if (!text || sending) return
    setSending(true)
    try {
      stompRef.current?.send(text)
      setInput('')
    } catch (err) {
      showToast((err as Error).message, 'error')
    } finally {
      setSending(false)
    }
  }

  const handleStatusChange = async (status: ChatRoomStatus) => {
    try {
      await chatApi.updateStatus(id, status)
      setRoom((prev) => (prev ? { ...prev, status } : prev))
      showToast('상담 상태를 변경했습니다.', 'success')
    } catch (err) {
      showToast((err as Error).message, 'error')
    }
  }

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorState message={error} />
  if (!room) return null

  const closed = room.status === 'COMPLETED'

  return (
    <div className="container chat-room-page">
      <button type="button" className="chat-room-page__back" onClick={() => navigate('/chat')}>
        ← 목록으로
      </button>

      <div className="chat-room-page__header">
        <div>
          <h1>{room.title}</h1>
          <p className="chat-room-page__meta">
            {room.customerName} · {new Date(room.createdDate).toLocaleString('ko-KR')}
          </p>
        </div>
        {isAdmin ? (
          <select
            className="input chat-room-page__status-select"
            value={room.status}
            onChange={(e) => handleStatusChange(e.target.value as ChatRoomStatus)}
          >
            {ADMIN_STATUS_OPTIONS.map((s) => (
              <option key={s} value={s} disabled={room.status === 'COMPLETED' && s !== 'COMPLETED'}>
                {CHAT_STATUS_LABEL[s]}
              </option>
            ))}
          </select>
        ) : (
          <span className="badge badge--tag">{CHAT_STATUS_LABEL[room.status]}</span>
        )}
      </div>

      <div className="chat-room-page__messages" ref={listRef}>
        {messages.map((m) => {
          const mine = m.senderId === user?.userId
          return (
            <div key={m.messageId} className={`chat-bubble-row ${mine ? 'chat-bubble-row--mine' : ''}`}>
              {!mine && <span className="chat-bubble__sender">{m.senderName}</span>}
              <div className="chat-bubble">{m.message}</div>
              <span className="chat-bubble__time">
                {new Date(m.sentAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          )
        })}
      </div>

      <form className="chat-room-page__input" onSubmit={handleSend}>
        <input
          className="input"
          placeholder={closed ? '완료된 문의에는 메시지를 보낼 수 없어요' : '메시지를 입력하세요'}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          disabled={closed || sending}
        />
        <button type="submit" className="btn btn--primary btn--sm" disabled={closed || sending || !input.trim()}>
          전송
        </button>
      </form>
    </div>
  )
}
