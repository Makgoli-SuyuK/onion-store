import { delay } from './helpers'
import { getAccessToken } from '@/utils/storage'
import type { LoginRequest, LoginResponse, SignupRequest, User } from '@/types/user'

const registeredUsers = new Map<string, { name: string; phoneNumber: string; role: User['role'] }>()
let nextUserId = 1

// 데모용: 이메일에 admin이 들어가면 관리자 계정으로 가입/로그인됩니다. 비밀번호는 검증하지 않습니다.
export async function mockSignup(req: SignupRequest): Promise<void> {
  await delay(300)
  if (!req.name.trim() || !req.email.trim() || !req.password.trim() || !req.phoneNumber.trim()) {
    throw new Error('이름, 이메일, 전화번호, 비밀번호를 모두 입력해 주세요.')
  }
  const email = req.email.trim().toLowerCase()
  if (registeredUsers.has(email)) {
    throw new Error('이미 사용 중인 이메일입니다.')
  }
  registeredUsers.set(email, {
    name: req.name.trim(),
    phoneNumber: req.phoneNumber.trim(),
    role: email.includes('admin') ? 'ADMIN' : 'CUSTOMER',
  })
}

export async function mockLogin(req: LoginRequest): Promise<LoginResponse> {
  await delay(300)
  if (!req.email.trim() || !req.password.trim()) {
    throw new Error('이메일과 비밀번호를 입력해 주세요.')
  }
  const email = req.email.trim().toLowerCase()
  let profile = registeredUsers.get(email)
  if (!profile) {
    // 가입 이력이 없어도 데모 편의상 즉석으로 계정을 만들어 로그인시킨다.
    profile = { name: email.split('@')[0], phoneNumber: '', role: email.includes('admin') ? 'ADMIN' : 'CUSTOMER' }
    registeredUsers.set(email, profile)
  }
  const userId = profile.role === 'ADMIN' ? 9000 : 1000 + nextUserId++
  const accessToken = `mock.${email}`
  return { accessToken, tokenType: 'Bearer', userId, role: profile.role }
}

// 다른 mock 모듈(채팅 등)이 "지금 로그인된 사용자"가 필요할 때 재사용
export function getCurrentMockUser(): User | null {
  const token = getAccessToken()
  const email = token?.startsWith('mock.') ? token.slice(5) : null
  const profile = email ? registeredUsers.get(email) : null
  if (!email || !profile) return null
  const userId = profile.role === 'ADMIN' ? 9000 : 1
  return { userId, email, name: profile.name, phoneNumber: profile.phoneNumber, role: profile.role }
}

export async function mockGetMe(): Promise<User> {
  await delay(150)
  const user = getCurrentMockUser()
  if (!user) throw new Error('로그인이 필요한 서비스입니다.')
  return user
}
