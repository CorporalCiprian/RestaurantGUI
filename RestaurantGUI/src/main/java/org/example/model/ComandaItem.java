package org.example.model;

import jakarta.persistence.*;
import org.example.Produs;

@Entity
@Table(name = "comanda_items")
public class ComandaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comanda_id", nullable = false)
    private Comanda comanda;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "quantity", nullable = false)
    private int cantitate;

    @Transient
    private Produs produsDomain;

    public ComandaItem() {
    }

    public ComandaItem(Produs produsDomain, int cantitate) {
        this.produsDomain = produsDomain;
        this.cantitate = cantitate;

        if (produsDomain != null) {
            this.productName = produsDomain.getNume();
            this.productType = produsDomain.getClass().getSimpleName().toUpperCase();
            this.unitPrice = produsDomain.getPret();
        }
    }

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

    public Object getProdus() {
        return null;
    }
}
