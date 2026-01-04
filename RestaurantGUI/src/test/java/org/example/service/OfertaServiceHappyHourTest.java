package org.example.service;

import org.example.Bautura;
import org.example.model.ComandaItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfertaServiceHappyHourTest {

    @Test
    void happyHourDiscountAppliesForEachPairOfDrinks() {
        OfferConfig cfg = OfferConfig.getInstance();
        cfg.setEnabled(OfferConfig.OfferType.HAPPY_HOUR, true);
        cfg.setEnabled(OfferConfig.OfferType.MEAL_DEAL, false);
        cfg.setEnabled(OfferConfig.OfferType.PARTY_PACK, false);

        OfertaService service = new OfertaService(cfg);

        Bautura apa = new Bautura("Apa", 8.0, 500, true);
        Bautura limonada = new Bautura("Limonada", 15.0, 400, true);

        // 4 drinks total => 2 discounted drinks, cheapest ones (8 and 8) => discount = 8
        List<ComandaItem> items = List.of(
                new ComandaItem(apa, 3),
                new ComandaItem(limonada, 1)
        );

        OfertaService.Receipt receipt = service.calculateReceipt(items);
        double discount = receipt.getDiscounts().stream()
                .filter(d -> d.getType() == OfferConfig.OfferType.HAPPY_HOUR)
                .mapToDouble(OfertaService.DiscountLine::getAmount)
                .sum();

        assertEquals(8.0, discount, 0.0001);
    }

    @Test
    void happyHourNoDiscountForSingleDrink() {
        OfferConfig cfg = OfferConfig.getInstance();
        cfg.setEnabled(OfferConfig.OfferType.HAPPY_HOUR, true);
        cfg.setEnabled(OfferConfig.OfferType.MEAL_DEAL, false);
        cfg.setEnabled(OfferConfig.OfferType.PARTY_PACK, false);

        OfertaService service = new OfertaService(cfg);

        Bautura apa = new Bautura("Apa", 8.0, 500, true);
        OfertaService.Receipt receipt = service.calculateReceipt(List.of(new ComandaItem(apa, 1)));

        assertEquals(0.0, receipt.getTotalDiscount(), 0.0001);
    }
}

