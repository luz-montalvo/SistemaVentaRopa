package pe.edu.upeu.sysventas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.FormValidator;
import pe.edu.upeu.sysventas.components.Mensaje;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.dto.PersonaDto;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.service.IClienteService;
import pe.edu.upeu.sysventas.utils.ConsultaDNI;
import pe.edu.upeu.sysventas.utils.ConsultaRUC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** CRUD de la tabla Cliente. */
@RequiredArgsConstructor
public class ClienteController {
    private final IClienteService cs;
    private final ConsultaDNI consultaDNI;
    private final ConsultaRUC consultaRUC;

    // Componentes de la pantalla (main_cliente.fxml). @FXML los conecta por su fx:id
    @FXML VBox miContenedor;
    @FXML ComboBox<TipoDocumento> cbxTipoDoc;
    @FXML TextField txtDniruc, txtNombres, txtRepLegal, txtDireccion, txtCelular, txtFiltro;
    @FXML Button btnGuardar, btnReniec;
    @FXML Label lblMsg, lblFormTitulo;
    @FXML TableView<Cliente> tableView;

    // Lista completa de clientes traída de la BD (se usa para filtrar sin volver a consultar)
    private List<Cliente> lista = new ArrayList<>();
    // 0 = registrando uno nuevo; mayor a 0 = editando ese cliente
    private Long idClienteEdit = 0L;

    // Se ejecuta solo al abrir la pantalla
    @FXML
    public void initialize() {
        // Llena el combo con DNI y RUC
        cbxTipoDoc.getItems().setAll(TipoDocumento.values());

        // Columnas de la tabla: título visible -> atributo del modelo Cliente y ancho
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID", new ColumnInfo("idCliente", 45.0));
        columns.put("Tipo", new ColumnInfo("tipoDocumento", 55.0));
        columns.put("DNI / RUC", new ColumnInfo("dniruc", 100.0));
        columns.put("Nombres / Razón social", new ColumnInfo("nombres", 200.0));
        columns.put("Rep. legal", new ColumnInfo("repLegal", 150.0));
        columns.put("Dirección", new ColumnInfo("direccion", 180.0));
        columns.put("Celular", new ColumnInfo("celular", 90.0));
        // Crea las columnas y agrega los botones Editar y Eliminar en cada fila
        new TableViewHelper<Cliente>().addColumnsInOrderWithSize(tableView, columns, this::editar, this::eliminar);

        // Búsqueda automática: al completar 8 dígitos (DNI) u 11 (RUC) se consultan los datos
        txtDniruc.textProperty().addListener((obs, viejo, nuevo) -> {
            String d = nuevo == null ? "" : nuevo.trim();
            if (idClienteEdit == 0 && txtNombres.getText().isBlank()
                    && ((cbxTipoDoc.getValue() == TipoDocumento.DNI && d.matches("\\d{8}"))
                    || (cbxTipoDoc.getValue() == TipoDocumento.RUC && d.matches("\\d{11}")))) {
                consultarReniec();
            }
        });

        // Deja el formulario vacío y carga la tabla
        limpiar();
        listar();
    }

    private void listar() {
        // Pide todos los clientes al servicio (Controller -> Service -> Repository -> BD)
        lista = cs.findAll();
        filtrar();
    }

    @FXML
    public void filtrar() {
        // Texto del buscador en minúsculas para comparar sin importar mayúsculas
        String t = txtFiltro.getText() == null ? "" : txtFiltro.getText().trim().toLowerCase();
        tableView.getItems().setAll(lista.stream()
                // Deja solo los clientes cuyo DNI/RUC o nombre contengan el texto
                .filter(c -> c.getDniruc().contains(t) || c.getNombres().toLowerCase().contains(t))
                .toList());
    }

    /** DNI = persona natural (sin representante). RUC = empresa (con representante legal). */
    @FXML
    public void cambiarTipo() {
        // Solo el RUC (empresa) usa representante legal
        boolean esRuc = cbxTipoDoc.getValue() == TipoDocumento.RUC;
        txtRepLegal.setDisable(!esRuc);
        if (!esRuc) txtRepLegal.clear();
    }

