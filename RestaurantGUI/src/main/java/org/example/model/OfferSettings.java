package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "offer_settings")
public class OfferSettings {

    @Id
    private Integer id = 1;

    @Column(name = "happy_hour_enabled", nullable = false)
    private boolean happyHourEnabled = true;

    @Column(name = "meal_deal_enabled", nullable = false)
    private boolean mealDealEnabled = true;

    @Column(name = "party_pack_enabled", nullable = false)
    private boolean partyPackEnabled = true;

    public OfferSettings() {
    }

    public OfferSettings(boolean happyHourEnabled, boolean mealDealEnabled, boolean partyPackEnabled) {
        this.id = 1;
        this.happyHourEnabled = happyHourEnabled;
        this.mealDealEnabled = mealDealEnabled;
        this.partyPackEnabled = partyPackEnabled;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isHappyHourEnabled() {
        return happyHourEnabled;
    }

    public void setHappyHourEnabled(boolean happyHourEnabled) {
        this.happyHourEnabled = happyHourEnabled;
    }

    public boolean isMealDealEnabled() {
        return mealDealEnabled;
    }

    public void setMealDealEnabled(boolean mealDealEnabled) {
        this.mealDealEnabled = mealDealEnabled;
    }

    public boolean isPartyPackEnabled() {
        return partyPackEnabled;
    }

    public void setPartyPackEnabled(boolean partyPackEnabled) {
        this.partyPackEnabled = partyPackEnabled;
    }
}
