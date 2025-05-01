"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Plus, CheckCircle, XCircle, ExternalLink, Eye } from "lucide-react"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { get, API_ENDPOINTS } from "@/lib/api"
import { toast } from "@/hooks/use-toast"

// Endpoint tipi tanımı
interface Endpoint {
  id: number
  type: string
  url: string
  method?: string
  enabled: boolean
  serviceId?: number
  serviceName?: string
  responseTime?: number
  lastChecked?: string
}

export default function EndpointsPage() {
  const router = useRouter()
  const [endpoints, setEndpoints] = useState<Endpoint[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchEndpoints = async () => {
      try {
        setIsLoading(true)
        const data = await get(API_ENDPOINTS.ENDPOINTS)

        // API yanıtı boş veya dizi değilse, örnek veri kullan
        if (!data || !Array.isArray(data) || data.length === 0) {
          console.log("API'den veri alınamadı, örnek veri kullanılıyor")
          // Örnek veri
          const mockEndpoints: Endpoint[] = [
            {
              id: 1,
              type: "HTTP",
              url: "https://api.example.com/users",
              method: "GET",
              enabled: true,
              serviceId: 1,
              serviceName: "Kullanıcı Servisi",
              responseTime: 120,
              lastChecked: "2023-05-01 10:15:30",
            },
            {
              id: 2,
              type: "HTTP",
              url: "https://api.example.com/products",
              method: "POST",
              enabled: true,
              serviceId: 2,
              serviceName: "Ürün Servisi",
              responseTime: 150,
              lastChecked: "2023-05-01 10:10:25",
            },
            {
              id: 3,
              type: "WEBSOCKET",
              url: "wss://socket.example.com",
              enabled: false,
              serviceId: 3,
              serviceName: "Bildirim Servisi",
              responseTime: 80,
              lastChecked: "2023-05-01 10:05:15",
            },
            {
              id: 4,
              type: "DATABASE",
              url: "mysql://db.example.com:3306/main",
              enabled: true,
              serviceId: 4,
              serviceName: "Veritabanı Servisi",
              responseTime: 95,
              lastChecked: "2023-05-01 10:00:10",
            },
          ]
          setEndpoints(mockEndpoints)
          return
        }

        // Her endpoint için serviceName ekle
        const enhancedData = data.map((endpoint: Endpoint) => {
          // Eğer serviceName yoksa ve serviceId varsa, API'den servis bilgisini al
          if (!endpoint.serviceName && endpoint.serviceId) {
            // Gerçek uygulamada burada API'den servis adını çekebilirsiniz
            // Şimdilik örnek olarak servis ID'sini kullanarak bir isim oluşturuyoruz
            return {
              ...endpoint,
              serviceName: `Servis #${endpoint.serviceId}`,
            }
          }
          return endpoint
        })

        setEndpoints(enhancedData)
      } catch (error) {
        console.error("Endpoint'ler yüklenirken hata oluştu:", error)
        toast({
          title: "Hata",
          description: "Endpoint'ler yüklenirken bir hata oluştu.",
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
            serviceId: 1,
            serviceName: "Kullanıcı Servisi",
            responseTime: 120,
            lastChecked: "2023-05-01 10:15:30",
          },
          {
            id: 2,
            type: "HTTP",
            url: "https://api.example.com/products",
            method: "POST",
            enabled: true,
            serviceId: 2,
            serviceName: "Ürün Servisi",
            responseTime: 150,
            lastChecked: "2023-05-01 10:10:25",
          },
        ]
        setEndpoints(mockEndpoints)
      } finally {
        setIsLoading(false)
      }
    }

    fetchEndpoints()
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

  // Tarih formatını düzenleyen yardımcı fonksiyon
  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return "-"
    try {
      const date = new Date(dateString)
      return new Intl.DateTimeFormat("tr-TR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      }).format(date)
    } catch (e) {
      return dateString // Tarih ayrıştırılamazsa orijinal string'i döndür
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Endpoint'ler</h1>
          <p className="text-muted-foreground">Endpoint'leri yönetin ve durumlarını izleyin.</p>
        </div>
        <Button className="gap-1" onClick={() => router.push("/endpoints/new")}>
          <Plus className="h-4 w-4" />
          Yeni Endpoint
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Endpoint Listesi</CardTitle>
          <CardDescription>Sistemde tanımlı tüm endpoint'lerin listesi.</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex justify-center py-8">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
            </div>
          ) : (
            <Tabs defaultValue="all" className="space-y-4">
              <TabsList>
                <TabsTrigger value="all">Tümü</TabsTrigger>
                <TabsTrigger value="http">HTTP</TabsTrigger>
                <TabsTrigger value="websocket">WebSocket</TabsTrigger>
                <TabsTrigger value="database">Veritabanı</TabsTrigger>
              </TabsList>
              <TabsContent value="all">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Tür</TableHead>
                      <TableHead>URL</TableHead>
                      <TableHead>Metod</TableHead>
                      <TableHead>Durum</TableHead>
                      <TableHead>Servis</TableHead>
                      <TableHead>Yanıt Süresi</TableHead>
                      <TableHead>Son Kontrol</TableHead>
                      <TableHead className="text-right">İşlemler</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {endpoints.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={8} className="text-center py-4 text-muted-foreground">
                          Henüz endpoint bulunmuyor.
                        </TableCell>
                      </TableRow>
                    ) : (
                      endpoints.map((endpoint) => (
                        <TableRow key={endpoint.id}>
                          <TableCell>
                            <Badge variant="secondary" className={getTypeColor(endpoint.type)}>
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
                            {endpoint.method && (
                              <Badge variant="secondary" className={getMethodColor(endpoint.method)}>
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
                          <TableCell>{endpoint.serviceName || "Atanmamış"}</TableCell>
                          <TableCell>{endpoint.responseTime ? `${endpoint.responseTime}ms` : "-"}</TableCell>
                          <TableCell>{formatDate(endpoint.lastChecked)}</TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => router.push(`/endpoints/${endpoint.id}`)}
                            >
                              <Eye className="h-4 w-4" />
                              <span className="sr-only">Detay</span>
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TabsContent>
              <TabsContent value="http">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>URL</TableHead>
                      <TableHead>Metod</TableHead>
                      <TableHead>Durum</TableHead>
                      <TableHead>Servis</TableHead>
                      <TableHead>Yanıt Süresi</TableHead>
                      <TableHead>Son Kontrol</TableHead>
                      <TableHead className="text-right">İşlemler</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {endpoints.filter((e) => e.type === "HTTP").length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={7} className="text-center py-4 text-muted-foreground">
                          HTTP endpoint bulunmuyor.
                        </TableCell>
                      </TableRow>
                    ) : (
                      endpoints
                        .filter((e) => e.type === "HTTP")
                        .map((endpoint) => (
                          <TableRow key={endpoint.id}>
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
                              {endpoint.method && (
                                <Badge variant="secondary" className={getMethodColor(endpoint.method)}>
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
                            <TableCell>{endpoint.serviceName || "Atanmamış"}</TableCell>
                            <TableCell>{endpoint.responseTime ? `${endpoint.responseTime}ms` : "-"}</TableCell>
                            <TableCell>{formatDate(endpoint.lastChecked)}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => router.push(`/endpoints/${endpoint.id}`)}
                              >
                                <Eye className="h-4 w-4" />
                                <span className="sr-only">Detay</span>
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                    )}
                  </TableBody>
                </Table>
              </TabsContent>
              <TabsContent value="websocket">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>URL</TableHead>
                      <TableHead>Durum</TableHead>
                      <TableHead>Servis</TableHead>
                      <TableHead>Yanıt Süresi</TableHead>
                      <TableHead>Son Kontrol</TableHead>
                      <TableHead className="text-right">İşlemler</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {endpoints.filter((e) => e.type === "WEBSOCKET").length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center py-4 text-muted-foreground">
                          WebSocket endpoint bulunmuyor.
                        </TableCell>
                      </TableRow>
                    ) : (
                      endpoints
                        .filter((e) => e.type === "WEBSOCKET")
                        .map((endpoint) => (
                          <TableRow key={endpoint.id}>
                            <TableCell className="max-w-[200px] truncate">
                              <div className="flex items-center gap-1">
                                <span className="truncate">{endpoint.url}</span>
                              </div>
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
                            <TableCell>{endpoint.serviceName || "Atanmamış"}</TableCell>
                            <TableCell>{endpoint.responseTime ? `${endpoint.responseTime}ms` : "-"}</TableCell>
                            <TableCell>{formatDate(endpoint.lastChecked)}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => router.push(`/endpoints/${endpoint.id}`)}
                              >
                                <Eye className="h-4 w-4" />
                                <span className="sr-only">Detay</span>
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                    )}
                  </TableBody>
                </Table>
              </TabsContent>
              <TabsContent value="database">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>URL</TableHead>
                      <TableHead>Durum</TableHead>
                      <TableHead>Servis</TableHead>
                      <TableHead>Yanıt Süresi</TableHead>
                      <TableHead>Son Kontrol</TableHead>
                      <TableHead className="text-right">İşlemler</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {endpoints.filter((e) => e.type === "DATABASE").length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center py-4 text-muted-foreground">
                          Veritabanı endpoint bulunmuyor.
                        </TableCell>
                      </TableRow>
                    ) : (
                      endpoints
                        .filter((e) => e.type === "DATABASE")
                        .map((endpoint) => (
                          <TableRow key={endpoint.id}>
                            <TableCell className="max-w-[200px] truncate">
                              <div className="flex items-center gap-1">
                                <span className="truncate">{endpoint.url}</span>
                              </div>
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
                            <TableCell>{endpoint.serviceName || "Atanmamış"}</TableCell>
                            <TableCell>{endpoint.responseTime ? `${endpoint.responseTime}ms` : "-"}</TableCell>
                            <TableCell>{formatDate(endpoint.lastChecked)}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => router.push(`/endpoints/${endpoint.id}`)}
                              >
                                <Eye className="h-4 w-4" />
                                <span className="sr-only">Detay</span>
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                    )}
                  </TableBody>
                </Table>
              </TabsContent>
            </Tabs>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
