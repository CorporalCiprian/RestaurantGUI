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
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import org.example.ui.util.FxAsync;

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

    @FXML
    private StackPane loadingOverlay;
    @FXML
    private Label loadingLabel;

    @FXML
    private Button importExportButton;
    @FXML
    private Button saveOffersButton;

    private final ManagerAdminService managerService = new ManagerAdminService();

    private void setLoading(boolean loading, String message) {
        if (loadingLabel != null && message != null) {
            loadingLabel.setText(message);
        }
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(loading);
            loadingOverlay.setManaged(loading);
        }

        if (importExportButton != null) importExportButton.setDisable(loading);
        if (saveOffersButton != null) saveOffersButton.setDisable(loading);
        if (staffTableView != null) staffTableView.setDisable(loading);
        if (menuTableView != null) menuTableView.setDisable(loading);
        if (globalHistoryTableView != null) globalHistoryTableView.setDisable(loading);
    }

    private void showError(String title, Throwable e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(e == null ? "Unknown error" : String.valueOf(e.getMessage()));
        a.showAndWait();
    }

    @FXML
    public void initialize() {
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        staffUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        menuNameColumn.setCellValueFactory(new PropertyValueFactory<>("nume"));
        menuPriceColumn.setCellValueFactory(new PropertyValueFactory<>("pret"));

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

        try {
            managerService.loadOfferConfigFromDbBestEffort();
        } catch (Exception ignored) {
        }
        happyHourCheckBox.setSelected(managerService.isOfferEnabled(OfferConfig.OfferType.HAPPY_HOUR));
        mealDealCheckBox.setSelected(managerService.isOfferEnabled(OfferConfig.OfferType.MEAL_DEAL));
        partyPackCheckBox.setSelected(managerService.isOfferEnabled(OfferConfig.OfferType.PARTY_PACK));

        setLoading(true, "Loading data...");
        Task<Void> loadTask = new Task<>() {
            java.util.List<User> staff;
            java.util.List<Produs> menu;
            java.util.List<Comanda> hist;

            @Override
            protected Void call() {
                staff = managerService.listStaff();
                menu = managerService.listMenu();
                hist = managerService.listGlobalHistory();
                return null;
            }
        };
        loadTask.setOnSucceeded(e -> {
            loadStaff();
            loadMenu();
            loadGlobalHistory();
            setLoading(false, null);
        });
        loadTask.setOnFailed(e -> {
            setLoading(false, null);
            showError("Load failed", loadTask.getException());
        });
        FxAsync.submit(loadTask);
    }

    private void loadStaff() {
        Task<java.util.List<User>> task = new Task<>() {
            @Override
            protected java.util.List<User> call() {
                return managerService.listStaff();
            }
        };
        task.setOnSucceeded(e -> staffTableView.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> showError("Load staff failed", task.getException()));
        FxAsync.submit(task);
    }

    private void loadMenu() {
        Task<java.util.List<Produs>> task = new Task<>() {
            @Override
            protected java.util.List<Produs> call() {
                return managerService.listMenu();
            }
        };
        task.setOnSucceeded(e -> {
            menuTableView.setItems(FXCollections.observableArrayList(task.getValue()));
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
            menuTableView.refresh();
        });
        task.setOnFailed(e -> showError("Load menu failed", task.getException()));
        FxAsync.submit(task);
    }

    private void loadGlobalHistory() {
        Task<java.util.List<Comanda>> task = new Task<>() {
            @Override
            protected java.util.List<Comanda> call() {
                return managerService.listGlobalHistory();
            }
        };
        task.setOnSucceeded(e -> globalHistoryTableView.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> showError("Load history failed", task.getException()));
        FxAsync.submit(task);
    }

    @FXML
    private void addStaff() {
        Optional<StaffDialog.StaffInput> input = StaffDialog.showAddStaffDialog();
        if (input.isEmpty()) return;

        String username = input.get().username();
        String password = input.get().password();

        setLoading(true, "Adding staff...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (managerService.staffUsernameExists(username)) {
                    throw new IllegalStateException("Username already exists.");
                }
                managerService.addStaff(username, password);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false, null);
            loadStaff();
        });
        task.setOnFailed(e -> {
            setLoading(false, null);
            showError("Add staff failed", task.getException());
        });
        FxAsync.submit(task);
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

            Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
            alert2.setTitle("Confirm Deletion (2/2)");
            alert2.setHeaderText("Final confirmation");
            alert2.setContentText("Delete staff '" + selectedUser.getUsername() + "' and ALL their orders?");

            Optional<ButtonType> result2 = alert2.showAndWait();
            if (result2.isPresent() && result2.get() == ButtonType.OK) {
                setLoading(true, "Deleting staff...");
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        managerService.removeStaffAndOrders(selectedUser);
                        return null;
                    }
                };
                task.setOnSucceeded(e -> {
                    loadStaff();
                    loadGlobalHistory();
                    setLoading(false, null);
                });
                task.setOnFailed(e -> {
                    setLoading(false, null);
                    showError("Delete failed", task.getException());
                });
                FxAsync.submit(task);
            }
        }
    }

    @FXML
    private void addProduct() {
        Optional<ProdusEntity> maybe = ProductDialog.showCreateDialog();
        if (maybe.isEmpty()) return;

        setLoading(true, "Saving product...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                managerService.addProduct(maybe.get());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false, null);
            loadMenu();
        });
        task.setOnFailed(e -> {
            setLoading(false, null);
            showError("Add product failed", task.getException());
        });
        FxAsync.submit(task);
    }

    @FXML
    private void editProduct() {
        Produs selected = menuTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        setLoading(true, "Loading product...");
        Task<java.util.Optional<ProdusEntity>> findTask = new Task<>() {
            @Override
            protected java.util.Optional<ProdusEntity> call() {
                String tip = selected.getClass().getSimpleName().toUpperCase();
                return managerService.findProductEntityByTypeAndName(tip, selected.getNume());
            }
        };
        findTask.setOnSucceeded(e -> {
            setLoading(false, null);
            Optional<ProdusEntity> existing = findTask.getValue();
            if (existing == null || existing.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Edit Product");
                a.setHeaderText(null);
                a.setContentText("Couldn't find the selected product in DB.");
                a.showAndWait();
                return;
            }

            Optional<ProdusEntity> edited = ProductDialog.showEditDialog(existing.get());
            if (edited.isEmpty()) return;

            setLoading(true, "Saving product...");
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() {
                    managerService.saveProduct(edited.get());
                    return null;
                }
            };
            saveTask.setOnSucceeded(ev -> {
                setLoading(false, null);
                loadMenu();
            });
            saveTask.setOnFailed(ev -> {
                setLoading(false, null);
                showError("Edit product failed", saveTask.getException());
            });
            FxAsync.submit(saveTask);
        });
        findTask.setOnFailed(e -> {
            setLoading(false, null);
            showError("Edit product failed", findTask.getException());
        });
        FxAsync.submit(findTask);
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

        setLoading(true, "Deleting product...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                managerService.removeProductByDomain(selectedProdus);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false, null);
            loadMenu();
        });
        task.setOnFailed(e -> {
            setLoading(false, null);
            showError("Delete product failed", task.getException());
        });
        FxAsync.submit(task);
    }

    @FXML
    private void importExportJson() {
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

                setLoading(true, "Exporting...");
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        managerService.exportMenu(target);
                        return null;
                    }
                };
                task.setOnSucceeded(e -> {
                    setLoading(false, null);
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Export");
                    a.setHeaderText(null);
                    a.setContentText("Menu exported to: " + target.getAbsolutePath());
                    a.showAndWait();
                });
                task.setOnFailed(e -> {
                    setLoading(false, null);
                    showError("Export failed", task.getException());
                });
                FxAsync.submit(task);
            } else {
                fc.setTitle("Import menu from JSON");
                File source = fc.showOpenDialog(menuTableView.getScene().getWindow());
                if (source == null) return;

                setLoading(true, "Importing...");
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        managerService.importMenu(source);
                        return null;
                    }
                };
                task.setOnSucceeded(e -> {
                    loadMenu();
                    setLoading(false, null);
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Import");
                    a.setHeaderText(null);
                    a.setContentText("Menu imported from: " + source.getAbsolutePath());
                    a.showAndWait();
                });
                task.setOnFailed(e -> {
                    setLoading(false, null);
                    showError("Import failed", task.getException());
                });
                FxAsync.submit(task);
            }
        } catch (Exception e) {
            showError("Import/Export", e);
        }
    }

    @FXML
    private void saveOffers() {
        managerService.setOfferEnabled(OfferConfig.OfferType.HAPPY_HOUR, happyHourCheckBox.isSelected());
        managerService.setOfferEnabled(OfferConfig.OfferType.MEAL_DEAL, mealDealCheckBox.isSelected());
        managerService.setOfferEnabled(OfferConfig.OfferType.PARTY_PACK, partyPackCheckBox.isSelected());

        setLoading(true, "Saving offers...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                managerService.saveOfferConfigToDb();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            setLoading(false, null);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Offers");
            a.setHeaderText(null);
            a.setContentText("Offer settings saved.");
            a.showAndWait();
        });
        task.setOnFailed(e -> {
            setLoading(false, null);
            showError("Save offers failed", task.getException());
        });
        FxAsync.submit(task);
    }
}
