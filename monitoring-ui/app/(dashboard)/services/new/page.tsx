"use client"

import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import ServiceForm from "@/components/service-form"
import { useState } from "react"
import { toast } from "@/hooks/use-toast"
import { post, API_ENDPOINTS } from "@/lib/api"

export default function NewServicePage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = async (data: any) => {
    setIsLoading(true)

    try {
      await post(API_ENDPOINTS.SERVICES, data)
      toast({
        title: "Başarılı",
        description: "Servis başarıyla oluşturuldu",
      })
      router.push("/services")
    } catch (error) {
      console.error("Servis oluşturulurken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Servis oluşturulurken bir hata oluştu.",
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
        <h1 className="text-3xl font-bold tracking-tight">Yeni Servis</h1>
      </div>

      <ServiceForm onSubmit={handleSubmit} onCancel={() => router.back()} isLoading={isLoading} />
    </div>
  )
}
