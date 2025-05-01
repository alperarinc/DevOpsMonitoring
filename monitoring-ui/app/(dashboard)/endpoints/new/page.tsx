"use client"

import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import EndpointForm from "@/components/endpoint-form"
import { useState } from "react"
import { toast } from "@/hooks/use-toast"
import { post, API_ENDPOINTS } from "@/lib/api"

export default function NewEndpointPage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = async (data: any) => {
    setIsLoading(true)

    try {
      await post(API_ENDPOINTS.ENDPOINTS, data)
      toast({
        title: "Başarılı",
        description: "Endpoint başarıyla oluşturuldu",
      })
      router.push("/endpoints")
    } catch (error) {
      console.error("Endpoint oluşturulurken hata oluştu:", error)
      toast({
        title: "Hata",
        description: "Endpoint oluşturulurken bir hata oluştu.",
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
        <h1 className="text-3xl font-bold tracking-tight">Yeni Endpoint</h1>
      </div>

      <EndpointForm onSubmit={handleSubmit} onCancel={() => router.back()} isLoading={isLoading} />
    </div>
  )
}
