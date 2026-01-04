package org.example.ui.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.Produs;
import org.example.model.Comanda;
import org.example.model.User;
import org.example.service.ManagerAdminService;
import org.example.service.OfferConfig;
import org.example.ui.dialogs.ProductDialog;
import org.example.ui.dialogs.StaffDialog;
import org.example.persistence.ProdusEntity;

import java.io.File;
import java.util.Optional;

public class ManagerController {

    @FXML
    private TableView<User> staffTableView;
    @FXML
    private TableColumn<User, Long> staffIdColumn;
    @FXML
    private TableColumn<User, String> staffUsernameColumn;

    @FXML
    private TableView<Produs> menuTableView;
    @FXML
    private TableColumn<Produs, String> menuNameColumn;
    @FXML
    private TableColumn<Produs, Double> menuPriceColumn;
    @FXML
    private TableColumn<Produs, String> menuTypeColumn;
    @FXML
    private TableColumn<Produs, String> menuDetailsColumn;

    @FXML
    private TableView<Comanda> globalHistoryTableView;
    @FXML
    private TableColumn<Comanda, Long> historyIdColumn;
    @FXML
    private TableColumn<Comanda, String> historyStaffColumn;
    @FXML
    private TableColumn<Comanda, Integer> historyTableColumn;
    @FXML
    private TableColumn<Comanda, Double> historyTotalColumn;

    @FXML
    private CheckBox happyHourCheckBox;
    @FXML
    private CheckBox mealDealCheckBox;
    @FXML
    private CheckBox partyPackCheckBox;

    private final ManagerAdminService managerService = new ManagerAdminService();

    @FXML
    public void initialize() {
        // Staff table
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        staffUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        loadStaff();

        // Menu table
        menuNameColumn.setCellValueFactory(new PropertyValueFactory<>("nume"));
        menuPriceColumn.setCellValueFactory(new PropertyValueFactory<>("pret"));
        loadMenu();

        menuDetailsColumn.setCellValueFactory(cellData -> {
            Produs p = cellData.getValue();
            String details = "";
            if (p instanceof org.example.Pizza pizza) {
                String tops = (pizza.getToppings() == null || pizza.getToppings().isEmpty())
                        ? "fara topping"
                        : pizza.getToppings().stream().map(Enum::name).collect(java.util.stream.Collectors.joining(", "));
                details = "Blat: " + pizza.getBlat() + ", Sos: " + pizza.getSos() + ", Toppings: " + tops;
            } else if (p instanceof org.example.Mancare m) {
                details = "Gramaj: " + m.getGramaj() + "g";
            } else if (p instanceof org.example.Bautura b) {
                details = "Volum: " + b.getVolum() + "ml";
            }
            return new javafx.beans.property.SimpleStringProperty(details);
        });

        // Global history table
        historyIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        historyStaffColumn.setCellValueFactory(cellData -> {
            String username = cellData.getValue() != null && cellData.getValue().getOspatar() != null
                    ? cellData.getValue().getOspatar().getUsername()
                    : "";
            return new javafx.beans.property.SimpleStringProperty(username);
        });
        historyTableColumn.setCellValueFactory(cellData -> {
            Integer masaNr = cellData.getValue() != null && cellData.getValue().getMasa() != null
                    ? cellData.getValue().getMasa().getNumar()
                    : null;
            return new javafx.beans.property.SimpleObjectProperty<>(masaNr);
        });
        historyTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        loadGlobalHistory();

        // Offers (load persisted settings if available)
        try {
            managerService.loadOfferConfigFromDbBestEffort();
        } catch (Exception ignored) {
        }
        happyHourCheckBox.setSelected(managerService.isOfferEnabled(OfferConfig.OfferType.HAPPY_HOUR));
        mealDealCheckBox.setSelected(managerService.isOfferEnabled(OfferConfig.OfferType.MEAL_DEAL));
        partyPackCheckBox.setSelected(managerService.isOfferEnabled(OfferConfig.OfferType.PARTY_PACK));
    }

    private void loadStaff() {
        staffTableView.setItems(FXCollections.observableArrayList(managerService.listStaff()));
    }

    private void loadMenu() {
        menuTableView.setItems(FXCollections.observableArrayList(managerService.listMenu()));
        menuTypeColumn.setCellValueFactory(cellData -> {
            Produs produs = cellData.getValue();
            String type = "";
            if (produs instanceof org.example.Pizza) {
                type = "Pizza";
            } else if (produs instanceof org.example.Mancare) {
                type = "Mancare";
            } else if (produs instanceof org.example.Bautura) {
                type = "Bautura";
            }
            return new javafx.beans.property.SimpleStringProperty(type);
        });

        // Ensure UI refreshes even if domain objects are not observable.
        menuTableView.refresh();
    }

