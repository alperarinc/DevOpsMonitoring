"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { AlertTriangle, CheckCircle, Clock, XCircle } from "lucide-react"
import { toast } from "@/hooks/use-toast"
import { get, put } from "@/lib/api"

// Alert interface based on the database schema
interface Alert {
  id: number
  details?: string
  level: string
  message: string
  resolved: boolean
  resolved_at?: string
  timestamp: string
  endpoint_config_id?: number
  monitoring_result_id?: number
}

interface AlertsListProps {
  sourceType?: string
  sourceId?: number
  limit?: number
  showHeader?: boolean
}

export default function AlertsList({ sourceType, sourceId, limit = 10, showHeader = true }: AlertsListProps) {
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    fetchAlerts()
  }, [sourceType, sourceId])

  const fetchAlerts = async () => {
    setIsLoading(true)
    try {
      // Build the API endpoint with optional filters
      let endpoint = "/api/alerts"

      // Add query parameters if filters are provided
      const params = new URLSearchParams()
      if (sourceType) params.append("sourceType", sourceType)
      if (sourceId) params.append("sourceId", sourceId.toString())
      if (limit) params.append("limit", limit.toString())

      const queryString = params.toString()
      if (queryString) endpoint += `?${queryString}`

      const response = await get(endpoint)

      if (response.success) {
        setAlerts(response.data || [])
      } else {
        console.error("Failed to fetch alerts:", response.message)
        setAlerts([])
      }
    } catch (error) {
      console.error("Error fetching alerts:", error)
      toast({
        title: "Hata",
        description: "Uyarılar yüklenirken bir hata oluştu.",
        variant: "destructive",
      })
      setAlerts([])
    } finally {
      setIsLoading(false)
    }
  }

  const handleResolveAlert = async (id: number) => {
    try {
      const response = await put(`/api/alerts/${id}/resolve`, {})

      if (response.success) {
        // Update the alert in the local state
        setAlerts(
          alerts.map((alert) =>
            alert.id === id ? { ...alert, resolved: true, resolved_at: new Date().toISOString() } : alert,
          ),
        )

        toast({
          title: "Başarılı",
          description: "Uyarı başarıyla çözüldü olarak işaretlendi.",
        })
      } else {
        toast({
          title: "Hata",
          description: response.message || "Uyarı çözüldü olarak işaretlenirken bir hata oluştu.",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error resolving alert:", error)
      toast({
        title: "Hata",
        description: "Uyarı çözüldü olarak işaretlenirken bir hata oluştu.",
        variant: "destructive",
      })
    }
  }

  const getSeverityIcon = (level: string) => {
    switch (level) {
      case "CRITICAL":
        return <XCircle className="h-4 w-4 text-red-500" />
      case "WARNING":
        return <AlertTriangle className="h-4 w-4 text-amber-500" />
      case "INFO":
        return <Clock className="h-4 w-4 text-blue-500" />
      default:
        return null
    }
  }

  const getSeverityColor = (level: string) => {
    switch (level) {
      case "CRITICAL":
        return "bg-red-100 text-red-800"
      case "WARNING":
        return "bg-amber-100 text-amber-800"
      case "INFO":
        return "bg-blue-100 text-blue-800"
      default:
        return "bg-slate-100 text-slate-800"
    }
  }

  const getStatusColor = (resolved: boolean) => {
    return resolved ? "bg-emerald-100 text-emerald-800" : "bg-red-100 text-red-800"
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat("tr-TR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    }).format(date)
  }

  return (
    <Card>
      {showHeader && (
        <CardHeader>
          <CardTitle>Uyarılar</CardTitle>
          <CardDescription>Sistem uyarıları ve bildirimler</CardDescription>
        </CardHeader>
      )}
      <CardContent>
        {isLoading ? (
          <div className="flex justify-center py-4">
            <div className="h-6 w-6 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          </div>
        ) : alerts.length === 0 ? (
          <div className="text-center py-4 text-muted-foreground">Uyarı bulunmuyor.</div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Zaman</TableHead>
                <TableHead>Mesaj</TableHead>
                <TableHead>Önem</TableHead>
                <TableHead>Durum</TableHead>
                <TableHead>Detaylar</TableHead>
                <TableHead className="text-right">İşlemler</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {alerts.map((alert) => (
                <TableRow key={alert.id}>
                  <TableCell className="whitespace-nowrap">{formatDate(alert.timestamp)}</TableCell>
                  <TableCell className="max-w-[300px] truncate">{alert.message}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {getSeverityIcon(alert.level)}
                      <Badge variant="secondary" className={getSeverityColor(alert.level)}>
                        {alert.level}
                      </Badge>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary" className={getStatusColor(alert.resolved)}>
                      {alert.resolved ? "Çözüldü" : "Aktif"}
                    </Badge>
                  </TableCell>
                  <TableCell className="max-w-[200px] truncate">{alert.details || "-"}</TableCell>
                  <TableCell className="text-right">
                    {!alert.resolved && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleResolveAlert(alert.id)}
                        className="h-8 px-2 text-xs"
                      >
                        <CheckCircle className="h-3.5 w-3.5 mr-1" />
                        Çözüldü
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}
