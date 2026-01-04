package org.example.service;

import org.example.Produs;
import org.example.auth.Role;
import org.example.model.Comanda;
import org.example.model.User;
import org.example.persistence.ComandaRepository;
import org.example.persistence.ProdusEntity;
import org.example.persistence.ProdusMapper;
import org.example.persistence.ProdusRepository;
import org.example.persistence.UserRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manager/Admin use-cases: staff management, menu management, offers toggles, global history.
 * Extracted from ManagerController to keep controllers UI-only.
 */
public class ManagerAdminService {

    private final UserRepository userRepository;
    private final ProdusRepository produsRepository;
    private final ComandaRepository comandaRepository;
    private final OfferConfig offerConfig;
    private final AuthService authService;
    private final JsonMenuService jsonMenuService;

    public ManagerAdminService() {
        this(new UserRepository(), new ProdusRepository(), new ComandaRepository(), OfferConfig.getInstance(), new AuthService(), new JsonMenuService());
    }

    public ManagerAdminService(
            UserRepository userRepository,
            ProdusRepository produsRepository,
            ComandaRepository comandaRepository,
            OfferConfig offerConfig,
            AuthService authService,
            JsonMenuService jsonMenuService
    ) {
        this.userRepository = userRepository;
        this.produsRepository = produsRepository;
        this.comandaRepository = comandaRepository;
        this.offerConfig = offerConfig;
        this.authService = authService;
        this.jsonMenuService = jsonMenuService;
    }

    public List<User> listStaff() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STAFF)
                .collect(Collectors.toList());
    }

    public boolean staffUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public void addStaff(String username, String password) {
        authService.register(username, password, Role.STAFF);
    }

    public void removeStaffAndOrders(User staffUser) {
        if (staffUser == null) throw new IllegalArgumentException("staffUser");
        comandaRepository.findByOspatar(staffUser).forEach(comandaRepository::delete);
        userRepository.delete(staffUser);
    }

    public List<Produs> listMenu() {
        return produsRepository.findAll().stream().map(ProdusMapper::toDomain).collect(Collectors.toList());
    }

    public void addProduct(ProdusEntity entity) {
        produsRepository.save(entity);
    }

    public Optional<ProdusEntity> findProductEntityByTypeAndName(String typeUpper, String name) {
        return produsRepository.findByTypeAndName(typeUpper, name);
    }

    public void saveProduct(ProdusEntity entity) {
        produsRepository.save(entity);
    }

    public void removeProductByDomain(Produs selected) {
        if (selected == null) return;
        String tip = selected.getClass().getSimpleName().toUpperCase();
        Optional<ProdusEntity> existing = produsRepository.findByTypeAndName(tip, selected.getNume());
        if (existing.isPresent() && existing.get().getId() != null) {
            produsRepository.deleteById(existing.get().getId());
        } else {
            produsRepository.delete(ProdusMapper.toEntity(selected));
        }
    }

    public List<Comanda> listGlobalHistory() {
        return comandaRepository.findAll();
    }

    public void importMenu(File file) throws IOException {
        jsonMenuService.importMenu(file);
    }

    public void exportMenu(File file) throws IOException {
        jsonMenuService.exportMenu(file);
    }

    public void loadOfferConfigFromDbBestEffort() {
        offerConfig.loadFromDb();
    }

    public boolean isOfferEnabled(OfferConfig.OfferType type) {
        return offerConfig.isEnabled(type);
    }

    public void setOfferEnabled(OfferConfig.OfferType type, boolean enabled) {
        offerConfig.setEnabled(type, enabled);
    }

    public void saveOfferConfigToDb() {
        offerConfig.saveToDb();
    }
}

