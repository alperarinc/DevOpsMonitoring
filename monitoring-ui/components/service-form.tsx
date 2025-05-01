"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Switch } from "@/components/ui/switch"
import { toast } from "@/hooks/use-toast"

interface ServiceFormProps {
  service?: {
    id?: number
    name: string
    description: string
    serviceType: string
    ownerGroupId?: number
    contactEmail: string
    enabled: boolean
    priority: string
    tags: string
  }
  onSubmit: (data: any) => void
  onCancel: () => void
  isLoading?: boolean
}

export default function ServiceForm({ service, onSubmit, onCancel, isLoading = false }: ServiceFormProps) {
  const [formData, setFormData] = useState({
    name: service?.name || "",
    description: service?.description || "",
    serviceType: service?.serviceType || "API",
    ownerGroupId: service?.ownerGroupId || undefined,
    contactEmail: service?.contactEmail || "",
    enabled: service?.enabled ?? true,
    priority: service?.priority || "MEDIUM",
    tags: service?.tags || "",
  })

  const handleChange = (field: string, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    // Basit doğrulama
    if (!formData.name.trim()) {
      toast({
        title: "Hata",
        description: "Servis adı boş olamaz",
        variant: "destructive",
      })
      return
    }

    onSubmit(formData)
  }

  return (
    <Card className="w-full">
      <form onSubmit={handleSubmit}>
        <CardHeader>
          <CardTitle>{service?.id ? "Servisi Düzenle" : "Yeni Servis Oluştur"}</CardTitle>
          <CardDescription>
            {service?.id
              ? "Mevcut servis bilgilerini güncelleyin."
              : "Yeni bir servis oluşturmak için aşağıdaki bilgileri doldurun."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-2">
            <Label htmlFor="name">Servis Adı</Label>
            <Input
              id="name"
              placeholder="Servis adını girin"
              value={formData.name}
              onChange={(e) => handleChange("name", e.target.value)}
              required
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="description">Açıklama</Label>
            <Textarea
              id="description"
              placeholder="Servis açıklamasını girin"
              value={formData.description}
              onChange={(e) => handleChange("description", e.target.value)}
              rows={3}
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="serviceType">Servis Türü</Label>
              <Select value={formData.serviceType} onValueChange={(value) => handleChange("serviceType", value)}>
                <SelectTrigger id="serviceType">
                  <SelectValue placeholder="Servis türünü seçin" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="WEB_APPLICATION">Web Uygulaması</SelectItem>
                  <SelectItem value="API">API</SelectItem>
                  <SelectItem value="DATABASE">Veritabanı</SelectItem>
                  <SelectItem value="INFRASTRUCTURE">Altyapı</SelectItem>
                  <SelectItem value="MICROSERVICE">Mikroservis</SelectItem>
                  <SelectItem value="THIRD_PARTY">Üçüncü Parti</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="grid gap-2">
              <Label htmlFor="priority">Öncelik</Label>
              <Select value={formData.priority} onValueChange={(value) => handleChange("priority", value)}>
                <SelectTrigger id="priority">
                  <SelectValue placeholder="Öncelik seçin" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CRITICAL">Kritik</SelectItem>
                  <SelectItem value="HIGH">Yüksek</SelectItem>
                  <SelectItem value="MEDIUM">Orta</SelectItem>
                  <SelectItem value="LOW">Düşük</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="contactEmail">İletişim E-posta</Label>
              <Input
                id="contactEmail"
                type="email"
                placeholder="İletişim e-posta adresini girin"
                value={formData.contactEmail}
                onChange={(e) => handleChange("contactEmail", e.target.value)}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="tags">Etiketler</Label>
              <Input
                id="tags"
                placeholder="Etiketleri virgülle ayırarak girin"
                value={formData.tags}
                onChange={(e) => handleChange("tags", e.target.value)}
              />
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <Switch
              id="enabled"
              checked={formData.enabled}
              onCheckedChange={(checked) => handleChange("enabled", checked)}
            />
            <Label htmlFor="enabled">Servis Etkin</Label>
          </div>
        </CardContent>
        <CardFooter className="flex justify-between">
          <Button variant="outline" onClick={onCancel} disabled={isLoading}>
            İptal
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? "Kaydediliyor..." : service?.id ? "Güncelle" : "Oluştur"}
          </Button>
        </CardFooter>
      </form>
    </Card>
  )
}
