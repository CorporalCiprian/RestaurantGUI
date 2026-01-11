package org.example.service;

import org.example.Bautura;
import org.example.Mancare;
import org.example.Pizza;
import org.example.Produs;
import org.example.model.ComandaItem;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class OfertaService {

    private final OfferConfig offerConfig;
    private final MenuQueryService menuQueryService;

    public OfertaService() {
        this(OfferConfig.getInstance(), new MenuQueryService());
    }

    public OfertaService(OfferConfig offerConfig) {
        this(offerConfig, new MenuQueryService());
    }

    public OfertaService(OfferConfig offerConfig, MenuQueryService menuQueryService) {
        this.offerConfig = offerConfig;
        this.menuQueryService = menuQueryService;
    }

    public static final class DiscountLine {
        private final OfferConfig.OfferType type;
        private final String label;
        private final double amount;

        public DiscountLine(OfferConfig.OfferType type, String label, double amount) {
            this.type = type;
            this.label = label;
            this.amount = amount;
        }

        public OfferConfig.OfferType getType() {
            return type;
        }

        public String getLabel() {
            return label;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static final class Receipt {
        private final double subtotal;
        private final List<DiscountLine> discounts;
        private final double total;

        public Receipt(double subtotal, List<DiscountLine> discounts, double total) {
            this.subtotal = subtotal;
            this.discounts = discounts;
            this.total = total;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public List<DiscountLine> getDiscounts() {
            return discounts;
        }

        public double getTotal() {
            return total;
        }

        public double getTotalDiscount() {
            return discounts.stream().mapToDouble(DiscountLine::getAmount).sum();
        }
    }

    public Receipt calculateReceipt(List<ComandaItem> items) {
        double subtotal = items.stream().mapToDouble(item -> productPrice(item) * item.getCantitate()).sum();

        List<DiscountLine> lines = new java.util.ArrayList<>();

        if (offerConfig.isEnabled(OfferConfig.OfferType.HAPPY_HOUR)) {
            double d = applyHappyHour(items);
            if (d > 0) lines.add(new DiscountLine(OfferConfig.OfferType.HAPPY_HOUR, offerLabel(OfferConfig.OfferType.HAPPY_HOUR), d));
        }
        if (offerConfig.isEnabled(OfferConfig.OfferType.MEAL_DEAL)) {
            double d = applyMealDeal(items);
            if (d > 0) lines.add(new DiscountLine(OfferConfig.OfferType.MEAL_DEAL, offerLabel(OfferConfig.OfferType.MEAL_DEAL), d));
        }
        if (offerConfig.isEnabled(OfferConfig.OfferType.PARTY_PACK)) {
            double d = applyPartyPack(items);
            if (d > 0) lines.add(new DiscountLine(OfferConfig.OfferType.PARTY_PACK, offerLabel(OfferConfig.OfferType.PARTY_PACK), d));
        }

        double total = subtotal - lines.stream().mapToDouble(DiscountLine::getAmount).sum();
        return new Receipt(subtotal, java.util.Collections.unmodifiableList(lines), total);
    }

    private static String offerLabel(OfferConfig.OfferType type) {
        return switch (type) {
            case HAPPY_HOUR -> "Happy Hour (50% la o băutură)";
            case MEAL_DEAL -> "Meal Deal (25% la desert)";
            case PARTY_PACK -> "Party Pack (o pizza gratis)";
        };
    }

    public double calculateTotal(List<ComandaItem> items) {
        return calculateReceipt(items).getTotal();
    }

    private static double productPrice(ComandaItem item) {
        Produs domain = item.getProdusDomain();
        if (domain != null) return domain.getPret();
        return item.getUnitPrice();
    }

    private static String productTypeUpper(ComandaItem item) {
        Produs domain = item.getProdusDomain();
        if (domain != null) return domain.getClass().getSimpleName().toUpperCase();
        return item.getProductType() == null ? "" : item.getProductType().trim().toUpperCase();
    }

    private double applyHappyHour(List<ComandaItem> items) {
        long bauturiCount = items.stream()
                .filter(i -> {
                    Produs d = i.getProdusDomain();
                    if (d != null) return d instanceof Bautura;
                    return "BAUTURA".equals(productTypeUpper(i));
                })
                .mapToLong(ComandaItem::getCantitate)
                .sum();

        long discountedUnits = bauturiCount / 2;
        if (discountedUnits <= 0) return 0.0;

        java.util.ArrayList<Double> unitPrices = new java.util.ArrayList<>();
        for (ComandaItem line : items) {
            Produs d = line.getProdusDomain();
            boolean isBautura = (d != null) ? (d instanceof Bautura) : "BAUTURA".equals(productTypeUpper(line));
            if (!isBautura) continue;
            double price = productPrice(line);
            for (int i = 0; i < Math.max(0, line.getCantitate()); i++) {
                unitPrices.add(price);
            }
        }
        unitPrices.sort(Double::compareTo);

        double discount = 0.0;
        for (int i = 0; i < discountedUnits && i < unitPrices.size(); i++) {
            discount += unitPrices.get(i) * 0.5;
        }
        return discount;
    }

    private double applyMealDeal(List<ComandaItem> items) {
        boolean hasPizza = items.stream().anyMatch(i -> {
            Produs d = i.getProdusDomain();
            if (d != null) return d instanceof Pizza;
            return "PIZZA".equals(productTypeUpper(i));
        });
        if (!hasPizza) return 0;

        Set<String> dessertNamesLower = menuQueryService.getDessertFoodNamesLowercase();

        return items.stream()
                .filter(i -> {
                    Produs d = i.getProdusDomain();
                    if (d != null) return d instanceof Mancare;
                    return "MANCARE".equals(productTypeUpper(i));
                })
                .filter(i -> {
                    String name = i.getProdusDomain() != null ? i.getProdusDomain().getNume() : i.getProductName();
                    if (name == null || name.isBlank()) return false;
                    return dessertNamesLower.contains(name.trim().toLowerCase());
                })
                .min(Comparator.comparing(OfertaService::productPrice))
                .map(i -> productPrice(i) * 0.25)
                .orElse(0.0);
    }

    private double applyPartyPack(List<ComandaItem> items) {
        long pizzaCount = items.stream()
                .filter(i -> {
                    Produs d = i.getProdusDomain();
                    if (d != null) return d instanceof Pizza;
                    return "PIZZA".equals(productTypeUpper(i));
                })
                .mapToLong(ComandaItem::getCantitate)
                .sum();

        long freeUnits = pizzaCount / 4;
        if (freeUnits <= 0) return 0.0;

        java.util.ArrayList<Double> unitPrices = new java.util.ArrayList<>();
        for (ComandaItem line : items) {
            Produs d = line.getProdusDomain();
            boolean isPizza = (d != null) ? (d instanceof Pizza) : "PIZZA".equals(productTypeUpper(line));
            if (!isPizza) continue;
            double price = productPrice(line);
            for (int i = 0; i < Math.max(0, line.getCantitate()); i++) {
                unitPrices.add(price);
            }
        }
        unitPrices.sort(Double::compareTo);

        double discount = 0.0;
        for (int i = 0; i < freeUnits && i < unitPrices.size(); i++) {
            discount += unitPrices.get(i);
        }
        return discount;
    }
}
