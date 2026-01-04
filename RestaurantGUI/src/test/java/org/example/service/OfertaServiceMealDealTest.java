package org.example.service;

import org.example.Pizza;
import org.example.model.ComandaItem;
import org.example.persistence.MancareEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfertaServiceMealDealTest {

    @Test
    void mealDealAppliesToCheapestDessertByFlag() {
        OfferConfig cfg = OfferConfig.getInstance();
        cfg.setEnabled(OfferConfig.OfferType.HAPPY_HOUR, false);
        cfg.setEnabled(OfferConfig.OfferType.PARTY_PACK, false);
        cfg.setEnabled(OfferConfig.OfferType.MEAL_DEAL, true);

        OfertaService service = new OfertaService(cfg);

        // One pizza => MealDeal eligible
        Pizza pizza = new Pizza.Builder(Pizza.TipBlat.CLASIC, Pizza.TipSos.ROSU)
                .nume("Pizza Margherita")
                .baza(40)
                .build();
        pizza.setPret(40);

        // Two desserts, one is cheaper.
        MancareEntity tiramisu = new MancareEntity("Tiramisu", 25, true, 120, true);
        MancareEntity inghetata = new MancareEntity("Inghetata asortata", 15, true, 100, true);

        // Some other food that is vegetarian but NOT a dessert
        MancareEntity supaVeg = new MancareEntity("Supa Veg", 24, true, 300, false);

        List<ComandaItem> items = List.of(
                new ComandaItem(pizza, 1),
                new ComandaItem(tiramisu, 1),
                new ComandaItem(inghetata, 1),
                new ComandaItem(supaVeg, 1)
        );

        OfertaService.Receipt receipt = service.calculateReceipt(items);

        // Discount should be 25% from cheapest dessert = 15 * 0.25 = 3.75
        assertEquals(3.75, receipt.getTotalDiscount(), 0.0001);

        // Ensure the discount line is present
        assertTrue(receipt.getDiscounts().stream().anyMatch(d -> d.getType() == OfferConfig.OfferType.MEAL_DEAL));
    }

    @Test
    void mealDealDoesNotApplyWithoutPizza() {
        OfferConfig cfg = OfferConfig.getInstance();
        cfg.setEnabled(OfferConfig.OfferType.HAPPY_HOUR, false);
        cfg.setEnabled(OfferConfig.OfferType.PARTY_PACK, false);
        cfg.setEnabled(OfferConfig.OfferType.MEAL_DEAL, true);

        OfertaService service = new OfertaService(cfg);

        MancareEntity inghetata = new MancareEntity("Inghetata asortata", 15, true, 100, true);
        List<ComandaItem> items = List.of(new ComandaItem(inghetata, 1));

        OfertaService.Receipt receipt = service.calculateReceipt(items);
        assertEquals(0.0, receipt.getTotalDiscount(), 0.0001);
    }
}
