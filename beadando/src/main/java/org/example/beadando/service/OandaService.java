
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

}
