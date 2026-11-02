package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.FormValidator;
import pe.edu.upeu.sysventas.components.Mensaje;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.model.Proveedor;
import pe.edu.upeu.sysventas.service.IProveedorService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** CRUD de la tabla proveedor. */
@RequiredArgsConstructor
public class ProveedorController {
    private final IProveedorService ps;

    @FXML VBox miContenedor;
    @FXML TextField txtRuc, txtRazonSocial, txtContacto, txtDireccion, txtCelular, txtEmail, txtFiltro;
    @FXML Button btnGuardar;
    @FXML Label lblMsg, lblFormTitulo;
    @FXML TableView<Proveedor> tableView;

    private List<Proveedor> lista = new ArrayList<>();
    private Long idProveedorEdit = 0L;

    @FXML
    public void initialize() {
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID", new ColumnInfo("idProveedor", 45.0));
        columns.put("RUC", new ColumnInfo("ruc", 100.0));
        columns.put("Razón social", new ColumnInfo("razonSocial", 210.0));
        columns.put("Contacto", new ColumnInfo("contacto", 130.0));
        columns.put("Dirección", new ColumnInfo("direccion", 190.0));
        columns.put("Celular", new ColumnInfo("celular", 90.0));
        columns.put("Email", new ColumnInfo("email", 170.0));
        new TableViewHelper<Proveedor>().addColumnsInOrderWithSize(tableView, columns, this::editar, this::eliminar);

        limpiar();
        listar();
    }

    private void listar() {
        lista = ps.findAll();
        filtrar();
    }

    @FXML
    public void filtrar() {
        String t = txtFiltro.getText() == null ? "" : txtFiltro.getText().trim().toLowerCase();
        tableView.getItems().setAll(lista.stream()
                .filter(p -> p.getRuc().contains(t) || p.getRazonSocial().toLowerCase().contains(t))
                .toList());
    }

    @FXML
    public void guardar() {
        String ruc = txtRuc.getText().trim();
        String celular = txtCelular.getText().trim();
        String email = txtEmail.getText().trim();

        FormValidator v = new FormValidator();
        v.limpiar(txtRuc, txtRazonSocial, txtContacto, txtDireccion, txtCelular, txtEmail);
        v.check(!ruc.matches("\\d{11}"), txtRuc, "El RUC debe tener 11 dígitos")
         .check(ps.existeRuc(ruc, idProveedorEdit), txtRuc, "Ya existe un proveedor con ese RUC")
         .check(FormValidator.vacio(txtRazonSocial.getText()), txtRazonSocial, "Escriba la razón social")
         .check(txtRazonSocial.getText().trim().length() > 150, txtRazonSocial, "Máximo 150 caracteres")
         .check(txtContacto.getText().trim().length() > 120, txtContacto, "Máximo 120 caracteres")
         .check(txtDireccion.getText().trim().length() > 150, txtDireccion, "Máximo 150 caracteres")
         .check(!celular.isEmpty() && !celular.matches("\\d{9}"), txtCelular, "El celular debe tener 9 dígitos")
         .check(!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"), txtEmail, "Email no válido")
         .check(email.length() > 100, txtEmail, "Máximo 100 caracteres");
        if (!v.esValido(lblMsg)) return;

        Proveedor p = Proveedor.builder()
                .ruc(ruc)
                .razonSocial(txtRazonSocial.getText().trim())
                .contacto(FormValidator.textoONull(txtContacto.getText()))
                .direccion(FormValidator.textoONull(txtDireccion.getText()))
                .celular(FormValidator.textoONull(celular))
                .email(FormValidator.textoONull(email))
                .build();
        try {
            if (idProveedorEdit > 0) {
                p.setIdProveedor(idProveedorEdit);
                ps.update(idProveedorEdit, p);
                Mensaje.exito(miContenedor, "Proveedor actualizado");
            } else {
                ps.save(p);
                Mensaje.exito(miContenedor, "Proveedor registrado");
            }
            limpiar();
            listar();
        } catch (Exception e) {
            lblMsg.setText("Error al guardar: " + e.getMessage());
        }
    }

    private void editar(Proveedor p) {
        limpiar();
        idProveedorEdit = p.getIdProveedor();
        txtRuc.setText(p.getRuc());
        txtRazonSocial.setText(p.getRazonSocial());
        txtContacto.setText(p.getContacto() == null ? "" : p.getContacto());
        txtDireccion.setText(p.getDireccion() == null ? "" : p.getDireccion());
        txtCelular.setText(p.getCelular() == null ? "" : p.getCelular());
        txtEmail.setText(p.getEmail() == null ? "" : p.getEmail());
        lblFormTitulo.setText("Editar proveedor #" + p.getIdProveedor());
        btnGuardar.setText("Actualizar");
    }

    private void eliminar(Proveedor p) {
        if (!Mensaje.confirmar("Eliminar proveedor", "¿Eliminar al proveedor \"" + p.getRazonSocial() + "\"?")) return;
        try {
            ps.delete(p.getIdProveedor());
            Mensaje.exito(miContenedor, "Proveedor eliminado");
            listar();
        } catch (Exception e) {
            Mensaje.error(miContenedor, "No se puede eliminar: el proveedor tiene compras registradas");
        }
    }

    @FXML
    public void limpiar() {
        new FormValidator().limpiar(txtRuc, txtRazonSocial, txtContacto, txtDireccion, txtCelular, txtEmail);
        txtRuc.clear();
        txtRazonSocial.clear();
        txtContacto.clear();
        txtDireccion.clear();
        txtCelular.clear();
        txtEmail.clear();
        lblMsg.setText("");
        lblFormTitulo.setText("Nuevo proveedor");
        btnGuardar.setText("Guardar");
        idProveedorEdit = 0L;
    }
}
