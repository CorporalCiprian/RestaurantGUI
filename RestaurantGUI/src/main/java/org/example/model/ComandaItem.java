package org.example.model;

import jakarta.persistence.*;
import org.example.Produs;

/**
 * UI/cart line item.
 *
 * Not persisted: we keep order history minimal (masa + total) to avoid FK issues when products are deleted/replaced.
 */
@Entity
@Table(name = "comanda_items")
public class ComandaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comanda_id", nullable = false)
    private Comanda comanda;

    /** Snapshot fields (no FK to menu products). */
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "quantity", nullable = false)
    private int cantitate;

    /** UI-only: original domain product (not persisted). */
    @Transient
    private Produs produsDomain;

    public ComandaItem() {
    }

    /** Convenience constructor for UI cart (not setting comanda yet). */
    public ComandaItem(Produs produsDomain, int cantitate) {
        this.produsDomain = produsDomain;
        this.cantitate = cantitate;

        if (produsDomain != null) {
            this.productName = produsDomain.getNume();
            this.productType = produsDomain.getClass().getSimpleName().toUpperCase();
            this.unitPrice = produsDomain.getPret();
        }
    }

    /** Snapshot constructor used when persisting history. */
    public ComandaItem(String productName, String productType, double unitPrice, int cantitate) {
        this.productName = productName;
        this.productType = productType;
        this.unitPrice = unitPrice;
        this.cantitate = cantitate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Comanda getComanda() {
        return comanda;
    }

    public void setComanda(Comanda comanda) {
        this.comanda = comanda;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Produs getProdusDomain() {
        return produsDomain;
    }

    public void setProdusDomain(Produs produsDomain) {
        this.produsDomain = produsDomain;
    }

    public int getCantitate() {
        return cantitate;
    }

    public void setCantitate(int cantitate) {
        this.cantitate = cantitate;
    }

    /**
     * Backward-compat hook: we no longer persist menu entities in order history.
     * Keep signature used by OfertaService/StaffController; always null for history.
     */
    public Object getProdus() {
        return null;
    }
}
