package pe.edu.upeu.sysventas.controller;

import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import pe.edu.upeu.sysventas.dto.DashboardDto;
import pe.edu.upeu.sysventas.dto.DashboardDto.Par;
import pe.edu.upeu.sysventas.dto.DashboardDto.UltimaVenta;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.repository.DashboardRepository;
import pe.edu.upeu.sysventas.service.IDashboardService;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Dashboard de la pantalla de inicio.
 * Se arma por código (sin FXML) y muestra tarjetas, gráficos y listas según el perfil:
 *  - Administrador de Tienda: todo el negocio.
 *  - Vendedor / Cajero: solo sus propias ventas.
 *  - Encargado de Almacén: stock, compras y proveedores.
 */
public class DashboardController {

    private static final String ADMIN = "Administrador de Tienda";
    private static final String CAJERO = "Vendedor de Tienda / Cajero";
    private static final String ALMACEN = "Encargado de Almacén";

    private final IDashboardService service;

    public DashboardController(IDashboardService service) {
        this.service = service;
    }

    /** Crea el dashboard listo para ponerlo en el área central. */
    public Node crear(Properties t) {
        VBox raiz = new VBox(18);
        raiz.getStyleClass().add("pantalla");
        llenar(raiz, t);
        ScrollPane scroll = new ScrollPane(raiz);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ------------------------------------------------------------------ armado

    private void llenar(VBox raiz, Properties t) {
        SessionManager s = SessionManager.getInstance();
        String perfil = s.getUserPerfil();
        Node encabezado = encabezado(raiz, t, s.getUserName(), perfil);

        DashboardDto d;
        try {
            d = service.cargar(perfil, s.getUserId());
        } catch (Exception ex) {
            ex.printStackTrace();
            Label err = new Label(tr(t, "dash.error", "No se pudo cargar el dashboard") + ": " + ex.getMessage());
            err.getStyleClass().add("msg-error");
            raiz.getChildren().setAll(encabezado, err);
            return;
        }
        raiz.getChildren().setAll(encabezado);

        if (ADMIN.equals(perfil)) {
            armarAdministrador(raiz, d, t);
        } else if (CAJERO.equals(perfil)) {
            armarCajero(raiz, d, t);
        } else if (ALMACEN.equals(perfil)) {
            armarAlmacen(raiz, d, t);
        }
    }

    private void armarAdministrador(VBox raiz, DashboardDto d, Properties t) {
        String ventas = tr(t, "dash.ventas", "ventas");
        GridPane kpis = grid(4);
        poner(kpis, kpi(tr(t, "dash.ventashoy", "Ventas de hoy"), money(d.ventasHoy()),
                d.numVentasHoy() + " " + ventas, "kpi-morado"), 0, 0, 1);
        poner(kpis, kpi(tr(t, "dash.ventasmes", "Ventas del mes"), money(d.ventasMes()),
                d.numVentasMes() + " " + ventas, "kpi-azul"), 1, 0, 1);
        poner(kpis, kpi(tr(t, "dash.utilidad", "Utilidad del mes"), money(d.utilidadMes()),
                tr(t, "dash.sinigv", "sin IGV"), "kpi-verde"), 2, 0, 1);
        poner(kpis, kpi(tr(t, "dash.compras", "Compras del mes"), money(d.comprasMes()),
                d.numComprasMes() + " " + tr(t, "dash.comprasn", "compras"), "kpi-ambar"), 3, 0, 1);
        poner(kpis, kpi(tr(t, "dash.stockbajo", "Stock bajo"), String.valueOf(d.stockBajo()),
                String.format(tr(t, "dash.umbral", "%d unidades o menos"), DashboardRepository.UMBRAL_STOCK_BAJO),
                "kpi-rojo"), 0, 1, 1);
        poner(kpis, kpi(tr(t, "dash.clientes", "Clientes"), String.valueOf(d.totalClientes()),
                tr(t, "dash.registrados", "registrados"), "kpi-morado"), 1, 1, 1);
        poner(kpis, kpi(tr(t, "dash.prendas", "Prendas"), String.valueOf(d.totalPrendas()),
                tr(t, "dash.encatalogo", "en catálogo"), "kpi-azul"), 2, 1, 1);
        poner(kpis, kpi(tr(t, "dash.proveedores", "Proveedores"), String.valueOf(d.totalProveedores()),
                tr(t, "dash.registrados", "registrados"), "kpi-ambar"), 3, 1, 1);

        GridPane graficos = grid(3);
        poner(graficos, tarjeta(tr(t, "dash.grafico7", "Ventas de los últimos 7 días (S/)"),
                graficoBarras(d.ventas7Dias())), 0, 0, 2);
        poner(graficos, tarjeta(tr(t, "dash.categorias", "Ventas del mes por categoría"),
                graficoTorta(d.ventasPorCategoria(), t)), 2, 0, 1);

        GridPane listas = grid(3);
        poner(listas, tarjeta(tr(t, "dash.top", "Prendas más vendidas del mes"), listaTop(d.topPrendas(), t)), 0, 0, 1);
        poner(listas, tarjeta(tr(t, "dash.stockcritico", "Stock crítico"), listaStock(d.stockCritico(), t)), 1, 0, 1);
        poner(listas, tarjeta(tr(t, "dash.cajeros", "Ventas por cajero (mes)"), listaCajeros(d.ventasPorCajero(), t)), 2, 0, 1);
        poner(listas, tarjeta(tr(t, "dash.ultimas", "Últimas ventas"), listaUltimas(d.ultimasVentas(), t)), 0, 1, 3);

        raiz.getChildren().addAll(kpis, graficos, listas);
    }

    private void armarCajero(VBox raiz, DashboardDto d, Properties t) {
        String ventas = tr(t, "dash.ventas", "ventas");
        double ticket = d.numVentasMes() == 0 ? 0 : d.ventasMes() / d.numVentasMes();
        GridPane kpis = grid(4);
        poner(kpis, kpi(tr(t, "dash.misventashoy", "Mis ventas de hoy"), money(d.ventasHoy()),
                d.numVentasHoy() + " " + ventas, "kpi-morado"), 0, 0, 1);
        poner(kpis, kpi(tr(t, "dash.misventasmes", "Mis ventas del mes"), money(d.ventasMes()),
                d.numVentasMes() + " " + ventas, "kpi-azul"), 1, 0, 1);
        poner(kpis, kpi(tr(t, "dash.ticket", "Ticket promedio"), money(ticket),
                tr(t, "dash.porventa", "por venta"), "kpi-verde"), 2, 0, 1);
        poner(kpis, kpi(tr(t, "dash.clientes", "Clientes"), String.valueOf(d.totalClientes()),
                tr(t, "dash.registrados", "registrados"), "kpi-ambar"), 3, 0, 1);

        GridPane graficos = grid(3);
        poner(graficos, tarjeta(tr(t, "dash.grafico7", "Ventas de los últimos 7 días (S/)"),
                graficoBarras(d.ventas7Dias())), 0, 0, 2);
        poner(graficos, tarjeta(tr(t, "dash.categorias", "Ventas del mes por categoría"),
                graficoTorta(d.ventasPorCategoria(), t)), 2, 0, 1);

        GridPane listas = grid(3);
        poner(listas, tarjeta(tr(t, "dash.top", "Prendas más vendidas del mes"), listaTop(d.topPrendas(), t)), 0, 0, 1);
        poner(listas, tarjeta(tr(t, "dash.ultimas", "Últimas ventas"), listaUltimas(d.ultimasVentas(), t)), 1, 0, 2);

        raiz.getChildren().addAll(kpis, graficos, listas);
    }

    private void armarAlmacen(VBox raiz, DashboardDto d, Properties t) {
        GridPane kpis = grid(4);
        poner(kpis, kpi(tr(t, "dash.prendas", "Prendas"), String.valueOf(d.totalPrendas()),
                tr(t, "dash.encatalogo", "en catálogo"), "kpi-azul"), 0, 0, 1);
        poner(kpis, kpi(tr(t, "dash.stockbajo", "Stock bajo"), String.valueOf(d.stockBajo()),
                String.format(tr(t, "dash.umbral", "%d unidades o menos"), DashboardRepository.UMBRAL_STOCK_BAJO),
                "kpi-rojo"), 1, 0, 1);
        poner(kpis, kpi(tr(t, "dash.compras", "Compras del mes"), money(d.comprasMes()),
                d.numComprasMes() + " " + tr(t, "dash.comprasn", "compras"), "kpi-ambar"), 2, 0, 1);
        poner(kpis, kpi(tr(t, "dash.proveedores", "Proveedores"), String.valueOf(d.totalProveedores()),
                tr(t, "dash.registrados", "registrados"), "kpi-morado"), 3, 0, 1);

        GridPane listas = grid(2);
        poner(listas, tarjeta(tr(t, "dash.stockcritico", "Stock crítico"), listaStock(d.stockCritico(), t)), 0, 0, 1);
        poner(listas, tarjeta(tr(t, "dash.top", "Prendas más vendidas del mes"), listaTop(d.topPrendas(), t)), 1, 0, 1);

        raiz.getChildren().addAll(kpis, listas);
    }

    private Node encabezado(VBox raiz, Properties t, String usuario, String perfil) {
        Label titulo = new Label(tr(t, "label.bienvenido", "Bienvenido") + ", " + usuario);
        titulo.getStyleClass().add("titulo-pantalla");
        Label subtitulo = new Label(CAJERO.equals(perfil)
                ? tr(t, "dash.subtitulo.cajero", "Resumen de tus ventas")
                : tr(t, "dash.subtitulo", "Resumen general de tu tienda"));
        subtitulo.getStyleClass().add("subtitulo-pantalla");
        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label chip = new Label(perfil);
        chip.getStyleClass().add("chip");
        Button actualizar = new Button(tr(t, "dash.actualizar", "Actualizar"));
        actualizar.getStyleClass().add("btn-secundario");
        actualizar.setOnAction(e -> llenar(raiz, t));

        HBox fila = new HBox(12, textos, espacio, chip, actualizar);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    // ------------------------------------------------------------------ componentes

    /** Tarjeta de indicador (KPI): título, valor grande y una línea de detalle. */
    private VBox kpi(String titulo, String valor, String detalle, String color) {
        Label lblTitulo = new Label(titulo.toUpperCase());
        lblTitulo.getStyleClass().add("kpi-titulo");
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("kpi-valor");
        Label lblDetalle = new Label(detalle);
        lblDetalle.getStyleClass().add("dato-suave");
        VBox caja = new VBox(4, lblTitulo, lblValor, lblDetalle);
        caja.getStyleClass().addAll("card", "kpi", color);
        return caja;
    }

    /** Tarjeta blanca con título que contiene un gráfico o una lista. */
    private VBox tarjeta(String titulo, Node contenido) {
        Label lbl = new Label(titulo);
        lbl.getStyleClass().add("card-titulo");
        VBox caja = new VBox(10, lbl, contenido);
        caja.getStyleClass().add("card");
        return caja;
    }

    private Node graficoBarras(List<Par> datos) {
        CategoryAxis ejeX = new CategoryAxis();
        NumberAxis ejeY = new NumberAxis();
        BarChart<String, Number> grafico = new BarChart<>(ejeX, ejeY);
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Par p : datos) {
            serie.getData().add(new XYChart.Data<String, Number>(p.etiqueta(), p.valor()));
        }
        grafico.getData().add(serie);
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setCategoryGap(24);
        grafico.setMinHeight(250);
        grafico.setPrefHeight(270);
        grafico.getStyleClass().add("dash-chart");
        return grafico;
    }

    private Node graficoTorta(List<Par> datos, Properties t) {
        if (datos.isEmpty()) {
            return vacio(tr(t, "dash.vacio", "Aún no hay datos para mostrar"));
        }
        PieChart torta = new PieChart();
        for (Par p : datos) {
            torta.getData().add(new PieChart.Data(p.etiqueta(), p.valor()));
        }
        torta.setLegendSide(Side.BOTTOM);
        torta.setLabelsVisible(false);
        torta.setAnimated(false);
        torta.setMinHeight(250);
        torta.setPrefHeight(270);
        torta.getStyleClass().add("dash-chart");
        return torta;
    }

    private Node listaTop(List<Par> datos, Properties t) {
        if (datos.isEmpty()) return vacio(tr(t, "dash.vacio", "Aún no hay datos para mostrar"));
        double max = datos.stream().mapToDouble(Par::valor).max().orElse(1);
        VBox lista = new VBox(10);
        int i = 1;
        for (Par p : datos) {
            String cantidad = (int) p.valor() + " " + tr(t, "dash.und", "und.");
            lista.getChildren().add(new VBox(4, fila(i + ". " + p.etiqueta(), cantidad, "dato"),
                    barra(max == 0 ? 0 : p.valor() / max)));
            i++;
        }
        return lista;
    }

    private Node listaCajeros(List<Par> datos, Properties t) {
        if (datos.isEmpty()) return vacio(tr(t, "dash.vacio", "Aún no hay datos para mostrar"));
        double max = datos.stream().mapToDouble(Par::valor).max().orElse(1);
        VBox lista = new VBox(10);
        for (Par p : datos) {
            lista.getChildren().add(new VBox(4, fila(p.etiqueta(), money(p.valor()), "dato"),
                    barra(max == 0 ? 0 : p.valor() / max)));
        }
        return lista;
    }

    private Node listaStock(List<Par> datos, Properties t) {
        if (datos.isEmpty()) return vacio(tr(t, "dash.sinstock", "Todas las prendas tienen stock suficiente"));
        VBox lista = new VBox(8);
        for (Par p : datos) {
            lista.getChildren().add(fila(p.etiqueta(), (int) p.valor() + " " + tr(t, "dash.und", "und."), "kpi-alerta"));
        }
        return lista;
    }

    private Node listaUltimas(List<UltimaVenta> datos, Properties t) {
        if (datos.isEmpty()) return vacio(tr(t, "dash.vacio", "Aún no hay datos para mostrar"));
        VBox lista = new VBox(8);
        for (UltimaVenta v : datos) {
            Label cliente = new Label(v.cliente());
            cliente.getStyleClass().add("dato");
            Label detalle = new Label(v.comprobante() + " - " + v.fecha());
            detalle.getStyleClass().add("dato-suave");
            VBox izquierda = new VBox(1, cliente, detalle);
            HBox.setHgrow(izquierda, Priority.ALWAYS);
            Label total = new Label(money(v.total()));
            total.getStyleClass().add("dato");
            HBox fila = new HBox(10, izquierda, total);
            fila.setAlignment(Pos.CENTER_LEFT);
            lista.getChildren().add(fila);
        }
        return lista;
    }

    /** Fila "texto a la izquierda, valor a la derecha". */
    private HBox fila(String izquierda, String derecha, String claseDerecha) {
        Label lblIzq = new Label(izquierda);
        lblIzq.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblIzq, Priority.ALWAYS);
        Label lblDer = new Label(derecha);
        lblDer.getStyleClass().add(claseDerecha);
        HBox fila = new HBox(10, lblIzq, lblDer);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private ProgressBar barra(double progreso) {
        ProgressBar b = new ProgressBar(progreso);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("dash-progreso");
        return b;
    }

    private Label vacio(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("dato-suave");
        l.setWrapText(true);
        return l;
    }

    // ------------------------------------------------------------------ utilidades

    /** Cuadrícula con columnas del mismo ancho (se adapta al tamaño de la ventana). */
    private GridPane grid(int columnas) {
        GridPane g = new GridPane();
        g.setHgap(16);
        g.setVgap(16);
        for (int i = 0; i < columnas; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columnas);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
        }
        return g;
    }

    private void poner(GridPane g, Region nodo, int columna, int fila, int ancho) {
        nodo.setMaxWidth(Double.MAX_VALUE);
        g.add(nodo, columna, fila, ancho, 1);
    }

    private String money(double v) {
        return String.format(Locale.US, "S/ %,.2f", v);
    }

    private String tr(Properties t, String clave, String porDefecto) {
        return t.getProperty(clave, porDefecto);
    }
}
