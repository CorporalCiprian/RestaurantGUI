module org.example.restaurant {
    // declarăm dependența pe JavaFX Controls (conține Scene, Controls, etc.)
    requires javafx.controls;
    // javafx.graphics este folosit intern de JavaFX; adăugăm explicit pentru claritate
    requires javafx.graphics;

    // Jackson (JSON) - declarăm dependențele pentru ca sistemul de module să poată accesa aceste biblioteci
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    // JPA / Hibernate / JDBC (runtime required)
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;

    // Exportăm pachetul ca să poată fi accesat (opțional dar util pentru unele scenarii)
    exports org.example;
    exports org.example.persistence;
    exports org.example.json;

    // JavaFX folosește reflexie pentru a apela metoda start; deschidem pachetul către modulele JavaFX
    // și, de asemenea, către Jackson pentru acces reflexiv
    opens org.example to javafx.graphics, javafx.fxml, com.fasterxml.jackson.databind;

    // Hibernate uses reflection on entities
    opens org.example.persistence to org.hibernate.orm.core, com.fasterxml.jackson.databind;

    // Jackson needs reflective access to DTOs
    opens org.example.json to com.fasterxml.jackson.databind;
}
