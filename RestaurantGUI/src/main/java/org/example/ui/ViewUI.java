package org.example.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Produs;

public final class ViewUI {

    private ViewUI() {
    }

    public static void show(Stage stage, ObservableList<Produs> produse) {
        if (stage == null) throw new IllegalArgumentException("stage");
        if (produse == null) produse = FXCollections.observableArrayList();

        ListView<Produs> listView = new ListView<>(produse);
        listView.setPrefWidth(260);

        Label title = new Label("Detalii produs");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label nameLabel = new Label();
        Label typeLabel = new Label();

        TextField priceField = new TextField();
        priceField.setPromptText("Pret");

        Label extraLabel = new Label();
        extraLabel.setWrapText(true);

        VBox form = new VBox(8,
                title,
                nameLabel,
                typeLabel,
                new HBox(8, new Label("Pret:"), priceField),
                extraLabel
        );
        form.setPadding(new Insets(12));
        form.setAlignment(Pos.TOP_LEFT);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                nameLabel.setText("");
                typeLabel.setText("");
                priceField.setText("");
                extraLabel.setText("");
                return;
            }

            nameLabel.setText("Nume: " + selected.getNume());
            typeLabel.setText("Categorie: " + selected.getClass().getSimpleName());
            priceField.setText(String.valueOf(selected.getPret()));
            extraLabel.setText(buildExtra(selected));
        });

        priceField.disableProperty().bind(Bindings.isNull(listView.getSelectionModel().selectedItemProperty()));

        priceField.textProperty().addListener((obs, old, val) -> {
            Produs selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            if (val == null || val.isBlank()) return;

            try {
                double newPrice = Double.parseDouble(val.trim());
                selected.setPret(newPrice);
                listView.refresh();
            } catch (NumberFormatException ignored) {
            } catch (RuntimeException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Pret invalid");
                a.setHeaderText(null);
                a.setContentText(String.valueOf(ex.getMessage()));
                a.showAndWait();
                priceField.setText(String.valueOf(selected.getPret()));
            }
        });

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setLeft(listView);
        root.setCenter(form);

        stage.setTitle("Restaurant - Menu Editor");
        stage.setScene(new Scene(root, 900, 520));
        stage.show();

        if (!produse.isEmpty()) {
            listView.getSelectionModel().select(0);
        }
    }

    private static String buildExtra(Produs p) {
        if (p == null) return "";
        if (p instanceof org.example.Mancare m) {
            return "Gramaj: " + m.getGramaj() + " g";
        }
        if (p instanceof org.example.Bautura b) {
            return "Volum: " + b.getVolum() + " ml";
        }
        if (p instanceof org.example.Pizza pizza) {
            return "Blat: " + pizza.getBlat() + ", Sos: " + pizza.getSos();
        }
        return "";
    }
}

