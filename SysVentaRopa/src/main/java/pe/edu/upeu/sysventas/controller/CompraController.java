package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import pe.edu.upeu.sysventas.components.AutoCompleteTextField;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.FormValidator;
import pe.edu.upeu.sysventas.components.Mensaje;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.model.*;
import pe.edu.upeu.sysventas.service.ICompraService;
import pe.edu.upeu.sysventas.service.IProductoService;
import pe.edu.upeu.sysventas.service.IProveedorService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/** Registro de compras + detalle_compra. Al guardar, aumenta el stock de cada prenda. */
public class CompraController {

    private final IProductoService ps;
    private final IProveedorService provService;
    private final ICompraService compraService;

    @FXML VBox miContenedor;
    @FXML ComboBox<ComboBoxOption> cbxProveedor;
    @FXML ComboBox<String> cbxTipoDoc;
    @FXML TextField txtSerie, txtNumDoc, txtBuscarProducto, txtPrecioCompra, txtCantidad;
    @FXML DatePicker dpFecha;
    @FXML Label lblStock, lblMsg, lblNeto, lblIgv, lblTotal;
    @FXML TableView<DetalleCompra> tableView;

    private final SortedSet<ModeloDataAutocomplet> entriesProducto = new TreeSet<>((a, b) -> a.toString().compareTo(b.toString()));
    private Producto productoSel;

    public CompraController(IProductoService ps, IProveedorService provService, ICompraService compraService) {
        this.ps = ps;
        this.provService = provService;
        this.compraService = compraService;
    }

    @FXML
    public void initialize() {
        cbxProveedor.getItems().setAll(provService.listarCombobox());
        cbxTipoDoc.getItems().setAll("FACTURA", "BOLETA", "GUIA");

        cargarProductos();
        AutoCompleteTextField<ModeloDataAutocomplet> ac = new AutoCompleteTextField<>(entriesProducto, txtBuscarProducto);
        ac.setOnSeleccion(m -> seleccionarProducto(Long.parseLong(m.getIdx())));

        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("Prenda", new ColumnInfo("nombreProducto", 300.0));
        columns.put("Cantidad", new ColumnInfo("cantidad", 90.0));
        columns.put("P. Compra S/", new ColumnInfo("precioCompra", 110.0));
        columns.put("Subtotal S/", new ColumnInfo("subtotal", 110.0));
        new TableViewHelper<DetalleCompra>().addColumnsInOrderWithSize(tableView, columns, null, this::quitarPrenda);

        cancelarCompra();
    }

    private void cargarProductos() {
        entriesProducto.clear();
        entriesProducto.addAll(ps.listAutoCompletProducto());
    }

    private void seleccionarProducto(Long idProducto) {
        productoSel = ps.findById(idProducto);
        lblStock.setText(String.valueOf(productoSel.getStock()));
        txtPrecioCompra.setText(String.format("%.2f", productoSel.getPreUnitario()).replace(",", "."));
        txtCantidad.setText("1");
        txtCantidad.requestFocus();
        txtCantidad.selectAll();
        lblMsg.setText("");
    }

    @FXML
    public void agregarPrenda() {
        Double precio = FormValidator.leerDouble(txtPrecioCompra.getText());
        Integer cantidad = FormValidator.leerEntero(txtCantidad.getText());
        if (productoSel == null) {
            lblMsg.setText("Primero busque y elija una prenda de la lista");
            return;
        }
        if (precio == null || precio <= 0) {
            lblMsg.setText("El precio de compra debe ser mayor a cero");
            return;
        }
        if (cantidad == null || cantidad <= 0) {
            lblMsg.setText("La cantidad debe ser un número entero mayor a cero");
            return;
        }
        // misma prenda y mismo precio: se suma la cantidad
        DetalleCompra existente = tableView.getItems().stream()
                .filter(d -> d.getIdProducto().getIdProducto().equals(productoSel.getIdProducto())
                        && d.getPrecioCompra().equals(precio))
                .findFirst().orElse(null);
        if (existente != null) {
            int total = existente.getCantidad() + cantidad;
            existente.setCantidad(total);
            existente.setSubtotal(DocumentoComercial.redondear(precio * total));
            tableView.refresh();
        } else {
            tableView.getItems().add(DetalleCompra.builder()
                    .idProducto(productoSel)
                    .precioCompra(precio)
                    .cantidad(cantidad)
                    .subtotal(DocumentoComercial.redondear(precio * cantidad))
                    .build());
        }
        limpiarPrenda();
        calcularTotales();
        txtBuscarProducto.requestFocus();
    }

