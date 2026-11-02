package pe.edu.upeu.sysventas.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import pe.edu.upeu.sysventas.components.AutoCompleteTextField;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.FormValidator;
import pe.edu.upeu.sysventas.components.Comprobante;
import pe.edu.upeu.sysventas.components.Mensaje;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.dto.PersonaDto;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.*;
import pe.edu.upeu.sysventas.service.IClienteService;
import pe.edu.upeu.sysventas.service.IProductoService;
import pe.edu.upeu.sysventas.service.IVentaService;
import pe.edu.upeu.sysventas.utils.ConsultaDNI;
import pe.edu.upeu.sysventas.utils.ConsultaRUC;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/** Registro de Venta + VentaDetalle. El detalle se arma en memoria y se guarda al final. */
public class VentaController {

    // Servicios que usa la pantalla (los entrega AppContext por el constructor)
    private final IProductoService ps;
    private final IClienteService cs;
    private final IVentaService vs;
    private final ConsultaDNI consultaDNI;
    private final ConsultaRUC consultaRUC;

    @FXML VBox miContenedor;
    @FXML TextField txtBuscarCliente, txtBuscarProducto, txtCantidad;
    @FXML Label lblFecha, lblCliNombre, lblCliDoc, lblCliDireccion, lblStock, lblPrecio, lblMsg, lblNeto, lblIgv, lblTotal;
    @FXML Button btnAgregar, btnRegistrar;
    @FXML TableView<VentaDetalle> tableView;

    // Listas para los buscadores con autocompletado (ordenadas alfabéticamente)
    private final SortedSet<ModeloDataAutocomplet> entriesCliente = new TreeSet<>((a, b) -> a.toString().compareTo(b.toString()));
    private final SortedSet<ModeloDataAutocomplet> entriesProducto = new TreeSet<>((a, b) -> a.toString().compareTo(b.toString()));

    // Cliente y prenda elegidos en este momento
    private Cliente clienteSel;
    private Producto productoSel;

    public VentaController(IProductoService ps, IClienteService cs, IVentaService vs, ConsultaDNI consultaDNI, ConsultaRUC consultaRUC) {
        this.ps = ps;
        this.cs = cs;
        this.vs = vs;
        this.consultaDNI = consultaDNI;
        this.consultaRUC = consultaRUC;
    }

    @FXML
    public void initialize() {
        // Muestra la fecha de hoy con formato día/mes/año
        lblFecha.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Buscador de clientes
        cargarClientes();
        AutoCompleteTextField<ModeloDataAutocomplet> acCliente = new AutoCompleteTextField<>(entriesCliente, txtBuscarCliente);
        // Al elegir un cliente de la lista desplegable, se carga en la venta
        acCliente.setOnSeleccion(m -> seleccionarCliente(m.getIdx()));

        // Buscador de prendas
        cargarProductos();
        AutoCompleteTextField<ModeloDataAutocomplet> acProducto = new AutoCompleteTextField<>(entriesProducto, txtBuscarProducto);
        // Al elegir una prenda, se muestran su stock y precio
        acProducto.setOnSeleccion(m -> seleccionarProducto(Long.parseLong(m.getIdx())));

        // Tabla del detalle (solo botón quitar)
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("Prenda", new ColumnInfo("nombreProducto", 300.0));
        columns.put("Cantidad", new ColumnInfo("cantidad", 90.0));
        columns.put("P. Venta S/", new ColumnInfo("precioVenta", 110.0));
        columns.put("Importe S/", new ColumnInfo("netoTotal", 110.0));
        new TableViewHelper<VentaDetalle>().addColumnsInOrderWithSize(tableView, columns, null, this::quitarPrenda);

        // Empieza con la venta vacía
        cancelarVenta();
    }

    private void cargarClientes() {
        entriesCliente.clear();
        entriesCliente.addAll(cs.listAutoCompletCliente());
    }

    private void cargarProductos() {
        entriesProducto.clear();
        entriesProducto.addAll(ps.listAutoCompletProducto());
    }

    // ------------------------------ CLIENTE ------------------------------

    /** Botón Buscar o Enter: busca por DNI/RUC exacto. */
    @FXML
    public void buscarCliente() {
        String texto = txtBuscarCliente.getText().trim();
        // Si viene del autocompletado ("DNI - Nombre"), nos quedamos solo con el número
        if (texto.contains(" - ")) texto = texto.substring(0, texto.indexOf(" - "));   // viene del autocompletado
        if (!texto.matches("\\d{8}|\\d{11}")) {
            Mensaje.error(miContenedor, "Escriba un DNI (8 dígitos) o RUC (11 dígitos)");
            return;
        }
        // Si el cliente no existe, ofrece registrarlo en ese momento
        if (!seleccionarCliente(texto)) {
            if (Mensaje.confirmar("Cliente no registrado",
                    "El documento " + texto + " no está registrado.\n¿Desea registrar al cliente ahora?")) {
                abrirDialogoCliente(texto);
            }
        }
    }

