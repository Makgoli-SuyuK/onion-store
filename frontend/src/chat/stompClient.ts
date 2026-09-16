import { Client, type IMessage } from '@stomp/stompjs'
import { getAccessToken } from '@/utils/storage'

function wsUrl(): string {
  const base = import.meta.env.VITE_API_BASE_URL || window.location.origin
  return base.replace(/^http/, 'ws') + '/ws-stomp'
}

// 채팅방 1개 구독용 STOMP 연결을 열고, 구독/해제 함수를 돌려준다.
export function connectChatRoom(roomId: number, onMessage: (raw: unknown) => void) {
  const client = new Client({
    brokerURL: wsUrl(),
    connectHeaders: { Authorization: `Bearer ${getAccessToken() ?? ''}` },
    reconnectDelay: 3000,
  })

  client.onConnect = () => {
    client.subscribe(`/sub/chats/rooms/${roomId}`, (frame: IMessage) => {
      try {
        onMessage(JSON.parse(frame.body))
      } catch {
        // 파싱 실패한 프레임은 무시
      }
    })
  }

  client.activate()

  return {
    send: (message: string) => {
      client.publish({
        destination: `/pub/chats/rooms/${roomId}/messages`,
        body: JSON.stringify({ message }),
      })
    },
    disconnect: () => {
      client.deactivate()
    },
  }
}
