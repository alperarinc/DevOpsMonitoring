"use client"

import { createContext, useContext, useState, useEffect, type ReactNode } from "react"
import { toast } from "@/hooks/use-toast"
import { post, API_ENDPOINTS } from "@/lib/api"
import LogoutOverlay from "@/components/logout-overlay"

// Basitleştirilmiş User interface
interface User {
  id: string
  firstName: string
  lastName: string
  username: string
  email: string
  role: string
}

// Basitleştirilmiş AuthContextType
interface AuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<boolean>
  register: (firstName: string, lastName: string, email: string, username: string, password: string) => Promise<boolean>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

// JWT token'ı decode etmek için yardımcı fonksiyon
function parseJwt(token: string) {
  try {
    // Base64Url decode
    const base64Url = token.split(".")[1]
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/")
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error("JWT parse error:", error)
    return null
  }
}

// Token'ın geçerlilik süresini kontrol eden fonksiyon
function isTokenExpired(token: string | null): boolean {
  if (!token) return true

  try {
    const decodedToken = parseJwt(token)
    if (!decodedToken) return true

    // exp değeri saniye cinsinden, Date.now() milisaniye cinsinden
    const currentTime = Date.now() / 1000
    return decodedToken.exp < currentTime
  } catch (error) {
    console.error("Token expiration check error:", error)
    return true
  }
}

