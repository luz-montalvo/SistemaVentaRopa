package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.model.Compra;
import pe.edu.upeu.sysventas.model.DetalleCompra;
import pe.edu.upeu.sysventas.service.ICompraService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Consulta de la tabla compras con su detalle_compra. */
@RequiredArgsConstructor
public class HistorialCompraController {
    private final ICompraService compraService;

    @FXML VBox miContenedor;
    @FXML TableView<Compra> tableCompras;
    @FXML TableView<DetalleCompra> tableDetalle;
    @FXML DatePicker dpDesde, dpHasta;
    @FXML TextField txtFiltro;
    @FXML Label lblCantidad, lblTotal, lblDetalle;

    private List<Compra> compras = new ArrayList<>();

    @FXML
    public void initialize() {
        LinkedHashMap<String, ColumnInfo> col = new LinkedHashMap<>();
        col.put("N° Compra", new ColumnInfo("id", 85.0));
        col.put("Fecha", new ColumnInfo("fechaCompra", 100.0));
        col.put("Comprobante", new ColumnInfo("comprobante", 170.0));
        col.put("Proveedor", new ColumnInfo("proveedorNombre", 260.0));
        col.put("Registrado por", new ColumnInfo("comprador", 150.0));
        col.put("Neto S/", new ColumnInfo("netoTotal", 95.0));
        col.put("IGV S/", new ColumnInfo("igv", 85.0));
        col.put("Total S/", new ColumnInfo("precioTotal", 95.0));
        new TableViewHelper<Compra>().addColumns(tableCompras, col);

        LinkedHashMap<String, ColumnInfo> colDet = new LinkedHashMap<>();
        colDet.put("Prenda", new ColumnInfo("nombreProducto", 300.0));
        colDet.put("Cantidad", new ColumnInfo("cantidad", 90.0));
        colDet.put("P. Compra S/", new ColumnInfo("precioCompra", 110.0));
        colDet.put("Subtotal S/", new ColumnInfo("subtotal", 110.0));
        new TableViewHelper<DetalleCompra>().addColumns(tableDetalle, colDet);

        tableCompras.getSelectionModel().selectedItemProperty().addListener((obs, old, c) -> mostrarDetalle(c));

        compras = compraService.findAll();
        limpiarFiltros();
    }

    @FXML
    public void filtrar() {
        String t = txtFiltro.getText() == null ? "" : txtFiltro.getText().trim().toLowerCase();
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        List<Compra> filtradas = compras.stream()
                .filter(c -> c.getProveedorNombre().toLowerCase().contains(t) || c.getComprobante().toLowerCase().contains(t))
                .filter(c -> desde == null || !c.getFechaCompra().isBefore(desde))
                .filter(c -> hasta == null || !c.getFechaCompra().isAfter(hasta))
                .toList();
        tableCompras.getItems().setAll(filtradas);
        lblCantidad.setText(String.valueOf(filtradas.size()));
        lblTotal.setText(String.format("S/ %.2f", filtradas.stream().mapToDouble(Compra::getPrecioTotal).sum()));
        mostrarDetalle(null);
    }

    @FXML
    public void limpiarFiltros() {
        txtFiltro.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        filtrar();
    }

    private void mostrarDetalle(Compra c) {
        if (c == null) {
            tableDetalle.getItems().clear();
            lblDetalle.setText("Detalle de la compra");
            return;
        }
        tableDetalle.getItems().setAll(compraService.listarDetalle(c.getId()));
        lblDetalle.setText("Detalle de la compra N° " + c.getId() + "  ·  " + c.getComprobante());
    }
}
