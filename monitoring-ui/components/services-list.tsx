"use client"

import { useEffect, useState } from "react"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { CheckCircle, AlertTriangle, XCircle, Clock } from "lucide-react"
import { get, API_ENDPOINTS } from "@/lib/api"

interface Service {
  id: number
  name: string
  type: string
  status: string
  group: string
  priority: string
}

export default function ServicesList() {
  const [services, setServices] = useState<Service[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function fetchServices() {
      try {
        setIsLoading(true)
        const data = await get(API_ENDPOINTS.SERVICES)
        setServices(data)
      } catch (err) {
        console.error("Servisler yüklenirken hata oluştu:", err)
        setError("Servisler yüklenirken bir hata oluştu.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchServices()
  }, [])

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "OPERATIONAL":
        return <CheckCircle className="h-4 w-4 text-emerald-500" />
      case "DEGRADED":
        return <AlertTriangle className="h-4 w-4 text-amber-500" />
      case "OUTAGE":
        return <XCircle className="h-4 w-4 text-red-500" />
      case "MAINTENANCE":
        return <Clock className="h-4 w-4 text-slate-500" />
      default:
        return null
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "OPERATIONAL":
        return "Çalışır Durumda"
      case "DEGRADED":
        return "Performans Düşük"
      case "OUTAGE":
        return "Çalışmıyor"
      case "MAINTENANCE":
        return "Bakımda"
      default:
        return status
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "OPERATIONAL":
        return "bg-emerald-100 text-emerald-800 hover:bg-emerald-200"
      case "DEGRADED":
        return "bg-amber-100 text-amber-800 hover:bg-amber-200"
      case "OUTAGE":
        return "bg-red-100 text-red-800 hover:bg-red-200"
      case "MAINTENANCE":
        return "bg-slate-100 text-slate-800 hover:bg-slate-200"
      default:
        return "bg-slate-100 text-slate-800 hover:bg-slate-200"
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "CRITICAL":
        return "bg-red-100 text-red-800"
      case "HIGH":
        return "bg-amber-100 text-amber-800"
      case "MEDIUM":
        return "bg-blue-100 text-blue-800"
      case "LOW":
        return "bg-slate-100 text-slate-800"
      default:
        return "bg-slate-100 text-slate-800"
    }
  }

  return (
    <>
      {isLoading ? (
        <div className="flex justify-center py-4">
          <div className="h-6 w-6 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      ) : error ? (
        <div className="text-center py-4 text-red-500">{error}</div>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Servis Adı</TableHead>
              <TableHead>Tür</TableHead>
              <TableHead>Durum</TableHead>
              <TableHead>İzleme Grubu</TableHead>
              <TableHead>Öncelik</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {services.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-4 text-muted-foreground">
                  Henüz servis bulunmuyor.
                </TableCell>
              </TableRow>
            ) : (
              services.map((service) => (
                <TableRow key={service.id}>
                  <TableCell className="font-medium">{service.name}</TableCell>
                  <TableCell>{service.type}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {getStatusIcon(service.status)}
                      <Badge variant="secondary" className={getStatusColor(service.status)}>
                        {getStatusText(service.status)}
                      </Badge>
                    </div>
                  </TableCell>
                  <TableCell>{service.group}</TableCell>
                  <TableCell>
                    <Badge variant="secondary" className={getPriorityColor(service.priority)}>
                      {service.priority}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      )}
    </>
  )
}
