package pe.edu.upeu.sysventas.components;

import javafx.stage.Stage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


public class StageManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

}

