package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.Comprobante;
import pe.edu.upeu.sysventas.components.Mensaje;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.service.IVentaService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Consulta de la tabla Venta con su VentaDetalle. */
@RequiredArgsConstructor
public class HistorialVentaController {
    private final IVentaService vs;

    @FXML VBox miContenedor;
    @FXML TableView<Venta> tableVentas;
    @FXML TableView<VentaDetalle> tableDetalle;
    @FXML DatePicker dpDesde, dpHasta;
    @FXML TextField txtFiltro;
    @FXML Label lblCantidad, lblTotal, lblDetalle;

    private List<Venta> ventas = new ArrayList<>();

    @FXML
    public void initialize() {
        LinkedHashMap<String, ColumnInfo> col = new LinkedHashMap<>();
        col.put("N° Venta", new ColumnInfo("id", 80.0));
        col.put("Fecha", new ColumnInfo("fechaVenta", 100.0));
        col.put("Cliente", new ColumnInfo("clienteNombre", 280.0));
        col.put("Vendedor", new ColumnInfo("vendedor", 170.0));
        col.put("Neto S/", new ColumnInfo("netoTotal", 100.0));
        col.put("IGV S/", new ColumnInfo("igv", 90.0));
        col.put("Total S/", new ColumnInfo("precioTotal", 100.0));
        new TableViewHelper<Venta>().addColumns(tableVentas, col);

        LinkedHashMap<String, ColumnInfo> colDet = new LinkedHashMap<>();
        colDet.put("Prenda", new ColumnInfo("nombreProducto", 280.0));
        colDet.put("Cantidad", new ColumnInfo("cantidad", 90.0));
        colDet.put("Costo S/", new ColumnInfo("precioUnit", 100.0));
        colDet.put("% Util.", new ColumnInfo("porceUtil", 80.0));
        colDet.put("P. Venta S/", new ColumnInfo("precioVenta", 100.0));
        colDet.put("Importe S/", new ColumnInfo("netoTotal", 100.0));
        new TableViewHelper<VentaDetalle>().addColumns(tableDetalle, colDet);

        tableVentas.getSelectionModel().selectedItemProperty().addListener((obs, old, v) -> mostrarDetalle(v));

        ventas = vs.findAll();
        limpiarFiltros();
    }

    @FXML
    public void filtrar() {
        String t = txtFiltro.getText() == null ? "" : txtFiltro.getText().trim().toLowerCase();
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        List<Venta> filtradas = ventas.stream()
                .filter(v -> v.getClienteNombre().toLowerCase().contains(t) || v.getVendedor().toLowerCase().contains(t))
                .filter(v -> desde == null || !v.getFechaVenta().isBefore(desde))
                .filter(v -> hasta == null || !v.getFechaVenta().isAfter(hasta))
                .toList();
        tableVentas.getItems().setAll(filtradas);
        lblCantidad.setText(String.valueOf(filtradas.size()));
        lblTotal.setText(String.format("S/ %.2f", filtradas.stream().mapToDouble(Venta::getPrecioTotal).sum()));
        mostrarDetalle(null);
    }

    @FXML
    public void limpiarFiltros() {
        txtFiltro.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        filtrar();
    }

    private void mostrarDetalle(Venta v) {
        if (v == null) {
            tableDetalle.getItems().clear();
            lblDetalle.setText("Detalle de la venta");
            return;
        }
        tableDetalle.getItems().setAll(vs.listarDetalle(v.getId()));
        lblDetalle.setText("Detalle de la venta N° " + v.getId() + "  ·  " + v.getClienteNombre());
    }

    /** Reimprime la boleta (DNI) o factura (RUC) de la venta seleccionada. */
    @FXML
    public void imprimirComprobante() {
        Venta v = tableVentas.getSelectionModel().getSelectedItem();
        if (v == null) {
            Mensaje.error(miContenedor, "Seleccione una venta de la tabla");
            return;
        }
        Comprobante.mostrar(v, vs.listarDetalle(v.getId()), v.getVendedor());
    }
}