    private void quitarPrenda(DetalleCompra d) {
        tableView.getItems().remove(d);
        calcularTotales();
    }

    private void limpiarPrenda() {
        productoSel = null;
        txtBuscarProducto.clear();
        txtPrecioCompra.clear();
        txtCantidad.clear();
        lblStock.setText("-");
        lblMsg.setText("");
    }

    private void calcularTotales() {
        Compra temp = Compra.builder().detalles(new ArrayList<>(tableView.getItems())).build();
        temp.calcularMontos();
        lblNeto.setText(String.format("S/ %.2f", temp.getNetoTotal()));
        lblIgv.setText(String.format("S/ %.2f", temp.getIgv()));
        lblTotal.setText(String.format("S/ %.2f", temp.getPrecioTotal()));
    }

    @FXML
    public void registrarCompra() {
        String serie = txtSerie.getText().trim().toUpperCase();
        String numDoc = txtNumDoc.getText().trim();

        FormValidator v = new FormValidator();
        v.limpiar(cbxProveedor, cbxTipoDoc, txtSerie, txtNumDoc, dpFecha);
        v.check(cbxProveedor.getValue() == null, cbxProveedor, "Seleccione el proveedor")
         .check(cbxTipoDoc.getValue() == null, cbxTipoDoc, "Seleccione el tipo de comprobante")
         .check(!serie.matches("[A-Z0-9]{4}"), txtSerie, "La serie debe tener 4 caracteres (ej. F001)")
         .check(!numDoc.matches("\\d{1,10}"), txtNumDoc, "El número debe tener de 1 a 10 dígitos")
         .check(dpFecha.getValue() == null, dpFecha, "Seleccione la fecha")
         .check(dpFecha.getValue() != null && dpFecha.getValue().isAfter(LocalDate.now()), dpFecha,
                 "La fecha no puede ser futura");
        if (!v.esValido(lblMsg)) {
            Mensaje.error(miContenedor, lblMsg.getText());
            return;
        }
        if (tableView.getItems().isEmpty()) {
            Mensaje.error(miContenedor, "Agregue al menos una prenda");
            return;
        }

        Compra compra = Compra.builder()
                .fechaCompra(dpFecha.getValue())
                .tipoDoc(cbxTipoDoc.getValue())
                .serie(serie)
                .numDoc(numDoc)
                .idProveedor(provService.findById(Long.parseLong(cbxProveedor.getValue().getKey())))
                .idUsuario(Usuario.builder().idUsuario(SessionManager.getInstance().getUserId()).build())
                .detalles(new ArrayList<>(tableView.getItems()))
                .build();
        try {
            Compra guardada = compraService.registrarCompra(compra);
            Mensaje.info("Compra registrada", String.format(
                    "Compra N° %d registrada correctamente.%n%nComprobante: %s%nProveedor: %s%nNeto: S/ %.2f%nIGV (18 %%): S/ %.2f%nTOTAL: S/ %.2f%n%nEl stock de las prendas fue actualizado.",
                    guardada.getId(), guardada.getComprobante(), guardada.getIdProveedor().getRazonSocial(),
                    guardada.getNetoTotal(), guardada.getIgv(), guardada.getPrecioTotal()));
            cargarProductos();
            cancelarCompra();
        } catch (Exception e) {
            Mensaje.error(miContenedor, "No se registró la compra: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarCompra() {
        new FormValidator().limpiar(cbxProveedor, cbxTipoDoc, txtSerie, txtNumDoc, dpFecha);
        cbxProveedor.getSelectionModel().clearSelection();
        cbxTipoDoc.setValue("FACTURA");
        txtSerie.clear();
        txtNumDoc.clear();
        dpFecha.setValue(LocalDate.now());
        tableView.getItems().clear();
        limpiarPrenda();
        calcularTotales();
    }
}
