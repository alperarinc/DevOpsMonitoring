"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Edit, Trash2 } from "lucide-react"
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
import EndpointForm from "./endpoint-form"
import { CheckCircle, XCircle } from "lucide-react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import AlertsList from "./alerts-list"

interface EndpointDetailProps {
  endpoint: {
    id: number
    type: string
    url: string
    method?: string
    headers?: Record<string, string>
    body?: string
    expectedStatus?: number
    expectedResponseContains?: string
    protocol?: string
    testMessage?: string
    expectedResponse?: string
    dbType?: string
    dbUsername?: string
    dbPassword?: string
    query?: string
    intervalMs?: number
    timeoutMs?: number
    thresholdMs?: number
    serviceId?: number
    enabled: boolean
    monitoringGroupIds?: number[]
  }
  onEdit?: (id: number, data: any) => void
  onDelete?: (id: number) => void
  onTest?: (id: number) => void
}

interface Metric {
  timestamp: string
  responseTime: number
  status: string
}

export default function EndpointDetail({ endpoint, onEdit, onDelete, onTest }: EndpointDetailProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [isTestLoading, setIsTestLoading] = useState(false)
  const [metrics, setMetrics] = useState<Metric[]>([])
  const [isLoadingMetrics, setIsLoadingMetrics] = useState(false)

  useEffect(() => {
    fetchMetrics()
  }, [])

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

  const getMethodColor = (method: string | undefined) => {
    if (!method) return ""
    switch (method) {
      case "GET":
        return "bg-emerald-100 text-emerald-800"
      case "POST":
        return "bg-blue-100 text-blue-800"
      case "PUT":
        return "bg-amber-100 text-amber-800"
      case "DELETE":
        return "bg-red-100 text-red-800"
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
      case "ERROR":
        return "text-red-500"
      default:
        return "text-slate-500"
    }
  }

  const handleEdit = async (data: any) => {
    setIsLoading(true)
    try {
      if (onEdit) {
        await onEdit(endpoint.id, data)
        setIsEditing(false)
        toast({
          title: "Başarılı",
          description: "Endpoint başarıyla güncellendi",
        })
      }
    } catch (error) {
      console.error("Endpoint güncellenirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Endpoint güncellenirken bir hata oluştu",
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
        await onDelete(endpoint.id)
        setIsDeleteDialogOpen(false)
        toast({
          title: "Başarılı",
          description: "Endpoint başarıyla silindi",
        })
      }
    } catch (error) {
      console.error("Endpoint silinirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Endpoint silinirken bir hata oluştu",
        variant: "destructive",
      })
      setIsDeleteDialogOpen(false)
    } finally {
      setIsLoading(false)
    }
  }

  const handleTest = async () => {
    setIsTestLoading(true)
    try {
      if (onTest) {
        await onTest(endpoint.id)
        toast({
          title: "Test Tamamlandı",
          description: "Endpoint testi başarıyla tamamlandı",
        })
      }
    } catch (error) {
      console.error("Endpoint test edilirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Endpoint test edilirken bir hata oluştu",
        variant: "destructive",
      })
    } finally {
      setIsTestLoading(false)
    }
  }

  const fetchMetrics = async () => {
    setIsLoadingMetrics(true)
    try {
      // NOT: Bu bir örnek veridir. Gerçek API endpoint'i mevcut olmadığından
      // burada statik veriler kullanılmaktadır.
      // API endpoint'i hazır olduğunda, bu kısım gerçek API çağrısı ile değiştirilmelidir.

      // Örnek veri:
      setTimeout(() => {
        const mockMetrics: Metric[] = [
          { timestamp: "2023-05-01 10:00:00", responseTime: 120, status: "OK" },
          { timestamp: "2023-05-01 09:00:00", responseTime: 115, status: "OK" },
          { timestamp: "2023-05-01 08:00:00", responseTime: 130, status: "OK" },
          { timestamp: "2023-05-01 07:00:00", responseTime: 180, status: "WARNING" },
          { timestamp: "2023-05-01 06:00:00", responseTime: 110, status: "OK" },
        ]
        setMetrics(mockMetrics)
        setIsLoadingMetrics(false)
      }, 500) // Gerçek bir API çağrısını simüle etmek için küçük bir gecikme
    } catch (error) {
      console.error("Metrik verileri alınamadı:", error)
      toast({
        title: "Hata",
        description: "Metrik verileri alınamadı",
        variant: "destructive",
      })
      setMetrics([])
      setIsLoadingMetrics(false)
    }
  }

  if (isEditing) {
    return (
      <EndpointForm
        endpoint={endpoint}
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
            <CardTitle className="text-2xl">
              <Badge variant="secondary" className={getTypeColor(endpoint.type)}>
                {endpoint.type}
              </Badge>
              <span className="ml-2">{endpoint.url}</span>
            </CardTitle>
            <CardDescription>
              {endpoint.method && (
                <Badge variant="outline" className={getMethodColor(endpoint.method)}>
                  {endpoint.method}
                </Badge>
              )}
            </CardDescription>
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
                  <DialogTitle>Endpoint'i Sil</DialogTitle>
                  <DialogDescription>
                    Bu endpoint'i silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.
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
          <div className="flex justify-end">
            <Button onClick={handleTest} disabled={isTestLoading}>
              {isTestLoading ? "Test Ediliyor..." : "Test Et"}
            </Button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Durum</p>
              <div className="flex items-center gap-2">
                {endpoint.enabled ? (
                  <CheckCircle className="h-4 w-4 text-emerald-500" />
                ) : (
                  <XCircle className="h-4 w-4 text-red-500" />
                )}
                <Badge
                  variant="secondary"
                  className={endpoint.enabled ? "bg-emerald-100 text-emerald-800" : "bg-red-100 text-red-800"}
                >
                  {endpoint.enabled ? "Aktif" : "Pasif"}
                </Badge>
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Kontrol Aralığı</p>
              <p>{endpoint.intervalMs ? `${endpoint.intervalMs / 1000} saniye` : "60 saniye"}</p>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Zaman Aşımı</p>
              <p>{endpoint.timeoutMs ? `${endpoint.timeoutMs / 1000} saniye` : "5 saniye"}</p>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-medium text-muted-foreground">Eşik Değeri</p>
              <p>{endpoint.thresholdMs ? `${endpoint.thresholdMs} ms` : "1000 ms"}</p>
            </div>
          </div>

          {endpoint.type === "HTTP" && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Beklenen Durum Kodu</p>
                  <p>{endpoint.expectedStatus || 200}</p>
                </div>
                {endpoint.expectedResponseContains && (
                  <div className="space-y-1">
                    <p className="text-sm font-medium text-muted-foreground">Beklenen Yanıt İçeriği</p>
                    <p>{endpoint.expectedResponseContains}</p>
                  </div>
                )}
              </div>
              {endpoint.headers && Object.keys(endpoint.headers).length > 0 && (
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Headers</p>
                  <pre className="text-xs bg-muted p-2 rounded-md overflow-auto">
                    {JSON.stringify(endpoint.headers, null, 2)}
                  </pre>
                </div>
              )}
              {endpoint.body && (
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Request Body</p>
                  <pre className="text-xs bg-muted p-2 rounded-md overflow-auto">{endpoint.body}</pre>
                </div>
              )}
            </div>
          )}

          {endpoint.type === "WEBSOCKET" && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Protokol</p>
                  <p>{endpoint.protocol || "WSS"}</p>
                </div>
              </div>
              {endpoint.testMessage && (
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Test Mesajı</p>
                  <pre className="text-xs bg-muted p-2 rounded-md overflow-auto">{endpoint.testMessage}</pre>
                </div>
              )}
              {endpoint.expectedResponse && (
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Beklenen Yanıt</p>
                  <pre className="text-xs bg-muted p-2 rounded-md overflow-auto">{endpoint.expectedResponse}</pre>
                </div>
              )}
            </div>
          )}

          {endpoint.type === "DATABASE" && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Veritabanı Türü</p>
                  <p>{endpoint.dbType || "MYSQL"}</p>
                </div>
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Kullanıcı Adı</p>
                  <p>{endpoint.dbUsername || "-"}</p>
                </div>
              </div>
              {endpoint.query && (
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Sorgu</p>
                  <pre className="text-xs bg-muted p-2 rounded-md overflow-auto">{endpoint.query}</pre>
                </div>
              )}
            </div>
          )}

          <Tabs defaultValue="details" className="w-full">
            <TabsList className="w-full">
              <TabsTrigger value="details">Detaylar</TabsTrigger>
              <TabsTrigger value="metrics">Metrikler</TabsTrigger>
              <TabsTrigger value="alerts">Uyarılar</TabsTrigger>
            </TabsList>
            <TabsContent value="details" className="space-y-4 mt-4">
              <div className="text-center py-4">Bu endpoint için detaylı bilgiler burada görüntülenecektir.</div>
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
                <div className="text-center py-4">Bu endpoint için metrik bulunmamaktadır.</div>
              )}
            </TabsContent>
            <TabsContent value="alerts" className="space-y-4 mt-4">
              <AlertsList sourceType="ENDPOINT" sourceId={endpoint.id} showHeader={false} />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  )
}
