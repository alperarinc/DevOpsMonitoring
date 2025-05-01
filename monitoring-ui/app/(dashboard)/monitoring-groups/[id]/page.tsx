"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import MonitoringGroupDetail from "@/components/monitoring-group-detail"
import { get, put, del, API_ENDPOINTS } from "@/lib/api"
import { toast } from "@/hooks/use-toast"

// İzleme grubu tipi tanımı
interface MonitoringGroup {
  id: number
  name: string
  description: string
  enabled: boolean
  notificationEmail: string
  endpointIds?: number[]
  ownedServiceIds?: number[]
  ownedServiceNames?: string[]
}

export default function MonitoringGroupDetailPage() {
  const params = useParams()
  const router = useRouter()
  const groupId = Number(params.id)
  const [group, setGroup] = useState<MonitoringGroup | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchGroup = async () => {
      try {
        setIsLoading(true)
        const data = await get(API_ENDPOINTS.MONITORING_GROUP_BY_ID(groupId))
        setGroup(data)
      } catch (error) {
        console.error("İzleme grubu detayı yüklenirken hata oluştu:", error)
        toast({
          title: "Hata",
          description: "İzleme grubu detayı yüklenirken bir hata oluştu.",
          variant: "destructive",
        })
        router.push("/monitoring-groups")
      } finally {
        setIsLoading(false)
      }
    }

    if (groupId) {
      fetchGroup()
    }
  }, [groupId, router])

  const handleEdit = async (id: number, data: any) => {
    try {
      await put(API_ENDPOINTS.MONITORING_GROUP_BY_ID(id), data)
      // Güncel veriyi çek
      const updatedGroup = await get(API_ENDPOINTS.MONITORING_GROUP_BY_ID(id))
      setGroup(updatedGroup)
      toast({
        title: "Başarılı",
        description: "İzleme grubu başarıyla güncellendi",
      })
    } catch (error) {
      console.error("İzleme grubu güncellenirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "İzleme grubu güncellenirken bir hata oluştu.",
        variant: "destructive",
      })
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await del(API_ENDPOINTS.MONITORING_GROUP_BY_ID(id))
      toast({
        title: "Başarılı",
        description: "İzleme grubu başarıyla silindi",
      })
      router.push("/monitoring-groups")
    } catch (error) {
      console.error("İzleme grubu silinirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "İzleme grubu silinirken bir hata oluştu.",
        variant: "destructive",
      })
    }
  }

  if (isLoading) {
    return (
      <div className="flex flex-col gap-6">
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">Geri</span>
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">İzleme Grubu Detayı</h1>
        </div>
        <div className="flex justify-center py-8">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      </div>
    )
  }

  if (!group) {
    return (
      <div className="flex flex-col gap-6">
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">Geri</span>
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">İzleme Grubu Bulunamadı</h1>
        </div>
        <p>İstenen izleme grubu bulunamadı veya erişim izniniz yok.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-2">
        <Button variant="outline" size="icon" onClick={() => router.back()}>
          <ArrowLeft className="h-4 w-4" />
          <span className="sr-only">Geri</span>
        </Button>
        <h1 className="text-3xl font-bold tracking-tight">İzleme Grubu Detayı</h1>
      </div>

      <MonitoringGroupDetail group={group} onEdit={handleEdit} onDelete={handleDelete} />
    </div>
  )
}
