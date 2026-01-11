module org.example.restaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;

    exports org.example;
    exports org.example.persistence;
    exports org.example.json;
    exports org.example.model;
    exports org.example.auth;
    exports org.example.service;
    exports org.example.ui.util;

    opens org.example to javafx.graphics, javafx.fxml, com.fasterxml.jackson.databind;
    opens org.example.ui.controllers to javafx.fxml;
    opens org.example.ui.util to javafx.fxml;

    opens org.example.persistence to org.hibernate.orm.core, com.fasterxml.jackson.databind;
    opens org.example.model to org.hibernate.orm.core;

    opens org.example.json to com.fasterxml.jackson.databind;
}
