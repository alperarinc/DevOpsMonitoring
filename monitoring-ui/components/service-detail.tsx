"use client"

import { DialogFooter } from "@/components/ui/dialog"
import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { CheckCircle, AlertTriangle, XCircle, Clock, Edit, Trash2, ExternalLink } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { toast } from "@/hooks/use-toast"
import ServiceForm from "./service-form"
import { get } from "@/lib/api"
import AlertsList from "./alerts-list"

interface ServiceDetailProps {
  service: {
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
  onEdit?: (id: number, data: any) => void
  onDelete?: (id: number) => void
}

// Endpoint ve Metrik için tip tanımlamaları
interface Endpoint {
  id: number
  type: string
  url: string
  method?: string
  enabled: boolean
  responseTime?: number
  lastChecked?: string
  serviceId?: number
  [key: string]: any // Diğer olası alanlar için
}

interface Metric {
  timestamp: string
  responseTime: number
  status: string
  [key: string]: any // Diğer olası alanlar için
}

export default function ServiceDetail({ service, onEdit, onDelete }: ServiceDetailProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [endpoints, setEndpoints] = useState<Endpoint[]>([])
  const [metrics, setMetrics] = useState<Metric[]>([])
  const [isLoadingEndpoints, setIsLoadingEndpoints] = useState(false)
  const [isLoadingMetrics, setIsLoadingMetrics] = useState(false)

  useEffect(() => {
    fetchEndpoints()
    fetchMetrics()
  }, [service.id])

  const fetchEndpoints = async () => {
    setIsLoadingEndpoints(true)
    try {
      // Tüm endpoint'leri çek ve servise ait olanları filtrele
      const response = await get(`/api/endpoints`)
      const allEndpoints = response || []
      // Servise ait endpoint'leri filtrele
      const filteredEndpoints = allEndpoints.filter((endpoint: Endpoint) => endpoint.serviceId === service.id)

      setEndpoints(filteredEndpoints)
    } catch (error) {
      console.error("Endpoint verileri alınamadı:", error)
      toast({
        title: "Hata",
        description: "Endpoint verileri alınamadı",
        variant: "destructive",
      })
      setEndpoints([])
    } finally {
      setIsLoadingEndpoints(false)
    }
  }

  const fetchMetrics = async () => {
    setIsLoadingMetrics(true)
    try {
      // API'de servis metriklerini almak için endpoint olmadığından
      // şimdilik örnek veri kullanıyoruz
      const mockMetrics: Metric[] = [
        { timestamp: "2023-05-01 10:00:00", responseTime: 120, status: "OK" },
        { timestamp: "2023-05-01 09:00:00", responseTime: 115, status: "OK" },
        { timestamp: "2023-05-01 08:00:00", responseTime: 130, status: "OK" },
        { timestamp: "2023-05-01 07:00:00", responseTime: 180, status: "WARNING" },
        { timestamp: "2023-05-01 06:00:00", responseTime: 110, status: "OK" },
      ]

      setMetrics(mockMetrics)
    } catch (error) {
      console.error("Metrik verileri alınamadı:", error)
      toast({
        title: "Hata",
        description: "Metrik verileri alınamadı",
        variant: "destructive",
      })
      setMetrics([])
    } finally {
      setIsLoadingMetrics(false)
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

  const getMetricStatusColor = (status: string) => {
    switch (status) {
      case "OK":
        return "text-emerald-500"
      case "WARNING":
        return "text-amber-500"
      case "CRITICAL":
        return "text-red-500"
      default:
        return "text-slate-500"
    }
  }

  const handleEdit = async (data: any) => {
    setIsLoading(true)
    try {
      if (onEdit) {
        await onEdit(service.id, data)
        setIsEditing(false)
        toast({
          title: "Başarılı",
          description: "Servis başarıyla güncellendi",
        })
      }
    } catch (error) {
      console.error("Servis güncellenirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Servis güncellenirken bir hata oluştu",
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
        await onDelete(service.id)
        setIsDeleteDialogOpen(false)
        toast({
          title: "Başarılı",
          description: "Servis başarıyla silindi",
        })
      }
    } catch (error) {
      console.error("Servis silinirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Servis silinirken bir hata oluştu",
        variant: "destructive",
      })
      setIsDeleteDialogOpen(false)
    } finally {
      setIsLoading(false)
    }
  }

  if (isEditing) {
    return (
      <ServiceForm service={service} onSubmit={handleEdit} onCancel={() => setIsEditing(false)} isLoading={isLoading} />
    )
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="flex flex-row items-start justify-between">
          <div>
            <CardTitle className="text-2xl">{service.name}</CardTitle>
            <CardDescription>{service.description}</CardDescription>
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
                  <DialogTitle>Servisi Sil</DialogTitle>
                  <DialogDescription>
                    Bu servisi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.
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
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Durum</p>
              <div className="flex items-center gap-2">
                {getStatusIcon(service.status)}
                <Badge variant="secondary" className={getStatusColor(service.status)}>
                  {getStatusText(service.status)}
                </Badge>
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Tür</p>
              <p>{service.serviceType}</p>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Öncelik</p>
              <Badge variant="secondary" className={getPriorityColor(service.priority)}>
                {service.priority}
              </Badge>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Etkin</p>
              <Badge variant="outline" className={service.enabled ? "bg-emerald-50" : "bg-red-50"}>
                {service.enabled ? "Evet" : "Hayır"}
              </Badge>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">İzleme Grubu</p>
              <p>{service.ownerGroupName || "Atanmamış"}</p>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">İletişim</p>
              <a href={`mailto:${service.contactEmail}`} className="text-sm hover:underline">
                {service.contactEmail}
              </a>
            </div>
          </div>

          <div className="space-y-1">
            <p className="text-sm font-medium text-muted-foreground">Etiketler</p>
            <div className="flex flex-wrap gap-2">
              {service.tags.split(",").map((tag, index) => (
                <Badge key={index} variant="secondary">
                  {tag.trim()}
                </Badge>
              ))}
            </div>
          </div>

          <Tabs defaultValue="endpoints" className="w-full">
            <TabsList className="grid grid-cols-3 w-full">
              <TabsTrigger value="endpoints">Endpoint'ler</TabsTrigger>
              <TabsTrigger value="metrics">Metrikler</TabsTrigger>
              <TabsTrigger value="alerts">Uyarılar</TabsTrigger>
            </TabsList>
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
                      <TableHead>Son Kontrol</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {endpoints.map((endpoint) => (
                      <TableRow key={endpoint.id}>
                        <TableCell>
                          <Badge variant="secondary" className="bg-blue-100 text-blue-800">
                            {endpoint.type}
                          </Badge>
                        </TableCell>
                        <TableCell className="max-w-[200px] truncate">
                          <div className="flex items-center gap-1">
                            <span className="truncate">{endpoint.url}</span>
                            <a
                              href={endpoint.url}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-muted-foreground hover:text-foreground"
                            >
                              <ExternalLink className="h-3 w-3" />
                            </a>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant="secondary" className="bg-blue-100 text-blue-800">
                            {endpoint.method}
                          </Badge>
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
                        <TableCell>{endpoint.responseTime}ms</TableCell>
                        <TableCell>{endpoint.lastChecked || "-"}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <div className="text-center py-4">Bu servise ait endpoint bulunmamaktadır.</div>
              )}
            </TabsContent>
            <TabsContent value="metrics" className="space-y-4 mt-4">
              {isLoadingMetrics ? (
                <div className="text-center py-4">Metrikler yükleniyor...</div>
              ) : metrics.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Zaman</TableHead>
                      <TableHead>Yanıt Süresi</TableHead>
                      <TableHead>Durum</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {metrics.map((metric, index) => (
                      <TableRow key={index}>
                        <TableCell>{metric.timestamp}</TableCell>
                        <TableCell>{metric.responseTime}ms</TableCell>
                        <TableCell>
                          <span className={getMetricStatusColor(metric.status)}>{metric.status}</span>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <div className="text-center py-4">Bu servis için metrik bulunmamaktadır.</div>
              )}
            </TabsContent>
            <TabsContent value="alerts" className="space-y-4 mt-4">
              <AlertsList sourceType="SERVICE" sourceId={service.id} showHeader={false} />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  )
}
