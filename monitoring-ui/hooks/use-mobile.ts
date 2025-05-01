"use client"

import { useState, useEffect } from "react"

// Tek bir tutarlı export kullanıyoruz
export function useIsMobile() {
  const [isMobile, setIsMobile] = useState(false)

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768) // Adjust breakpoint as needed
    }

    // Set initial value
    handleResize()

    // Listen for window resize events
    window.addEventListener("resize", handleResize)

    // Clean up event listener on unmount
    return () => {
      window.removeEventListener("resize", handleResize)
    }
  }, [])

  return isMobile
}

// Geriye dönük uyumluluk için useMobile adını da export ediyoruz
export { useIsMobile as useMobile }
