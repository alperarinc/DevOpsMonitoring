"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Plus, CheckCircle, AlertTriangle, XCircle, Clock, Eye } from "lucide-react"
import { get, API_ENDPOINTS } from "@/lib/api"
import { toast } from "@/hooks/use-toast"

// Servis tipi tanımı
interface Service {
  id: number
  name: string
  description: string
  serviceType: string
  status: string
  ownerGroupId?: number
  ownerGroupName?: string
  contactEmail: string
  enabled: boolean
  priority: string
  tags: string
}

export default function ServicesPage() {
  const router = useRouter()
  const [services, setServices] = useState<Service[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchServices = async () => {
      try {
        setIsLoading(true)
        const data = await get(API_ENDPOINTS.SERVICES)

        // API yanıtı boş veya dizi değilse, örnek veri kullan
        if (!data || !Array.isArray(data) || data.length === 0) {
          console.log("API'den veri alınamadı, örnek veri kullanılıyor")
          // Örnek veri
          const mockServices: Service[] = [
            {
              id: 1,
              name: "Kullanıcı Servisi",
              description: "Kullanıcı yönetimi ve kimlik doğrulama",
              serviceType: "API",
              status: "OPERATIONAL",
              ownerGroupId: 1,
              ownerGroupName: "Kritik Servisler",
              contactEmail: "user-service@example.com",
              enabled: true,
              priority: "HIGH",
              tags: "api,users,auth",
            },
            {
              id: 2,
              name: "Ürün Servisi",
              description: "Ürün kataloğu ve envanter yönetimi",
              serviceType: "API",
              status: "DEGRADED",
              ownerGroupId: 2,
              ownerGroupName: "Web Servisleri",
              contactEmail: "product-service@example.com",
              enabled: true,
              priority: "MEDIUM",
              tags: "api,products,inventory",
            },
            {
              id: 3,
              name: "Bildirim Servisi",
              description: "E-posta ve push bildirimleri",
              serviceType: "MICROSERVICE",
              status: "OPERATIONAL",
              ownerGroupId: 2,
              ownerGroupName: "Web Servisleri",
              contactEmail: "notification-service@example.com",
              enabled: true,
              priority: "LOW",
              tags: "notifications,email,push",
            },
            {
              id: 4,
              name: "Veritabanı Servisi",
              description: "Ana veritabanı sunucusu",
              serviceType: "DATABASE",
              status: "OUTAGE",
              ownerGroupId: 3,
              ownerGroupName: "Veritabanı Servisleri",
              contactEmail: "db-service@example.com",
              enabled: false,
              priority: "CRITICAL",
              tags: "database,mysql,storage",
            },
            {
              id: 5,
              name: "Ödeme Servisi",
              description: "Ödeme işlemleri ve entegrasyonları",
              serviceType: "API",
              status: "MAINTENANCE",
              ownerGroupId: 1,
              ownerGroupName: "Kritik Servisler",
              contactEmail: "payment-service@example.com",
              enabled: false,
              priority: "CRITICAL",
              tags: "payments,transactions,financial",
            },
          ]
          setServices(mockServices)
          return
        }

        setServices(data)
      } catch (error) {
        console.error("Servisler yüklenirken hata oluştu:", error)
        toast({
          title: "Hata",
          description: "Servisler yüklenirken bir hata oluştu.",
          variant: "destructive",
        })

        // Hata durumunda örnek veri kullan
        const mockServices: Service[] = [
          {
            id: 1,
            name: "Kullanıcı Servisi",
            description: "Kullanıcı yönetimi ve kimlik doğrulama",
            serviceType: "API",
            status: "OPERATIONAL",
            ownerGroupId: 1,
            ownerGroupName: "Kritik Servisler",
            contactEmail: "user-service@example.com",
            enabled: true,
            priority: "HIGH",
            tags: "api,users,auth",
          },
          {
            id: 2,
            name: "Ürün Servisi",
            description: "Ürün kataloğu ve envanter yönetimi",
            serviceType: "API",
            status: "DEGRADED",
            ownerGroupId: 2,
            ownerGroupName: "Web Servisleri",
            contactEmail: "product-service@example.com",
            enabled: true,
            priority: "MEDIUM",
            tags: "api,products,inventory",
          },
        ]
        setServices(mockServices)
      } finally {
        setIsLoading(false)
      }
    }

    fetchServices()
  }, [])

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "OPERATIONAL":
        return <CheckCircle className="h-4 w-4 text-emerald-500" />
      case "DEGRADED":
        return <AlertTriangle className="h-4 w-4 text-amber-500" />
      case "OUTAGE":
        return <XCircle className="h-4 w-4 text-red-500" />
      case "MAINTENANCE":
        return <Clock className="h-4 w-4 text-slate-500" />
      default:
        return null
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "OPERATIONAL":
        return "Çalışır Durumda"
      case "DEGRADED":
        return "Performans Düşük"
      case "OUTAGE":
        return "Çalışmıyor"
      case "MAINTENANCE":
        return "Bakımda"
      default:
        return status
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "OPERATIONAL":
        return "bg-emerald-100 text-emerald-800 hover:bg-emerald-200"
      case "DEGRADED":
        return "bg-amber-100 text-amber-800 hover:bg-amber-200"
      case "OUTAGE":
        return "bg-red-100 text-red-800 hover:bg-red-200"
      case "MAINTENANCE":
        return "bg-slate-100 text-slate-800 hover:bg-slate-200"
      default:
        return "bg-slate-100 text-slate-800 hover:bg-slate-200"
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "CRITICAL":
        return "bg-red-100 text-red-800"
      case "HIGH":
        return "bg-amber-100 text-amber-800"
      case "MEDIUM":
        return "bg-blue-100 text-blue-800"
      case "LOW":
        return "bg-slate-100 text-slate-800"
      default:
        return "bg-slate-100 text-slate-800"
    }
  }

  // Aktif/İnaktif durumunu belirle
  const getActiveStatus = (service: Service) => {
    return service.enabled ? "ACTIVE" : "INACTIVE"
  }

  const getActiveStatusColor = (isActive: boolean) => {
    return isActive ? "bg-emerald-100 text-emerald-800" : "bg-slate-100 text-slate-800"
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Servisler</h1>
          <p className="text-muted-foreground">Tüm servisleri yönetin ve durumlarını izleyin.</p>
        </div>
        <Button className="gap-1" onClick={() => router.push("/services/new")}>
          <Plus className="h-4 w-4" />
          Yeni Servis
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Servis Listesi</CardTitle>
          <CardDescription>Sistemde tanımlı tüm servislerin listesi.</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex justify-center py-8">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Servis Adı</TableHead>
                  <TableHead>Tür</TableHead>
                  <TableHead>Durum</TableHead>
                  <TableHead>Aktif/İnaktif</TableHead>
                  <TableHead>İzleme Grubu</TableHead>
                  <TableHead>Öncelik</TableHead>
                  <TableHead>İletişim</TableHead>
                  <TableHead className="text-right">İşlemler</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {services.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center py-4 text-muted-foreground">
                      Henüz servis bulunmuyor.
                    </TableCell>
                  </TableRow>
                ) : (
                  services.map((service) => (
                    <TableRow key={service.id}>
                      <TableCell>
                        <div className="font-medium">{service.name}</div>
                        <div className="text-xs text-muted-foreground">{service.description}</div>
                      </TableCell>
                      <TableCell>{service.serviceType}</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          {getStatusIcon(service.status)}
                          <Badge variant="secondary" className={getStatusColor(service.status)}>
                            {getStatusText(service.status)}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="secondary" className={getActiveStatusColor(service.enabled)}>
                          {service.enabled ? "ACTIVE" : "INACTIVE"}
                        </Badge>
                      </TableCell>
                      <TableCell>{service.ownerGroupName || "Atanmamış"}</TableCell>
                      <TableCell>
                        <Badge variant="secondary" className={getPriorityColor(service.priority)}>
                          {service.priority}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <a
                          href={`mailto:${service.contactEmail}`}
                          className="text-sm text-muted-foreground hover:underline"
                        >
                          {service.contactEmail}
                        </a>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="icon" onClick={() => router.push(`/services/${service.id}`)}>
                          <Eye className="h-4 w-4" />
                          <span className="sr-only">Detay</span>
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
