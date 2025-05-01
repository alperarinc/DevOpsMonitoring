"use client"

import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import MonitoringGroupForm from "@/components/monitoring-group-form"
import { useState } from "react"
import { toast } from "@/hooks/use-toast"
import { post, API_ENDPOINTS } from "@/lib/api"

export default function NewMonitoringGroupPage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = async (data: any) => {
    setIsLoading(true)

    try {
      await post(API_ENDPOINTS.MONITORING_GROUPS, data)
      toast({
        title: "Başarılı",
        description: "İzleme grubu başarıyla oluşturuldu",
      })
      router.push("/monitoring-groups")
    } catch (error) {
      console.error("İzleme grubu oluşturulurken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "İzleme grubu oluşturulurken bir hata oluştu.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-2">
        <Button variant="outline" size="icon" onClick={() => router.back()}>
          <ArrowLeft className="h-4 w-4" />
          <span className="sr-only">Geri</span>
        </Button>
        <h1 className="text-3xl font-bold tracking-tight">Yeni İzleme Grubu</h1>
      </div>

      <MonitoringGroupForm onSubmit={handleSubmit} onCancel={() => router.back()} isLoading={isLoading} />
    </div>
  )
}
