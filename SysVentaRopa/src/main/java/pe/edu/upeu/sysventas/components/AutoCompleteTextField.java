package pe.edu.upeu.sysventas.components;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Consumer;

/** Muestra sugerencias debajo de un TextField mientras se escribe. */
public class AutoCompleteTextField<T> {

    private final TextField autoCompleteTextField;
    private final SortedSet<T> entries;
    private final ContextMenu entryMenu = new ContextMenu();
    private T lastSelectedObject;
    private Consumer<T> onSeleccion;

    public AutoCompleteTextField(SortedSet<T> entries, TextField autTF) {
        this.entries = entries;
        this.autoCompleteTextField = autTF;
        this.autoCompleteTextField.setOnKeyTyped(e -> acccion());
    }

    /** Acción que se ejecuta cuando el usuario elige una sugerencia. */
    public void setOnSeleccion(Consumer<T> onSeleccion) {
        this.onSeleccion = onSeleccion;
    }

    public void acccion() {
        String input = autoCompleteTextField.getText().toLowerCase();
        List<MenuItem> menuItems = new ArrayList<>();
        if (!input.isBlank()) {
            entries.stream()
                    .filter(e -> e.toString().toLowerCase().contains(input))
                    .limit(12)
                    .forEach(entry -> {
                        MenuItem item = new MenuItem(entry.toString());
                        item.setOnAction(e -> {
                            autoCompleteTextField.setText(entry.toString());
                            lastSelectedObject = entry;
                            entryMenu.hide();
                            if (onSeleccion != null) onSeleccion.accept(entry);
                        });
                        menuItems.add(item);
                    });
        }
        entryMenu.getItems().setAll(menuItems);
        if (!menuItems.isEmpty()) {
            entryMenu.show(autoCompleteTextField, Side.BOTTOM, 0, 0);
        } else {
            entryMenu.hide();
        }
    }

    public ContextMenu getEntryMenu() {
        return entryMenu;
    }

    public T getLastSelectedObject() {
        return lastSelectedObject;
    }
}
