package org.example.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comenzi")
public class Comanda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Masa masa;

    @ManyToOne
    private User ospatar;

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComandaItem> items = new ArrayList<>();

    private double total;

    @Column(name = "subtotal")
    private Double subtotal;

    @ElementCollection
    @CollectionTable(name = "comanda_discounts", joinColumns = @JoinColumn(name = "comanda_id"))
    private List<DiscountEntry> discounts = new ArrayList<>();

    @Embeddable
    public static class DiscountEntry {
        @Enumerated(EnumType.STRING)
        @Column(name = "offer_type")
        private org.example.service.OfferConfig.OfferType type;

        @Column(name = "label")
        private String label;

        @Column(name = "amount")
        private Double amount;

        public DiscountEntry() {
        }

        public DiscountEntry(org.example.service.OfferConfig.OfferType type, String label, Double amount) {
            this.type = type;
            this.label = label;
            this.amount = amount;
        }

        public org.example.service.OfferConfig.OfferType getType() {
            return type;
        }

        public void setType(org.example.service.OfferConfig.OfferType type) {
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }

    public Comanda() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Masa getMasa() {
        return masa;
    }

    public void setMasa(Masa masa) {
        this.masa = masa;
    }

    public User getOspatar() {
        return ospatar;
    }

    public void setOspatar(User ospatar) {
        this.ospatar = ospatar;
    }

    public List<ComandaItem> getItems() {
        return items;
    }

    public void setItems(List<ComandaItem> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }

    public void addItem(ComandaItem item) {
        if (item == null) return;
        item.setComanda(this);
        this.items.add(item);
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public List<DiscountEntry> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<DiscountEntry> discounts) {
        this.discounts = discounts;
    }

    @Override
    public String toString() {
        String masaTxt = masa == null ? "?" : String.valueOf(masa.getNumar());
        String ospatarTxt = (ospatar == null || ospatar.getUsername() == null) ? "?" : ospatar.getUsername();
        String totalTxt = String.format("%.2f", total);
        return "Masa " + masaTxt + " | Ospatar: " + ospatarTxt + " | Total: " + totalTxt + " RON";
    }
}