// Basit storage yardımcı fonksiyonları - hem localStorage hem de sessionStorage kullanıyoruz
const storage = {
  // Kullanıcı bilgilerini al
  getUser: (): User | null => {
    try {
      // Önce sessionStorage'dan kontrol et
      const sessionUser = sessionStorage.getItem("user")
      if (sessionUser && sessionUser !== "undefined" && sessionUser !== "null") {
        console.log("User found in sessionStorage")
        try {
          return JSON.parse(sessionUser)
        } catch (e) {
          console.error("Error parsing sessionStorage user:", e)
          // Hatalı veriyi temizle
          sessionStorage.removeItem("user")
        }
      }

      // Sonra localStorage'dan kontrol et
      const localUser = localStorage.getItem("user")
      if (localUser && localUser !== "undefined" && localUser !== "null") {
        console.log("User found in localStorage")
        try {
          // Bulunan kullanıcıyı sessionStorage'a da kopyala
          sessionStorage.setItem("user", localUser)
          return JSON.parse(localUser)
        } catch (e) {
          console.error("Error parsing localStorage user:", e)
          // Hatalı veriyi temizle
          localStorage.removeItem("user")
        }
      }

      // Hatalı verileri temizle
      if (sessionUser === "undefined" || sessionUser === "null") {
        console.log("Cleaning invalid sessionStorage user data")
        sessionStorage.removeItem("user")
      }
      if (localUser === "undefined" || localUser === "null") {
        console.log("Cleaning invalid localStorage user data")
        localStorage.removeItem("user")
      }

      return null
    } catch (e) {
      console.error("Error getting user from storage:", e)
      return null
    }
  },

  // Token bilgisini al
  getToken: (): string | null => {
    try {
      // Önce sessionStorage'dan kontrol et
      const sessionToken = sessionStorage.getItem("token")
      if (sessionToken && sessionToken !== "undefined" && sessionToken !== "null") {
        return sessionToken
      }

      // Sonra localStorage'dan kontrol et
      const localToken = localStorage.getItem("token")
      if (localToken && localToken !== "undefined" && localToken !== "null") {
        // Bulunan token'ı sessionStorage'a da kopyala
        sessionStorage.setItem("token", localToken)
        return localToken
      }

      return null
    } catch (e) {
      console.error("Error getting token from storage:", e)
      return null
    }
  },

  // Kullanıcı bilgilerini kaydet
  setUser: (user: User): void => {
    try {
      const userStr = JSON.stringify(user)
      // Her iki storage'a da kaydet
      localStorage.setItem("user", userStr)
      sessionStorage.setItem("user", userStr)
      console.log("User saved to both localStorage and sessionStorage")
    } catch (e) {
      console.error("Error setting user in storage:", e)
      // localStorage başarısız olursa en azından sessionStorage'a kaydetmeyi dene
      try {
        sessionStorage.setItem("user", JSON.stringify(user))
        console.log("User saved to sessionStorage only")
      } catch (e2) {
        console.error("Error setting user in sessionStorage:", e2)
      }
    }
  },

  // Token bilgisini kaydet
  setToken: (token: string): void => {
    try {
      // Her iki storage'a da kaydet
      localStorage.setItem("token", token)
      sessionStorage.setItem("token", token)
      console.log("Token saved to both localStorage and sessionStorage")
    } catch (e) {
      console.error("Error setting token in storage:", e)
      // localStorage başarısız olursa en azından sessionStorage'a kaydetmeyi dene
      try {
        sessionStorage.setItem("token", token)
        console.log("Token saved to sessionStorage only")
      } catch (e2) {
        console.error("Error setting token in sessionStorage:", e2)
      }
    }
  },

  // Tüm auth bilgilerini temizle
  clearAuth: (): void => {
    try {
      // Her iki storage'dan da temizle
      localStorage.removeItem("user")
      localStorage.removeItem("token")
      sessionStorage.removeItem("user")
      sessionStorage.removeItem("token")
      console.log("Auth data cleared from both localStorage and sessionStorage")
    } catch (e) {
      console.error("Error clearing auth from storage:", e)
    }
  },

  // Storage'ın çalışıp çalışmadığını test et
  testStorage: (): boolean => {
    try {
      const testKey = "_test_storage_"
      const testValue = "working"
      const testObj = { test: "value" }
      const testJson = JSON.stringify(testObj)

      // localStorage test
      let localResult = false
      try {
        localStorage.setItem(testKey, testValue)
        const localBasicResult = localStorage.getItem(testKey) === testValue
        localStorage.removeItem(testKey)

        // JSON test
        localStorage.setItem(testKey, testJson)
        const storedJson = localStorage.getItem(testKey)
        const parsedJson = storedJson ? JSON.parse(storedJson) : null
        const localJsonResult = parsedJson && parsedJson.test === "value"
        localStorage.removeItem(testKey)

        localResult = localBasicResult && localJsonResult
      } catch (e) {
        console.error("localStorage test failed:", e)
        localResult = false
      }

      // sessionStorage test
      let sessionResult = false
      try {
        sessionStorage.setItem(testKey, testValue)
        const sessionBasicResult = sessionStorage.getItem(testKey) === testValue
        sessionStorage.removeItem(testKey)

        // JSON test
        sessionStorage.setItem(testKey, testJson)
        const storedJson = sessionStorage.getItem(testKey)
        const parsedJson = storedJson ? JSON.parse(storedJson) : null
        const sessionJsonResult = parsedJson && parsedJson.test === "value"
        sessionStorage.removeItem(testKey)

        sessionResult = sessionBasicResult && sessionJsonResult
      } catch (e) {
        console.error("sessionStorage test failed:", e)
        sessionResult = false
      }

      console.log("Storage test results:", {
        localStorage: localResult,
        sessionStorage: sessionResult,
        localStorageAvailable: typeof localStorage !== "undefined",
        sessionStorageAvailable: typeof sessionStorage !== "undefined",
      })

      return localResult && sessionResult
    } catch (e) {
      console.error("Storage test failed:", e)
      return false
    }
  },
}

