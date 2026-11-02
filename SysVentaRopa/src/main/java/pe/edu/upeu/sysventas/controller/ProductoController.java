package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.FormValidator;
import pe.edu.upeu.sysventas.components.Mensaje;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.service.ICategoriaService;
import pe.edu.upeu.sysventas.service.IProductoService;
import pe.edu.upeu.sysventas.service.IUnidadMedidaService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** CRUD de la tabla Productos (prendas). */
@RequiredArgsConstructor
public class ProductoController {
    private final ICategoriaService cs;
    private final IProductoService ps;
    private final IUnidadMedidaService ums;

    @FXML VBox miContenedor;
    @FXML ComboBox<ComboBoxOption> cbxCategoria, cbxUnidadMedida;
    @FXML TextField txtNombre, txtCosto, txtUtilidad, txtStock, txtFiltroDato;
    @FXML Label lbnMsg, lblPrecioVenta, lblFormTitulo;
    @FXML Button btnGuardar;
    @FXML TableView<Producto> tableView;

    private List<Producto> listaProductos = new ArrayList<>();
    private Long idProductoCE = 0L;   // 0 = nuevo, >0 = editando

    @FXML
    public void initialize() {
        cbxCategoria.getItems().setAll(cs.lisCategoria());
        cbxUnidadMedida.getItems().setAll(ums.listarCombobox());

        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID", new ColumnInfo("idProducto", 50.0));
        columns.put("Prenda", new ColumnInfo("nombre", 210.0));
        columns.put("Categoría", new ColumnInfo("idCateg.nombre", 140.0));
        columns.put("Unidad", new ColumnInfo("idUnidadMed.nombre", 130.0));
        columns.put("Costo S/", new ColumnInfo("preUnitario", 80.0));
        columns.put("% Util.", new ColumnInfo("porceUtil", 65.0));
        columns.put("P. Venta S/", new ColumnInfo("precioVenta", 90.0));
        columns.put("Stock", new ColumnInfo("stock", 60.0));
        new TableViewHelper<Producto>().addColumnsInOrderWithSize(tableView, columns, this::editForm, this::eliminar);

        clearForm();
        listar();
    }

    public void listar() {
        listaProductos = ps.findAll();
        filtrar();
    }

    /** Filtra la tabla por nombre o categoría mientras se escribe. */
    @FXML
    public void filtrar() {
        String texto = txtFiltroDato.getText() == null ? "" : txtFiltroDato.getText().trim().toLowerCase();
        tableView.getItems().setAll(listaProductos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(texto)
                        || p.getIdCateg().getNombre().toLowerCase().contains(texto))
                .toList());
    }

    /** Muestra en vivo: costo + % utilidad = precio de venta. */
    @FXML
    public void calcularPrecioVenta() {
        Double costo = FormValidator.leerDouble(txtCosto.getText());
        Double util = FormValidator.leerDouble(txtUtilidad.getText());
        double precio = Producto.calcularPrecioVenta(costo == null ? 0.0 : costo, util == null ? 0.0 : util);
        lblPrecioVenta.setText(String.format("S/ %.2f", precio));
    }

    @FXML
    public void validarFormulario() {
        Double costo = FormValidator.leerDouble(txtCosto.getText());
        Double util = FormValidator.leerDouble(txtUtilidad.getText());
        Integer stock = FormValidator.leerEntero(txtStock.getText());

        FormValidator v = new FormValidator();
        v.limpiar(txtNombre, txtCosto, txtUtilidad, txtStock, cbxCategoria, cbxUnidadMedida);
        v.check(FormValidator.vacio(txtNombre.getText()), txtNombre, "El nombre de la prenda es obligatorio")
         .check(txtNombre.getText().trim().length() > 100, txtNombre, "Máximo 100 caracteres")
         .check(costo == null || costo <= 0, txtCosto, "El costo debe ser un número mayor a cero")
         .check(util == null || util < 0 || util > 999, txtUtilidad, "El % de utilidad debe estar entre 0 y 999")
         .check(stock == null || stock < 0, txtStock, "El stock debe ser un número entero, 0 o más")
         .check(cbxCategoria.getValue() == null, cbxCategoria, "Seleccione la categoría")
         .check(cbxUnidadMedida.getValue() == null, cbxUnidadMedida, "Seleccione la unidad de medida");
        if (!v.esValido(lbnMsg)) return;

        Producto p = Producto.builder()
                .nombre(txtNombre.getText().trim())
                .preUnitario(costo)
                .porceUtil(util)
                .stock(stock)
                .idCateg(cs.findById(Long.parseLong(cbxCategoria.getValue().getKey())))
                .idUnidadMed(ums.findById(Long.parseLong(cbxUnidadMedida.getValue().getKey())))
                .build();
        try {
            if (idProductoCE > 0) {
                p.setIdProducto(idProductoCE);
                ps.update(idProductoCE, p);
                Mensaje.exito(miContenedor, "Prenda actualizada correctamente");
            } else {
                ps.save(p);
                Mensaje.exito(miContenedor, "Prenda registrada correctamente");
            }
            clearForm();
            listar();
        } catch (Exception e) {
            lbnMsg.setText("Error al guardar: " + e.getMessage());
        }
    }

    public void editForm(Producto p) {
        clearForm();
        idProductoCE = p.getIdProducto();
        txtNombre.setText(p.getNombre());
        txtCosto.setText(String.valueOf(p.getPreUnitario()));
        txtUtilidad.setText(String.valueOf(p.getPorceUtil()));
        txtStock.setText(String.valueOf(p.getStock()));
        seleccionar(cbxCategoria, p.getIdCateg().getIdCateg());
        seleccionar(cbxUnidadMedida, p.getIdUnidadMed().getIdUnidadMed());
        calcularPrecioVenta();
        lblFormTitulo.setText("Editar prenda #" + p.getIdProducto());
        btnGuardar.setText("Actualizar");
    }

    private void seleccionar(ComboBox<ComboBoxOption> combo, Long id) {
        combo.getItems().stream()
                .filter(o -> o.getKey().equals(String.valueOf(id)))
                .findFirst()
                .ifPresent(o -> combo.getSelectionModel().select(o));
    }

    private void eliminar(Producto p) {
        if (!Mensaje.confirmar("Eliminar prenda", "¿Eliminar la prenda \"" + p.getNombre() + "\"?")) return;
        try {
            ps.delete(p.getIdProducto());
            Mensaje.exito(miContenedor, "Prenda eliminada");
            listar();
        } catch (Exception e) {
            // si la prenda ya tiene ventas o compras, la llave foránea no deja borrarla
            Mensaje.error(miContenedor, "No se puede eliminar: la prenda tiene ventas o compras registradas");
        }
    }

    @FXML
    public void clearForm() {
        new FormValidator().limpiar(txtNombre, txtCosto, txtUtilidad, txtStock, cbxCategoria, cbxUnidadMedida);
        txtNombre.clear();
        txtCosto.clear();
        txtUtilidad.clear();
        txtStock.clear();
        cbxCategoria.getSelectionModel().clearSelection();
        cbxUnidadMedida.getSelectionModel().clearSelection();
        lbnMsg.setText("");
        lblPrecioVenta.setText("S/ 0.00");
        lblFormTitulo.setText("Nueva prenda");
        btnGuardar.setText("Guardar");
        idProductoCE = 0L;
    }
}
