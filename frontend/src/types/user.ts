export type UserRole = 'CUSTOMER' | 'ADMIN'

// GET /api/users/me 응답
export interface User {
  userId: number
  email: string
  name: string
  phoneNumber: string
  role: UserRole
}

export interface LoginRequest {
  email: string
  password: string
}

// POST /api/auth/login 응답: 이름/이메일은 없다. 로그인 후 /api/users/me를 따로 호출해야 함.
export interface LoginResponse {
  accessToken: string
  tokenType: string
  userId: number
  role: UserRole
}

export interface SignupRequest {
  email: string
  password: string
  name: string
  phoneNumber: string
}

export interface UserProfileUpdateRequest {
  name: string
  phoneNumber: string
}

export interface PasswordChangeRequest {
  currentPassword: string
  newPassword: string
}

export interface WithdrawRequest {
  password: string
}