export function AuthProvider({ children }: { children: ReactNode }) {
  // Başlangıçta storage'dan kullanıcı bilgisini al
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [storageAvailable, setStorageAvailable] = useState(true)
  const [tokenCheckInterval, setTokenCheckInterval] = useState<NodeJS.Timeout | null>(null)
  const [showLogoutOverlay, setShowLogoutOverlay] = useState(false)

  // Token kontrolü için fonksiyon
  const checkTokenExpiration = () => {
    const token = storage.getToken()

    if (isTokenExpired(token)) {
      console.log("Token expired, logging out...")
      
      // Önce overlay'i göster
      setShowLogoutOverlay(true)
      
      // Sonra state ve storage'ı temizle
      storage.clearAuth()
      setUser(null)

      // Kullanıcıya bilgi ver
      toast({
        title: "Oturum Süresi Doldu",
        description: "Güvenliğiniz için oturumunuz sonlandırıldı. Lütfen tekrar giriş yapın.",
        variant: "default",
      })

      // Kısa bir gecikme ile login sayfasına yönlendir
      setTimeout(() => {
        window.location.href = "/login"
      }, 300)
      
      return false
    }

    return true
  }

  // Sayfa yüklendiğinde storage'ı test et ve kullanıcı bilgisini al
  useEffect(() => {
    try {
      // Storage'ı test et
      const isStorageWorking = storage.testStorage()
      setStorageAvailable(isStorageWorking)

      if (!isStorageWorking) {
        console.error("Storage is not available or not working properly")
        toast({
          title: "Depolama Hatası",
          description: "Tarayıcı depolama alanına erişilemiyor. Gizli modda mısınız?",
          variant: "destructive",
        })
        setIsLoading(false)
        return
      }

      // Kullanıcı bilgisini al
      const storedUser = storage.getUser()
      console.log("Initial auth check - stored user:", storedUser)

      // Token'ın geçerliliğini kontrol et
      const token = storage.getToken()
      if (token && !isTokenExpired(token)) {
        if (storedUser) {
          setUser(storedUser)
          console.log("User loaded from storage:", storedUser)

          // Token geçerli, periyodik kontrol başlat
          const interval = setInterval(checkTokenExpiration, 60000) // Her dakika kontrol et
          setTokenCheckInterval(interval)
        }
      } else if (token) {
        console.log("Token expired during initial check, clearing auth data")
        storage.clearAuth()
        // Kullanıcıya bilgi ver
        toast({
          title: "Oturum Süresi Doldu",
          description: "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.",
          variant: "default",
        })
      } else {
        console.log("No token found in storage")
      }
    } catch (error) {
      console.error("Error during initial auth check:", error)
    } finally {
      setIsLoading(false)
    }

    // Component unmount olduğunda interval'i temizle
    return () => {
      if (tokenCheckInterval) {
        clearInterval(tokenCheckInterval)
      }
    }
  }, [])

  // Login fonksiyonu - ResponseWrapper'a uygun şekilde güncellendi
  const login = async (username: string, password: string): Promise<boolean> => {
    setIsLoading(true)

    // Önce mevcut hatalı verileri temizle
    try {
      const sessionUser = sessionStorage.getItem("user")
      const localUser = localStorage.getItem("user")

      if (sessionUser === "undefined" || sessionUser === "null") {
        console.log("Cleaning invalid sessionStorage user data before login")
        sessionStorage.removeItem("user")
      }
      if (localUser === "undefined" || localUser === "null") {
        console.log("Cleaning invalid localStorage user data before login")
        localStorage.removeItem("user")
      }
    } catch (e) {
      console.error("Error cleaning storage before login:", e)
    }

    // Storage kullanılamıyorsa uyarı ver
    if (!storageAvailable) {
      toast({
        title: "Depolama Hatası",
        description: "Tarayıcı depolama alanına erişilemiyor. Oturum açılamaz.",
        variant: "destructive",
      })
      setIsLoading(false)
      return false
    }

    try {
      console.log("Login attempt for:", username)

      // API çağrısı
      const response = await post(API_ENDPOINTS.LOGIN, { username, password })

      // ResponseWrapper yapısını kontrol et
      if (response.success) {
        console.log("Login response success:", response)

        // ResponseWrapper içindeki data alanını kullan - JwtResponse
        const jwtResponse = response.data

        // JwtResponse içindeki alanları çıkar
        const token = jwtResponse.token
        const username = jwtResponse.username
        const roles = jwtResponse.roles

        // Kullanıcı nesnesini oluştur
        const userData: User = {
          id: username, // ID yoksa username kullan
          firstName: "", // API'den bu bilgiler gelmiyorsa boş bırak
          lastName: "",
          username: username,
          email: "",
          role: roles && roles.length > 0 ? roles[0] : "USER", // İlk rolü kullan
        }

        console.log("Constructed user data:", userData)

        // Kullanıcı bilgilerini ve token'ı sakla
        storage.setUser(userData)
        storage.setToken(token)

        // State'i güncelle
        setUser(userData)

        // Token kontrolü için interval başlat
        if (tokenCheckInterval) {
          clearInterval(tokenCheckInterval)
        }
        const interval = setInterval(checkTokenExpiration, 60000) // Her dakika kontrol et
        setTokenCheckInterval(interval)

        toast({
          title: "Giriş Başarılı",
          description: "Başarıyla giriş yaptınız.",
        })

        return true
      } else {
        // Başarısız login
        console.error("Login failed:", response.message)
        toast({
          title: "Giriş Başarısız",
          description: response.message || "Kullanıcı adı veya şifre hatalı.",
          variant: "destructive",
        })

        return false
      }
    } catch (error) {
      console.error("Login error:", error)

      toast({
        title: "Giriş Başarısız",
        description: "Kullanıcı adı veya şifre hatalı.",
        variant: "destructive",
      })

      return false
    } finally {
      setIsLoading(false)
    }
  }

  // Basitleştirilmiş register fonksiyonu - ResponseWrapper'a uygun şekilde güncellendi
  const register = async (
    firstName: string,
    lastName: string,
    email: string,
    username: string,
    password: string,
  ): Promise<boolean> => {
    setIsLoading(true)
    try {
      const response = await post(API_ENDPOINTS.REGISTER, {
        firstName,
        lastName,
        email,
        username,
        password,
      })

      // ResponseWrapper yapısını kontrol et
      if (response.success) {
        toast({
          title: "Kayıt Başarılı",
          description: response.message || "Başarıyla kayıt oldunuz. Şimdi giriş yapabilirsiniz.",
        })

        return true
      } else {
        console.error("Register failed:", response.message)
        toast({
          title: "Kayıt Başarısız",
          description: response.message || "Kayıt işlemi sırasında bir hata oluştu.",
          variant: "destructive",
        })

        return false
      }
    } catch (error) {
      console.error("Register error:", error)

      toast({
        title: "Kayıt Başarısız",
        description: "Kayıt işlemi sırasında bir hata oluştu.",
        variant: "destructive",
      })

      return false
    } finally {
      setIsLoading(false)
    }
  }

  // Basitleştirilmiş logout fonksiyonu - ResponseWrapper'a uygun şekilde güncellendi
  const logout = () => {
    // Önce overlay'i göster - API çağrısından önce
    setShowLogoutOverlay(true)
    
    // Interval'i temizle
    if (tokenCheckInterval) {
      clearInterval(tokenCheckInterval)
      setTokenCheckInterval(null)
    }
    
    // Kullanıcı state'ini hemen temizle
    setUser(null)
    
    // Kısa bir gecikme ile API çağrısı ve diğer işlemleri yap
    setTimeout(async () => {
      try {
        // API çağrısı
        await post(API_ENDPOINTS.LOGOUT, {})
      } catch (error) {
        console.error("Logout API error:", error)
      } finally {
        // Storage'ı temizle
        storage.clearAuth()
        
        // Kullanıcıya bilgi ver
        toast({
          title: "Çıkış Yapıldı",
          description: "Başarıyla çıkış yaptınız.",
        })
        
        // Login sayfasına yönlendir
        window.location.href = "/login"
      }
    }, 100)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
      }}
    >
      {showLogoutOverlay && <LogoutOverlay />}
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}