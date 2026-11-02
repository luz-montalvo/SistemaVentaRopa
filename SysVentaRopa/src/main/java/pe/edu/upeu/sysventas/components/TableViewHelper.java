package pe.edu.upeu.sysventas.components;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Crea las columnas de un TableView a partir de un mapa "Título" -> campo del modelo. */
public class TableViewHelper<T> {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Columnas + columna "Acciones". Si una acción es null, su botón no aparece. */
    public void addColumnsInOrderWithSize(TableView<T> tableView, LinkedHashMap<String, ColumnInfo> columns,
                                          Consumer<T> updateAction, Consumer<T> deleteAction) {
        addColumns(tableView, columns);
        if (updateAction != null || deleteAction != null) {
            addActionColumn(tableView, updateAction, deleteAction);
        }
    }

    /** Solo columnas de datos (para tablas de consulta). */
    public void addColumns(TableView<T> tableView, LinkedHashMap<String, ColumnInfo> columns) {
        for (Map.Entry<String, ColumnInfo> entry : columns.entrySet()) {
            TableColumn<T, Object> column = new TableColumn<>(entry.getKey());
            String field = entry.getValue().getField();

            if (field.contains(".")) {
                // campo de un objeto relacionado, ej: "idCateg.nombre"
                column.setCellValueFactory(cellData -> {
                    T item = cellData.getValue();
                    String[] fieldPath = field.split("\\.");
                    try {
                        Object value = item.getClass().getMethod("get" + capitalize(fieldPath[0])).invoke(item);
                        if (value != null) {
                            Object nested = value.getClass().getMethod("get" + capitalize(fieldPath[1])).invoke(value);
                            return new SimpleObjectProperty<>(nested);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new SimpleObjectProperty<>(null);
                });
            } else {
                column.setCellValueFactory(new PropertyValueFactory<>(field));
            }

            // Formato: números con 2 decimales y fechas dd/MM/yyyy
            column.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else if (item instanceof Double d) {
                        setText(String.format("%.2f", d));
                    } else if (item instanceof LocalDate f) {
                        setText(f.format(FECHA));
                    } else {
                        setText(item.toString());
                    }
                }
            });

            if (entry.getValue().getWidth() != null) {
                column.setPrefWidth(entry.getValue().getWidth());
            }
            tableView.getColumns().add(column);
        }
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void addActionColumn(TableView<T> tableView, Consumer<T> updateAction, Consumer<T> deleteAction) {
        TableColumn<T, Void> actionColumn = new TableColumn<>("Acciones");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnUpdate = crearBoton("/img/document-edit-icon.png", "Editar");
            private final Button btnDelete = crearBoton("/img/del-icon.png", "Eliminar");
            private final HBox botones = new HBox(8);

            {
                if (updateAction != null) {
                    btnUpdate.setOnAction(e -> updateAction.accept(getTableView().getItems().get(getIndex())));
                    botones.getChildren().add(btnUpdate);
                }
                if (deleteAction != null) {
                    btnDelete.setOnAction(e -> deleteAction.accept(getTableView().getItems().get(getIndex())));
                    botones.getChildren().add(btnDelete);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : botones);
            }
        });
        actionColumn.setPrefWidth(100);
        actionColumn.setMinWidth(90);
        actionColumn.setSortable(false);
        tableView.getColumns().add(actionColumn);
    }

    private Button crearBoton(String icono, String texto) {
        ImageView img = new ImageView(new Image(getClass().getResource(icono).toExternalForm()));
        img.setFitWidth(18);
        img.setFitHeight(18);
        img.setPreserveRatio(true);
        Button b = new Button("", img);
        b.getStyleClass().add("btn-icono");
        b.setTooltip(new Tooltip(texto));
        return b;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
