package org.example.service;

/**
 * Central place for defining what counts as a "dessert" for the Meal Deal offer.
 *
 * In Iteratia 7, Meal Deal says: "Cine ia Pizza, primește cel mai ieftin desert cu 25% reducere".
 *
 * Because our product model doesn't persist a menu category, we classify desserts by name.
 * You can improve this later by adding a dedicated category field to ProdusEntity.
 */
public final class MealDealDessertClassifier {
    private MealDealDessertClassifier() {
    }

    public static boolean isDessertName(String name) {
        if (name == null) return false;
        String n = name.trim().toLowerCase();
        if (n.isEmpty()) return false;

        // Keep it simple & robust for provided sample data + common Romanian terms.
        return n.contains("tiramisu")
                || n.contains("papan") // papanasi / papanași
                || n.contains("înghe") || n.contains("inghe") // înghețată / inghetata
                || n.contains("clătit") || n.contains("clatit") // clătite / clatite
                || n.contains("desert")
                || n.contains("prajitura")
                || n.contains("prăjitur")
                || n.contains("cheesecake")
                || n.contains("lava cake")
                || n.contains("brownie")
                || n.contains("cozonac")
                || n.contains("gogos") || n.contains("gogoș")
                || n.contains("sufleu")
                || n.contains("tort")
                || n.contains("budinc")
                || n.contains("mousse");
    }
}
