"use client"

import type React from "react"

import { useState, useEffect } from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Activity } from "lucide-react"
import { toast } from "@/hooks/use-toast"
import { useAuth } from "@/lib/auth-context"
import { useRouter } from "next/navigation"

export default function LoginPage() {
  const { login, isAuthenticated, isLoading: authLoading } = useAuth()
  const [isLoading, setIsLoading] = useState(false)
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  })
  const router = useRouter()

  // Kullanıcı zaten giriş yapmışsa dashboard'a yönlendir
  useEffect(() => {
    if (!authLoading && isAuthenticated) {
      console.log("User already authenticated, redirecting to dashboard...")
      router.push("/dashboard")
    }
  }, [isAuthenticated, authLoading, router])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)

    try {
      // Login işlemini gerçekleştir
      const success = await login(formData.username, formData.password)

      if (success) {
        console.log("Login başarılı, dashboard'a yönlendiriliyor...")

        // Biraz bekle ve storage'ı kontrol et
        setTimeout(() => {
          const userInLocalStorage = localStorage.getItem("user")
          const userInSessionStorage = sessionStorage.getItem("user")

          console.log("Storage after login:", {
            localStorage: userInLocalStorage,
            sessionStorage: userInSessionStorage,
          })

          // Next.js router ile yönlendirme
          router.push("/dashboard")
        }, 100)
      }
    } catch (error) {
      console.error("Login error:", error)
      toast({
        title: "Giriş başarısız",
        description: "Bir hata oluştu. Lütfen tekrar deneyin.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  // Auth loading durumunda veya kullanıcı giriş yapmışsa boş ekran göster
  if (authLoading || isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-lg">Yükleniyor...</div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-2 text-center">
          <div className="flex justify-center">
            <Activity className="h-10 w-10 text-emerald-600" />
          </div>
          <CardTitle className="text-2xl">DevOps Monitoring</CardTitle>
          <CardDescription>Hesabınıza giriş yapın</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">Kullanıcı Adı</Label>
              <Input
                id="username"
                name="username"
                type="text"
                required
                value={formData.username}
                onChange={handleChange}
                disabled={isLoading}
              />
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Şifre</Label>
                <Link href="/forgot-password" className="text-xs text-muted-foreground hover:text-primary">
                  Şifremi unuttum
                </Link>
              </div>
              <Input
                id="password"
                name="password"
                type="password"
                required
                value={formData.password}
                onChange={handleChange}
                disabled={isLoading}
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Giriş yapılıyor..." : "Giriş Yap"}
            </Button>
            <div className="text-center text-sm text-muted-foreground">
              Hesabınız yok mu?{" "}
              <Link href="/register" className="text-primary hover:underline">
                Kayıt olun
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}
