"use client"

import type React from "react"

import { useState } from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Activity } from "lucide-react"
import { toast } from "@/hooks/use-toast"
import { API_ENDPOINTS, post } from "@/lib/api"

export default function ForgotPasswordPage() {
  const [isLoading, setIsLoading] = useState(false)
  const [email, setEmail] = useState("")
  const [submitted, setSubmitted] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)

    try {
      // API'ye şifre sıfırlama e-postası gönderme isteği yap
      await post(API_ENDPOINTS.FORGOT_PASSWORD, {
        email: email,
      })

      setSubmitted(true)
      toast({
        title: "Şifre sıfırlama bağlantısı gönderildi",
        description: "E-posta adresinize şifre sıfırlama bağlantısı gönderdik. Lütfen gelen kutunuzu kontrol edin.",
      })
    } catch (error) {
      console.error("Password reset error:", error)

      // Hata mesajını al
      let errorMessage = "Şifre sıfırlama bağlantısı gönderilirken bir hata oluştu. Lütfen tekrar deneyin."
      if (error instanceof Error) {
        errorMessage = error.message
      }

      toast({
        title: "İşlem başarısız",
        description: errorMessage,
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-2 text-center">
          <div className="flex justify-center">
            <Activity className="h-10 w-10 text-emerald-600" />
          </div>
          <CardTitle className="text-2xl">Şifremi Unuttum</CardTitle>
          <CardDescription>
            {submitted
              ? "E-posta adresinize şifre sıfırlama bağlantısı gönderdik."
              : "Şifrenizi sıfırlamak için e-posta adresinizi girin."}
          </CardDescription>
        </CardHeader>
        {!submitted ? (
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">E-posta</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="ornek@infina.com.tr"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isLoading}
                />
              </div>
            </CardContent>
            <CardFooter className="flex flex-col space-y-4">
              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? "Gönderiliyor..." : "Şifre Sıfırlama Bağlantısı Gönder"}
              </Button>
              <div className="text-center text-sm text-muted-foreground">
                <Link href="/login" className="text-primary hover:underline">
                  Giriş sayfasına dön
                </Link>
              </div>
            </CardFooter>
          </form>
        ) : (
          <CardContent className="space-y-4">
            <p className="text-center text-sm text-muted-foreground">
              E-posta adresinize şifre sıfırlama bağlantısı gönderdik. Lütfen gelen kutunuzu kontrol edin.
            </p>
            <div className="flex justify-center">
              <Link href="/login">
                <Button>Giriş sayfasına dön</Button>
              </Link>
            </div>
          </CardContent>
        )}
      </Card>
    </div>
  )
}