    private void loadGlobalHistory() {
        globalHistoryTableView.setItems(FXCollections.observableArrayList(managerService.listGlobalHistory()));
    }


    @FXML
    private void addStaff() {
        Optional<StaffDialog.StaffInput> input = StaffDialog.showAddStaffDialog();
        if (input.isEmpty()) return;

        String username = input.get().username();
        String password = input.get().password();

        if (managerService.staffUsernameExists(username)) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Add Staff");
            a.setHeaderText(null);
            a.setContentText("Username already exists.");
            a.showAndWait();
            return;
        }

        managerService.addStaff(username, password);
        loadStaff();
    }

    @FXML
    private void removeStaff() {
        User selectedUser = staffTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are you sure you want to delete " + selectedUser.getUsername() + "?");
            alert.setContentText("This will also delete all their orders.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            // Second confirmation (double-check)
            Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
            alert2.setTitle("Confirm Deletion (2/2)");
            alert2.setHeaderText("Final confirmation");
            alert2.setContentText("Delete staff '" + selectedUser.getUsername() + "' and ALL their orders?");

            Optional<ButtonType> result2 = alert2.showAndWait();
            if (result2.isPresent() && result2.get() == ButtonType.OK) {
                managerService.removeStaffAndOrders(selectedUser);
                loadStaff();
                loadGlobalHistory();
            }
        }
    }

    @FXML
    private void addProduct() {
        Optional<ProdusEntity> maybe = ProductDialog.showCreateDialog();
        if (maybe.isEmpty()) return;
        managerService.addProduct(maybe.get());
        loadMenu();
    }

    @FXML
    private void editProduct() {
        Produs selected = menuTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Find matching entity by type+name to preserve identity.
        String tip = selected.getClass().getSimpleName().toUpperCase();
        Optional<ProdusEntity> existing = managerService.findProductEntityByTypeAndName(tip, selected.getNume());
        if (existing.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Edit Product");
            a.setHeaderText(null);
            a.setContentText("Couldn't find the selected product in DB.");
            a.showAndWait();
            return;
        }

        Optional<ProdusEntity> edited = ProductDialog.showEditDialog(existing.get());
        if (edited.isEmpty()) return;

        managerService.saveProduct(edited.get());
        loadMenu();
        menuTableView.refresh();
    }

    @FXML
    private void removeProduct() {
        Produs selectedProdus = menuTableView.getSelectionModel().getSelectedItem();
        if (selectedProdus == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete " + selectedProdus.getNume() + "?");
        alert.setContentText("This action is irreversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        managerService.removeProductByDomain(selectedProdus);
        loadMenu();
    }

    @FXML
    private void importExportJson() {
        // Simple choice dialog
        ChoiceDialog<String> choice = new ChoiceDialog<>("Import", "Import", "Export");
        choice.setTitle("Import/Export JSON");
        choice.setHeaderText(null);
        choice.setContentText("Choose action:");
        Optional<String> selected = choice.showAndWait();
        if (selected.isEmpty()) return;

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));

        try {
            if ("Export".equalsIgnoreCase(selected.get())) {
                fc.setTitle("Export menu to JSON");
                File target = fc.showSaveDialog(menuTableView.getScene().getWindow());
                if (target == null) return;
                managerService.exportMenu(target);

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Export");
                a.setHeaderText(null);
                a.setContentText("Menu exported to: " + target.getAbsolutePath());
                a.showAndWait();
            } else {
                fc.setTitle("Import menu from JSON");
                File source = fc.showOpenDialog(menuTableView.getScene().getWindow());
                if (source == null) return;
                managerService.importMenu(source);
                loadMenu();

                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Import");
                a.setHeaderText(null);
                a.setContentText("Menu imported from: " + source.getAbsolutePath());
                a.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Import/Export");
            a.setHeaderText(null);
            a.setContentText("Operation failed: " + e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void saveOffers() {
        managerService.setOfferEnabled(OfferConfig.OfferType.HAPPY_HOUR, happyHourCheckBox.isSelected());
        managerService.setOfferEnabled(OfferConfig.OfferType.MEAL_DEAL, mealDealCheckBox.isSelected());
        managerService.setOfferEnabled(OfferConfig.OfferType.PARTY_PACK, partyPackCheckBox.isSelected());

        managerService.saveOfferConfigToDb();

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Offers");
        a.setHeaderText(null);
        a.setContentText("Offer settings saved.");
        a.showAndWait();
    }
}
