"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Activity, Database, Server } from "lucide-react"
import { StatusOverview } from "@/components/status-overview"
import ServicesList from "@/components/services-list"
import { get, API_ENDPOINTS } from "@/lib/api"
import { toast } from "@/hooks/use-toast"
import AlertsList from "@/components/alerts-list"

// Veri tipleri
interface Service {
  id: number
  name: string
  status: string
  // Diğer servis özellikleri...
}

interface Endpoint {
  id: number
  enabled: boolean
  responseTime?: number
  // Diğer endpoint özellikleri...
}

interface MonitoringGroup {
  id: number
  enabled: boolean
  // Diğer grup özellikleri...
}

export default function DashboardPage() {
  // Veri state'leri
  const [services, setServices] = useState<Service[]>([])
  const [endpoints, setEndpoints] = useState<Endpoint[]>([])
  const [groups, setGroups] = useState<MonitoringGroup[]>([])

  // Yükleme ve hata state'leri
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Hesaplanmış özet bilgiler
  const [dashboardStats, setDashboardStats] = useState({
    totalServices: 0,
    servicesInMaintenance: 0,
    totalGroups: 0,
    activeGroups: 0,
    totalEndpoints: 0,
    failingEndpoints: 0,
    averageResponseTime: 0,
    responseTimeImprovement: 12, // Varsayılan değer, gerçek veri yoksa
  })

  useEffect(() => {
    async function fetchDashboardData() {
      setIsLoading(true)
      setError(null)

      try {
        // Tüm verileri paralel olarak çek
        const [servicesData, endpointsData, groupsData] = await Promise.all([
          get(API_ENDPOINTS.SERVICES),
          get(API_ENDPOINTS.ENDPOINTS),
          get(API_ENDPOINTS.MONITORING_GROUPS),
        ])

        // Verileri state'e kaydet
        setServices(servicesData)
        setEndpoints(endpointsData)
        setGroups(groupsData)

        // Özet bilgileri hesapla
        calculateDashboardStats(servicesData, endpointsData, groupsData)
      } catch (err) {
        console.error("Dashboard verisi yüklenirken hata oluştu:", err)
        setError("Dashboard verisi yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.")
        toast({
          title: "Veri Yükleme Hatası",
          description: "Dashboard verisi yüklenirken bir hata oluştu.",
          variant: "destructive",
        })
      } finally {
        setIsLoading(false)
      }
    }

    fetchDashboardData()
  }, [])

  // Özet bilgileri hesaplama fonksiyonu
  function calculateDashboardStats(services: Service[], endpoints: Endpoint[], groups: MonitoringGroup[]) {
    try {
      // Servis istatistikleri
      const totalServices = Array.isArray(services) ? services.length : 0
      const servicesInMaintenance = Array.isArray(services)
        ? services.filter((s) => s.status === "MAINTENANCE").length
        : 0

      // Grup istatistikleri
      const totalGroups = Array.isArray(groups) ? groups.length : 0
      const activeGroups = Array.isArray(groups) ? groups.filter((g) => g.enabled).length : 0

      // Endpoint istatistikleri
      const totalEndpoints = Array.isArray(endpoints) ? endpoints.length : 0
      const failingEndpoints = Array.isArray(endpoints) ? endpoints.filter((e) => !e.enabled).length : 0

      // Ortalama yanıt süresi hesaplama
      let totalResponseTime = 0
      let endpointsWithResponseTime = 0

      if (Array.isArray(endpoints)) {
        endpoints.forEach((endpoint) => {
          if (endpoint.responseTime) {
            totalResponseTime += endpoint.responseTime
            endpointsWithResponseTime++
          }
        })
      }

      const averageResponseTime =
        endpointsWithResponseTime > 0 ? Math.round(totalResponseTime / endpointsWithResponseTime) : 0

      // Özet bilgileri güncelle
      setDashboardStats({
        totalServices,
        servicesInMaintenance,
        totalGroups,
        activeGroups,
        totalEndpoints,
        failingEndpoints,
        averageResponseTime,
        responseTimeImprovement: 12, // Varsayılan değer, gerçek veri yoksa
      })
    } catch (error) {
      console.error("Dashboard istatistikleri hesaplanırken hata oluştu:", error)
      // Hata durumunda varsayılan değerleri kullan
      setDashboardStats({
        totalServices: 0,
        servicesInMaintenance: 0,
        totalGroups: 0,
        activeGroups: 0,
        totalEndpoints: 0,
        failingEndpoints: 0,
        averageResponseTime: 0,
        responseTimeImprovement: 0,
      })
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Sistem ve servis durumlarını izleyin.</p>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-8">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      ) : error ? (
        <Card>
          <CardContent className="flex items-center justify-center p-6">
            <div className="text-center">
              <p className="mt-2 text-lg font-medium">{error}</p>
              <button
                onClick={() => window.location.reload()}
                className="mt-4 rounded-md bg-primary px-4 py-2 text-white hover:bg-primary/90"
              >
                Yeniden Dene
              </button>
            </div>
          </CardContent>
        </Card>
      ) : (
        <>
          <StatusOverview />

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Toplam Servis</CardTitle>
                <Server className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{dashboardStats.totalServices}</div>
                <p className="text-xs text-muted-foreground">
                  {dashboardStats.servicesInMaintenance} servis bakım modunda
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">İzleme Grupları</CardTitle>
                <Activity className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{dashboardStats.totalGroups}</div>
                <p className="text-xs text-muted-foreground">
                  {dashboardStats.activeGroups === dashboardStats.totalGroups
                    ? "Tüm gruplar aktif durumda"
                    : `${dashboardStats.activeGroups} grup aktif durumda`}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Toplam Endpoint</CardTitle>
                <Database className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{dashboardStats.totalEndpoints}</div>
                <p className="text-xs text-muted-foreground">
                  {dashboardStats.failingEndpoints} endpoint yanıt vermiyor
                </p>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Servisler</CardTitle>
            </CardHeader>
            <CardContent>
              <ServicesList />
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Son Uyarılar</CardTitle>
            </CardHeader>
            <CardContent>
              <AlertsList limit={5} />
            </CardContent>
          </Card>
        </>
      )}
    </div>
  )
}
