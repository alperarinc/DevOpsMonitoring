package com.monitoring.enums;

/**
 * HTTP metotlarını tanımlayan enum
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;

    /**
     * Spring HttpMethod'unu bu enum tipine dönüştürür
     */
    public static HttpMethod fromSpringHttpMethod(org.springframework.http.HttpMethod springMethod) {
        if (springMethod == null) {
            return null;
        }

        try {
            return HttpMethod.valueOf(springMethod.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Desteklenmeyen HTTP metodu: " + springMethod, e);
        }
    }
}