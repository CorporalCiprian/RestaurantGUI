package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import org.example.json.LegacyProdusListReader;
import org.example.json.ProdusJsonDto;
import org.example.json.ProdusJsonMapper;
import org.example.persistence.*;

import java.io.File;
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

    private final ObjectMapper mapper = new ObjectMapper();
    private final ProdusRepository produsRepository = new ProdusRepository();

    public static void main(String[] args) {
        // Launch JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // DB is the source of truth. If empty, seed it with the sample data.
        try {
            new DatabaseSeeder(produsRepository).seedIfEmpty();
            sampleList.clear();
            produsRepository.findAll().forEach(e -> sampleList.add(ProdusMapper.toDomain(e)));
        } catch (Exception ex) {
            // Fallback if DB isn't available: still allow starting the GUI with local sample data.
            sampleList.clear();
            sampleList.addAll(SampleDataFactory.createSampleProducts());
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // --- Menu bar (File: Export JSON / Import JSON / Reload from DB / Save to DB / Exit) ---
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem miExport = new MenuItem("Export JSON");
        MenuItem miImport = new MenuItem("Import JSON");
        MenuItem miReloadDb = new MenuItem("Reload from DB");
        MenuItem miSaveDb = new MenuItem("Save to DB");
        MenuItem miExit = new MenuItem("Exit");
        fileMenu.getItems().addAll(miExport, miImport, new SeparatorMenuItem(), miReloadDb, miSaveDb, new SeparatorMenuItem(), miExit);
        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);

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
                                    // keep pending as null
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
                    String toppingsText = (p.getToppings() == null || p.getToppings().isEmpty())
                            ? "fara topping"
                            : p.getToppings().stream().map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("fara topping");
                    lblExtra.setText("Blat: " + p.getBlat() + ", Sos: " + p.getSos() + ", Topping: " + toppingsText);
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

                // Instead of bindBidirectional, attach a safe listener
                ChangeListener<Double> valListener = (ob, oldV, newV) -> {
                    try {
                        if (newV == null) return;
                        if (newV < 0) {
                            Platform.runLater(() -> formatter.setValue(oldV));
                            lblStatus.setText("Pretul trebuie sa fie >= 0");
                        } else {
                            lblStatus.setText("");
                            newP.pretProperty().set(newV);
                        }
                    } catch (Exception ex) {
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

        // --- Menu actions ---
        miExit.setOnAction(e -> {
            primaryStage.close();
            Platform.exit();
        });

        miReloadDb.setOnAction(e -> {
            try {
                List<Produs> loaded = new ArrayList<>();
                produsRepository.findAll().forEach(pe -> loaded.add(ProdusMapper.toDomain(pe)));
                items.setAll(loaded);
                if (!items.isEmpty()) listView.getSelectionModel().select(0);
                showInfo("Reload from DB", "Meniul a fost incarcat din baza de date.");
            } catch (Exception ex) {
                showError("Reload from DB", "Nu am putut incarca din DB: " + ex.getMessage());
            }
        });

        miSaveDb.setOnAction(e -> {
            try {
                // Keep stable IDs: update existing rows (merge) instead of delete+insert.
                List<ProdusEntity> ents = items.stream().map(p -> {
                    String tip = (p instanceof Pizza) ? "PIZZA" : (p instanceof Mancare) ? "MANCARE" : (p instanceof Bautura) ? "BAUTURA" : "";
                    ProdusEntity existing = tip.isBlank() ? null : produsRepository.findByTypeAndName(tip, p.getNume()).orElse(null);
                    return ProdusMapper.toEntityWithIdIfPresent(p, existing);
                }).toList();

                produsRepository.saveAll(ents);
                showInfo("Save to DB", "Modificarile au fost salvate in baza de date (ID-urile au ramas stabile).");
            } catch (Exception ex) {
                showError("Save to DB", "Nu am putut salva in DB: " + ex.getMessage());
            }
        });

        miExport.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export JSON");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            chooser.setInitialFileName("meniu-export.json");
            File f = chooser.showSaveDialog(primaryStage);
            if (f == null) return;
            try {
                // Export should reflect what's currently in DB (source of truth)
                List<Produs> fromDb = new ArrayList<>();
                produsRepository.findAll().forEach(pe -> fromDb.add(ProdusMapper.toDomain(pe)));

                List<ProdusJsonDto> dtos = fromDb.stream().map(ProdusJsonMapper::toDto).toList();
                mapper.writerWithDefaultPrettyPrinter().writeValue(f, dtos);
                showInfo("Export JSON", "Export reusit: " + f.getAbsolutePath());
            } catch (Exception ex) {
                showError("Export JSON", "Eroare la export: " + ex.getMessage());
            }
        });

        miImport.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Import JSON");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File f = chooser.showOpenDialog(primaryStage);
            if (f == null) return;

            try {
                // Backward compatible: supports new format (with "tip") and old files (without it)
                List<ProdusJsonDto> importedDtos = LegacyProdusListReader.readDtos(mapper, f);

                List<Produs> imported = importedDtos.stream().map(ProdusJsonMapper::toDomain).toList();

                // Import requirement: add products in DB (upsert without duplicates), then refresh UI.
                // Duplicate key = (discriminator tip + nume). If found, reuse ID so merge() performs UPDATE.
                List<ProdusEntity> ents = imported.stream().map(p -> {
                    String tip = (p instanceof Pizza) ? "PIZZA" : (p instanceof Mancare) ? "MANCARE" : (p instanceof Bautura) ? "BAUTURA" : "";
                    ProdusEntity existing = tip.isBlank() ? null : produsRepository.findByTypeAndName(tip, p.getNume()).orElse(null);
                    return ProdusMapper.toEntityWithIdIfPresent(p, existing);
                }).toList();

                produsRepository.saveAll(ents);

                List<Produs> loaded = new ArrayList<>();
                produsRepository.findAll().forEach(pe -> loaded.add(ProdusMapper.toDomain(pe)));
                items.setAll(loaded);
                if (!items.isEmpty()) listView.getSelectionModel().select(0);
                showInfo("Import JSON", "Import reusit: " + imported.size() + " produse (upsert in DB, fara duplicate pe tip+nume).");
            } catch (Exception ex) {
                showError("Import JSON", "Eroare la import (fisier invalid sau DB indisponibil): " + ex.getMessage());
            }
        });

        // select first if available
        if (!items.isEmpty()) listView.getSelectionModel().select(0);

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Meniu Restaurant - GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // ensure JPA resources are released
        try {
            JpaUtil.shutdown();
        } catch (Exception ignored) {
        }
    }

    private static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private static void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
