package org.example.beadando.controller;
import org.example.beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForexController {

    private final OandaService oandaService;
    private final ObjectMapper objectMapper;

    public ForexController(
            OandaService oandaService,
            ObjectMapper objectMapper) {
        this.oandaService = oandaService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/forex/account")
    public String account(Model model) {
        try {
            // OANDA API lekérdezés
            String response =
                    oandaService.getAccountSummary();
            // JSON feldolgozása
            JsonNode root =
                    objectMapper.readTree(response);
            JsonNode account = root.path("account");
            // Számlaadatok átadása a HTML oldalnak
            model.addAttribute("accountId",
                    account.path("id").asText());

            model.addAttribute("alias",
                    account.path("alias").asText());

            model.addAttribute("currency",
                    account.path("currency").asText());

            model.addAttribute("balance",
                    account.path("balance").asText());

            model.addAttribute("nav",
                    account.path("NAV").asText());

            model.addAttribute("marginAvailable",
                    account.path("marginAvailable").asText());

            model.addAttribute("openTradeCount",
                    account.path("openTradeCount").asInt());

            model.addAttribute("unrealizedPL",
                    account.path("unrealizedPL").asText());
        } catch (Exception e) {
            model.addAttribute("error",
                    "Nem sikerült lekérni a számlaadatokat. "
                            + "Ellenőrizd az OANDA kapcsolatot!");
        }
        return "forex-account";
    }

    @GetMapping("/forex/price")
    public String currentPrice(
            @RequestParam(required = false,
                    defaultValue = "EUR_USD")
            String instrument,
            Model model) {

        model.addAttribute("selectedInstrument", instrument);
        try {
            // OANDA API meghívása
            String response =
                    oandaService.getCurrentPrice(instrument);

            // JSON feldolgozása
            JsonNode root = objectMapper.readTree(response);
            JsonNode prices = root.path("prices");
            if (!prices.isArray() || prices.isEmpty()) {
                throw new IllegalStateException(
                        "Nem érkezett árfolyamadat.");
            }
            JsonNode price = prices.get(0);

            // Ellenőrizzük, hogy van-e vételi és eladási ár
            if (!"tradeable".equals(
                    price.path("status").asText())
                    || price.path("bids").isEmpty()
                    || price.path("asks").isEmpty()) {

                throw new IllegalStateException(
                        "Az instrumentum jelenleg nem kereskedhető.");
            }

            String bid = price.path("bids")
                    .get(0).path("price").asText();

            String ask = price.path("asks")
                    .get(0).path("price").asText();

            // Adatok átadása a HTML oldalnak
            model.addAttribute("bid", bid);
            model.addAttribute("ask", ask);
            model.addAttribute("priceTime",
                    price.path("time").asText());

            model.addAttribute("status",
                    price.path("status").asText());

        } catch (Exception e) {
            model.addAttribute("error",
                    "Nem sikerült lekérni az árfolyamot. "
                            + "Ellenőrizd az instrumentumot és az OANDA kapcsolatot!");
        }
        return "forex-price";
    }
    @GetMapping("/forex/history")
    public String historicalPrices(
            @RequestParam(
                    defaultValue = "EUR_USD") String instrument,
            @RequestParam(
                    defaultValue = "D") String granularity,
            Model model) {
        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("selectedGranularity", granularity);
        try {
            String response = oandaService.getHistoricalPrices(
                    instrument, granularity);
            JsonNode root = objectMapper.readTree(response);
            JsonNode candles = root.path("candles");
            java.util.List<java.util.Map<String, String>> rows =
                    new java.util.ArrayList<>();
            if (!candles.isArray()) {
                throw new IllegalStateException(
                        "Nem érkezett gyertyaadat.");
            }

            // Csak a lezárt gyertyákat dolgozzuk fel
            for (JsonNode candle : candles) {
                if (!candle.path("complete").asBoolean()) {
                    continue;
                }
                JsonNode mid = candle.path("mid");
                java.util.Map<String, String> row =
                        new java.util.LinkedHashMap<>();
                row.put("time",
                        candle.path("time").asText());
                row.put("open",
                        mid.path("o").asText());
                row.put("high",
                        mid.path("h").asText());
                row.put("low",
                        mid.path("l").asText());
                row.put("close",
                        mid.path("c").asText());
                rows.add(row);
            }

            // Csak az utolsó 10 lezárt gyertyát tartjuk meg
            if (rows.size() > 10) {
                rows = new java.util.ArrayList<>(
                        rows.subList(rows.size() - 10, rows.size()));
            }
            if (rows.isEmpty()) {
                throw new IllegalStateException(
                        "Nem érhető el lezárt historikus árfolyam.");
            }
            model.addAttribute("history", rows);

            // A Chart.js számára külön listák
            java.util.List<String> labels =
                    new java.util.ArrayList<>();
            java.util.List<Double> closingPrices =
                    new java.util.ArrayList<>();
            for (java.util.Map<String, String> row : rows) {
                labels.add(row.get("time"));
                closingPrices.add(
                        Double.parseDouble(row.get("close")));
            }
            model.addAttribute("chartLabels", labels);
            model.addAttribute("chartPrices", closingPrices);

        } catch (Exception e) {
            model.addAttribute("error",
                    "Nem sikerült lekérni a historikus árfolyamokat: "
                            + e.getMessage());
        }
        return "forex-history";
    }
}
