package pe.edu.upeu.sysventas.components;

import javafx.scene.Scene;

import java.util.List;
import java.util.prefs.Preferences;

/** Aplica el estilo (CSS) elegido por el usuario a cualquier ventana. */
public final class Tema {

    public static final List<String> TEMAS = List.of("Claro", "Oscuro", "Rosa");
    private static final Preferences PREFS = Preferences.userRoot().node("sysventaropa");

    private Tema() {
    }

    public static String actual() {
        return PREFS.get("tema", "Claro");
    }

    public static void guardar(String tema) {
        PREFS.put("tema", tema);
    }

    public static void aplicar(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Tema.class.getResource("/css/style.css").toExternalForm());
        switch (actual()) {
            case "Oscuro" -> scene.getStylesheets().add(Tema.class.getResource("/css/tema-oscuro.css").toExternalForm());
            case "Rosa" -> scene.getStylesheets().add(Tema.class.getResource("/css/tema-rosa.css").toExternalForm());
            default -> { }
        }
    }
}
