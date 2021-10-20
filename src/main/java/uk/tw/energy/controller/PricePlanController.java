package uk.tw.energy.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;

@RestController
@RequestMapping("/price-plans")
public class PricePlanController {

    private final PricePlanService pricePlanService;
    private final AccountService accountService;

    public PricePlanController(PricePlanService pricePlanService,
        AccountService accountService) {
        this.pricePlanService = pricePlanService;
        this.accountService = accountService;
    }

}
