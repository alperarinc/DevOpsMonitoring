package com.monitoring.enums;

/**
 * İletişim protokolleri
 */
public enum Protocol {
    HTTP,
    HTTPS,
    WSS;

    private final String version;

    Protocol() {
        this.version = null;
    }

    Protocol(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Versiyon string'inden enum değeri döndürür
     */
    public static Protocol fromVersion(String version) {
        for (Protocol protocol : Protocol.values()) {
            if (protocol.version != null && protocol.version.equals(version)) {
                return protocol;
            }
        }
        return null; // Veya default bir değer dönebilir
    }
}
