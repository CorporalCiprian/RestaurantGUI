package org.example.ui.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.Pizza;
import org.example.persistence.BauturaEntity;
import org.example.persistence.MancareEntity;
import org.example.persistence.PizzaEntity;
import org.example.persistence.ProdusEntity;

import java.util.LinkedHashSet;
import java.util.Optional;

/** Simple dialog for creating/editing a product entity. */
public final class ProductDialog {

    private ProductDialog() {
    }

    public enum ProductType {
        PIZZA,
        MANCARE,
        BAUTURA
    }

    public static Optional<ProdusEntity> showCreateDialog() {
        return showDialog("Add Product", null);
    }

    public static Optional<ProdusEntity> showEditDialog(ProdusEntity existing) {
        return showDialog("Edit Product", existing);
    }

    private static Optional<ProdusEntity> showDialog(String title, ProdusEntity existing) {
        Dialog<ProdusEntity> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setResizable(true);
        dialog.getDialogPane().setMinWidth(520);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<ProductType> typeBox = new ComboBox<>(FXCollections.observableArrayList(ProductType.values()));
        typeBox.setPrefWidth(340);

        TextField nameField = new TextField();
        nameField.setPrefColumnCount(28);
        nameField.setPromptText("Ex: Margherita");

        TextField priceField = new TextField();
        priceField.setPrefColumnCount(12);
        priceField.setPromptText("Ex: 29.99");

        CheckBox vegBox = new CheckBox("Vegetarian");

        CheckBox dessertBox = new CheckBox("Dessert");

        // Type-specific
        TextField gramajField = new TextField();
        gramajField.setPrefColumnCount(12);
        gramajField.setPromptText("Ex: 250");

        TextField volumField = new TextField();
        volumField.setPrefColumnCount(12);
        volumField.setPromptText("Ex: 330");

        ComboBox<Pizza.TipBlat> blatBox = new ComboBox<>(FXCollections.observableArrayList(Pizza.TipBlat.values()));
        blatBox.setPrefWidth(340);

        ComboBox<Pizza.TipSos> sosBox = new ComboBox<>(FXCollections.observableArrayList(Pizza.TipSos.values()));
        sosBox.setPrefWidth(340);

        TextField toppingsField = new TextField(); // comma separated enum names
        toppingsField.setPrefColumnCount(28);
        toppingsField.setPromptText("Ex: CIUPERCI,SALAM,EXTRA_BRANZA");

        // Determine initial type
        if (existing instanceof PizzaEntity) typeBox.setValue(ProductType.PIZZA);
        else if (existing instanceof MancareEntity) typeBox.setValue(ProductType.MANCARE);
        else if (existing instanceof BauturaEntity) typeBox.setValue(ProductType.BAUTURA);
        else typeBox.setValue(ProductType.MANCARE);

        if (existing != null) {
            nameField.setText(existing.getNume());
            priceField.setText(Double.toString(existing.getPret()));
            vegBox.setSelected(existing.isVegetarian());
            if (existing instanceof MancareEntity m) {
                gramajField.setText(m.getGramaj() == null ? "" : m.getGramaj().toString());
                dessertBox.setSelected(m.isDesert());
            }
            if (existing instanceof BauturaEntity b) {
                volumField.setText(b.getVolum() == null ? "" : b.getVolum().toString());
            }
            if (existing instanceof PizzaEntity p) {
                blatBox.setValue(p.getBlat());
                sosBox.setValue(p.getSos());
                if (p.getToppings() != null && !p.getToppings().isEmpty()) {
                    toppingsField.setText(p.getToppings().stream().map(Enum::name).reduce((a, c) -> a + "," + c).orElse(""));
                }
            }
        } else {
            blatBox.setValue(Pizza.TipBlat.CLASIC);
            sosBox.setValue(Pizza.TipSos.ROSU);
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(14));

        // Let the second column grow, so controls expand and stay visible.
        javafx.scene.layout.ColumnConstraints c0 = new javafx.scene.layout.ColumnConstraints();
        c0.setMinWidth(110);
        javafx.scene.layout.ColumnConstraints c1 = new javafx.scene.layout.ColumnConstraints();
        c1.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        int r = 0;
        Label typeLabel = new Label("Type");
        grid.add(typeLabel, 0, r);
        grid.add(typeBox, 1, r++);

        Label nameLabel = new Label("Name");
        grid.add(nameLabel, 0, r);
        grid.add(nameField, 1, r++);

        Label priceLabel = new Label("Price");
        grid.add(priceLabel, 0, r);
        grid.add(priceField, 1, r++);

        grid.add(vegBox, 1, r++);
        grid.add(dessertBox, 1, r++);

        Label gramajLabel = new Label("Gramaj (g)");
        grid.add(gramajLabel, 0, r);
        grid.add(gramajField, 1, r++);

        Label volumLabel = new Label("Volum (ml)");
        grid.add(volumLabel, 0, r);
        grid.add(volumField, 1, r++);

        Label blatLabel = new Label("Blat");
        grid.add(blatLabel, 0, r);
        grid.add(blatBox, 1, r++);

        Label sosLabel = new Label("Sos");
        grid.add(sosLabel, 0, r);
        grid.add(sosBox, 1, r++);

        Label toppingsLabel = new Label("Toppings (comma)");
        grid.add(toppingsLabel, 0, r);
        grid.add(toppingsField, 1, r++);

        dialog.getDialogPane().setContent(grid);

        // Ensure content isn't clipped; allow scrolling on small screens.
        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setPrefViewportHeight(420);
        dialog.getDialogPane().setContent(sp);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        Runnable refreshVisibility = () -> {
            ProductType type = typeBox.getValue();
            boolean isMancare = type == ProductType.MANCARE;
            boolean isBautura = type == ProductType.BAUTURA;
            boolean isPizza = type == ProductType.PIZZA;

            setRowVisible(gramajLabel, gramajField, isMancare);
            setRowVisible(volumLabel, volumField, isBautura);
            setRowVisible(blatLabel, blatBox, isPizza);
            setRowVisible(sosLabel, sosBox, isPizza);
            setRowVisible(toppingsLabel, toppingsField, isPizza);

            dessertBox.setVisible(isMancare);
            dessertBox.setManaged(isMancare);
            if (!isMancare) dessertBox.setSelected(false);

            // lighter UX: clear irrelevant fields when switching type
            if (!isMancare) gramajField.clear();
            if (!isBautura) volumField.clear();
            if (!isPizza) toppingsField.clear();

            // allow OK only if base fields valid
            okButton.setDisable(nameField.getText() == null || nameField.getText().trim().isEmpty()
                    || priceField.getText() == null || priceField.getText().trim().isEmpty());
        };

        typeBox.valueProperty().addListener((obs, old, val) -> refreshVisibility.run());
        nameField.textProperty().addListener((obs, old, val) -> refreshVisibility.run());
        priceField.textProperty().addListener((obs, old, val) -> refreshVisibility.run());

        // initial state
        refreshVisibility.run();

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (name.isBlank()) {
                showError("Name is required");
                return null;
            }

            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
            } catch (Exception e) {
                showError("Invalid price");
                return null;
            }
            if (price < 0) {
                showError("Price must be >= 0");
                return null;
            }

