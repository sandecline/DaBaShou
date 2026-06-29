import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTrustLevel } from '@/utils/format'

export function useUser() {
  const userStore = useUserStore()

  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const currentUser = computed(() => userStore.user)
  const trustLevel = computed(() => {
    if (!userStore.user) return null
    return getTrustLevel(userStore.user.trustScore)
  })

  return {
    isLoggedIn,
    currentUser,
    trustLevel,
    login: userStore.login,
    logout: userStore.logout,
    fetchProfile: userStore.fetchProfile,
  }
}
