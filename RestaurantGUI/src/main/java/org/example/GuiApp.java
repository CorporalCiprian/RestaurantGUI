package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GuiApp extends Application {
    private final List<Produs> sampleList = new ArrayList<>();
    // currently selected product (for commit logic)
    private Produs currentSelected = null;

    public static void main(String[] args) {
        // Launch JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        buildSampleData();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        ListView<Produs> listView = new ListView<>();
        javafx.collections.ObservableList<Produs> items = javafx.collections.FXCollections.observableArrayList(sampleList);
        listView.setItems(items);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Produs item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        root.setLeft(listView);
        BorderPane.setMargin(listView, new Insets(0, 10, 0, 0));
        listView.setPrefWidth(300);

        // Details pane (center)
        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(8);
        details.setPadding(new Insets(5));

        Label lblNume = new Label();
        Label lblTip = new Label();
        Label lblExtra = new Label();
        CheckBox chkVeg = new CheckBox("Vegetarian");
        chkVeg.setDisable(true);

        TextField txtPret = new TextField();
        txtPret.setPrefColumnCount(8);

        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-text-fill: red;");

        details.add(new Label("Nume:"), 0, 0);
        details.add(lblNume, 1, 0);
        details.add(new Label("Tip:"), 0, 1);
        details.add(lblTip, 1, 1);
        details.add(new Label("Detalii:"), 0, 2);
        details.add(lblExtra, 1, 2);
        details.add(new Label("Vegetarian:"), 0, 3);
        details.add(chkVeg, 1, 3);
        details.add(new Label("Pret (RON):"), 0, 4);
        details.add(txtPret, 1, 4);
        details.add(lblStatus, 0, 5, 2, 1);

        // Make center scrollable if needed
        VBox centerBox = new VBox(details);
        VBox.setVgrow(details, Priority.ALWAYS);
        root.setCenter(centerBox);

        // Commit helper: commit current txtPret value into currentSelected
        Runnable commitRunnable = () -> commitPrice(listView, txtPret, lblStatus);

        // Set listeners on txtPret once (avoid adding multiple listeners on selection change)
        txtPret.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                commitRunnable.run();
            }
        });
        txtPret.focusedProperty().addListener((o, was, isNow) -> {
            if (!isNow) commitRunnable.run();
        });

        // Selection handling
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            // Before changing selection, commit any pending edit for the previously selected product
            commitRunnable.run();
            lblStatus.setText("");
            if (sel == null) {
                currentSelected = null;
                lblNume.setText("");
                lblTip.setText("");
                lblExtra.setText("");
                chkVeg.setSelected(false);
                txtPret.setText("");
                txtPret.setDisable(true);
                return;
            }
            // Set currentSelected to the newly selected product
            currentSelected = sel;
            lblNume.setText(sel.getNume());
            chkVeg.setSelected(sel.isVegetarian());
            txtPret.setDisable(false);

            if (sel instanceof Pizza p) {
                lblTip.setText("Pizza");
                lblExtra.setText("Blat: " + p.getBlat() + ", Sos: " + p.getSos());
            } else if (sel instanceof Mancare m) {
                lblTip.setText("Mancare");
                lblExtra.setText("Gramaj: " + m.getGramaj() + " g");
            } else if (sel instanceof Bautura b) {
                lblTip.setText("Bautura");
                lblExtra.setText("Volum: " + b.getVolum() + " ml");
            } else {
                lblTip.setText(sel.getClass().getSimpleName());
                lblExtra.setText("");
            }

            // Initialize text with the selected product's price
            txtPret.setText(String.format("%.1f", sel.getPret()));
        });

        // select first if available
        if (!items.isEmpty()) listView.getSelectionModel().select(0);

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Meniu Restaurant - GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Parse and commit the value currently in txtPret into the currently selected product
    private void commitPrice(ListView<Produs> listView, TextField txtPret, Label lblStatus) {
        if (currentSelected == null) return;
        String txt = txtPret.getText().trim();
        if (txt.isEmpty()) {
            lblStatus.setText("Pret invalid");
            return;
        }
        try {
            double val = Double.parseDouble(txt.replace(',', '.'));
            if (val < 0) {
                lblStatus.setText("Pretul trebuie sa fie >= 0");
                return;
            }
            currentSelected.setPret(val);
            listView.refresh();
            lblStatus.setText("Pret actualizat");
        } catch (NumberFormatException ex) {
            lblStatus.setText("Pret invalid: introduceti o valoare numerica");
        }
    }

    private void buildSampleData() {
        Pizza pizza = new Pizza.Builder(Pizza.TipBlat.CLASIC, Pizza.TipSos.ROSU)
                .nume("Pizza Margherita")
                .vegetarian(true)
                .baza(45.0)
                .build();
        Mancare paste = new Mancare("Paste Carbonara", 52.5, 400, false);
        Mancare supaVeg = new Mancare("Supa Veg", 24.0, 300, true);
        Bautura limonada = new Bautura("Limonada", 15.0, 400, true);
        Bautura apa = new Bautura("Apa Plata", 8.0, 500, true);
        Bautura lichiorOua = new Bautura("Lichior de Oua", 22.0, 50, false);

        Mancare bruschette = new Mancare("Bruschette cu rosii", 18.0, 120, true);
        Mancare salataCaprese = new Mancare("Salata Caprese", 22.0, 150, true);
        Mancare chiftelute = new Mancare("Chiftelute de pui", 20.0, 100, false);

        Mancare tiramisu = new Mancare("Tiramisu", 25.0, 120, true);
        Mancare papanasi = new Mancare("Papanasi cu smantana si gem", 28.0, 200, true);
        Mancare inghetata = new Mancare("Inghetata asortata", 15.0, 100, true);

        sampleList.add(pizza);
        sampleList.add(paste);
        sampleList.add(supaVeg);
        sampleList.add(limonada);
        sampleList.add(apa);
        sampleList.add(lichiorOua);
        sampleList.add(bruschette);
        sampleList.add(salataCaprese);
        sampleList.add(chiftelute);
        sampleList.add(tiramisu);
        sampleList.add(papanasi);
        sampleList.add(inghetata);
    }
}
