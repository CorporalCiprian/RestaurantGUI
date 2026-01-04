package org.example.ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.example.Bautura;
import org.example.Mancare;
import org.example.Pizza;
import org.example.Produs;
import org.example.service.MenuQueryService;

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

    private final MenuQueryService menuQueryService = new MenuQueryService();
    private final ObservableList<Produs> allProducts = FXCollections.observableArrayList();
    private Set<String> dessertNamesLowercase = java.util.Collections.emptySet();

    @FXML
    public void initialize() {
        allProducts.setAll(menuQueryService.getAllProducts());
        dessertNamesLowercase = menuQueryService.getDessertFoodNamesLowercase();
        menuListView.setItems(allProducts);

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
    }

    /**
     * Optional-based search requirement: try to find an exact match by name (case-insensitive).
     * If found, select it in the list so details panel updates.
     */
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