            boolean veg = vegBox.isSelected();
            boolean isDessert = dessertBox.isSelected();

            ProdusEntity result;
            ProductType type = typeBox.getValue();
            if (type == ProductType.PIZZA) {
                Pizza.TipBlat blat = blatBox.getValue() == null ? Pizza.TipBlat.CLASIC : blatBox.getValue();
                Pizza.TipSos sos = sosBox.getValue() == null ? Pizza.TipSos.ROSU : sosBox.getValue();

                LinkedHashSet<Pizza.Topping> tops = new LinkedHashSet<>();
                String topsText = toppingsField.getText();
                if (topsText != null && !topsText.isBlank()) {
                    for (String part : topsText.split(",")) {
                        String token = part.trim();
                        if (token.isEmpty()) continue;
                        try {
                            tops.add(Pizza.Topping.valueOf(token.toUpperCase()));
                        } catch (Exception ignored) {
                            // ignore unknown
                        }
                    }
                }

                result = new PizzaEntity(name, price, veg, blat, sos, tops);
            } else if (type == ProductType.BAUTURA) {
                int volum;
                try {
                    volum = Integer.parseInt(volumField.getText().trim());
                } catch (Exception e) {
                    volum = 1;
                }
                result = new BauturaEntity(name, price, veg, volum);
            } else {
                int gramaj;
                try {
                    gramaj = Integer.parseInt(gramajField.getText().trim());
                } catch (Exception e) {
                    gramaj = 1;
                }
                result = new MancareEntity(name, price, veg, gramaj, isDessert);
            }

            // preserve identity on edit
            if (existing != null && existing.getId() != null) {
                result.setId(existing.getId());
            }

            return result;
        });

        return dialog.showAndWait();
    }

    private static void setRowVisible(Label label, Control control, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        control.setVisible(visible);
        control.setManaged(visible);
    }

    private static void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Invalid input");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
