"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import ServiceDetail from "@/components/service-detail"
import { get, put, del, API_ENDPOINTS } from "@/lib/api"
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

export default function ServiceDetailPage() {
  const params = useParams()
  const router = useRouter()
  const serviceId = Number(params.id)
  const [service, setService] = useState<Service | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchService = async () => {
      try {
        setIsLoading(true)
        const data = await get(API_ENDPOINTS.SERVICE_BY_ID(serviceId))
        setService(data)
      } catch (error) {
        console.error("Servis detayı yüklenirken hata oluştu:", error)
        toast({
          title: "Hata",
          description: "Servis detayı yüklenirken bir hata oluştu.",
          variant: "destructive",
        })
        router.push("/services")
      } finally {
        setIsLoading(false)
      }
    }

    if (serviceId) {
      fetchService()
    }
  }, [serviceId, router])

  const handleEdit = async (id: number, data: any) => {
    try {
      await put(API_ENDPOINTS.SERVICE_BY_ID(id), data)
      // Güncel veriyi çek
      const updatedService = await get(API_ENDPOINTS.SERVICE_BY_ID(id))
      setService(updatedService)
      toast({
        title: "Başarılı",
        description: "Servis başarıyla güncellendi",
      })
    } catch (error) {
      console.error("Servis güncellenirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Servis güncellenirken bir hata oluştu.",
        variant: "destructive",
      })
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await del(API_ENDPOINTS.SERVICE_BY_ID(id))
      toast({
        title: "Başarılı",
        description: "Servis başarıyla silindi",
      })
      router.push("/services")
    } catch (error) {
      console.error("Servis silinirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Servis silinirken bir hata oluştu.",
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
          <h1 className="text-3xl font-bold tracking-tight">Servis Detayı</h1>
        </div>
        <div className="flex justify-center py-8">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      </div>
    )
  }

  if (!service) {
    return (
      <div className="flex flex-col gap-6">
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">Geri</span>
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">Servis Bulunamadı</h1>
        </div>
        <p>İstenen servis bulunamadı veya erişim izniniz yok.</p>
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
        <h1 className="text-3xl font-bold tracking-tight">Servis Detayı</h1>
      </div>

      <ServiceDetail service={service} onEdit={handleEdit} onDelete={handleDelete} />
    </div>
  )
}
