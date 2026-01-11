package org.example.ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.Produs;
import org.example.model.Comanda;
import org.example.model.ComandaItem;
import org.example.model.Masa;
import org.example.model.User;
import org.example.service.MenuQueryService;
import org.example.service.OfertaService;
import org.example.service.StaffOrderService;
import org.example.ui.util.FxAsync;

import java.util.ArrayList;
import java.util.Optional;

public class StaffController {

    @FXML
    private ListView<Masa> tablesListView;
    @FXML
    private ListView<Produs> menuListView;
    @FXML
    private ListView<Object> cartListView;
    @FXML
    private ListView<Comanda> orderHistoryListView;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label productPriceLabel;
    @FXML
    private Label totalLabel;

    @FXML
    private Label receiptDetailsLabel;
    @FXML
    private Label orderReceiptDetailsLabel;

    @FXML
    private Button addToCartButton;
    @FXML
    private Button increaseButton;
    @FXML
    private Button decreaseButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button placeOrderButton;

    @FXML
    private Pane menuPane;

    @FXML
    private VBox cartPane;

    @FXML
    private Label selectTableHintLabel;

    @FXML
    private StackPane loadingOverlay;
    @FXML
    private Label loadingLabel;

    private final MenuQueryService menuQueryService = new MenuQueryService();
    private final StaffOrderService staffOrderService = new StaffOrderService();

    private final ObservableList<Produs> allProducts = FXCollections.observableArrayList();
    private final ObservableList<Masa> allTables = FXCollections.observableArrayList();
    private final ObservableList<ComandaItem> cart = FXCollections.observableArrayList();
    private final ObservableList<Object> cartDisplay = FXCollections.observableArrayList();
    private final ObservableList<Comanda> orderHistory = FXCollections.observableArrayList();

    private User currentUser;

    private static final class DiscountCartLine {
        private final String label;
        private final double amount;

        private DiscountCartLine(String label, double amount) {
            this.label = label;
            this.amount = amount;
        }
    }

