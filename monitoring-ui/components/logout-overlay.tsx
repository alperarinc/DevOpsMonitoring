"use client"

import React from "react"

export default function LogoutOverlay() {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
      <div className="flex flex-col items-center space-y-4 text-center">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        <h2 className="text-xl font-semibold">Çıkış Yapılıyor...</h2>
        <p className="text-muted-foreground">Lütfen bekleyin, çıkış işleminiz gerçekleştiriliyor.</p>
      </div>
    </div>
  )
}