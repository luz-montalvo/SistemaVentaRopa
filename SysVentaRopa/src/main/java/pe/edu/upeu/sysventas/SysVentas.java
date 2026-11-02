package pe.edu.upeu.sysventas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.components.StageManager;
import pe.edu.upeu.sysventas.components.Tema;
import pe.edu.upeu.sysventas.config.AppContext;
import pe.edu.upeu.sysventas.config.DatabaseConfig;
import pe.edu.upeu.sysventas.service.IUsuarioService;

import java.io.IOException;

public class SysVentas extends Application {
    // Primer método que ejecuta JavaFX al abrir el programa
    @Override
    public void start(Stage stage) throws IOException {
        AppContext context = AppContext.getInstance();
        // Punto 6: encripta las claves que aún estén en texto plano (SHA-256 + sal).
        // Si algo falla aquí, igual se abre el sistema.
        try {
            context.getBean(IUsuarioService.class).encriptarClavesPendientes();
        } catch (Exception e) {
            System.err.println("No se pudieron encriptar las claves: " + e.getMessage());
            e.printStackTrace();
        }
        // Carga la pantalla de login; setControllerFactory hace que AppContext entregue el controlador
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        fxmlLoader.setControllerFactory(context::getBean);
        Scene scene = new Scene(fxmlLoader.load());
        Tema.aplicar(scene);
        stage.setTitle("SysVentaRopa - Sistema de Ventas de Ropa");
        stage.setResizable(false);
        stage.setScene(scene);
        StageManager.setPrimaryStage(stage);
        stage.show();
    }

    // Se ejecuta al cerrar la ventana
    @Override
    public void stop() {
        // Libera las conexiones del pool al cerrar la ventana.
        DatabaseConfig.shutdown();
    }
}
