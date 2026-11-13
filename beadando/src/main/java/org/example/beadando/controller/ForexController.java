package org.example.beadando.controller;
import org.example.beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
}
