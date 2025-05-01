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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { toast } from "@/hooks/use-toast"

interface EndpointFormProps {
  endpoint?: {
    id?: number
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
  }
  onSubmit: (data: any) => void
  onCancel: () => void
  isLoading?: boolean
}

export default function EndpointForm({ endpoint, onSubmit, onCancel, isLoading = false }: EndpointFormProps) {
  const [formData, setFormData] = useState({
    type: endpoint?.type || "HTTP",
    url: endpoint?.url || "",
    method: endpoint?.method || "GET",
    headers: endpoint?.headers ? JSON.stringify(endpoint.headers, null, 2) : "{}",
    body: endpoint?.body || "",
    expectedStatus: endpoint?.expectedStatus || 200,
    expectedResponseContains: endpoint?.expectedResponseContains || "",
    protocol: endpoint?.protocol || "HTTPS",
    testMessage: endpoint?.testMessage || "",
    expectedResponse: endpoint?.expectedResponse || "",
    dbType: endpoint?.dbType || "MYSQL",
    dbUsername: endpoint?.dbUsername || "",
    dbPassword: endpoint?.dbPassword || "",
    query: endpoint?.query || "SELECT 1",
    intervalMs: endpoint?.intervalMs || 60000,
    timeoutMs: endpoint?.timeoutMs || 5000,
    thresholdMs: endpoint?.thresholdMs || 1000,
    serviceId: endpoint?.serviceId || undefined,
    enabled: endpoint?.enabled ?? true,
  })

  const handleChange = (field: string, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    // Basit doğrulama
    if (!formData.url.trim()) {
      toast({
        title: "Hata",
        description: "URL boş olamaz",
        variant: "destructive",
      })
      return
    }

    // Headers JSON doğrulama
    try {
      const headers = JSON.parse(formData.headers)
      if (typeof headers !== "object" || headers === null) {
        throw new Error("Headers geçerli bir JSON objesi olmalıdır")
      }
    } catch (error) {
      toast({
        title: "Hata",
        description: "Headers geçerli bir JSON formatında olmalıdır",
        variant: "destructive",
      })
      return
    }

    // Form verilerini hazırla
    const submitData = {
      ...formData,
      headers: JSON.parse(formData.headers),
    }

    onSubmit(submitData)
  }

  return (
    <Card className="w-full">
      <form onSubmit={handleSubmit}>
        <CardHeader>
          <CardTitle>{endpoint?.id ? "Endpoint'i Düzenle" : "Yeni Endpoint Oluştur"}</CardTitle>
          <CardDescription>
            {endpoint?.id
              ? "Mevcut endpoint bilgilerini güncelleyin."
              : "Yeni bir endpoint oluşturmak için aşağıdaki bilgileri doldurun."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="type">Endpoint Türü</Label>
              <Select value={formData.type} onValueChange={(value) => handleChange("type", value)}>
                <SelectTrigger id="type">
                  <SelectValue placeholder="Endpoint türünü seçin" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="HTTP">HTTP</SelectItem>
                  <SelectItem value="WEBSOCKET">WebSocket</SelectItem>
                  <SelectItem value="DATABASE">Veritabanı</SelectItem>
                  <SelectItem value="SYSTEM">Sistem</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="grid gap-2">
              <Label htmlFor="url">URL</Label>
              <Input
                id="url"
                placeholder="Endpoint URL'sini girin"
                value={formData.url}
                onChange={(e) => handleChange("url", e.target.value)}
                required
              />
            </div>
          </div>

          <Tabs defaultValue={formData.type} className="w-full" onValueChange={(value) => handleChange("type", value)}>
            <TabsList className="grid grid-cols-3 w-full">
              <TabsTrigger value="HTTP">HTTP</TabsTrigger>
              <TabsTrigger value="WEBSOCKET">WebSocket</TabsTrigger>
              <TabsTrigger value="DATABASE">Veritabanı</TabsTrigger>
            </TabsList>

            {/* HTTP Endpoint Ayarları */}
            <TabsContent value="HTTP" className="space-y-4 mt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="method">HTTP Metodu</Label>
                  <Select value={formData.method} onValueChange={(value) => handleChange("method", value)}>
                    <SelectTrigger id="method">
                      <SelectValue placeholder="HTTP metodunu seçin" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="GET">GET</SelectItem>
                      <SelectItem value="POST">POST</SelectItem>
                      <SelectItem value="PUT">PUT</SelectItem>
                      <SelectItem value="DELETE">DELETE</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="expectedStatus">Beklenen Durum Kodu</Label>
                  <Input
                    id="expectedStatus"
                    type="number"
                    placeholder="200"
                    value={formData.expectedStatus}
                    onChange={(e) => handleChange("expectedStatus", Number.parseInt(e.target.value))}
                  />
                </div>
              </div>
              <div className="grid gap-2">
                <Label htmlFor="headers">Headers (JSON)</Label>
                <Textarea
                  id="headers"
                  placeholder='{"Content-Type": "application/json", "Authorization": "Bearer token"}'
                  value={formData.headers}
                  onChange={(e) => handleChange("headers", e.target.value)}
                  rows={3}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="body">Request Body</Label>
                <Textarea
                  id="body"
                  placeholder="İstek gövdesini girin (opsiyonel)"
                  value={formData.body}
                  onChange={(e) => handleChange("body", e.target.value)}
                  rows={3}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="expectedResponseContains">Beklenen Yanıt İçeriği</Label>
                <Input
                  id="expectedResponseContains"
                  placeholder="Yanıtta bulunması gereken metin (opsiyonel)"
                  value={formData.expectedResponseContains}
                  onChange={(e) => handleChange("expectedResponseContains", e.target.value)}
                />
              </div>
            </TabsContent>

            {/* WebSocket Endpoint Ayarları */}
            <TabsContent value="WEBSOCKET" className="space-y-4 mt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="protocol">Protokol</Label>
                  <Select value={formData.protocol} onValueChange={(value) => handleChange("protocol", value)}>
                    <SelectTrigger id="protocol">
                      <SelectValue placeholder="Protokol seçin" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="WSS">WSS (WebSocket Secure)</SelectItem>
                      <SelectItem value="WS">WS (WebSocket)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid gap-2">
                <Label htmlFor="testMessage">Test Mesajı</Label>
                <Textarea
                  id="testMessage"
                  placeholder="WebSocket bağlantısı için test mesajı"
                  value={formData.testMessage}
                  onChange={(e) => handleChange("testMessage", e.target.value)}
                  rows={3}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="expectedResponse">Beklenen Yanıt</Label>
                <Textarea
                  id="expectedResponse"
                  placeholder="Beklenen yanıt mesajı (opsiyonel)"
                  value={formData.expectedResponse}
                  onChange={(e) => handleChange("expectedResponse", e.target.value)}
                  rows={3}
                />
              </div>
            </TabsContent>

            {/* Veritabanı Endpoint Ayarları */}
            <TabsContent value="DATABASE" className="space-y-4 mt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="dbType">Veritabanı Türü</Label>
                  <Select value={formData.dbType} onValueChange={(value) => handleChange("dbType", value)}>
                    <SelectTrigger id="dbType">
                      <SelectValue placeholder="Veritabanı türünü seçin" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="MYSQL">MySQL</SelectItem>
                      <SelectItem value="POSTGRESQL">PostgreSQL</SelectItem>
                      <SelectItem value="ORACLE">Oracle</SelectItem>
                      <SelectItem value="MSSQL">MS SQL</SelectItem>
                      <SelectItem value="SQLSERVER">SQL Server</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="dbUsername">Kullanıcı Adı</Label>
                  <Input
                    id="dbUsername"
                    placeholder="Veritabanı kullanıcı adı"
                    value={formData.dbUsername}
                    onChange={(e) => handleChange("dbUsername", e.target.value)}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="dbPassword">Şifre</Label>
                  <Input
                    id="dbPassword"
                    type="password"
                    placeholder="Veritabanı şifresi"
                    value={formData.dbPassword}
                    onChange={(e) => handleChange("dbPassword", e.target.value)}
                  />
                </div>
              </div>
              <div className="grid gap-2">
                <Label htmlFor="query">Sorgu</Label>
                <Textarea
                  id="query"
                  placeholder="Test sorgusu (örn: SELECT 1)"
                  value={formData.query}
                  onChange={(e) => handleChange("query", e.target.value)}
                  rows={3}
                />
              </div>
            </TabsContent>
          </Tabs>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="intervalMs">Kontrol Aralığı (ms)</Label>
              <Input
                id="intervalMs"
                type="number"
                placeholder="60000"
                value={formData.intervalMs}
                onChange={(e) => handleChange("intervalMs", Number.parseInt(e.target.value))}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="timeoutMs">Zaman Aşımı (ms)</Label>
              <Input
                id="timeoutMs"
                type="number"
                placeholder="5000"
                value={formData.timeoutMs}
                onChange={(e) => handleChange("timeoutMs", Number.parseInt(e.target.value))}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="thresholdMs">Eşik Değeri (ms)</Label>
              <Input
                id="thresholdMs"
                type="number"
                placeholder="1000"
                value={formData.thresholdMs}
                onChange={(e) => handleChange("thresholdMs", Number.parseInt(e.target.value))}
              />
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <Switch
              id="enabled"
              checked={formData.enabled}
              onCheckedChange={(checked) => handleChange("enabled", checked)}
            />
            <Label htmlFor="enabled">Endpoint Etkin</Label>
          </div>
        </CardContent>
        <CardFooter className="flex justify-between">
          <Button variant="outline" onClick={onCancel} disabled={isLoading}>
            İptal
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? "Kaydediliyor..." : endpoint?.id ? "Güncelle" : "Oluştur"}
          </Button>
        </CardFooter>
      </form>
    </Card>
  )
}
