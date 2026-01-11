package org.example.service;

import org.example.model.OfferSettings;
import org.example.persistence.OfferSettingsRepository;

import java.util.EnumMap;
import java.util.Map;

public final class OfferConfig {

    public enum OfferType {
        HAPPY_HOUR,
        MEAL_DEAL,
        PARTY_PACK
    }

    private static final OfferConfig INSTANCE = new OfferConfig();

    private final Map<OfferType, Boolean> enabled = new EnumMap<>(OfferType.class);
    private final OfferSettingsRepository repo = new OfferSettingsRepository();

    private OfferConfig() {
        for (OfferType t : OfferType.values()) {
            enabled.put(t, Boolean.TRUE);
        }

        try {
            loadFromDb();
        } catch (Exception ignored) {
        }
    }

    public static OfferConfig getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled(OfferType type) {
        return Boolean.TRUE.equals(enabled.get(type));
    }

    public void setEnabled(OfferType type, boolean value) {
        enabled.put(type, value);
    }

    public void loadFromDb() {
        OfferSettings s = repo.ensureDefault();
        enabled.put(OfferType.HAPPY_HOUR, s.isHappyHourEnabled());
        enabled.put(OfferType.MEAL_DEAL, s.isMealDealEnabled());
        enabled.put(OfferType.PARTY_PACK, s.isPartyPackEnabled());
    }

    public void saveToDb() {
        OfferSettings s = new OfferSettings(
                isEnabled(OfferType.HAPPY_HOUR),
                isEnabled(OfferType.MEAL_DEAL),
                isEnabled(OfferType.PARTY_PACK)
        );
        repo.save(s);
    }
}
