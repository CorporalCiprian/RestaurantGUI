package org.example.ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.example.Bautura;
import org.example.Mancare;
import org.example.Pizza;
import org.example.Produs;
import org.example.service.MenuQueryService;
import org.example.ui.util.FxAsync;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GuestController {

    @FXML
    private ListView<Produs> menuListView;

    @FXML
    private TextField nameFilterField;

    @FXML
    private ComboBox<String> typeFilterComboBox;

    @FXML
    private TextField minPriceField;

    @FXML
    private TextField maxPriceField;

    @FXML
    private CheckBox vegetarianFilterCheckBox;

    @FXML
    private CheckBox dessertFilterCheckBox;

    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label detailsLabel;

    @FXML
    private StackPane loadingOverlay;
    @FXML
    private Label loadingLabel;

    private final MenuQueryService menuQueryService = new MenuQueryService();
    private final ObservableList<Produs> allProducts = FXCollections.observableArrayList();
    private Set<String> dessertNamesLowercase = java.util.Collections.emptySet();

    private void setLoading(boolean loading, String message) {
        if (loadingLabel != null && message != null) {
            loadingLabel.setText(message);
        }
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(loading);
            loadingOverlay.setManaged(loading);
        }

        boolean disable = loading;
        if (nameFilterField != null) nameFilterField.setDisable(disable);
        if (typeFilterComboBox != null) typeFilterComboBox.setDisable(disable);
        if (minPriceField != null) minPriceField.setDisable(disable);
        if (maxPriceField != null) maxPriceField.setDisable(disable);
        if (vegetarianFilterCheckBox != null) vegetarianFilterCheckBox.setDisable(disable);
        if (dessertFilterCheckBox != null) dessertFilterCheckBox.setDisable(disable);
        if (menuListView != null) menuListView.setDisable(disable);
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
        typeFilterComboBox.setItems(FXCollections.observableArrayList("All", "Pizza", "Mancare", "Bautura"));
        typeFilterComboBox.setValue("All");

        nameFilterField.textProperty().addListener((obs, old, val) -> {
            applyFilters();
            applyExactNameSearchSelection();
        });
        typeFilterComboBox.valueProperty().addListener((obs, old, val) -> applyFilters());
        minPriceField.textProperty().addListener((obs, old, val) -> applyFilters());
        maxPriceField.textProperty().addListener((obs, old, val) -> applyFilters());
        vegetarianFilterCheckBox.selectedProperty().addListener((obs, old, val) -> applyFilters());
        if (dessertFilterCheckBox != null) {
            dessertFilterCheckBox.selectedProperty().addListener((obs, old, val) -> applyFilters());
        }

        menuListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                nameLabel.setText("Nume: " + val.getNume());
                priceLabel.setText(String.format("Pret: %.2f RON", val.getPret()));
                String detailsText;
                if (val instanceof Pizza p) {
                    String toppingsText = (p.getToppings() == null || p.getToppings().isEmpty())
                            ? "fara topping"
                            : p.getToppings().stream().map(Enum::name).collect(Collectors.joining(", "));
                    detailsText = "Blat: " + p.getBlat() + "\nSos: " + p.getSos() + "\nTopping: " + toppingsText;
                } else if (val instanceof Mancare m) {
                    detailsText = "Gramaj: " + m.getGramaj() + " g";
                } else if (val instanceof Bautura b) {
                    detailsText = "Volum: " + b.getVolum() + " ml";
                } else {
                    detailsText = "";
                }
                detailsLabel.setText(detailsText);
            } else {
                nameLabel.setText("");
                priceLabel.setText("");
                detailsLabel.setText("");
            }
        });

        setLoading(true, "Loading menu...");
        Task<Void> loadTask = new Task<>() {
            java.util.List<Produs> products;
            java.util.Set<String> desserts;

            @Override
            protected Void call() {
                products = menuQueryService.getAllProducts();
                desserts = menuQueryService.getDessertFoodNamesLowercase();
                return null;
            }
        };
        loadTask.setOnSucceeded(e -> {
            allProducts.setAll(menuQueryService.getAllProducts());
            dessertNamesLowercase = menuQueryService.getDessertFoodNamesLowercase();
            menuListView.setItems(allProducts);
            setLoading(false, null);
        });
        loadTask.setOnFailed(e -> {
            setLoading(false, null);
            showError("Load failed", loadTask.getException());
        });
        FxAsync.submit(loadTask);
    }

    private void applyExactNameSearchSelection() {
        String q = nameFilterField.getText();
        if (q == null || q.isBlank()) return;

        String needle = q.trim().toLowerCase();
        Optional<Produs> match = menuListView.getItems().stream()
                .filter(p -> p.getNume() != null && p.getNume().trim().toLowerCase().equals(needle))
                .findFirst();

        match.ifPresent(p -> {
            menuListView.getSelectionModel().select(p);
            menuListView.scrollTo(p);
        });
    }

    private void applyFilters() {
        ObservableList<Produs> filteredList = allProducts.stream()
                .filter(p -> nameFilterField.getText().isEmpty() || p.getNume().toLowerCase().contains(nameFilterField.getText().toLowerCase()))
                .filter(p -> {
                    String type = typeFilterComboBox.getValue();
                    if (type.equals("All")) return true;
                    if (type.equals("Pizza")) return p instanceof Pizza;
                    if (type.equals("Mancare")) return p instanceof Mancare;
                    if (type.equals("Bautura")) return p instanceof Bautura;
                    return false;
                })
                .filter(p -> {
                    try {
                        double minPrice = minPriceField.getText().isEmpty() ? 0 : Double.parseDouble(minPriceField.getText());
                        return p.getPret() >= minPrice;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                })
                .filter(p -> {
                    try {
                        double maxPrice = maxPriceField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceField.getText());
                        return p.getPret() <= maxPrice;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                })
                .filter(p -> !vegetarianFilterCheckBox.isSelected() || p.isVegetarian())
                .filter(p -> {
                    if (dessertFilterCheckBox == null || !dessertFilterCheckBox.isSelected()) return true;
                    if (!(p instanceof Mancare)) return false;

                    String key = p.getNume() == null ? "" : p.getNume().trim().toLowerCase();
                    return dessertNamesLowercase.contains(key);
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        menuListView.setItems(filteredList);
    }
}
