"use client"

import type React from "react"

import { useAuth } from "@/lib/auth-context"
import Sidebar from "@/components/sidebar"
import Header from "@/components/header"
import { useEffect, useState } from "react"

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const { isAuthenticated, isLoading, user } = useAuth()
  const [mounted, setMounted] = useState(false)

  // Client-side rendering için mounted state'ini ayarla
  useEffect(() => {
    setMounted(true)
  }, [])

  // Kullanıcı durumunu konsola yazdır
  useEffect(() => {
    if (mounted) {
      console.log("Dashboard Layout - Auth durumu:", {
        isAuthenticated,
        isLoading,
        user,
        localStorage: {
          user: localStorage.getItem("user"),
          token: localStorage.getItem("token"),
        },
        sessionStorage: {
          user: sessionStorage.getItem("user"),
          token: sessionStorage.getItem("token"),
        },
      })
    }
  }, [isAuthenticated, isLoading, user, mounted])

  // Sayfa henüz client-side'da render edilmediyse veya yükleme devam ediyorsa loading göster
  if (!mounted || isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
      </div>
    )
  }

  // Kullanıcı giriş yapmamışsa login sayfasına yönlendir
  if (!isAuthenticated) {
    // Hata mesajı göster ve login sayfasına yönlendir
    return (
      <div className="flex h-screen flex-col items-center justify-center gap-4">
        <h1 className="text-2xl font-bold text-red-500">Erişim Hatası</h1>
        <p>Bu sayfayı görüntülemek için giriş yapmanız gerekiyor.</p>
        <button onClick={() => (window.location.href = "/login")} className="rounded bg-primary px-4 py-2 text-white">
          Giriş Sayfasına Git
        </button>
        <div className="mt-4 rounded border p-4">
          <p className="font-semibold">Debug Bilgisi:</p>
          <p>isAuthenticated: {String(isAuthenticated)}</p>
          <p>isLoading: {String(isLoading)}</p>
          <p>user: {user ? JSON.stringify(user) : "null"}</p>
          <p>localStorage user: {localStorage.getItem("user") || "null"}</p>
          <p>localStorage token: {localStorage.getItem("token") ? "***" : "null"}</p>
          <p>sessionStorage user: {sessionStorage.getItem("user") || "null"}</p>
          <p>sessionStorage token: {sessionStorage.getItem("token") ? "***" : "null"}</p>
        </div>
      </div>
    )
  }

  // Kullanıcı giriş yapmışsa normal layout'u göster
  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar />
      <div className="flex flex-col flex-1 overflow-hidden">
        <Header />
        <main className="flex-1 overflow-auto p-4 md:p-6">{children}</main>
      </div>
    </div>
  )
}
