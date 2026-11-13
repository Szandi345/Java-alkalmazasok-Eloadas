package org.example.beadando.controller;
import org.example.beadando.MessagePrice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapImpl;
@Controller
public class MnbController {
    @GetMapping("/exercise")
    public String form(Model model) {
        model.addAttribute("param", new MessagePrice());
        return "form";
    }
    @PostMapping("/exercise")
    public String result(
            @ModelAttribute("param") MessagePrice param,
            Model model) {
        try {
            MNBArfolyamServiceSoap client =
                    new MNBArfolyamServiceSoapImpl()
                            .getCustomBindingMNBArfolyamServiceSoap();
            String response = client.getExchangeRates(
                    param.getStartDate(),
                    param.getEndDate(),
                    param.getCurrency()
            );
            model.addAttribute("sendOut", response);
            return "result";
        } catch (Exception e) {
            model.addAttribute("error",
                    "Nem sikerült lekérni az MNB árfolyamokat.");
            return "form";
        }
    }
}