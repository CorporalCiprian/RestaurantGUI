module org.example.restaurant {
    // declarăm dependența pe JavaFX Controls (conține Scene, Controls, etc.)
    requires javafx.controls;
    // javafx.graphics este folosit intern de JavaFX; adăugăm explicit pentru claritate
    requires javafx.graphics;

    // Jackson (JSON) - declarăm dependențele pentru ca sistemul de module să poată accesa aceste biblioteci
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    // Exportăm pachetul ca să poată fi accesat (opțional dar util pentru unele scenarii)
    exports org.example;

    // JavaFX folosește reflexie pentru a apela metoda start; deschidem pachetul către modulele JavaFX
    // și, de asemenea, către Jackson pentru acces reflexiv
    opens org.example to javafx.graphics, javafx.fxml, com.fasterxml.jackson.databind;
}
