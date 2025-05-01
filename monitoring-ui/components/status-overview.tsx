"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { CheckCircle, AlertTriangle, XCircle, Clock } from "lucide-react"
import { get, API_ENDPOINTS } from "@/lib/api"
import { toast } from "@/hooks/use-toast"
import { useTheme } from "next-themes"

interface StatusCounts {
  operational: number
  degraded: number
  outage: number
  maintenance: number
  total: number
  activeServices: number
  inactiveServices: number
}

export function StatusOverview() {
  const [statusCounts, setStatusCounts] = useState<StatusCounts>({
    operational: 0,
    degraded: 0,
    outage: 0,
    maintenance: 0,
    total: 0,
    activeServices: 0,
    inactiveServices: 0,
  })
  const [isLoading, setIsLoading] = useState(true)
  const [isMockData, setIsMockData] = useState(false)
  const { theme } = useTheme()
  const isDarkTheme = theme === "dark"

  useEffect(() => {
    const fetchStatusCounts = async () => {
      try {
        setIsLoading(true)
        setIsMockData(false)

        // Gerçek API çağrısı
        const services = await get(API_ENDPOINTS.SERVICES)

        if (!services || !Array.isArray(services)) {
          throw new Error("Geçersiz API yanıtı")
        }

        // Durum sayılarını hesapla
        const counts: StatusCounts = {
          operational: 0,
          degraded: 0,
          outage: 0,
          maintenance: 0,
          total: services.length,
          activeServices: 0,
          inactiveServices: 0,
        }

        services.forEach((service: any) => {
          // Aktif/inaktif sayılarını hesapla
          if (service.enabled) {
            counts.activeServices++
          } else {
            counts.inactiveServices++
          }

          // Durum sayılarını hesapla
          if (service.status === "OPERATIONAL" && service.enabled) counts.operational++
          else if (service.status === "DEGRADED" && service.enabled) counts.degraded++
          else if (service.status === "OUTAGE" && service.enabled) counts.outage++
          else if (service.status === "MAINTENANCE") counts.maintenance++
        })

        setStatusCounts(counts)
      } catch (error) {
        console.error("Durum bilgileri yüklenirken hata oluştu:", error)

        // Hata durumunda örnek veri kullan
        setIsMockData(true)

        // Örnek veri
        const mockData: StatusCounts = {
          operational: 12,
          degraded: 3,
          outage: 1,
          maintenance: 2,
          total: 18,
          activeServices: 15,
          inactiveServices: 3,
        }

        setStatusCounts(mockData)

        // Sadece geliştirme ortamında değilse hata mesajı göster
        if (process.env.NODE_ENV === "production") {
          toast({
            title: "Veri Yükleme Hatası",
            description: "Sistem durumu bilgileri yüklenirken bir hata oluştu.",
            variant: "destructive",
          })
        }
      } finally {
        setIsLoading(false)
      }
    }

    fetchStatusCounts()
  }, [])

  // Tema bazlı renk sınıfları
  const statusCardClasses = {
    operational: {
      bg: isDarkTheme ? "bg-emerald-950" : "bg-emerald-100",
      icon: isDarkTheme ? "text-emerald-400" : "text-emerald-700",
      badge: isDarkTheme ? "bg-emerald-900 text-emerald-100" : "bg-emerald-50 text-emerald-800",
      border: isDarkTheme ? "border-emerald-800" : "border-emerald-200",
    },
    degraded: {
      bg: isDarkTheme ? "bg-amber-950" : "bg-amber-100",
      icon: isDarkTheme ? "text-amber-400" : "text-amber-700",
      badge: isDarkTheme ? "bg-amber-900 text-amber-100" : "bg-amber-50 text-amber-800",
      border: isDarkTheme ? "border-amber-800" : "border-amber-200",
    },
    outage: {
      bg: isDarkTheme ? "bg-red-950" : "bg-red-100",
      icon: isDarkTheme ? "text-red-400" : "text-red-700",
      badge: isDarkTheme ? "bg-red-900 text-red-100" : "bg-red-50 text-red-800",
      border: isDarkTheme ? "border-red-800" : "border-red-200",
    },
    active: {
      bg: isDarkTheme ? "bg-blue-950" : "bg-blue-100",
      icon: isDarkTheme ? "text-blue-400" : "text-blue-700",
      badge: isDarkTheme ? "bg-blue-900 text-blue-100" : "bg-blue-50 text-blue-800",
      border: isDarkTheme ? "border-blue-800" : "border-blue-200",
    },
  }

  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="flex items-center justify-between text-base">
          <span>Sistem Durumu</span>
          {isMockData && (
            <Badge
              variant="outline"
              className={isDarkTheme ? "bg-yellow-900 text-yellow-100" : "bg-yellow-50 text-yellow-800"}
            >
              Örnek Veri
            </Badge>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex justify-center py-4">
            <div className="h-6 w-6 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          </div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            {/* Çalışır Durumda */}
            <div
              className={`flex flex-col items-center gap-1 rounded-lg border p-3 ${statusCardClasses.operational.border}`}
            >
              <div
                className={`flex h-8 w-8 items-center justify-center rounded-full ${statusCardClasses.operational.bg}`}
              >
                <CheckCircle className={`h-4 w-4 ${statusCardClasses.operational.icon}`} />
              </div>
              <span className="text-sm font-medium">Çalışır Durumda</span>
              <Badge variant="outline" className={statusCardClasses.operational.badge}>
                {statusCounts.operational} Servis
              </Badge>
            </div>

            {/* Performans Düşük */}
            <div
              className={`flex flex-col items-center gap-1 rounded-lg border p-3 ${statusCardClasses.degraded.border}`}
            >
              <div className={`flex h-8 w-8 items-center justify-center rounded-full ${statusCardClasses.degraded.bg}`}>
                <AlertTriangle className={`h-4 w-4 ${statusCardClasses.degraded.icon}`} />
              </div>
              <span className="text-sm font-medium">Performans Düşük</span>
              <Badge variant="outline" className={statusCardClasses.degraded.badge}>
                {statusCounts.degraded} Servis
              </Badge>
            </div>

            {/* Çalışmıyor */}
            <div
              className={`flex flex-col items-center gap-1 rounded-lg border p-3 ${statusCardClasses.outage.border}`}
            >
              <div className={`flex h-8 w-8 items-center justify-center rounded-full ${statusCardClasses.outage.bg}`}>
                <XCircle className={`h-4 w-4 ${statusCardClasses.outage.icon}`} />
              </div>
              <span className="text-sm font-medium">Çalışmıyor</span>
              <Badge variant="outline" className={statusCardClasses.outage.badge}>
                {statusCounts.outage} Servis
              </Badge>
            </div>

            {/* Aktif Servisler */}
            <div
              className={`flex flex-col items-center gap-1 rounded-lg border p-3 ${statusCardClasses.active.border}`}
            >
              <div className={`flex h-8 w-8 items-center justify-center rounded-full ${statusCardClasses.active.bg}`}>
                <Clock className={`h-4 w-4 ${statusCardClasses.active.icon}`} />
              </div>
              <span className="text-sm font-medium">Aktif Servisler</span>
              <Badge variant="outline" className={statusCardClasses.active.badge}>
                {statusCounts.activeServices} / {statusCounts.total}
              </Badge>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
