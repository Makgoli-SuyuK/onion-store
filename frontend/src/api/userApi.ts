import { http, unwrap } from './client'
import type { PasswordChangeRequest, User, UserProfileUpdateRequest, WithdrawRequest } from '@/types/user'

export const userApi = {
  async getMe(): Promise<User> {
    const res = await http.get('/api/users/me')
    return unwrap(res)
  },

  async updateProfile(request: UserProfileUpdateRequest): Promise<User> {
    const res = await http.patch('/api/users/me', request)
    return unwrap(res)
  },

  async changePassword(request: PasswordChangeRequest): Promise<void> {
    const res = await http.patch('/api/users/me/password', request)
    unwrap(res)
  },

  async withdraw(request: WithdrawRequest): Promise<void> {
    const res = await http.delete('/api/users/me', { data: request })
    unwrap(res)
  },
}