    /** Busca los datos en RENIEC (DNI) o SUNAT (RUC) sin congelar la pantalla. */
    @FXML
    public void consultarReniec() {
        String doc = txtDniruc.getText().trim();
        // Decide si consulta RENIEC (8 dígitos) o SUNAT (11 dígitos)
        if (doc.matches("\\d{8}")) {
            consultarDni(doc);
        } else if (doc.matches("\\d{11}")) {
            consultarRuc(doc);
        } else {
            lblMsg.setText("Escriba un DNI (8 dígitos) o RUC (11 dígitos) para consultar");
        }
    }

    private void consultarDni(String dni) {
        lblMsg.setText("Consultando RENIEC...");
        btnReniec.setDisable(true);
        // La consulta por internet se hace en otro hilo para que la ventana no se congele
        Thread hilo = new Thread(() -> {
            PersonaDto encontrado;
            try {
                // Llama a ConsultaDNI (lee los datos de eldni.com)
                encontrado = consultaDNI.consultarDNI(dni);
            } catch (Exception ex) {
                encontrado = null;
            }
            PersonaDto p = encontrado;
            // Volvemos al hilo de JavaFX para poder modificar la pantalla
            Platform.runLater(() -> {
                btnReniec.setDisable(false);
                if (p != null && p.getNombre() != null) {
                    cbxTipoDoc.setValue(TipoDocumento.DNI);
                    txtNombres.setText(p.getNombre() + " " + p.getApellidoPaterno() + " " + p.getApellidoMaterno());
                    lblMsg.setText("");
                } else {
                    lblMsg.setText("No se encontró el DNI (o no hay internet). Escriba el nombre manualmente.");
                }
            });
        });
        // Daemon: si se cierra el programa, este hilo también se cierra
        hilo.setDaemon(true);
        hilo.start();
    }

