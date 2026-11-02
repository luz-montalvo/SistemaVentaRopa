package pe.edu.upeu.sysventas.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.Tema;
import pe.edu.upeu.sysventas.config.AppContext;
import pe.edu.upeu.sysventas.dto.MenuMenuItenTO;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.service.IMenuMenuItemDao;
import pe.edu.upeu.sysventas.utils.UtilsX;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.prefs.Preferences;

/** Ventana principal: menú lateral (según perfil) + área de contenido. */
@RequiredArgsConstructor
public class MainguiController {

    private final IMenuMenuItemDao mmiDao;
    private final DashboardController dashboard;

    @FXML BorderPane bp;
    @FXML VBox sidebar, menuBox;
    @FXML StackPane contenido;
    @FXML Button btnInicio, btnSalir;
    @FXML ComboBox<String> cbxTema, cbxIdioma;
    @FXML Label lblApp, lblAppSub, lblTema, lblIdioma, lblTitulo, lblFecha, lblInicial, lblUsuario, lblPerfil;

    private final Preferences userPrefs = Preferences.userRoot().node("sysventaropa");
    private final UtilsX util = new UtilsX();
    private Properties idioma = new Properties();
    private Button botonActivo;
    private String fxmlActual;

    @FXML
    public void initialize() {
        SessionManager s = SessionManager.getInstance();
        lblUsuario.setText(s.getUserName());
        lblPerfil.setText(s.getUserPerfil());
        lblInicial.setText(s.getUserName().substring(0, 1).toUpperCase());
        lblFecha.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM yyyy", Locale.of("es", "PE"))));

        cbxTema.setItems(FXCollections.observableArrayList(Tema.TEMAS));
        cbxTema.getSelectionModel().select(Tema.actual());
        cbxTema.setOnAction(e -> cambiarTema());

        cbxIdioma.setItems(FXCollections.observableArrayList("Español", "English", "Français"));
        String cod = userPrefs.get("IDIOMAX", "es");
        cbxIdioma.getSelectionModel().select(cod.equals("en") ? 1 : cod.equals("fr") ? 2 : 0);
        cbxIdioma.setOnAction(e -> cambiarIdioma());

        fxmlActual = null;
        graficarMenus();
        irInicio();
    }

    /** Dibuja las opciones del menú lateral agrupadas por sección. */
    private void graficarMenus() {
        idioma = util.detectLanguage(userPrefs.get("IDIOMAX", "es"));
        lblApp.setText(idioma.getProperty("app.nombre", "SysVentaRopa"));
        lblAppSub.setText(idioma.getProperty("app.sub", "Tienda de ropa"));
        lblTema.setText(idioma.getProperty("label.tema", "Tema").toUpperCase());
        lblIdioma.setText(idioma.getProperty("label.idioma", "Idioma").toUpperCase());
        btnInicio.setText(idioma.getProperty("label.inicio", "Inicio"));

        List<MenuMenuItenTO> lista = mmiDao.listaAccesos(SessionManager.getInstance().getUserPerfil(), idioma);
        menuBox.getChildren().clear();
        String seccion = null;
        for (MenuMenuItenTO op : lista) {
            if (op.getTipoTab().equals("C")) {          // cerrar sesión va abajo
                btnSalir.setText(op.getMenuitemnombre());
                continue;
            }
            if (!op.getMenunombre().equals(seccion)) {
                seccion = op.getMenunombre();
                Label lbl = new Label(seccion.toUpperCase());
                lbl.getStyleClass().add("menu-seccion");
                menuBox.getChildren().add(lbl);
            }
            Button b = new Button(op.getMenuitemnombre());
            b.getStyleClass().add("menu-btn");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> abrirPantalla(op.getRutaFile(), op.getNombreTab(), b));
            if (op.getRutaFile().equals(fxmlActual)) marcarActivo(b);
            menuBox.getChildren().add(b);
        }
        if (fxmlActual == null) marcarActivo(btnInicio);
    }

    private void marcarActivo(Button b) {
        if (botonActivo != null) botonActivo.getStyleClass().remove("activo");
        botonActivo = b;
        if (!b.getStyleClass().contains("activo")) b.getStyleClass().add("activo");
    }

    /** Carga un FXML dentro del área central. */
    private void abrirPantalla(String fxmlPath, String titulo, Button boton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Parent root = loader.load();
            ScrollPane scroll = new ScrollPane(root);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(true);
            contenido.getChildren().setAll(scroll);
            lblTitulo.setText(titulo);
            fxmlActual = fxmlPath;
            marcarActivo(boton);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la pantalla: " + ex.getMessage()).showAndWait();
        }
    }

    /** Pantalla de inicio: Dashboard con los indicadores según el perfil. */
    @FXML
    public void irInicio() {
        contenido.getChildren().setAll(dashboard.crear(idioma));
        lblTitulo.setText(idioma.getProperty("label.inicio", "Inicio"));
        fxmlActual = null;
        marcarActivo(btnInicio);
    }

    private void cambiarTema() {
        Tema.guardar(cbxTema.getValue());
        Tema.aplicar(bp.getScene());
    }

    private void cambiarIdioma() {
        String cod = switch (cbxIdioma.getSelectionModel().getSelectedIndex()) {
            case 1 -> "en";
            case 2 -> "fr";
            default -> "es";
        };
        userPrefs.put("IDIOMAX", cod);
        graficarMenus();
        if (fxmlActual == null) irInicio();
    }

    /** Vuelve a la pantalla de login. */
    @FXML
    public void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Scene scene = new Scene(loader.load());
            Tema.aplicar(scene);
            Stage stage = (Stage) bp.getScene().getWindow();
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
