package uk.tw.energy.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

public class PricePlanComparatorControllerTest {

    private static final String PRICE_PLAN_1_ID = "test-supplier";
    private static final String PRICE_PLAN_2_ID = "best-supplier";
    private static final String PRICE_PLAN_3_ID = "second-best-supplier";
    private static final String SMART_METER_ID = "smart-meter-id";
    private PricePlanComparatorController controller;
    private MeterReadingService meterReadingService;
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        meterReadingService = new MeterReadingService(new HashMap<>());
        PricePlan pricePlan1 = new PricePlan(PRICE_PLAN_1_ID, null, BigDecimal.TEN, null);
        PricePlan pricePlan2 = new PricePlan(PRICE_PLAN_2_ID, null, BigDecimal.ONE, null);
        PricePlan pricePlan3 = new PricePlan(PRICE_PLAN_3_ID, null, BigDecimal.valueOf(2), null);

        List<PricePlan> pricePlans = Arrays.asList(pricePlan1, pricePlan2, pricePlan3);
        PricePlanService tariffService = new PricePlanService(pricePlans, meterReadingService);

        Map<String, String> meterToTariffs = new HashMap<>();
        meterToTariffs.put(SMART_METER_ID, PRICE_PLAN_1_ID);
        accountService = new AccountService(meterToTariffs);

        controller = new PricePlanComparatorController(tariffService, accountService);
    }

    @Test
    public void shouldCalculateCostForMeterReadingsForEveryPricePlan() {

        ElectricityReading electricityReading = new ElectricityReading(Instant.now().minusSeconds(3600), BigDecimal.valueOf(15.0));
        ElectricityReading otherReading = new ElectricityReading(Instant.now(), BigDecimal.valueOf(5.0));
        meterReadingService.storeReadings(SMART_METER_ID, Arrays.asList(electricityReading, otherReading));

        Map<String, BigDecimal> expectedPricePlanToCost = new HashMap<>();
        expectedPricePlanToCost.put(PRICE_PLAN_1_ID, BigDecimal.valueOf(100.0));
        expectedPricePlanToCost.put(PRICE_PLAN_2_ID, BigDecimal.valueOf(10.0));
        expectedPricePlanToCost.put(PRICE_PLAN_3_ID, BigDecimal.valueOf(20.0));

        Map<String, Object> expected = new HashMap<>();
        expected.put(PricePlanComparatorController.PRICE_PLAN_ID_KEY, PRICE_PLAN_1_ID);
        expected.put(PricePlanComparatorController.PRICE_PLAN_COMPARISONS_KEY, expectedPricePlanToCost);
        assertThat(controller.calculatedCostForEachPricePlan(SMART_METER_ID).getBody()).isEqualTo(expected);
    }

    @Test
    public void givenNoMatchingMeterIdShouldReturnNotFound() {
        assertThat(controller.calculatedCostForEachPricePlan("not-found").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
