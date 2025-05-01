"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import EndpointDetail from "@/components/endpoint-detail"
import { get, put, del, post, API_ENDPOINTS } from "@/lib/api"
import { toast } from "@/hooks/use-toast"

// Endpoint tipi tanımı
interface Endpoint {
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

export default function EndpointDetailPage() {
  const params = useParams()
  const router = useRouter()
  const endpointId = Number(params.id)
  const [endpoint, setEndpoint] = useState<Endpoint | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchEndpoint = async () => {
      try {
        setIsLoading(true)
        const data = await get(API_ENDPOINTS.ENDPOINT_BY_ID(endpointId))
        setEndpoint(data)
      } catch (error) {
        console.error("Endpoint detayı yüklenirken hata oluştu:", error)
        toast({
          title: "Hata",
          description: "Endpoint detayı yüklenirken bir hata oluştu.",
          variant: "destructive",
        })
        router.push("/endpoints")
      } finally {
        setIsLoading(false)
      }
    }

    if (endpointId) {
      fetchEndpoint()
    }
  }, [endpointId, router])

  const handleEdit = async (id: number, data: any) => {
    try {
      await put(API_ENDPOINTS.ENDPOINT_BY_ID(id), data)
      // Güncel veriyi çek
      const updatedEndpoint = await get(API_ENDPOINTS.ENDPOINT_BY_ID(id))
      setEndpoint(updatedEndpoint)
      toast({
        title: "Başarılı",
        description: "Endpoint başarıyla güncellendi",
      })
    } catch (error) {
      console.error("Endpoint güncellenirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Endpoint güncellenirken bir hata oluştu.",
        variant: "destructive",
      })
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await del(API_ENDPOINTS.ENDPOINT_BY_ID(id))
      toast({
        title: "Başarılı",
        description: "Endpoint başarıyla silindi",
      })
      router.push("/endpoints")
    } catch (error) {
      console.error("Endpoint silinirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Endpoint silinirken bir hata oluştu.",
        variant: "destructive",
      })
    }
  }

  const handleTest = async (id: number) => {
    try {
      await post(API_ENDPOINTS.TEST_ENDPOINT(id), {})
      toast({
        title: "Test Tamamlandı",
        description: "Endpoint testi başarıyla tamamlandı",
      })
    } catch (error) {
      console.error("Endpoint test edilirken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Endpoint test edilirken bir hata oluştu.",
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
          <h1 className="text-3xl font-bold tracking-tight">Endpoint Detayı</h1>
        </div>
        <div className="flex justify-center py-8">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      </div>
    )
  }

  if (!endpoint) {
    return (
      <div className="flex flex-col gap-6">
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">Geri</span>
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">Endpoint Bulunamadı</h1>
        </div>
        <p>İstenen endpoint bulunamadı veya erişim izniniz yok.</p>
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
        <h1 className="text-3xl font-bold tracking-tight">Endpoint Detayı</h1>
      </div>

      <EndpointDetail endpoint={endpoint} onEdit={handleEdit} onDelete={handleDelete} onTest={handleTest} />
    </div>
  )
}
