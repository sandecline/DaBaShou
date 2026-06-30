const TOKEN_KEY = 'dabashou_token'
const USER_KEY = 'dabashou_user'

function parseJwtPayload(token: string): Record<string, any> | null {
  const [, payload] = token.split('.')
  if (!payload) return null
  try {
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(normalized)
        .split('')
        .map(char => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join(''),
    )
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function isTokenExpired(token: string | null): boolean {
  if (!token) return true
  const payload = parseJwtPayload(token)
  if (!payload?.exp) return false
  return payload.exp * 1000 <= Date.now()
}

export function getToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY)
  if (isTokenExpired(token)) {
    removeToken()
    removeUserInfo()
    return null
  }
  return token
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function isLoggedIn(): boolean {
  return !!getToken()
}

export function getUserInfo(): Record<string, any> | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function setUserInfo(user: Record<string, any>): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_KEY)
}

export function logout(): void {
  removeToken()
  removeUserInfo()
}
