package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.ArrayList;
import java.util.List;

public class GuiApp extends Application {
    private final List<Produs> sampleList = new ArrayList<>();
    // track which product is currently bound to the price text field
    private Produs boundPretProd = null;
    // current TextFormatter used on txtPret (so we can unbind it on selection change)
    private TextFormatter<Double> currentFormatter = null;
    // listener attached to the current formatter's valueProperty
    private ChangeListener<Double> currentFormatterListener = null;

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
        // Reactive cell: listen to product properties so cell text updates automatically
        listView.setCellFactory(lv -> new ListCell<>() {
            private Produs observed = null;
            private final ChangeListener<Number> priceListener = (obs, oldV, newV) -> updateText();
            private final ChangeListener<String> nameListener = (obs, oldV, newV) -> updateText();

            private void updateText() {
                Produs item = getItem();
                try {
                    if (item == null) setText(null);
                    else setText(item.getNume() + " - " + String.format("%.1f", item.getPret()) + " RON");
                } catch (Exception ex) {
                    // defensive: if anything unexpected happens, show a fallback and avoid crashing the UI thread
                    setText(item == null ? null : item.getNume());
                }
            }

            @Override
            protected void updateItem(Produs item, boolean empty) {
                // remove listeners from previous observed item
                if (observed != null) {
                    observed.pretProperty().removeListener(priceListener);
                    observed.numeProperty().removeListener(nameListener);
                }

                super.updateItem(item, empty);

                observed = item;
                if (empty || item == null) {
                    setText(null);
                } else {
                    // listen to changes so cell updates live
                    item.pretProperty().addListener(priceListener);
                    item.numeProperty().addListener(nameListener);
                    updateText();
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

        // Bind UI to selected item properties
        ReadOnlyObjectProperty<Produs> selected = listView.getSelectionModel().selectedItemProperty();

        // Bind name label to selected.numeProperty (or empty when null)
        lblNume.textProperty().bind(Bindings.createStringBinding(() -> {
            Produs p = selected.get();
            return p == null ? "" : p.getNume();
        }, selected));

        // Vegetarian checkbox reflect property
        selected.addListener((obs, oldP, newP) -> {
            if (oldP != null) {
                chkVeg.selectedProperty().unbind();
            }

            // Unbind any previous formatter/binding of the price field
            if (boundPretProd != null) {
                if (currentFormatter != null) {
                    // Commit any pending edit to the model before removing formatter
                    try {
                        Double pending = currentFormatter.getValue();
                        if (pending == null) {
                            String txt = txtPret.getText();
                            if (txt != null && !txt.isBlank()) {
                                // tolerant parsing: trim, remove trailing dot/comma if present
                                String s = txt.trim();
                                if (s.endsWith(".") || s.endsWith(",")) s = s.substring(0, s.length() - 1);
                                s = s.replace(',', '.');
                                try {
                                    pending = Double.parseDouble(s);
                                } catch (Exception ex) {
                                    pending = null;
                                }
                            }
                        }

                        if (pending != null) {
                            if (pending < 0) {
                                lblStatus.setText("Pretul trebuie sa fie >= 0");
                                // revert UI to previous value
                                txtPret.setText(String.format("%.1f", boundPretProd.getPret()));
                            } else {
                                // apply
                                boundPretProd.pretProperty().set(pending);
                                lblStatus.setText("");
                                // ensure list shows updated value immediately
                                listView.refresh();
                            }
                        } else {
                            // no valid pending value -> revert the text field to the product's current price
                            txtPret.setText(String.format("%.1f", boundPretProd.getPret()));
                        }
                    } catch (Exception ex) {
                        // ignore commit errors but don't lose app stability
                        lblStatus.setText("Eroare la aplicarea pretului");
                        txtPret.setText(String.format("%.1f", boundPretProd.getPret()));
                    }

                    // remove listener and formatter
                    if (currentFormatterListener != null) currentFormatter.valueProperty().removeListener(currentFormatterListener);
                    txtPret.setText("");
                    txtPret.setTextFormatter(null);
                    currentFormatter = null;
                    currentFormatterListener = null;
                }
                boundPretProd = null;
            }

            if (newP == null) {
                chkVeg.setSelected(false);
                txtPret.setDisable(true);
                lblTip.setText("");
                lblExtra.setText("");
                txtPret.setText("");
                lblStatus.setText("");
            } else {
                chkVeg.selectedProperty().bind(newP.vegetarianProperty());
                txtPret.setDisable(false);

                // set tip/extra labels based on runtime type
                if (newP instanceof Pizza p) {
                    lblTip.setText("Pizza");
                    lblExtra.setText("Blat: " + p.getBlat() + ", Sos: " + p.getSos());
                } else if (newP instanceof Mancare m) {
                    lblTip.setText("Mancare");
                    lblExtra.setText("Gramaj: " + m.getGramaj() + " g");
                } else if (newP instanceof Bautura b) {
                    lblTip.setText("Bautura");
                    lblExtra.setText("Volum: " + b.getVolum() + " ml");
                } else {
                    lblTip.setText(newP.getClass().getSimpleName());
                    lblExtra.setText("");
                }

                // Create a TextFormatter that validates numeric input and rejects negative numbers
                TextFormatter<Double> formatter = new TextFormatter<>(new DoubleStringConverter(), newP.getPret(), change -> {
                    String newText = change.getControlNewText();
                    if (newText == null || newText.isEmpty()) {
                        // allow empty (user might be typing)
                        return change;
                    }
                    // Allow a single '-' while typing (so user can type negative sign but we won't accept negative value on commit)
                    if (newText.equals("-")) {
                        return change;
                    }
                    try {
                        double v = Double.parseDouble(newText.replace(',', '.'));
                        // reject negative numbers
                        if (v < 0) {
                            lblStatus.setText("Pretul trebuie sa fie >= 0");
                            return null;
                        }
                        lblStatus.setText("");
                        return change;
                    } catch (NumberFormatException ex) {
                        // reject invalid numeric input
                        return null;
                    }
                });

                txtPret.setTextFormatter(formatter);

                // force the control to show the product price immediately
                formatter.setValue(newP.getPret());
                txtPret.setText(String.format("%.1f", newP.getPret()));

                // Instead of bindBidirectional, attach a safe listener: when formatter.valueProperty() yields a non-null valid number,
                // update the product's pretProperty(). This avoids null-related exceptions and gives us control.
                ChangeListener<Double> valListener = (ob, oldV, newV) -> {
                    try {
                        if (newV == null) {
                            // don't update model when value is temporarily empty while editing
                            return;
                        }
                        // newV already validated by the filter: ensure non-negative
                        if (newV < 0) {
                            // revert to old value in UI
                            Platform.runLater(() -> formatter.setValue(oldV));
                            lblStatus.setText("Pretul trebuie sa fie >= 0");
                        } else {
                            lblStatus.setText("");
                            // update model
                            newP.pretProperty().set(newV);
                        }
                    } catch (Exception ex) {
                        // On unexpected error revert and show status instead of crashing
                        Platform.runLater(() -> formatter.setValue(oldV));
                        lblStatus.setText("Eroare la citirea valorii introduse");
                    }
                };

                formatter.valueProperty().addListener(valListener);
                currentFormatter = formatter;
                currentFormatterListener = valListener;
                boundPretProd = newP;
            }
        });

        // select first if available
        if (!items.isEmpty()) listView.getSelectionModel().select(0);

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Meniu Restaurant - GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
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
