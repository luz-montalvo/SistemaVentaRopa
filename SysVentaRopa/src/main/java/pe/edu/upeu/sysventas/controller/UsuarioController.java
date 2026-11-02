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
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.model.Perfil;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.service.IPerfilService;
import pe.edu.upeu.sysventas.service.IUsuarioService;

import java.util.LinkedHashMap;

/** CRUD de la tabla Usuario (con su Perfil). Solo lo ve el Administrador. */
@RequiredArgsConstructor
public class UsuarioController {
    private final IUsuarioService us;
    private final IPerfilService perfilService;

    @FXML VBox miContenedor;
    @FXML TextField txtUsuario;
    @FXML PasswordField txtClave, txtClave2;
    @FXML ComboBox<ComboBoxOption> cbxPerfil;
    @FXML Button btnGuardar;
    @FXML Label lblMsg, lblFormTitulo;
    @FXML TableView<Usuario> tableView;

    // 0 = nuevo usuario; mayor a 0 = editando
    private Long idUsuarioEdit = 0L;

    @FXML
    public void initialize() {
        cbxPerfil.getItems().setAll(perfilService.listarCombobox());

        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID", new ColumnInfo("idUsuario", 60.0));
        columns.put("Usuario", new ColumnInfo("usuario", 260.0));
        columns.put("Perfil", new ColumnInfo("idPerfil.nombre", 240.0));
        // Muestra el hash (clave encriptada), nunca la clave real
        columns.put("Clave guardada (SHA-256 + sal)", new ColumnInfo("clave", 300.0));
        new TableViewHelper<Usuario>().addColumnsInOrderWithSize(tableView, columns, this::editar, this::eliminar);

        limpiar();
        listar();
    }

    private void listar() {
        tableView.getItems().setAll(us.findAll());
    }

    @FXML
    public void guardar() {
        String usuario = txtUsuario.getText().trim();
        String clave = txtClave.getText();

        FormValidator v = new FormValidator();
        v.limpiar(txtUsuario, txtClave, txtClave2, cbxPerfil);
        // Al editar, la clave puede quedar vacía (se mantiene la anterior)
        // Al editar, si deja las claves vacías se conserva la clave anterior
        boolean mantenerClave = idUsuarioEdit > 0 && clave.isEmpty() && txtClave2.getText().isEmpty();
        // VALIDACIONES del formulario de usuario
        v.check(usuario.isEmpty(), txtUsuario, "Escriba el nombre de usuario")
         .check(usuario.length() > 80, txtUsuario, "Máximo 80 caracteres")
         .check(us.existeUsuario(usuario, idUsuarioEdit), txtUsuario, "Ese nombre de usuario ya existe")
         .check(!mantenerClave && clave.length() < 6, txtClave, "La clave debe tener al menos 6 caracteres")
         .check(clave.length() > 100, txtClave, "Máximo 100 caracteres")
         .check(!clave.equals(txtClave2.getText()), txtClave2, "Las claves no coinciden")
         .check(cbxPerfil.getValue() == null, cbxPerfil, "Seleccione el perfil");
        if (!v.esValido(lblMsg)) return;

        // La clave va en texto aquí; el servicio la encripta antes de guardarla
        Usuario u = Usuario.builder()
                .usuario(usuario)
                .clave(clave)
                .idPerfil(Perfil.builder()
                        .idPerfil(Long.parseLong(cbxPerfil.getValue().getKey()))
                        .nombre(cbxPerfil.getValue().getValue())
                        .build())
                .build();
        try {
            if (idUsuarioEdit > 0) {
                u.setIdUsuario(idUsuarioEdit);
                us.update(idUsuarioEdit, u);
                Mensaje.exito(miContenedor, "Usuario actualizado");
            } else {
                us.save(u);
                Mensaje.exito(miContenedor, "Usuario registrado");
            }
            limpiar();
            listar();
        } catch (Exception e) {
            lblMsg.setText("Error al guardar: " + e.getMessage());
        }
    }

    // Carga el usuario elegido en el formulario (la clave no se muestra porque está encriptada)
    private void editar(Usuario u) {
        limpiar();
        idUsuarioEdit = u.getIdUsuario();
        txtUsuario.setText(u.getUsuario());
        // La clave está encriptada: no se muestra. Si la deja vacía, se conserva.
        txtClave.setPromptText("Vacío = mantener la clave actual");
        txtClave2.setPromptText("Vacío = mantener la clave actual");
        cbxPerfil.getItems().stream()
                .filter(o -> o.getKey().equals(String.valueOf(u.getIdPerfil().getIdPerfil())))
                .findFirst()
                .ifPresent(o -> cbxPerfil.getSelectionModel().select(o));
        lblFormTitulo.setText("Editar usuario #" + u.getIdUsuario());
        btnGuardar.setText("Actualizar");
    }

    private void eliminar(Usuario u) {
        // No se puede borrar el usuario que está usando el sistema
        if (u.getIdUsuario().equals(SessionManager.getInstance().getUserId())) {
            Mensaje.error(miContenedor, "No puede eliminar el usuario con el que inició sesión");
            return;
        }
        if (!Mensaje.confirmar("Eliminar usuario", "¿Eliminar al usuario \"" + u.getUsuario() + "\"?")) return;
        try {
            us.delete(u.getIdUsuario());
            Mensaje.exito(miContenedor, "Usuario eliminado");
            listar();
        } catch (Exception e) {
            Mensaje.error(miContenedor, "No se puede eliminar: el usuario tiene ventas o compras registradas");
        }
    }

    @FXML
    public void limpiar() {
        new FormValidator().limpiar(txtUsuario, txtClave, txtClave2, cbxPerfil);
        txtUsuario.clear();
        txtClave.clear();
        txtClave2.clear();
        txtClave.setPromptText("Mínimo 6 caracteres");
        txtClave2.setPromptText("");
        cbxPerfil.getSelectionModel().clearSelection();
        lblMsg.setText("");
        lblFormTitulo.setText("Nuevo usuario");
        btnGuardar.setText("Guardar");
        idUsuarioEdit = 0L;
    }
}
