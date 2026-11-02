package pe.edu.upeu.sysventas.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Boleta o Factura de una venta, con vista previa y botón Imprimir.
 * - Cliente con RUC  -> FACTURA DE VENTA, serie F001
 * - Cliente con DNI  -> BOLETA DE VENTA, serie B001
 * Se imprime con javafx.print.PrinterJob (sale el diálogo de impresoras del sistema;
 * también se puede elegir "Microsoft Print to PDF" para guardarlo como PDF).
 */
public final class Comprobante {

    private static final String TIENDA = "SYSVENTAROPA - TIENDA DE ROPA";
    private static final String RUC_TIENDA = "RUC: 20123456789";
    private static final String DIRECCION_TIENDA = "Av. Principal 123 - Lima";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Comprobante() {
    }

    public static boolean esFactura(Cliente c) {
        return c != null && c.getTipoDocumento() == TipoDocumento.RUC;
    }

    public static String nombreTipo(Cliente c) {
        return esFactura(c) ? "Factura" : "Boleta";
    }

    /** Abre la vista previa del comprobante con los botones Imprimir y Cerrar. */
    public static void mostrar(Venta venta, List<VentaDetalle> detalles, String vendedor) {
        VBox hoja = crearHoja(venta, detalles, vendedor);

        Button btnImprimir = new Button("Imprimir");
        btnImprimir.getStyleClass().add("btn-primario");
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.getStyleClass().add("btn-secundario");
        HBox botones = new HBox(10, btnImprimir, btnCerrar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(hoja);
        scroll.setFitToWidth(true);
        VBox raiz = new VBox(scroll, botones);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(nombreTipo(venta.getIdCliente()) + " " + numero(venta));
        Scene scene = new Scene(raiz, 480, 640);
        Tema.aplicar(scene);
        stage.setScene(scene);

        btnCerrar.setOnAction(e -> stage.close());
        btnImprimir.setOnAction(e -> {
            if (imprimir(hoja, stage)) stage.close();
        });
        stage.showAndWait();
    }

    /** Envía la hoja a la impresora elegida. Devuelve true si se imprimió. */
    private static boolean imprimir(VBox hoja, Stage owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Mensaje.info("Imprimir", "No se encontró ninguna impresora instalada.");
            return false;
        }
        if (!job.showPrintDialog(owner)) {
            return false; // el usuario canceló
        }
        // Escala la hoja para que entre en el ancho del papel
        PageLayout pagina = job.getJobSettings().getPageLayout();
        double escala = Math.min(1.0, pagina.getPrintableWidth() / hoja.getBoundsInParent().getWidth());
        Scale s = new Scale(escala, escala);
        hoja.getTransforms().add(s);
        boolean ok = job.printPage(hoja);
        hoja.getTransforms().remove(s);
        if (ok) {
            job.endJob();
            Mensaje.info("Imprimir", "Comprobante enviado a la impresora.");
        } else {
            Mensaje.info("Imprimir", "No se pudo imprimir el comprobante.");
        }
        return ok;
    }

    public static String numero(Venta v) {
        String serie = esFactura(v.getIdCliente()) ? "F001" : "B001";
        return serie + "-" + String.format("%08d", v.getId());
    }

    /** Arma el diseño del comprobante (lo que se ve y se imprime). */
    private static VBox crearHoja(Venta v, List<VentaDetalle> detalles, String vendedor) {
        Cliente c = v.getIdCliente();
        boolean factura = esFactura(c);

        VBox hoja = new VBox(4);
        hoja.setPadding(new Insets(20));
        hoja.setPrefWidth(440);
        hoja.setStyle("-fx-background-color: white;");

        hoja.getChildren().addAll(
                centrado(TIENDA, 15, true),
                centrado(RUC_TIENDA, 11, false),
                centrado(DIRECCION_TIENDA, 11, false),
                linea(),
                centrado(factura ? "FACTURA DE VENTA" : "BOLETA DE VENTA", 14, true),
                centrado(numero(v), 13, true),
                linea(),
                texto("Fecha: " + (v.getFechaVenta() == null ? "" : v.getFechaVenta().format(FECHA)), 11, false),
                texto((factura ? "RUC: " : "DNI: ") + (c == null ? "" : c.getDniruc()), 11, false),
                texto((factura ? "Razón social: " : "Cliente: ") + (c == null ? "" : c.getNombres()), 11, false));
        if (c != null && c.getDireccion() != null && !c.getDireccion().isBlank()) {
            hoja.getChildren().add(texto("Dirección: " + c.getDireccion(), 11, false));
        }
        if (vendedor != null && !vendedor.isBlank()) {
            hoja.getChildren().add(texto("Atendido por: " + vendedor, 11, false));
        }
        hoja.getChildren().add(linea());

        // Detalle: Cant | Descripción | P. Unit | Importe
        GridPane tabla = new GridPane();
        tabla.setHgap(6);
        tabla.setVgap(3);
        ColumnConstraints c1 = new ColumnConstraints(40);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(70);
        ColumnConstraints c4 = new ColumnConstraints(75);
        tabla.getColumnConstraints().addAll(c1, c2, c3, c4);

        tabla.addRow(0, texto("Cant.", 11, true), texto("Descripción", 11, true),
                derecha("P. Unit", 11, true), derecha("Importe", 11, true));
        int fila = 1;
        for (VentaDetalle d : detalles) {
            Label desc = texto(d.getNombreProducto(), 11, false);
            desc.setWrapText(true);
            tabla.addRow(fila++,
                    texto(String.valueOf(d.getCantidad()), 11, false),
                    desc,
                    derecha(String.format("%.2f", d.getPrecioVenta()), 11, false),
                    derecha(String.format("%.2f", d.getNetoTotal()), 11, false));
        }
        hoja.getChildren().addAll(tabla, linea());

        hoja.getChildren().addAll(
                total(factura ? "Op. gravada S/" : "Subtotal S/", v.getNetoTotal(), false),
                total("IGV (18 %) S/", v.getIgv(), false),
                total("TOTAL S/", v.getPrecioTotal(), true),
                linea(),
                centrado("¡Gracias por su compra!", 12, true),
                centrado("Representación impresa del comprobante de venta", 9, false));
        return hoja;
    }

    // ---------------- ayudas de diseño ----------------

    private static Label texto(String t, double tam, boolean negrita) {
        Label l = new Label(t);
        l.setFont(Font.font("Monospaced", negrita ? FontWeight.BOLD : FontWeight.NORMAL, tam));
        l.setStyle("-fx-text-fill: black;");
        return l;
    }

    private static Label centrado(String t, double tam, boolean negrita) {
        Label l = texto(t, tam, negrita);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private static Label derecha(String t, double tam, boolean negrita) {
        Label l = texto(t, tam, negrita);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER_RIGHT);
        return l;
    }

    private static HBox total(String etiqueta, double monto, boolean negrita) {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label valor = texto(String.format("%.2f", monto), negrita ? 13 : 11, negrita);
        valor.setMinWidth(80);
        valor.setAlignment(Pos.CENTER_RIGHT);
        return new HBox(8, espacio, texto(etiqueta, negrita ? 13 : 11, negrita), valor);
    }

    private static Separator linea() {
        Separator s = new Separator();
        s.setPadding(new Insets(3, 0, 3, 0));
        return s;
    }
}
