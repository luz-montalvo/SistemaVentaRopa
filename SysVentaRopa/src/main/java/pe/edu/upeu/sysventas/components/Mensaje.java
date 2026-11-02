package pe.edu.upeu.sysventas.components;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Mensajes cortos (Toast) y confirmaciones para todas las pantallas. */
public final class Mensaje {

    private Mensaje() {
    }

    public static void exito(Node nodo, String texto) {
        mostrar(nodo, texto, "#bbf7d0");
    }

    public static void error(Node nodo, String texto) {
        mostrar(nodo, texto, "#fecaca");
    }

    private static void mostrar(Node nodo, String texto, String color) {
        if (nodo == null || nodo.getScene() == null) return;
        Window w = nodo.getScene().getWindow();
        if (!(w instanceof Stage stage)) return;
        double x = stage.getX() + stage.getWidth() / 2 - 160;
        double y = stage.getY() + stage.getHeight() - 140;
        Toast.showToast(stage, texto, color, 2500, x, y);
    }

    public static boolean confirmar(String titulo, String texto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static void info(String titulo, String texto) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, texto);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
