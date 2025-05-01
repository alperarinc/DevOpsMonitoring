// API konfigürasyon dosyası
export const API_BASE_URL = "http://localhost:8080"

// API endpoint'leri
export const API_ENDPOINTS = {
  // Servisler
  SERVICES: "/api/services",
  SERVICE_BY_ID: (id: number) => `/api/services/${id}`,

  // Endpoint'ler
  ENDPOINTS: "/api/endpoints",
  ENDPOINT_BY_ID: (id: number) => `/api/endpoints/${id}`,
  TEST_ENDPOINT: (id: number) => `/api/endpoints/${id}/test`,

  // İzleme Grupları
  MONITORING_GROUPS: "/api/monitoring-groups",
  MONITORING_GROUP_BY_ID: (id: number) => `/api/monitoring-groups/${id}`,

  // Veritabanları
  DATABASES: "/api/databases",
  DATABASE_BY_ID: (id: number) => `/api/databases/${id}`,
  TEST_DATABASE: (id: number) => `/api/databases/${id}/test`,

  // Uyarılar
  ALERTS: "/api/alerts",
  ALERT_BY_ID: (id: number) => `/api/alerts/${id}`,
  RESOLVE_ALERT: (id: number) => `/api/alerts/${id}/resolve`,

  // Dashboard ve Durum Bilgileri
  STATUS_OVERVIEW: "/api/status/overview",
  SYSTEM_METRICS: "/api/status/metrics",

  // Kimlik doğrulama
  LOGIN: "/api/auth/login",
  REGISTER: "/api/auth/register",
  LOGOUT: "/api/auth/logout",
  VERIFY_TOKEN: "/api/auth/verify",
  FORGOT_PASSWORD: "/api/auth/forgot-password",
  RESET_PASSWORD: "/api/auth/reset-password",
}

export const USE_MOCK_DATA = false

// Zaman aşımı süresi (ms cinsinden)
const REQUEST_TIMEOUT = 30000

export async function fetchApi(endpoint: string, options: RequestInit = {}) {
  const url = `${API_BASE_URL}${endpoint.startsWith("/") ? endpoint : `/${endpoint}`}`

  // Token'ı localStorage ve sessionStorage'dan al
  const localStorageToken = typeof window !== "undefined" ? localStorage.getItem("token") : null
  const sessionStorageToken = typeof window !== "undefined" ? sessionStorage.getItem("token") : null
  const token = localStorageToken || sessionStorageToken

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) || {}),
  }

  // Eğer token varsa Authorization header'ını ekle
  if (token) {
    headers["Authorization"] = `Bearer ${token}`
  }

  // Timeout için abort controller oluştur
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT)

  try {
    console.log(`API isteği yapılıyor: ${url}`, { method: options.method || "GET", headers })

    const response = await fetch(url, {
      ...options,
      headers,
      signal: controller.signal,
    })

    // İstek tamamlandı, timeout'u temizle
    clearTimeout(timeoutId)

    console.log(`API yanıtı alındı: ${url}`, { status: response.status, statusText: response.statusText })

    if (!response.ok) {
      let errorMessage = `API isteği başarısız: ${response.status} ${response.statusText}`
      try {
        const errorData = await response.json()
        // ResponseWrapper yapısına göre hata mesajını çıkar
        errorMessage = errorData.message || (errorData.success === false ? errorData.message : errorMessage)
        console.error("API hata detayı:", errorData)
      } catch (e) {
        console.error("API hata yanıtı JSON olarak ayrıştırılamadı")
      }
      throw new Error(errorMessage)
    }

    // 204 No Content durumunda boş obje döndür
    if (response.status === 204) {
      return {}
    }

    const data = await response.json()
    console.log(`API yanıt verisi:`, data)
    return data
  } catch (error: unknown) {
    // Tip eklendi: unknown
    // Timeout durumunda veya diğer hatalar için timeout'u temizle
    clearTimeout(timeoutId)

    // TypeScript için error tipini kontrol et
    if (error instanceof Error) {
      // Zaman aşımı hatası kontrolü
      if (error.name === "AbortError") {
        console.error(`API isteği zaman aşımına uğradı (${url})`)
        throw new Error("İstek zaman aşımına uğradı. Lütfen internet bağlantınızı kontrol edin.")
      }
    }

    console.error(`API isteği sırasında hata (${url}):`, error)
    throw error
  }
}

export async function get(endpoint: string) {
  return fetchApi(endpoint)
}

export async function post(endpoint: string, data: any) {
  return fetchApi(endpoint, {
    method: "POST",
    body: JSON.stringify(data),
  })
}

export async function put(endpoint: string, data: any) {
  return fetchApi(endpoint, {
    method: "PUT",
    body: JSON.stringify(data),
  })
}

export async function del(endpoint: string) {
  return fetchApi(endpoint, {
    method: "DELETE",
  })
}

// Kimlik doğrulama gerektiren istekler için yardımcı fonksiyonlar
export async function authenticatedGet(endpoint: string) {
  const token = localStorage.getItem("token") || sessionStorage.getItem("token")
  if (!token) {
    throw new Error("Kimlik doğrulama gerekiyor")
  }
  return get(endpoint)
}

export async function authenticatedPost(endpoint: string, data: any) {
  const token = localStorage.getItem("token") || sessionStorage.getItem("token")
  if (!token) {
    throw new Error("Kimlik doğrulama gerekiyor")
  }
  return post(endpoint, data)
}

export async function authenticatedPut(endpoint: string, data: any) {
  const token = localStorage.getItem("token") || sessionStorage.getItem("token")
  if (!token) {
    throw new Error("Kimlik doğrulama gerekiyor")
  }
  return put(endpoint, data)
}

export async function authenticatedDel(endpoint: string) {
  const token = localStorage.getItem("token") || sessionStorage.getItem("token")
  if (!token) {
    throw new Error("Kimlik doğrulama gerekiyor")
  }
  return del(endpoint)
}

// Token doğrulama fonksiyonu
export async function verifyToken() {
  try {
    const response = await get(API_ENDPOINTS.VERIFY_TOKEN)
    return response.success === true
  } catch (error) {
    console.error("Token doğrulama hatası:", error)
    return false
  }
}
