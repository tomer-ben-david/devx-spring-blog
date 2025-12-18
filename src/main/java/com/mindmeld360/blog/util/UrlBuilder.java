package com.mindmeld360.blog.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/**
 * Utility for building URLs from HTTP requests.
 *
 * Note: X-Forwarded-* header processing is handled by Spring Boot via
 * server.forward-headers-strategy=framework in application.properties.
 * This means request.getScheme(), getServerName(), and getServerPort()
 * automatically return the correct proxy values.
 */
public class UrlBuilder {

    /**
     * Builds the base URL from the request.
     * When behind a proxy, Spring's ForwardedHeaderFilter ensures
     * the request methods return the correct external-facing values.
     *
     * @param request the HTTP servlet request
     * @return base URL like "https://example.com" or "http://localhost:8080"
     */
    public String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        if (isDefaultPort(scheme, port)) {
            return scheme + "://" + host;
        }
        return scheme + "://" + host + ":" + port;
    }

    /**
     * URL-encodes a path segment using RFC 3986 path segment encoding.
     *
     * @param segment the path segment to encode (may be null)
     * @return encoded segment, or empty string if null
     */
    public String encodePathSegment(String segment) {
        if (segment == null) {
            return "";
        }
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }

    private boolean isDefaultPort(String scheme, int port) {
        return ("http".equals(scheme) && port == 80) ||
               ("https".equals(scheme) && port == 443);
    }
}
