package pe.edu.upeu.sysventas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.StageManager;
import pe.edu.upeu.sysventas.components.Tema;
import pe.edu.upeu.sysventas.config.AppContext;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.service.IUsuarioService;

@RequiredArgsConstructor
public class LoginController {

    // Servicio de usuarios (inyectado por AppContext)
    private final IUsuarioService usuarioService;

    @FXML TextField usuario;
    @FXML PasswordField clave;
    @FXML Label lblError;

    @FXML
    public void initialize() {
        lblError.setText("");
        Platform.runLater(() -> usuario.requestFocus());
    }

    // Botón Ingresar
    @FXML
    public void login() {
        // Validación: los dos campos son obligatorios
        if (usuario.getText().isBlank() || clave.getText().isBlank()) {
            lblError.setText("Ingrese usuario y clave");
            return;
        }
        try {
            // Busca el usuario y compara la clave encriptada (ver PasswordUtil)
            usuarioService.loginUsuario(usuario.getText().trim(), clave.getText())
                    .ifPresentOrElse(this::abrirMain,
                            () -> {
                                lblError.setText("Usuario o clave incorrectos. Intente nuevamente.");
                                clave.clear();
                            });
        } catch (Exception ex) {
            lblError.setText("No se pudo conectar a la base de datos");
            ex.printStackTrace();
        }
    }

    // Login correcto: guarda los datos de la sesión y abre la ventana principal
    private void abrirMain(Usuario usu) {
        try {
            // SessionManager recuerda quién inició sesión (se usa al registrar ventas)
            SessionManager.getInstance().setUserId(usu.getIdUsuario());
            SessionManager.getInstance().setUserName(usu.getUsuario());
            SessionManager.getInstance().setUserPerfil(usu.getIdPerfil().getNombre());

            // Carga la ventana principal y le pide los controladores a AppContext
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/maingui.fxml"));
            fxmlLoader.setControllerFactory(AppContext.getInstance()::getBean);
            Rectangle2D r2d = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), r2d.getWidth(), r2d.getHeight());
            Tema.aplicar(scene);

            Stage stage = (Stage) usuario.getScene().getWindow();
            stage.setResizable(true);
            stage.setScene(scene);
            stage.setX(r2d.getMinX());
            stage.setY(r2d.getMinY());
            stage.setWidth(r2d.getWidth());
            stage.setHeight(r2d.getHeight());
            StageManager.setPrimaryStage(stage);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void salir() {
        Platform.exit();
    }
}
