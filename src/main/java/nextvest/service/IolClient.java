package nextvest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente de la API de IOL Inversiones (https://api.iolinversiones.com).
 * Maneja el login con token (y su renovación) y la consulta de cotizaciones.
 *
 * Todas las respuestas de la API vienen como { meta, data, error }.
 */
@Component
public class IolClient {

    private static final Logger log = LoggerFactory.getLogger(IolClient.class);

    private final RestClient restClient;
    private final String username;
    private final String password;
    private final String market;
    private final String term;
    private final String fciTerm;

    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public IolClient(@Value("${iol.base-url:https://api.iolinversiones.com}") String baseUrl,
                     @Value("${iol.username:}") String username,
                     @Value("${iol.password:}") String password,
                     @Value("${iol.market:BCBA}") String market,
                     @Value("${iol.term:T1}") String term,
                     @Value("${iol.fci-term:T0}") String fciTerm) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.username = username;
        this.password = password;
        this.market = market;
        this.term = term;
        this.fciTerm = fciTerm;
    }

    public boolean isConfigured() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }

    // Precio actual y variación del día (en %) de un instrumento.
    public record Quote(double price, Double changePercent) {}

    // Acciones, bonos y CEDEARs: plazo T1 (24 horas).
    public Optional<Quote> getMarketQuote(String symbol) {
        return extractQuote(getQuote(symbol, term, true));
    }

    // Fondos comunes de inversión: IOL los cotiza en BCBA con plazo T0.
    public Optional<Quote> getFciQuote(String symbol) {
        return extractQuote(getQuote(symbol, fciTerm, true));
    }

    // GET /broker/market-data/{symbol}/quote
    private Map<?, ?> getQuote(String symbol, String quoteTerm, boolean retryOnUnauthorized) {
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/broker/market-data/{symbol}/quote")
                            .queryParam("market", market)
                            .queryParam("term", quoteTerm)
                            .build(symbol))
                    .header("Authorization", "Bearer " + token())
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401 && retryOnUnauthorized) {
                invalidateToken();
                return getQuote(symbol, quoteTerm, false);
            }
            throw e;
        }
    }

    // El precio viene en data.prices.current y la variación del día en data.change.percent
    private Optional<Quote> extractQuote(Map<?, ?> body) {
        if (body != null
                && body.get("data") instanceof Map<?, ?> data
                && data.get("prices") instanceof Map<?, ?> prices
                && prices.get("current") instanceof Number current
                && current.doubleValue() > 0) {
            Double change = null;
            if (data.get("change") instanceof Map<?, ?> ch && ch.get("percent") instanceof Number pc) {
                change = pc.doubleValue();
            }
            return Optional.of(new Quote(current.doubleValue(), change));
        }
        return Optional.empty();
    }

    private synchronized String token() {
        if (accessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
            return accessToken;
        }

        // Si hay refresh token se intenta renovar; si falla, se vuelve a loguear.
        if (refreshToken != null) {
            try {
                saveToken(requestToken(Map.of(
                        "grant_type", "refresh_token",
                        "refresh_token", refreshToken)));
                return accessToken;
            } catch (RestClientResponseException e) {
                log.info("No se pudo renovar el token de IOL, se vuelve a iniciar sesión");
                refreshToken = null;
            }
        }

        saveToken(requestToken(Map.of(
                "grant_type", "password",
                "username", username,
                "password", password)));
        return accessToken;
    }

    private Map<?, ?> requestToken(Map<String, String> body) {
        return restClient.post()
                .uri("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private void saveToken(Map<?, ?> response) {
        if (response == null
                || !(response.get("data") instanceof Map<?, ?> data)
                || data.get("access_token") == null) {
            throw new IllegalStateException("IOL no devolvió un token");
        }

        accessToken = data.get("access_token").toString();
        refreshToken = data.get("refresh_token") != null ? data.get("refresh_token").toString() : null;
        long expiresIn = data.get("expires_in") instanceof Number n ? n.longValue() : 600;
        tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
        log.info("Token de IOL renovado");
    }

    private synchronized void invalidateToken() {
        accessToken = null;
        tokenExpiresAt = Instant.EPOCH;
    }
}