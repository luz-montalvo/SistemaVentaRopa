package pe.edu.upeu.sysventas.components;

import javafx.application.Platform;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Valida un formulario: marca en rojo los campos con error (con tooltip)
 * y muestra el primer mensaje en un Label.
 */
public class FormValidator {

    private final ToltipCustom ttc = new ToltipCustom();
    private final Map<Control, String> errores = new LinkedHashMap<>();

    /** Si la condición es verdadera, el campo queda con error. */
    public FormValidator check(boolean hayError, Control campo, String mensaje) {
        if (hayError && !errores.containsKey(campo)) {
            errores.put(campo, mensaje);
        }
        return this;
    }

    /** Pinta los errores y devuelve true si el formulario está correcto. */
    public boolean esValido(Label lblMensaje) {
        if (errores.isEmpty()) {
            if (lblMensaje != null) lblMensaje.setText("");
            return true;
        }
        Control primero = null;
        for (Map.Entry<Control, String> e : errores.entrySet()) {
            ttc.marcarError(e.getKey(), e.getValue());
            if (primero == null) primero = e.getKey();
        }
        if (lblMensaje != null) lblMensaje.setText(errores.values().iterator().next());
        Control foco = primero;
        Platform.runLater(foco::requestFocus);
        return false;
    }

    public void limpiar(Control... campos) {
        errores.clear();
        for (Control c : campos) ttc.limpiarCampo(c);
    }

    // ---------- utilidades para leer números de los TextField ----------

    public static Double leerDouble(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer leerEntero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public static String textoONull(String texto) {
        return vacio(texto) ? null : texto.trim();
    }
}
