"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Plus, CheckCircle, XCircle, Eye } from "lucide-react"
import { get, API_ENDPOINTS } from "@/lib/api"
import { toast } from "@/hooks/use-toast"

// İzleme grubu tipi tanımı
interface MonitoringGroup {
  id: number
  name: string
  description: string
  enabled: boolean
  notificationEmail: string
  endpointCount?: number
  serviceCount?: number
  endpointIds?: number[]
  ownedServiceIds?: number[]
}

export default function MonitoringGroupsPage() {
  const router = useRouter()
  const [groups, setGroups] = useState<MonitoringGroup[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchGroups = async () => {
      try {
        setIsLoading(true)
        const data = await get(API_ENDPOINTS.MONITORING_GROUPS)

        // API yanıtı boş veya dizi değilse, örnek veri kullan
        if (!data || !Array.isArray(data) || data.length === 0) {
          console.log("API'den veri alınamadı, örnek veri kullanılıyor")
          // Örnek veri
          const mockGroups: MonitoringGroup[] = [
            {
              id: 1,
              name: "Kritik Servisler",
              description: "Kritik öneme sahip servisler için izleme grubu",
              enabled: true,
              notificationEmail: "alerts@example.com",
              endpointCount: 5,
              serviceCount: 3,
              endpointIds: [1, 2, 3, 4, 5],
              ownedServiceIds: [1, 2, 3],
            },
            {
              id: 2,
              name: "Web Servisleri",
              description: "Web uygulamaları ve API'ler için izleme grubu",
              enabled: true,
              notificationEmail: "web-alerts@example.com",
              endpointCount: 8,
              serviceCount: 4,
              endpointIds: [6, 7, 8, 9, 10, 11, 12, 13],
              ownedServiceIds: [4, 5, 6, 7],
            },
            {
              id: 3,
              name: "Veritabanı Servisleri",
              description: "Veritabanı servisleri için izleme grubu",
              enabled: false,
              notificationEmail: "db-alerts@example.com",
              endpointCount: 3,
              serviceCount: 2,
              endpointIds: [14, 15, 16],
              ownedServiceIds: [8, 9],
            },
          ]
          setGroups(mockGroups)
          return
        }

        // Her grup için endpoint ve servis sayılarını hesapla
        const enhancedData = data.map((group: MonitoringGroup) => {
          return {
            ...group,
            endpointCount: group.endpointIds?.length || 0,
            serviceCount: group.ownedServiceIds?.length || 0,
          }
        })

        setGroups(enhancedData)
      } catch (error) {
        console.error("İzleme grupları yüklenirken hata oluştu:", error)

        toast({
          title: "Hata",
          description: "İzleme grupları yüklenirken bir hata oluştu.",
          variant: "destructive",
        })

        // Hata durumunda örnek veri kullan
        const mockGroups: MonitoringGroup[] = [
          {
            id: 1,
            name: "Kritik Servisler",
            description: "Kritik öneme sahip servisler için izleme grubu",
            enabled: true,
            notificationEmail: "alerts@example.com",
            endpointCount: 5,
            serviceCount: 3,
          },
          {
            id: 2,
            name: "Web Servisleri",
            description: "Web uygulamaları ve API'ler için izleme grubu",
            enabled: true,
            notificationEmail: "web-alerts@example.com",
            endpointCount: 8,
            serviceCount: 4,
          },
        ]
        setGroups(mockGroups)
      } finally {
        setIsLoading(false)
      }
    }

    fetchGroups()
  }, [])

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">İzleme Grupları</h1>
          <p className="text-muted-foreground">İzleme gruplarını yönetin ve yapılandırın.</p>
        </div>
        <Button className="gap-1" onClick={() => router.push("/monitoring-groups/new")}>
          <Plus className="h-4 w-4" />
          Yeni Grup
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Grup Listesi</CardTitle>
          <CardDescription>Sistemde tanımlı tüm izleme gruplarının listesi.</CardDescription>
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
                  <TableHead>Grup Adı</TableHead>
                  <TableHead>Açıklama</TableHead>
                  <TableHead>Durum</TableHead>
                  <TableHead>Endpoint Sayısı</TableHead>
                  <TableHead>Servis Sayısı</TableHead>
                  <TableHead>Bildirim E-posta</TableHead>
                  <TableHead className="text-right">İşlemler</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {groups.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-4 text-muted-foreground">
                      Henüz izleme grubu bulunmuyor.
                    </TableCell>
                  </TableRow>
                ) : (
                  groups.map((group) => (
                    <TableRow key={group.id}>
                      <TableCell className="font-medium">{group.name}</TableCell>
                      <TableCell>{group.description}</TableCell>
                      <TableCell>
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
                      </TableCell>
                      <TableCell>{group.endpointCount || 0}</TableCell>
                      <TableCell>{group.serviceCount || 0}</TableCell>
                      <TableCell>
                        <a
                          href={`mailto:${group.notificationEmail}`}
                          className="text-sm text-muted-foreground hover:underline"
                        >
                          {group.notificationEmail}
                        </a>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => router.push(`/monitoring-groups/${group.id}`)}
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
          )}
        </CardContent>
      </Card>
    </div>
  )
}
