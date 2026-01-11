package org.example.service;

import org.example.model.Comanda;
import org.example.model.ComandaItem;
import org.example.model.Masa;
import org.example.model.User;
import org.example.persistence.ComandaRepository;
import org.example.persistence.MasaRepository;

import java.util.ArrayList;
import java.util.List;

public class StaffOrderService {

    private final MasaRepository masaRepository;
    private final ComandaRepository comandaRepository;
    private final OfertaService ofertaService;

    public StaffOrderService() {
        this(new MasaRepository(), new ComandaRepository(), new OfertaService(OfferConfig.getInstance(), new MenuQueryService()));
    }

    public StaffOrderService(
            MasaRepository masaRepository,
            ComandaRepository comandaRepository,
            OfertaService ofertaService
    ) {
        this.masaRepository = masaRepository;
        this.comandaRepository = comandaRepository;
        this.ofertaService = ofertaService;
    }

    public List<Masa> getOrCreateTables(int count) {
        List<Masa> tables = new ArrayList<>(masaRepository.findAll());
        if (!tables.isEmpty()) return tables;

        for (int i = 1; i <= count; i++) {
            Masa masa = new Masa(i);
            masaRepository.save(masa);
            tables.add(masa);
        }
        return tables;
    }

    public List<Comanda> getOrderHistoryFor(User user) {
        if (user == null) return List.of();
        return comandaRepository.findByOspatar(user);
    }

    public OfertaService.Receipt calculateReceipt(List<ComandaItem> items) {
        return ofertaService.calculateReceipt(items);
    }

    public OfertaService.Receipt placeOrder(Masa table, User staffUser, List<ComandaItem> cartLinesDomain) {
        if (table == null) throw new IllegalArgumentException("table");
        if (staffUser == null) throw new IllegalArgumentException("staffUser");
        if (cartLinesDomain == null || cartLinesDomain.isEmpty()) throw new IllegalArgumentException("cartLinesDomain");

        OfertaService.Receipt receipt = ofertaService.calculateReceipt(cartLinesDomain);

        Comanda comanda = new Comanda();
        comanda.setMasa(table);
        comanda.setOspatar(staffUser);

        for (ComandaItem line : cartLinesDomain) {
            if (line == null) continue;
            if ((line.getProductName() == null || line.getProductName().isBlank()) && line.getProdusDomain() != null) {
                line.setProductName(line.getProdusDomain().getNume());
            }
            if ((line.getProductType() == null || line.getProductType().isBlank()) && line.getProdusDomain() != null) {
                line.setProductType(line.getProdusDomain().getClass().getSimpleName().toUpperCase());
            }
            if (line.getUnitPrice() == 0.0 && line.getProdusDomain() != null) {
                line.setUnitPrice(line.getProdusDomain().getPret());
            }

            ComandaItem snapshot = new ComandaItem(line.getProductName(), line.getProductType(), line.getUnitPrice(), line.getCantitate());
            comanda.addItem(snapshot);
        }

        comanda.setSubtotal(receipt.getSubtotal());
        comanda.setDiscounts(receipt.getDiscounts().stream()
                .map(d -> new Comanda.DiscountEntry(d.getType(), d.getLabel(), d.getAmount()))
                .toList());
        comanda.setTotal(receipt.getTotal());

        comandaRepository.save(comanda);
        return receipt;
    }
}