    // Busca el cliente en la BD y muestra sus datos en la pantalla
    private boolean seleccionarCliente(String dniruc) {
        return cs.buscarPorDniruc(dniruc).map(c -> {
            clienteSel = c;
            txtBuscarCliente.setText(c.getDniruc() + " - " + c.getNombres());
            lblCliNombre.setText(c.getNombres());
            lblCliDoc.setText(c.getTipoDocumento() + ": " + c.getDniruc()
                    + (c.getRepLegal() != null ? "   ·   Rep. legal: " + c.getRepLegal() : ""));
            lblCliDireccion.setText((c.getDireccion() == null ? "Sin dirección" : c.getDireccion())
                    + (c.getCelular() == null ? "" : "   ·   Cel. " + c.getCelular()));
            txtBuscarProducto.requestFocus();
            return true;
        }).orElse(false);
    }

    @FXML
    public void nuevoCliente() {
        String texto = txtBuscarCliente.getText().trim();
        abrirDialogoCliente(texto.matches("\\d{8}|\\d{11}") ? texto : "");
    }

    /** Ventana rápida para registrar un cliente sin salir de la venta. */
    private void abrirDialogoCliente(String documento) {
        // Ventana emergente (Dialog) con el formulario rápido de cliente
        Dialog<Cliente> dialog = new Dialog<>();
        dialog.setTitle("Nuevo cliente");
        dialog.setHeaderText("Registrar cliente");
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().addAll(miContenedor.getScene().getStylesheets());

        ComboBox<TipoDocumento> cbxTipo = new ComboBox<>();
        cbxTipo.getItems().setAll(TipoDocumento.values());
        cbxTipo.setValue(documento.length() == 11 ? TipoDocumento.RUC : TipoDocumento.DNI);
        TextField txtDoc = new TextField(documento);
        TextField txtNom = new TextField();
        TextField txtRep = new TextField();
        TextField txtDir = new TextField();
        TextField txtCel = new TextField();
        Label lblError = new Label();
        lblError.getStyleClass().add("msg-error");
        // El representante legal solo se habilita cuando el tipo es RUC
        txtRep.disableProperty().bind(cbxTipo.valueProperty().isNotEqualTo(TipoDocumento.RUC));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Tipo documento"), cbxTipo);
        grid.addRow(1, new Label("DNI / RUC"), txtDoc);
        grid.addRow(2, new Label("Nombres / Razón social"), txtNom);
        grid.addRow(3, new Label("Representante legal"), txtRep);
        grid.addRow(4, new Label("Dirección"), txtDir);
        grid.addRow(5, new Label("Celular"), txtCel);
        grid.add(lblError, 0, 6, 2, 1);
        txtNom.setPrefWidth(280);
        dialog.getDialogPane().setContent(grid);

        // Al completar un DNI (8) o RUC (11) se buscan los datos automáticamente (RENIEC / SUNAT)
        txtDoc.textProperty().addListener((obs, viejo, nuevo) -> {
            String d = nuevo.trim();
            if (cbxTipo.getValue() == TipoDocumento.DNI && d.matches("\\d{8}")) {
                autocompletarDni(d, txtNom, lblError);
            } else if (cbxTipo.getValue() == TipoDocumento.RUC && d.matches("\\d{11}")) {
                autocompletarRuc(d, txtNom, txtDir, lblError);
            }
        });
        if (documento.matches("\\d{8}")) {
            autocompletarDni(documento, txtNom, lblError);
        } else if (documento.matches("\\d{11}")) {
            autocompletarRuc(documento, txtNom, txtDir, lblError);
        }

        // Validación antes de cerrar la ventana
        // VALIDACIÓN antes de cerrar: si hay error, ev.consume() evita que la ventana se cierre
        Button ok = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            String doc = txtDoc.getText().trim();
            String cel = txtCel.getText().trim();
            String error = null;
            if (cbxTipo.getValue() == TipoDocumento.DNI && !doc.matches("\\d{8}")) error = "El DNI debe tener 8 dígitos";
            else if (cbxTipo.getValue() == TipoDocumento.RUC && !doc.matches("\\d{11}")) error = "El RUC debe tener 11 dígitos";
            else if (cs.existeDniruc(doc, 0L)) error = "Ya existe un cliente con ese documento";
            else if (txtNom.getText().isBlank()) error = "Escriba los nombres o la razón social";
            else if (cbxTipo.getValue() == TipoDocumento.RUC && txtRep.getText().isBlank()) error = "El RUC requiere representante legal";
            else if (!cel.isEmpty() && !cel.matches("\\d{9}")) error = "El celular debe tener 9 dígitos";
            if (error != null) {
                lblError.setText(error);
                ev.consume();   // no deja cerrar la ventana
            }
        });

        // Si presionó Guardar, convierte los campos en un objeto Cliente
        dialog.setResultConverter(bt -> bt == btnGuardar ? Cliente.builder()
                .tipoDocumento(cbxTipo.getValue())
                .dniruc(txtDoc.getText().trim())
                .nombres(txtNom.getText().trim())
                .repLegal(cbxTipo.getValue() == TipoDocumento.RUC ? txtRep.getText().trim() : null)
                .direccion(FormValidator.textoONull(txtDir.getText()))
                .celular(FormValidator.textoONull(txtCel.getText()))
                .build() : null);

        // Guarda el cliente, recarga la lista y lo deja seleccionado en la venta
        dialog.showAndWait().ifPresent(c -> {
            cs.save(c);
            cargarClientes();
            seleccionarCliente(c.getDniruc());
            Mensaje.exito(miContenedor, "Cliente registrado");
        });
    }

    /** Consulta el DNI en segundo plano (para no congelar la ventana) y llena los nombres. */
    // Consulta el DNI en otro hilo (para no congelar la ventana) y llena los nombres
    private void autocompletarDni(String dni, TextField txtNom, Label lblInfo) {
        lblInfo.setText("Consultando RENIEC...");
        Thread hilo = new Thread(() -> {
            PersonaDto encontrado;
            try {
                encontrado = consultaDNI.consultarDNI(dni);
            } catch (Exception ex) {
                encontrado = null;
            }
            PersonaDto p = encontrado;
            Platform.runLater(() -> {
                if (p != null && p.getNombre() != null) {
                    txtNom.setText(p.getNombre() + " " + p.getApellidoPaterno() + " " + p.getApellidoMaterno());
                    lblInfo.setText("");
                } else {
                    lblInfo.setText("No se encontró el DNI (o no hay internet). Escriba el nombre.");
                }
            });
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Consulta el RUC en segundo plano y llena razón social y dirección. */
    // Consulta el RUC en otro hilo y llena razón social y dirección
    private void autocompletarRuc(String ruc, TextField txtNom, TextField txtDir, Label lblInfo) {
        lblInfo.setText("Consultando SUNAT...");
        Thread hilo = new Thread(() -> {
            ConsultaRUC.EmpresaDto encontrado;
            try {
                encontrado = consultaRUC.consultarRUC(ruc);
            } catch (Exception ex) {
                encontrado = null;
            }
            ConsultaRUC.EmpresaDto e = encontrado;
            Platform.runLater(() -> {
                if (e != null) {
                    txtNom.setText(e.razonSocial());
                    if (!e.direccion().isBlank()) txtDir.setText(e.direccion());
                    lblInfo.setText("");
                } else if (!ruc.startsWith("10") && !consultaRUC.tieneToken()) {
                    lblInfo.setText("Para RUC 20 configure api.ruc.token. Escriba la razón social.");
                } else {
                    lblInfo.setText("No se encontró el RUC (o no hay internet). Escriba la razón social.");
                }
            });
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    // ------------------------------ PRENDAS ------------------------------

    // Carga la prenda elegida y muestra stock y precio de venta
    private void seleccionarProducto(Long idProducto) {
        productoSel = ps.findById(idProducto);
        lblStock.setText(String.valueOf(productoSel.getStock()));
        lblPrecio.setText(String.format("%.2f", productoSel.getPrecioVenta()));
        txtCantidad.setText("1");
        txtCantidad.requestFocus();
        txtCantidad.selectAll();
        lblMsg.setText("");
    }

    // Botón Agregar: pasa la prenda al detalle (carrito)
    @FXML
    public void agregarPrenda() {
        Integer cantidad = FormValidator.leerEntero(txtCantidad.getText());
        if (productoSel == null) {
            lblMsg.setText("Primero busque y elija una prenda de la lista");
            return;
        }
        // VALIDACIÓN: la cantidad debe ser un número entero positivo
        if (cantidad == null || cantidad <= 0) {
            lblMsg.setText("La cantidad debe ser un número entero mayor a cero");
            return;
        }
        // si la prenda ya está en el detalle, se suma la cantidad
        VentaDetalle existente = tableView.getItems().stream()
                .filter(d -> d.getIdProducto().getIdProducto().equals(productoSel.getIdProducto()))
                .findFirst().orElse(null);
        int total = cantidad + (existente == null ? 0 : existente.getCantidad());
        // VALIDACIÓN: no se puede vender más de lo que hay en stock
        if (total > productoSel.getStock()) {
            lblMsg.setText("Stock insuficiente. Disponible: " + productoSel.getStock());
            return;
        }
        // Precio de venta = costo x (1 + % utilidad / 100)
        double precioVenta = productoSel.getPrecioVenta();
        if (existente != null) {
            existente.setCantidad(total);
            existente.setNetoTotal(DocumentoComercial.redondear(precioVenta * total));
            tableView.refresh();
        } else {
            tableView.getItems().add(VentaDetalle.builder()
                    .idProducto(productoSel)
                    .precioUnit(productoSel.getPreUnitario())
                    .porceUtil(productoSel.getPorceUtil())
                    .cantidad(cantidad)
                    .netoTotal(DocumentoComercial.redondear(precioVenta * cantidad))
                    .build());
        }
        limpiarPrenda();
        calcularTotales();
        txtBuscarProducto.requestFocus();
    }

    private void quitarPrenda(VentaDetalle d) {
        tableView.getItems().remove(d);
        calcularTotales();
    }

    private void limpiarPrenda() {
        productoSel = null;
        txtBuscarProducto.clear();
        txtCantidad.clear();
        lblStock.setText("-");
        lblPrecio.setText("-");
        lblMsg.setText("");
    }

    /** Usa la misma lógica del modelo (DocumentoComercial.calcularMontos). */
    // Recalcula Neto, IGV (18 %) y Total cada vez que cambia el detalle
    private void calcularTotales() {
        Venta temp = Venta.builder().detalles(new ArrayList<>(tableView.getItems())).build();
        temp.calcularMontos();
        lblNeto.setText(String.format("S/ %.2f", temp.getNetoTotal()));
        lblIgv.setText(String.format("S/ %.2f", temp.getIgv()));
        lblTotal.setText(String.format("S/ %.2f", temp.getPrecioTotal()));
    }

    // ------------------------------ REGISTRAR ------------------------------

    // Botón Registrar venta
    @FXML
    public void registrarVenta() {
        if (clienteSel == null) {
            Mensaje.error(miContenedor, "Seleccione o registre un cliente");
            txtBuscarCliente.requestFocus();
            return;
        }
        if (tableView.getItems().isEmpty()) {
            Mensaje.error(miContenedor, "Agregue al menos una prenda");
            return;
        }
        // Arma la cabecera de la venta: fecha, cliente, vendedor (usuario en sesión) y detalle
        Venta venta = Venta.builder()
                .fechaVenta(LocalDate.now())
                .idCliente(clienteSel)
                .idUsuario(Usuario.builder().idUsuario(SessionManager.getInstance().getUserId()).build())
                .detalles(new ArrayList<>(tableView.getItems()))
                .build();
        try {
            // Guarda cabecera + detalle y descuenta stock (todo en una transacción)
            Venta guardada = vs.registrarVenta(venta);
            Mensaje.info("Venta registrada", String.format(
                    "Venta N° %d registrada correctamente.%n%nCliente: %s%nNeto: S/ %.2f%nIGV (18 %%): S/ %.2f%nTOTAL: S/ %.2f",
                    guardada.getId(), clienteSel.getNombres(),
                    guardada.getNetoTotal(), guardada.getIgv(), guardada.getPrecioTotal()));
            // Boleta (DNI) o Factura (RUC) para imprimir
            // Ofrece imprimir Boleta (DNI) o Factura (RUC)
            String tipo = Comprobante.nombreTipo(clienteSel);
            if (Mensaje.confirmar("Imprimir " + tipo, "¿Desea ver e imprimir la " + tipo.toLowerCase() + " de esta venta?")) {
                guardada.setIdCliente(clienteSel);
                Comprobante.mostrar(guardada, new ArrayList<>(tableView.getItems()),
                        SessionManager.getInstance().getUserName());
            }
            cargarProductos();   // el stock cambió
            cancelarVenta();
        } catch (Exception e) {
            Mensaje.error(miContenedor, "No se registró la venta: " + e.getMessage());
        }
    }

    // Limpia toda la venta para empezar otra
    @FXML
    public void cancelarVenta() {
        clienteSel = null;
        txtBuscarCliente.clear();
        lblCliNombre.setText("Ningún cliente seleccionado");
        lblCliDoc.setText("");
        lblCliDireccion.setText("");
        tableView.getItems().clear();
        limpiarPrenda();
        calcularTotales();
    }
}
