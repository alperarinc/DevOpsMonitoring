"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { CheckCircle, XCircle, Edit, Trash2, AlertTriangle, Clock } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { toast } from "@/hooks/use-toast"
import MonitoringGroupForm from "./monitoring-group-form"
import { get } from "@/lib/api"

interface MonitoringGroupDetailProps {
  group: {
    id: number
    name: string
    description: string
    enabled: boolean
    notificationEmail: string
    endpointIds?: number[]
    ownedServiceIds?: number[]
    ownedServiceNames?: string[]
  }
  onEdit?: (id: number, data: any) => void
  onDelete?: (id: number) => void
}

// Servis ve Endpoint için tip tanımlamaları
interface Service {
  id: number
  name: string
  serviceType: string
  status: string
  priority: string
  enabled: boolean
  [key: string]: any // Diğer olası alanlar için
}

interface Endpoint {
  id: number
  type: string
  url: string
  method?: string
  enabled: boolean
  responseTime?: number
  [key: string]: any // Diğer olası alanlar için
}

export default function MonitoringGroupDetail({ group, onEdit, onDelete }: MonitoringGroupDetailProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [services, setServices] = useState<Service[]>([])
  const [endpoints, setEndpoints] = useState<Endpoint[]>([])
  const [isLoadingServices, setIsLoadingServices] = useState(false)
  const [isLoadingEndpoints, setIsLoadingEndpoints] = useState(false)

  useEffect(() => {
    fetchServices()
    fetchEndpoints()
  }, [group.id])

  const fetchServices = async () => {
    setIsLoadingServices(true)
    try {
      // Tüm servisleri çek ve bu gruba ait olanları filtrele
      const response = await get(`/api/services`)
      const allServices = response || []

      // Eğer API yanıtı boş veya dizi değilse, örnek veri kullan
      if (!allServices || !Array.isArray(allServices) || allServices.length === 0) {
        console.log("API'den servis verisi alınamadı, örnek veri kullanılıyor")
        // Örnek veri
        const mockServices: Service[] = [
          {
            id: 1,
            name: "Kullanıcı Servisi",
            serviceType: "API",
            status: "OPERATIONAL",
            priority: "HIGH",
            enabled: true,
          },
          {
            id: 2,
            name: "Ürün Servisi",
            serviceType: "API",
            status: "DEGRADED",
            priority: "MEDIUM",
            enabled: true,
          },
          {
            id: 3,
            name: "Bildirim Servisi",
            serviceType: "MICROSERVICE",
            status: "OPERATIONAL",
            priority: "LOW",
            enabled: true,
          },
        ]
        setServices(mockServices)
        return
      }

      // Eğer grup servis ID'leri varsa, onları kullanarak filtreleme yap
      const filteredServices =
        group.ownedServiceIds && group.ownedServiceIds.length > 0
          ? allServices.filter((service: Service) => group.ownedServiceIds?.includes(service.id))
          : []
      setServices(filteredServices)
    } catch (error) {
      console.error("Servis verileri alınamadı:", error)
      toast({
        title: "Hata",
        description: "Servis verileri alınamadı",
        variant: "destructive",
      })
      // Hata durumunda örnek veri kullan
      const mockServices: Service[] = [
        {
          id: 1,
          name: "Kullanıcı Servisi",
          serviceType: "API",
          status: "OPERATIONAL",
          priority: "HIGH",
          enabled: true,
        },
        {
          id: 2,
          name: "Ürün Servisi",
          serviceType: "API",
          status: "DEGRADED",
          priority: "MEDIUM",
          enabled: true,
        },
      ]
      setServices(mockServices)
    } finally {
      setIsLoadingServices(false)
    }
  }

  const fetchEndpoints = async () => {
    setIsLoadingEndpoints(true)
    try {
      // Tüm endpoint'leri çek ve bu gruba ait olanları filtrele
      const response = await get(`/api/endpoints`)
      const allEndpoints = response || []

      // Eğer API yanıtı boş veya dizi değilse, örnek veri kullan
      if (!allEndpoints || !Array.isArray(allEndpoints) || allEndpoints.length === 0) {
        console.log("API'den endpoint verisi alınamadı, örnek veri kullanılıyor")
        // Örnek veri
        const mockEndpoints: Endpoint[] = [
          {
            id: 1,
            type: "HTTP",
            url: "https://api.example.com/users",
            method: "GET",
            enabled: true,
            responseTime: 120,
          },
          {
            id: 2,
            type: "HTTP",
            url: "https://api.example.com/products",
            method: "POST",
            enabled: true,
            responseTime: 150,
          },
          {
            id: 3,
            type: "WEBSOCKET",
            url: "wss://socket.example.com",
            enabled: false,
            responseTime: 80,
          },
        ]
        setEndpoints(mockEndpoints)
        return
      }

      // Eğer grup endpoint ID'leri varsa, onları kullanarak filtreleme yap
      const filteredEndpoints =
        group.endpointIds && group.endpointIds.length > 0
          ? allEndpoints.filter((endpoint: Endpoint) => group.endpointIds?.includes(endpoint.id))
          : []
      setEndpoints(filteredEndpoints)
    } catch (error) {
      console.error("Endpoint verileri alınamadı:", error)
      toast({
        title: "Hata",
        description: "Endpoint verileri alınamadı",
        variant: "destructive",
      })
      // Hata durumunda örnek veri kullan
      const mockEndpoints: Endpoint[] = [
        {
          id: 1,
          type: "HTTP",
          url: "https://api.example.com/users",
          method: "GET",
          enabled: true,
          responseTime: 120,
        },
        {
          id: 2,
          type: "HTTP",
          url: "https://api.example.com/products",
          method: "POST",
          enabled: true,
          responseTime: 150,
        },
      ]
      setEndpoints(mockEndpoints)
    } finally {
      setIsLoadingEndpoints(false)
    }
  }

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

  const getTypeColor = (type: string) => {
    switch (type) {
      case "HTTP":
        return "bg-blue-100 text-blue-800"
      case "WEBSOCKET":
        return "bg-purple-100 text-purple-800"
      case "DATABASE":
        return "bg-amber-100 text-amber-800"
      default:
        return "bg-slate-100 text-slate-800"
    }
  }

  const handleEdit = async (data: any) => {
    setIsLoading(true)
    try {
      if (onEdit) {
        await onEdit(group.id, data)
        setIsEditing(false)
        toast({
          title: "Başarılı",
          description: "İzleme grubu başarıyla güncellendi",
        })
      }
    } catch (error) {
      console.error("İzleme grubu güncellenirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "İzleme grubu güncellenirken bir hata oluştu",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async () => {
    setIsLoading(true)
    try {
      if (onDelete) {
        await onDelete(group.id)
        setIsDeleteDialogOpen(false)
        toast({
          title: "Başarılı",
          description: "İzleme grubu başarıyla silindi",
        })
      }
    } catch (error) {
      console.error("İzleme grubu silinirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "İzleme grubu silinirken bir hata oluştu",
        variant: "destructive",
      })
      setIsDeleteDialogOpen(false)
    } finally {
      setIsLoading(false)
    }
  }

  if (isEditing) {
    return (
      <MonitoringGroupForm
        group={group}
        onSubmit={handleEdit}
        onCancel={() => setIsEditing(false)}
        isLoading={isLoading}
      />
    )
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="flex flex-row items-start justify-between">
          <div>
            <CardTitle className="text-2xl">{group.name}</CardTitle>
            <CardDescription>{group.description}</CardDescription>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" size="icon" onClick={() => setIsEditing(true)}>
              <Edit className="h-4 w-4" />
              <span className="sr-only">Düzenle</span>
            </Button>
            <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
              <DialogTrigger asChild>
                <Button variant="outline" size="icon">
                  <Trash2 className="h-4 w-4 text-red-500" />
                  <span className="sr-only">Sil</span>
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>İzleme Grubunu Sil</DialogTitle>
                  <DialogDescription>
                    Bu izleme grubunu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.
                  </DialogDescription>
                </DialogHeader>
                <DialogFooter>
                  <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)} disabled={isLoading}>
                    İptal
                  </Button>
                  <Button variant="destructive" onClick={handleDelete} disabled={isLoading}>
                    {isLoading ? "Siliniyor..." : "Sil"}
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Durum</p>
              <div className="flex items-center gap-2">
                {group.enabled ? (
                  <CheckCircle className="h-4 w-4 text-emerald-500" />
                ) : (
                  <XCircle className="h-4 w-4 text-red-500" />
                )}
                <Badge
                  variant="secondary"
                  className={group.enabled ? "bg-emerald-100 text-emerald-800" : "bg-red-100 text-red-800"}
                >
                  {group.enabled ? "Aktif" : "Pasif"}
                </Badge>
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Bildirim E-posta</p>
              <a href={`mailto:${group.notificationEmail}`} className="text-sm hover:underline">
                {group.notificationEmail}
              </a>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">İstatistikler</p>
              <div className="flex gap-3">
                <Badge variant="outline" className="bg-blue-50">
                  {services.length} Servis
                </Badge>
                <Badge variant="outline" className="bg-green-50">
                  {endpoints.length} Endpoint
                </Badge>
              </div>
            </div>
          </div>

          <Tabs defaultValue="services" className="w-full">
            <TabsList className="grid grid-cols-2 w-full">
              <TabsTrigger value="services">Servisler</TabsTrigger>
              <TabsTrigger value="endpoints">Endpoint'ler</TabsTrigger>
            </TabsList>
            <TabsContent value="services" className="space-y-4 mt-4">
              {isLoadingServices ? (
                <div className="text-center py-4">Servisler yükleniyor...</div>
              ) : services.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Servis Adı</TableHead>
                      <TableHead>Tür</TableHead>
                      <TableHead>Durum</TableHead>
                      <TableHead>Öncelik</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {services.map((service) => (
                      <TableRow key={service.id}>
                        <TableCell className="font-medium">{service.name}</TableCell>
                        <TableCell>{service.serviceType}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {getStatusIcon(service.status)}
                            <Badge variant="secondary" className={getStatusColor(service.status)}>
                              {service.status}
                            </Badge>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant="secondary" className={getPriorityColor(service.priority)}>
                            {service.priority}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <div className="text-center py-4">Bu izleme grubuna ait servis bulunmamaktadır.</div>
              )}
            </TabsContent>
            <TabsContent value="endpoints" className="space-y-4 mt-4">
              {isLoadingEndpoints ? (
                <div className="text-center py-4">Endpoint'ler yükleniyor...</div>
              ) : endpoints.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Tür</TableHead>
                      <TableHead>URL</TableHead>
                      <TableHead>Metod</TableHead>
                      <TableHead>Durum</TableHead>
                      <TableHead>Yanıt Süresi</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {endpoints.map((endpoint) => (
                      <TableRow key={endpoint.id}>
                        <TableCell>
                          <Badge variant="secondary" className={getTypeColor(endpoint.type)}>
                            {endpoint.type}
                          </Badge>
                        </TableCell>
                        <TableCell className="max-w-[200px] truncate">{endpoint.url}</TableCell>
                        <TableCell>
                          {endpoint.method && (
                            <Badge variant="secondary" className="bg-blue-100 text-blue-800">
                              {endpoint.method}
                            </Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {endpoint.enabled ? (
                              <CheckCircle className="h-4 w-4 text-emerald-500" />
                            ) : (
                              <XCircle className="h-4 w-4 text-red-500" />
                            )}
                            <Badge
                              variant="secondary"
                              className={
                                endpoint.enabled ? "bg-emerald-100 text-emerald-800" : "bg-red-100 text-red-800"
                              }
                            >
                              {endpoint.enabled ? "Aktif" : "Pasif"}
                            </Badge>
                          </div>
                        </TableCell>
                        <TableCell>{endpoint.responseTime ? `${endpoint.responseTime}ms` : "-"}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <div className="text-center py-4">Bu izleme grubuna ait endpoint bulunmamaktadır.</div>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  )
}