    @FXML
    public void initialize() {
        setLoading(true, "Loading menu & tables...");
        setOrderingVisible(false);
        setOrderingEnabled(false);

        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() {
                menuQueryService.getAllProducts();
                staffOrderService.getOrCreateTables(10);
                return null;
            }
        };
        initTask.setOnSucceeded(evt -> {
            allProducts.setAll(menuQueryService.getAllProducts());
            menuListView.setItems(allProducts);

            allTables.setAll(staffOrderService.getOrCreateTables(10));
            tablesListView.setItems(allTables);

            cartListView.setItems(cartDisplay);
            cartListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Object obj, boolean empty) {
                    super.updateItem(obj, empty);
                    if (empty || obj == null) {
                        setText(null);
                        return;
                    }
                    if (obj instanceof ComandaItem item) {
                        if (item.getProdusDomain() != null) {
                            setText(item.getProdusDomain().getNume() + " x" + item.getCantitate());
                        } else if (item.getProductName() != null && !item.getProductName().isBlank()) {
                            setText(item.getProductName() + " x" + item.getCantitate());
                        } else {
                            setText(null);
                        }
                        return;
                    }
                    if (obj instanceof DiscountCartLine d) {
                        setText(String.format("%s: -%.2f RON", d.label, d.amount));
                        return;
                    }
                    setText(obj.toString());
                }
            });

            orderHistoryListView.setItems(orderHistory);
            orderHistoryListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
                if (orderReceiptDetailsLabel == null) return;
                if (val == null) {
                    orderReceiptDetailsLabel.setText("");
                    return;
                }
                orderReceiptDetailsLabel.setText(buildReceiptTextFromOrder(val));
            });

            menuListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
                if (val != null) {
                    productNameLabel.setText(val.getNume());
                    productPriceLabel.setText(String.format("%.2f RON", val.getPret()));
                }
            });

            cart.addListener((javafx.collections.ListChangeListener.Change<? extends ComandaItem> c) -> updateTotal());
            updateTotal();

            tablesListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
                boolean hasTable = val != null;
                setOrderingVisible(hasTable);
                setOrderingEnabled(hasTable);

                boolean switchedTable = old != null && val != null && !old.equals(val);
                if (!hasTable || switchedTable) {
                    cart.clear();
                    cartDisplay.clear();
                    productNameLabel.setText("");
                    productPriceLabel.setText("");
                    if (receiptDetailsLabel != null) receiptDetailsLabel.setText("");
                    totalLabel.setText("Total: 0.00 RON");
                }
            });

            setLoading(false, null);
        });
        initTask.setOnFailed(evt -> {
            setLoading(false, null);
            showError("Load failed", initTask.getException());
        });
        FxAsync.submit(initTask);
    }

    private void setLoading(boolean loading, String message) {
        if (loadingLabel != null && message != null) {
            loadingLabel.setText(message);
        }
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(loading);
            loadingOverlay.setManaged(loading);
        }
    }

    private void showError(String title, Throwable e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(e == null ? "Unknown error" : String.valueOf(e.getMessage()));
        a.showAndWait();
    }

    private void setOrderingEnabled(boolean enabled) {
        if (menuListView != null) menuListView.setDisable(!enabled);
        if (cartListView != null) cartListView.setDisable(!enabled);
        if (addToCartButton != null) addToCartButton.setDisable(!enabled);
        if (increaseButton != null) increaseButton.setDisable(!enabled);
        if (decreaseButton != null) decreaseButton.setDisable(!enabled);
        if (removeButton != null) removeButton.setDisable(!enabled);
        if (placeOrderButton != null) placeOrderButton.setDisable(!enabled);
    }

    private void setOrderingVisible(boolean visible) {
        if (selectTableHintLabel != null) {
            selectTableHintLabel.setVisible(!visible);
            selectTableHintLabel.setManaged(!visible);
        }

        if (menuPane != null) {
            menuPane.setVisible(visible);
            menuPane.setManaged(visible);
        }
        if (cartPane != null) {
            cartPane.setVisible(visible);
            cartPane.setManaged(visible);
        }
    }

    @FXML
    private void addToCart() {
        Produs selectedProdus = menuListView.getSelectionModel().getSelectedItem();
        if (selectedProdus == null) return;

        Optional<ComandaItem> existingItem = cart.stream()
                .filter(item -> item.getProdusDomain() != null && item.getProdusDomain().equals(selectedProdus))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setCantitate(existingItem.get().getCantitate() + 1);
        } else {
            cart.add(new ComandaItem(selectedProdus, 1));
        }

        updateTotal();
    }

    @FXML
    private void increaseQuantity() {
        Object selected = cartListView.getSelectionModel().getSelectedItem();
        if (!(selected instanceof ComandaItem selectedItem)) return;
        selectedItem.setCantitate(selectedItem.getCantitate() + 1);
        refreshCartDisplay();
        updateTotal();
    }

    @FXML
    private void decreaseQuantity() {
        Object selected = cartListView.getSelectionModel().getSelectedItem();
        if (!(selected instanceof ComandaItem selectedItem)) return;

        if (selectedItem.getCantitate() > 1) {
            selectedItem.setCantitate(selectedItem.getCantitate() - 1);
            refreshCartDisplay();
            updateTotal();
        } else {
            cart.remove(selectedItem);
        }
    }

    @FXML
    private void removeFromCart() {
        Object selected = cartListView.getSelectionModel().getSelectedItem();
        if (!(selected instanceof ComandaItem selectedItem)) return;
        cart.remove(selectedItem);
    }

    private void loadOrderHistoryAsync() {
        if (currentUser == null) return;
        setLoading(true, "Loading history...");
        Task<java.util.List<Comanda>> task = new Task<>() {
            @Override
            protected java.util.List<Comanda> call() {
                return staffOrderService.getOrderHistoryFor(currentUser);
            }
        };
        task.setOnSucceeded(e -> {
            orderHistory.setAll(task.getValue());
            setLoading(false, null);
        });
        task.setOnFailed(e -> {
            setLoading(false, null);
            showError("History load failed", task.getException());
        });
        FxAsync.submit(task);
    }

    @FXML
    private void placeOrder() {
        Masa selectedTable = tablesListView.getSelectionModel().getSelectedItem();
        if (selectedTable == null) return;
        if (cart.isEmpty()) return;
        if (currentUser == null) return;

        java.util.ArrayList<ComandaItem> lines = new java.util.ArrayList<>(cart);

        setLoading(true, "Placing order...");
        setOrderingEnabled(false);

        Task<OfertaService.Receipt> task = new Task<>() {
            @Override
            protected OfertaService.Receipt call() {
                return staffOrderService.placeOrder(selectedTable, currentUser, lines);
            }
        };
        task.setOnSucceeded(e -> {
            OfertaService.Receipt receipt = task.getValue();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bon");
            alert.setHeaderText("Comanda plasată");
            alert.setContentText(buildReceiptText(receipt));
            alert.showAndWait();

            cart.clear();
            refreshCartDisplay();
            updateTotal();

            setLoading(false, null);
            setOrderingEnabled(true);

            loadOrderHistoryAsync();
        });
        task.setOnFailed(e -> {
            setLoading(false, null);
            setOrderingEnabled(true);
            showError("Place order failed", task.getException());
        });
        FxAsync.submit(task);
    }

    private static String buildReceiptText(OfertaService.Receipt receipt) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Subtotal: %.2f RON", receipt.getSubtotal()));
        if (!receipt.getDiscounts().isEmpty()) {
            for (OfertaService.DiscountLine d : receipt.getDiscounts()) {
                sb.append("\n").append(String.format("%s: -%.2f RON", d.getLabel(), d.getAmount()));
            }
            sb.append("\n").append(String.format("Reduceri total: -%.2f RON", receipt.getTotalDiscount()));
        }
        sb.append("\n").append(String.format("TOTAL: %.2f RON", receipt.getTotal()));
        return sb.toString();
    }

    private static String buildReceiptTextFromOrder(Comanda order) {
        StringBuilder sb = new StringBuilder();

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (ComandaItem it : order.getItems()) {
                if (it == null) continue;
                String name = it.getProductName() != null ? it.getProductName() : "";
                int qty = it.getCantitate();
                double unit = it.getUnitPrice();
                double lineTotal = unit * qty;
                sb.append(String.format("%s x%d  (%.2f RON) = %.2f RON", name, qty, unit, lineTotal));
                sb.append("\n");
            }
            sb.append("\n");
        }

        double subtotal = order.getSubtotal() != null ? order.getSubtotal() : order.getTotal();
        sb.append(String.format("Subtotal: %.2f RON", subtotal));

        if (order.getDiscounts() != null && !order.getDiscounts().isEmpty()) {
            double totalDisc = 0.0;
            for (Comanda.DiscountEntry d : order.getDiscounts()) {
                if (d == null || d.getAmount() == null) continue;
                totalDisc += d.getAmount();
                sb.append("\n").append(String.format("%s: -%.2f RON", d.getLabel(), d.getAmount()));
            }
            sb.append("\n").append(String.format("Reduceri total: -%.2f RON", totalDisc));
        }

        sb.append("\n").append(String.format("TOTAL: %.2f RON", order.getTotal()));
        return sb.toString();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadOrderHistoryAsync();
    }

    private void loadOrderHistory() {
        loadOrderHistoryAsync();
    }

    private void updateTotal() {
        if (totalLabel == null) return;
        OfertaService.Receipt receipt = staffOrderService.calculateReceipt(new ArrayList<>(cart));
        totalLabel.setText(String.format("Total: %.2f RON", receipt.getTotal()));
        if (receiptDetailsLabel != null) {
            receiptDetailsLabel.setText(buildReceiptText(receipt));
        }
        refreshCartDisplay();
    }

    private void refreshCartDisplay() {
        cartDisplay.setAll(cart);

        OfertaService.Receipt receipt = staffOrderService.calculateReceipt(new ArrayList<>(cart));
        for (OfertaService.DiscountLine d : receipt.getDiscounts()) {
            cartDisplay.add(new DiscountCartLine(d.getLabel(), d.getAmount()));
        }
    }
}