    private void consultarRuc(String ruc) {
        lblMsg.setText("Consultando SUNAT...");
        btnReniec.setDisable(true);
        Thread hilo = new Thread(() -> {
            ConsultaRUC.EmpresaDto encontrado;
            try {
                // Llama a ConsultaRUC (RUC 10 usa el DNI; RUC 20 usa la API de SUNAT con token)
                encontrado = consultaRUC.consultarRUC(ruc);
            } catch (Exception ex) {
                encontrado = null;
            }
            ConsultaRUC.EmpresaDto e = encontrado;
            Platform.runLater(() -> {
                btnReniec.setDisable(false);
                if (e != null) {
                    cbxTipoDoc.setValue(TipoDocumento.RUC);
                    txtNombres.setText(e.razonSocial());
                    if (!e.direccion().isBlank()) txtDireccion.setText(e.direccion());
                    lblMsg.setText("");
                } else if (!ruc.startsWith("10") && !consultaRUC.tieneToken()) {
                    lblMsg.setText("Para RUC 20 configure api.ruc.token en application.properties. Escriba la razón social.");
                } else {
                    lblMsg.setText("No se encontró el RUC (o no hay internet). Escriba la razón social manualmente.");
                }
            });
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    public void guardar() {
        TipoDocumento tipo = cbxTipoDoc.getValue();
        String doc = txtDniruc.getText().trim();
        String celular = txtCelular.getText().trim();

        // VALIDACIONES (punto 5): cada check marca en rojo el campo si la condición es verdadera
        FormValidator v = new FormValidator();
        // Quita las marcas rojas de un intento anterior
        v.limpiar(cbxTipoDoc, txtDniruc, txtNombres, txtRepLegal, txtDireccion, txtCelular);
        v.check(tipo == null, cbxTipoDoc, "Seleccione el tipo de documento")
         // El DNI debe tener exactamente 8 números y el RUC 11
         .check(tipo == TipoDocumento.DNI && !doc.matches("\\d{8}"), txtDniruc, "El DNI debe tener 8 dígitos")
         .check(tipo == TipoDocumento.RUC && !doc.matches("\\d{11}"), txtDniruc, "El RUC debe tener 11 dígitos")
         // No se permiten dos clientes con el mismo documento
         .check(!doc.isEmpty() && cs.existeDniruc(doc, idClienteEdit), txtDniruc, "Ya existe un cliente con ese DNI/RUC")
         .check(FormValidator.vacio(txtNombres.getText()), txtNombres, "Escriba los nombres o la razón social")
         .check(txtNombres.getText().trim().length() > 120, txtNombres, "Máximo 120 caracteres")
         .check(tipo == TipoDocumento.RUC && FormValidator.vacio(txtRepLegal.getText()), txtRepLegal,
                 "Para RUC el representante legal es obligatorio")
         .check(txtDireccion.getText().trim().length() > 150, txtDireccion, "Máximo 150 caracteres")
         // El celular es opcional, pero si se escribe debe tener 9 números
         .check(!celular.isEmpty() && !celular.matches("\\d{9}"), txtCelular, "El celular debe tener 9 dígitos");
        // Si hay algún error se muestra el mensaje y no se guarda
        if (!v.esValido(lblMsg)) return;

        // Arma el objeto Cliente con los datos del formulario (patrón Builder de Lombok)
        Cliente c = Cliente.builder()
                .tipoDocumento(tipo)
                .dniruc(doc)
                .nombres(txtNombres.getText().trim())
                .repLegal(tipo == TipoDocumento.RUC ? txtRepLegal.getText().trim() : null)
                .direccion(FormValidator.textoONull(txtDireccion.getText()))
                .celular(FormValidator.textoONull(celular))
                .build();
        try {
            // Si estamos editando -> UPDATE; si no -> INSERT
            if (idClienteEdit > 0) {
                c.setIdCliente(idClienteEdit);
                cs.update(idClienteEdit, c);
                // Mensaje de la acción realizada (Toast verde)
                Mensaje.exito(miContenedor, "Cliente actualizado");
            } else {
                cs.save(c);
                Mensaje.exito(miContenedor, "Cliente registrado");
            }
            // Limpia el formulario y recarga la tabla con el cambio
            limpiar();
            listar();
        } catch (Exception e) {
            lblMsg.setText("Error al guardar: " + e.getMessage());
        }
    }

    // Pasa los datos de la fila elegida al formulario para modificarlos
    private void editar(Cliente c) {
        limpiar();
        idClienteEdit = c.getIdCliente();
        cbxTipoDoc.setValue(c.getTipoDocumento());
        cambiarTipo();
        txtDniruc.setText(c.getDniruc());
        txtNombres.setText(c.getNombres());
        txtRepLegal.setText(c.getRepLegal() == null ? "" : c.getRepLegal());
        txtDireccion.setText(c.getDireccion() == null ? "" : c.getDireccion());
        txtCelular.setText(c.getCelular() == null ? "" : c.getCelular());
        lblFormTitulo.setText("Editar cliente #" + c.getIdCliente());
        btnGuardar.setText("Actualizar");
    }

    // Pide confirmación y elimina. Si el cliente tiene ventas, la BD no deja borrarlo (llave foránea)
    private void eliminar(Cliente c) {
        if (!Mensaje.confirmar("Eliminar cliente", "¿Eliminar al cliente \"" + c.getNombres() + "\"?")) return;
        try {
            cs.delete(c.getIdCliente());
            Mensaje.exito(miContenedor, "Cliente eliminado");
            listar();
        } catch (Exception e) {
            Mensaje.error(miContenedor, "No se puede eliminar: el cliente tiene ventas registradas");
        }
    }

    // Deja el formulario como nuevo
    @FXML
    public void limpiar() {
        new FormValidator().limpiar(cbxTipoDoc, txtDniruc, txtNombres, txtRepLegal, txtDireccion, txtCelular);
        cbxTipoDoc.setValue(TipoDocumento.DNI);
        cambiarTipo();
        txtDniruc.clear();
        txtNombres.clear();
        txtRepLegal.clear();
        txtDireccion.clear();
        txtCelular.clear();
        lblMsg.setText("");
        lblFormTitulo.setText("Nuevo cliente");
        btnGuardar.setText("Guardar");
        idClienteEdit = 0L;
    }
}
