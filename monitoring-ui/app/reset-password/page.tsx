"use client"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import Link from "next/link"
import { z } from "zod"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { Eye, EyeOff, CheckCircle, AlertCircle, ArrowLeft } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { API_ENDPOINTS, post } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"

// Şifre sıfırlama formu için şema
const resetPasswordSchema = z
  .object({
    password: z
      .string()
      .min(8, "Şifre en az 8 karakter olmalıdır")
      .max(100, "Şifre en fazla 100 karakter olabilir")
      .regex(/[A-Z]/, "Şifre en az bir büyük harf içermelidir")
      .regex(/[a-z]/, "Şifre en az bir küçük harf içermelidir")
      .regex(/[0-9]/, "Şifre en az bir rakam içermelidir")
      .regex(/[^A-Za-z0-9]/, "Şifre en az bir özel karakter içermelidir"),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Şifreler eşleşmiyor",
    path: ["confirmPassword"],
  })

export default function ResetPasswordPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()

  const [token, setToken] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [resetStatus, setResetStatus] = useState<"idle" | "success" | "error">("idle")
  const [errorMessage, setErrorMessage] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  // Form tanımı
  const form = useForm<z.infer<typeof resetPasswordSchema>>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      password: "",
      confirmPassword: "",
    },
  })

  // URL'den token'ı al
  useEffect(() => {
    const tokenParam = searchParams.get("token")
    if (tokenParam) {
      setToken(tokenParam)
    } else {
      setResetStatus("error")
      setErrorMessage("Geçersiz şifre sıfırlama bağlantısı. Lütfen e-postanızdaki bağlantıyı kontrol edin.")
    }
  }, [searchParams])

  // Form gönderme işlemi
  async function onSubmit(values: z.infer<typeof resetPasswordSchema>) {
    if (!token) {
      toast({
        title: "Hata",
        description: "Geçersiz şifre sıfırlama bağlantısı",
        variant: "destructive",
      })
      return
    }

    setIsSubmitting(true)
    setResetStatus("idle")

    try {
      // API'ye şifre sıfırlama isteği gönder - "password" yerine "newPassword" kullan
      const response = await post(API_ENDPOINTS.RESET_PASSWORD, {
        token,
        newPassword: values.password,
      })

      if (response.success) {
        setResetStatus("success")
        toast({
          title: "Başarılı",
          description: "Şifreniz başarıyla değiştirildi. Şimdi giriş yapabilirsiniz.",
        })

        // 3 saniye sonra giriş sayfasına yönlendir
        setTimeout(() => {
          router.push("/login")
        }, 3000)
      } else {
        setResetStatus("error")
        setErrorMessage(response.message || "Şifre sıfırlama işlemi başarısız oldu. Lütfen tekrar deneyin.")
        toast({
          title: "Hata",
          description: response.message || "Şifre sıfırlama işlemi başarısız oldu. Lütfen tekrar deneyin.",
          variant: "destructive",
        })
      }
    } catch (error) {
      setResetStatus("error")
      const errorMsg = error instanceof Error ? error.message : "Bir hata oluştu. Lütfen tekrar deneyin."
      setErrorMessage(errorMsg)
      toast({
        title: "Hata",
        description: errorMsg,
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 p-4 dark:bg-gray-900">
      <div className="w-full max-w-md">
        <Card className="border-gray-200 shadow-lg dark:border-gray-800">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-center">Şifre Sıfırlama</CardTitle>
            <CardDescription className="text-center">Lütfen yeni şifrenizi belirleyin</CardDescription>
          </CardHeader>

          <CardContent>
            {resetStatus === "error" && (
              <Alert variant="destructive" className="mb-4">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Hata</AlertTitle>
                <AlertDescription>{errorMessage}</AlertDescription>
              </Alert>
            )}

            {resetStatus === "success" && (
              <Alert className="mb-4 bg-green-50 text-green-800 dark:bg-green-900/20 dark:text-green-400">
                <CheckCircle className="h-4 w-4" />
                <AlertTitle>Başarılı</AlertTitle>
                <AlertDescription>
                  Şifreniz başarıyla değiştirildi. Giriş sayfasına yönlendiriliyorsunuz...
                </AlertDescription>
              </Alert>
            )}

            {resetStatus !== "success" && token && (
              <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                  <FormField
                    control={form.control}
                    name="password"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Yeni Şifre</FormLabel>
                        <div className="relative">
                          <FormControl>
                            <Input
                              type={showPassword ? "text" : "password"}
                              placeholder="••••••••"
                              {...field}
                              className="pr-10"
                            />
                          </FormControl>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                            onClick={() => setShowPassword(!showPassword)}
                          >
                            {showPassword ? (
                              <EyeOff className="h-4 w-4 text-gray-500" />
                            ) : (
                              <Eye className="h-4 w-4 text-gray-500" />
                            )}
                            <span className="sr-only">{showPassword ? "Şifreyi gizle" : "Şifreyi göster"}</span>
                          </Button>
                        </div>
                        <FormMessage />
                        <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                          Şifreniz en az 8 karakter uzunluğunda olmalı ve en az bir büyük harf, bir küçük harf, bir
                          rakam ve bir özel karakter içermelidir.
                        </p>
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="confirmPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Şifreyi Tekrarla</FormLabel>
                        <div className="relative">
                          <FormControl>
                            <Input
                              type={showConfirmPassword ? "text" : "password"}
                              placeholder="••••••••"
                              {...field}
                              className="pr-10"
                            />
                          </FormControl>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                          >
                            {showConfirmPassword ? (
                              <EyeOff className="h-4 w-4 text-gray-500" />
                            ) : (
                              <Eye className="h-4 w-4 text-gray-500" />
                            )}
                            <span className="sr-only">{showConfirmPassword ? "Şifreyi gizle" : "Şifreyi göster"}</span>
                          </Button>
                        </div>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <Button type="submit" className="w-full" disabled={isSubmitting}>
                    {isSubmitting ? "İşleniyor..." : "Şifreyi Değiştir"}
                  </Button>
                </form>
              </Form>
            )}
          </CardContent>

          <CardFooter className="flex justify-center border-t border-gray-200 p-4 dark:border-gray-800">
            <Link
              href="/login"
              className="flex items-center text-sm text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-100"
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Giriş sayfasına dön
            </Link>
          </CardFooter>
        </Card>
      </div>
    </div>
  )
}
