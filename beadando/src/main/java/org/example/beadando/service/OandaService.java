
package org.example.beadando.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OandaService {

    private final RestClient restClient;
    private final String accountId;

    public OandaService(
            @Value("${oanda.base-url}") String baseUrl,
            @Value("${oanda.account-id}") String accountId,
            @Value("${oanda.token}") String token) {

        this.accountId = accountId;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    public String getAccountSummary() {

        return restClient.get()
                .uri("/v3/accounts/{id}/summary", accountId)
                .retrieve()
                .body(String.class);
    }

    public String getCurrentPrice(String instrument) {

        // Csak az engedélyezett devizapárok használhatók
        if (!java.util.Set.of(
                "EUR_USD", "GBP_USD", "USD_JPY"
        ).contains(instrument)) {
            throw new IllegalArgumentException(
                    "Érvénytelen instrumentum!");
        }

        // Aktuális árfolyam lekérése az OANDA API-tól
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v3/accounts/{id}/pricing")
                        .queryParam("instruments", instrument)
                        .build(accountId))
                .retrieve()
                .body(String.class);
    }
    public String getHistoricalPrices(
            String instrument,
            String granularity) {

        // Instrumentum ellenőrzése
        if (!java.util.Set.of(
                "EUR_USD", "GBP_USD", "USD_JPY"
        ).contains(instrument)) {
            throw new IllegalArgumentException(
                    "Érvénytelen instrumentum!");
        }

        // Granularity ellenőrzése
        if (!java.util.Set.of(
                "H1", "H4", "D", "W"
        ).contains(granularity)) {
            throw new IllegalArgumentException(
                    "Érvénytelen időfelbontás!");
        }

        // OANDA historikus gyertyák lekérése
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v3/instruments/{instrument}/candles")
                        .queryParam("count", 11)
                        .queryParam("price", "M")
                        .queryParam("granularity", granularity)
                        .build(instrument))
                .retrieve()
                .body(String.class);
    }
    public String openTrade(String instrument, int units) {

        // Csak a megadott instrumentumokat engedélyezzük
        if (!java.util.Set.of(
                "EUR_USD", "GBP_USD", "USD_JPY"
        ).contains(instrument)) {
            throw new IllegalArgumentException(
                    "Érvénytelen instrumentum!");
        }

        // Mennyiség ellenőrzése
        if (units == 0 || Math.abs((long) units) > 1000) {
            throw new IllegalArgumentException(
                    "A mennyiség -1000 és 1000 között lehet, "
                            + "de nem lehet nulla!");
        }

        // OANDA MARKET megbízás
        java.util.Map<String, Object> order =
                java.util.Map.of(
                        "order", java.util.Map.of(
                                "type", "MARKET",
                                "instrument", instrument,
                                "units", String.valueOf(units),
                                "timeInForce", "FOK",
                                "positionFill", "OPEN_ONLY"
                        )
                );

        // POST kérés az OANDA API felé
        return restClient.post()
                .uri("/v3/accounts/{id}/orders", accountId)
                .contentType(
                        org.springframework.http.MediaType.APPLICATION_JSON)
                .body(order)
                .retrieve()
                .body(String.class);
    }
    public String getOpenTrades() {

        // Nyitott trade-ek lekérése az OANDA API-tól
        return restClient.get()
                .uri("/v3/accounts/{id}/openTrades", accountId)
                .retrieve()
                .body(String.class);
    }
}
