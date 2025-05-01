"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Switch } from "@/components/ui/switch"
import { toast } from "@/hooks/use-toast"

interface MonitoringGroupFormProps {
  group?: {
    id?: number
    name: string
    description: string
    enabled: boolean
    notificationEmail: string
  }
  onSubmit: (data: any) => void
  onCancel: () => void
  isLoading?: boolean
}

export default function MonitoringGroupForm({
  group,
  onSubmit,
  onCancel,
  isLoading = false,
}: MonitoringGroupFormProps) {
  const [formData, setFormData] = useState({
    name: group?.name || "",
    description: group?.description || "",
    enabled: group?.enabled ?? true,
    notificationEmail: group?.notificationEmail || "",
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
        description: "Grup adı boş olamaz",
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
          <CardTitle>{group?.id ? "İzleme Grubunu Düzenle" : "Yeni İzleme Grubu Oluştur"}</CardTitle>
          <CardDescription>
            {group?.id
              ? "Mevcut izleme grubu bilgilerini güncelleyin."
              : "Yeni bir izleme grubu oluşturmak için aşağıdaki bilgileri doldurun."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-2">
            <Label htmlFor="name">Grup Adı</Label>
            <Input
              id="name"
              placeholder="Grup adını girin"
              value={formData.name}
              onChange={(e) => handleChange("name", e.target.value)}
              required
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="description">Açıklama</Label>
            <Textarea
              id="description"
              placeholder="Grup açıklamasını girin"
              value={formData.description}
              onChange={(e) => handleChange("description", e.target.value)}
              rows={3}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="notificationEmail">Bildirim E-posta</Label>
            <Input
              id="notificationEmail"
              type="email"
              placeholder="Bildirim e-posta adresini girin"
              value={formData.notificationEmail}
              onChange={(e) => handleChange("notificationEmail", e.target.value)}
            />
          </div>
          <div className="flex items-center space-x-2">
            <Switch
              id="enabled"
              checked={formData.enabled}
              onCheckedChange={(checked) => handleChange("enabled", checked)}
            />
            <Label htmlFor="enabled">Grup Etkin</Label>
          </div>
        </CardContent>
        <CardFooter className="flex justify-between">
          <Button variant="outline" onClick={onCancel} disabled={isLoading}>
            İptal
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? "Kaydediliyor..." : group?.id ? "Güncelle" : "Oluştur"}
          </Button>
        </CardFooter>
      </form>
    </Card>
  )
}
