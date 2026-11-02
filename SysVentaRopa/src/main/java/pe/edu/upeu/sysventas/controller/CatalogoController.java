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
import pe.edu.upeu.sysventas.model.Categoria;
import pe.edu.upeu.sysventas.model.UnidadMedida;
import pe.edu.upeu.sysventas.service.ICategoriaService;
import pe.edu.upeu.sysventas.service.IUnidadMedidaService;

import java.util.LinkedHashMap;

/** CRUD de las tablas Categoria y UnidadMedida en una sola pantalla. */
@RequiredArgsConstructor
public class CatalogoController {
    private final ICategoriaService cs;
    private final IUnidadMedidaService ums;

    @FXML VBox miContenedor;
    @FXML TextField txtCategoria, txtUnidad;
    @FXML Button btnGuardarCat, btnGuardarUni;
    @FXML Label lblMsgCat, lblMsgUni;
    @FXML TableView<Categoria> tableCategoria;
    @FXML TableView<UnidadMedida> tableUnidad;

    private Long idCategEdit = 0L;
    private Long idUnidadEdit = 0L;

    @FXML
    public void initialize() {
        LinkedHashMap<String, ColumnInfo> colCat = new LinkedHashMap<>();
        colCat.put("ID", new ColumnInfo("idCateg", 60.0));
        colCat.put("Categoría", new ColumnInfo("nombre", 250.0));
        new TableViewHelper<Categoria>().addColumnsInOrderWithSize(tableCategoria, colCat,
                this::editarCategoria, this::eliminarCategoria);

        LinkedHashMap<String, ColumnInfo> colUni = new LinkedHashMap<>();
        colUni.put("ID", new ColumnInfo("idUnidadMed", 60.0));
        colUni.put("Unidad de medida", new ColumnInfo("nombre", 250.0));
        new TableViewHelper<UnidadMedida>().addColumnsInOrderWithSize(tableUnidad, colUni,
                this::editarUnidad, this::eliminarUnidad);

        cancelarCategoria();
        cancelarUnidad();
        listar();
    }

    private void listar() {
        tableCategoria.getItems().setAll(cs.findAll());
        tableUnidad.getItems().setAll(ums.findAll());
    }

    // ------------------------------ CATEGORÍA ------------------------------

    @FXML
    public void guardarCategoria() {
        FormValidator v = new FormValidator();
        v.limpiar(txtCategoria);
        v.check(FormValidator.vacio(txtCategoria.getText()), txtCategoria, "Escriba el nombre de la categoría")
         .check(txtCategoria.getText().trim().length() > 60, txtCategoria, "Máximo 60 caracteres");
        if (!v.esValido(lblMsgCat)) return;

        Categoria c = Categoria.builder().nombre(txtCategoria.getText().trim()).build();
        if (idCategEdit > 0) {
            c.setIdCateg(idCategEdit);
            cs.update(idCategEdit, c);
            Mensaje.exito(miContenedor, "Categoría actualizada");
        } else {
            cs.save(c);
            Mensaje.exito(miContenedor, "Categoría registrada");
        }
        cancelarCategoria();
        listar();
    }

    private void editarCategoria(Categoria c) {
        idCategEdit = c.getIdCateg();
        txtCategoria.setText(c.getNombre());
        btnGuardarCat.setText("Actualizar");
        txtCategoria.requestFocus();
    }

    private void eliminarCategoria(Categoria c) {
        if (!Mensaje.confirmar("Eliminar", "¿Eliminar la categoría \"" + c.getNombre() + "\"?")) return;
        try {
            cs.delete(c.getIdCateg());
            Mensaje.exito(miContenedor, "Categoría eliminada");
            listar();
        } catch (Exception e) {
            Mensaje.error(miContenedor, "No se puede eliminar: hay prendas con esta categoría");
        }
    }

    @FXML
    public void cancelarCategoria() {
        new FormValidator().limpiar(txtCategoria);
        txtCategoria.clear();
        lblMsgCat.setText("");
        btnGuardarCat.setText("Guardar");
        idCategEdit = 0L;
    }

    // ------------------------------ UNIDAD DE MEDIDA ------------------------------

    @FXML
    public void guardarUnidad() {
        FormValidator v = new FormValidator();
        v.limpiar(txtUnidad);
        v.check(FormValidator.vacio(txtUnidad.getText()), txtUnidad, "Escriba el nombre de la unidad")
         .check(txtUnidad.getText().trim().length() > 50, txtUnidad, "Máximo 50 caracteres");
        if (!v.esValido(lblMsgUni)) return;

        UnidadMedida u = UnidadMedida.builder().nombre(txtUnidad.getText().trim()).build();
        if (idUnidadEdit > 0) {
            u.setIdUnidadMed(idUnidadEdit);
            ums.update(idUnidadEdit, u);
            Mensaje.exito(miContenedor, "Unidad actualizada");
        } else {
            ums.save(u);
            Mensaje.exito(miContenedor, "Unidad registrada");
        }
        cancelarUnidad();
        listar();
    }

    private void editarUnidad(UnidadMedida u) {
        idUnidadEdit = u.getIdUnidadMed();
        txtUnidad.setText(u.getNombre());
        btnGuardarUni.setText("Actualizar");
        txtUnidad.requestFocus();
    }

    private void eliminarUnidad(UnidadMedida u) {
        if (!Mensaje.confirmar("Eliminar", "¿Eliminar la unidad \"" + u.getNombre() + "\"?")) return;
        try {
            ums.delete(u.getIdUnidadMed());
            Mensaje.exito(miContenedor, "Unidad eliminada");
            listar();
        } catch (Exception e) {
            Mensaje.error(miContenedor, "No se puede eliminar: hay prendas con esta unidad");
        }
    }

    @FXML
    public void cancelarUnidad() {
        new FormValidator().limpiar(txtUnidad);
        txtUnidad.clear();
        lblMsgUni.setText("");
        btnGuardarUni.setText("Guardar");
        idUnidadEdit = 0L;
    }
}
