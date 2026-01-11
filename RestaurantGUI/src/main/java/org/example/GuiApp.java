package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.auth.Role;
import org.example.persistence.DatabaseSeeder;
import org.example.persistence.JpaUtil;
import org.example.persistence.ProdusRepository;
import org.example.service.AuthService;
import org.example.service.MenuQueryService;
import org.example.ui.ViewUI;
import org.example.ui.controllers.LoginController;
import org.example.ui.util.AppExecutors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GuiApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new DatabaseSeeder(new ProdusRepository()).seedIfEmpty();

        if (!getParameters().getRaw().isEmpty() && "menuEditor".equalsIgnoreCase(getParameters().getRaw().getFirst())) {
            ObservableList<Produs> produse = FXCollections.observableArrayList(new MenuQueryService().getAllProducts());
            ViewUI.show(primaryStage, produse);
            return;
        }

        AuthService authService = new AuthService();
        authService.register("manager", "manager", Role.MANAGER);
        authService.register("staff", "staff", Role.STAFF);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/views/login.fxml"));
        Parent root = loader.load();

        try {
            LoginController controller = loader.getController();
            if (controller != null) {
                controller.setAuthService(authService);
            }
        } catch (Exception ignored) {
        }

        primaryStage.setTitle("Restaurant");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        try {
            JpaUtil.shutdown();
        } catch (Exception ignored) {
        }

        try {
            AppExecutors.shutdownBestEffort();
        } catch (Exception ignored) {
        }
    }
}
