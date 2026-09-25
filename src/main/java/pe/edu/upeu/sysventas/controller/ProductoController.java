package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.components.ToltipCustom;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.enums.TipoProducto;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.service.ICategoriaService;
import pe.edu.upeu.sysventas.service.IMarcaService;
import pe.edu.upeu.sysventas.service.IProductoService;
import pe.edu.upeu.sysventas.service.IUnidadMedidaService;


import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ProductoController {
    private final IMarcaService ms;
    private final ICategoriaService cs;
    private final IProductoService ps;
    private final IUnidadMedidaService ums;

    @FXML ComboBox<ComboBoxOption> cbxTipoProducto;
    @FXML ComboBox<ComboBoxOption> cbxCategoria, cbxMarca, cbxUnidadMedida;
    @FXML TextField txtNombreProducto, txtPUnit,
            txtPUnitOld, txtUtilidad, txtStock, txtStockOld, txtFiltroDato;

    @FXML private TableView<Producto> tableView;
    ObservableList<Producto> listarProducto;

    Producto formulario;
    Long idProductoCE = 0L;

    @FXML Label lbnMsg;
    @FXML private AnchorPane miContenedor;
    Stage stage;
    private Validator validator;
    private final ToltipCustom ttc=new ToltipCustom();


    @FXML
    public void initialize(){
        System.out.println("Holasss");
        cbxTipoProducto.getItems().addAll(ps.listarTipoProducto());

        cbxCategoria.getItems().addAll(cs.lisCategoria());
        cbxMarca.getItems().addAll(ms.listarCombobox());
        cbxUnidadMedida.getItems().addAll(ums.listarCombobox());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        TableViewHelper<Producto> tableViewHelper=new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns=new LinkedHashMap<>();
        columns.put("ID Prod.", new ColumnInfo("idProducto", 60.0));
        columns.put("Tipo Producto", new ColumnInfo("tipoProducto", 150.0));
        columns.put("Nombre", new ColumnInfo("nombre", 200.0));
        columns.put("P. Unitario", new ColumnInfo("pu", 150.0));
        columns.put("Utilidad", new ColumnInfo("utilidad", 100.0));
        columns.put("Marca", new ColumnInfo("idMarca.nombre", 200.0));
        columns.put("Categoria", new ColumnInfo("idCategoria.nombre", 200.0));

        Consumer<Producto> updateAction= p->{
            editForm(p);
            idProductoCE=p.getIdProducto();
        };
        Consumer<Producto> deleteAction= p->{
            ps.delete(p.getIdProducto());
            Stage stage = (Stage) miContenedor.getScene().getWindow();
            double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
            Toast.showToast(stage, "Se eliminó correctamente!!", 2000, w, h);
            listar();
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns,
                updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);
        listar();
    }

    public void listar(){
        try {
            tableView.getItems().clear();
            listarProducto= FXCollections.observableArrayList(ps.findAll());
            tableView.getItems().addAll(listarProducto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void setStage(Stage stage) {
        this.stage = stage;
        System.out.println("Llego"+stage.getTitle());
    }

    @FXML
    public void validarFormulario() {
        formulario = new Producto();
        formulario.setNombre(txtNombreProducto.getText());
        formulario.setPu(parseDoubleSafe(txtPUnit.getText()));
        formulario.setPuold(parseDoubleSafe(txtPUnitOld.getText()));
        formulario.setUtilidad(parseDoubleSafe(txtUtilidad.getText()));
        formulario.setStock(parseDoubleSafe(txtStock.getText()));
        formulario.setStockold(parseDoubleSafe(txtStockOld.getText()));

        String idxTP = cbxTipoProducto.getSelectionModel().getSelectedItem() == null ? ""
                : cbxTipoProducto.getSelectionModel().getSelectedItem().getKey();
        formulario.setTipoProducto(idxTP.equals("") ? null : TipoProducto.valueOf(idxTP));

        String idxM = cbxMarca.getSelectionModel().getSelectedItem() == null ? "0"
                : cbxMarca.getSelectionModel().getSelectedItem().getKey();
        formulario.setIdMarca(idxM.equals("0") ? null : ms.findById(Long.parseLong(idxM)));

        String idxC = cbxCategoria.getSelectionModel().getSelectedItem() == null ? "0"
                : cbxCategoria.getSelectionModel().getSelectedItem().getKey();
        formulario.setIdCategoria(idxC.equals("0") ? null : cs.findById(Long.parseLong(idxC)));

        String idxUM = cbxUnidadMedida.getSelectionModel().getSelectedItem() == null ? "0"
                : cbxUnidadMedida.getSelectionModel().getSelectedItem().getKey();
        formulario.setIdUnidad(idxUM.equals("0") ? null : ums.findById(Long.parseLong(idxUM)));

        Set<ConstraintViolation<Producto>> violaciones = validator.validate(formulario);
        List<ConstraintViolation<Producto>> violacionesOrdenadas = violaciones.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString())).toList();

        if (violacionesOrdenadas.isEmpty()) {
            procesarFormulario();

        } else {
            mostrarErroresValidacion(violacionesOrdenadas);
        }

    }

    private void mostrarErroresValidacion(List<ConstraintViolation<Producto>> violaciones) {
        limpiarError();
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombreProducto);
        campos.put("tipoProducto", cbxTipoProducto);
        campos.put("pu", txtPUnit);
        campos.put("puold", txtPUnitOld);
        campos.put("utilidad", txtUtilidad);
        campos.put("stock", txtStock);
        campos.put("stockold", txtStockOld);
        campos.put("idMarca", cbxMarca);
        campos.put("idCategoria", cbxCategoria);
        campos.put("idUnidad", cbxUnidadMedida);

        LinkedHashMap<String, String> erroresOrdenados = new LinkedHashMap<>();
        final Control[] primerCtrl = {null};
        for (String campo : campos.keySet()) {
            violaciones.stream()
                    .filter(v -> v.getPropertyPath().toString().equals(campo))
                    .findFirst().ifPresent(v -> {

                        erroresOrdenados.put(campo, v.getMessage());

                        Control c = campos.get(campo);
                        if (c != null && !c.getStyleClass().contains("text-field-error")){
                            //c.getStyleClass().add("text-field-error");
                            if (c != null) ttc.marcarError(c, v.getMessage().trim());
                        }
                        if (primerCtrl[0] == null) primerCtrl[0] = c;
                    });
        }
        if (!erroresOrdenados.isEmpty()) {
            lbnMsg.setText(erroresOrdenados.entrySet().iterator().next().getValue());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            if (primerCtrl[0] != null) Platform.runLater(primerCtrl[0]::requestFocus);
        }
    }

    private void procesarFormulario() {
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        Stage stage = (Stage) miContenedor.getScene().getWindow();

        limpiarError();
        double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
        if (idProductoCE > 0L) {
            formulario.setIdProducto(idProductoCE);
            ps.update(idProductoCE, formulario);
            Toast.showToast(stage, "Se actualizó correctamente!!", 2000, w, h);
        } else {
            ps.save(formulario);
            Toast.showToast(stage, "Se guardó correctamente!!", 2000, w, h);
        }
        clearForm(); listar();
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }


    public void editForm(Producto producto) {
        txtNombreProducto.setText(producto.getNombre());

        txtPUnit.setText(producto.getPu().toString());
        txtPUnitOld.setText(producto.getPuold().toString());
        txtUtilidad.setText(producto.getUtilidad().toString());
        txtStock.setText(producto.getStock().toString());
        txtStockOld.setText(producto.getStockold().toString());

        cbxTipoProducto.getSelectionModel().select(
                cbxTipoProducto.getItems().stream()
                        .filter(m -> m.getKey() == producto.getTipoProducto().name())
                        .findFirst().orElse(null));

        cbxMarca.getSelectionModel().select(
                cbxMarca.getItems().stream()
                        .filter(m -> Long.parseLong(m.getKey()) == producto.getIdMarca().getIdMarca())
                        .findFirst().orElse(null));
        cbxCategoria.getSelectionModel().select(
                cbxCategoria.getItems().stream()
                        .filter(c -> Long.parseLong(c.getKey()) == producto.getIdCategoria().getIdCategoria())
                        .findFirst().orElse(null));
        cbxUnidadMedida.getSelectionModel().select(
                cbxUnidadMedida.getItems().stream()
                        .filter(u -> Long.parseLong(u.getKey()) == producto.getIdUnidad().getIdUnidad())
                        .findFirst().orElse(null));
        idProductoCE = producto.getIdProducto();
        limpiarError();
    }

    public void limpiarError() {
        List.of(txtNombreProducto,
                        cbxTipoProducto,
                        txtPUnit, txtPUnitOld, txtUtilidad,
                        txtStock, txtStockOld, cbxMarca, cbxCategoria, cbxUnidadMedida)
                .forEach(c -> {c.getStyleClass().remove("text-field-error");
                    ttc.limpiarCampo(c);
                });
    }

    public void clearForm() {
        txtNombreProducto.clear();
        cbxTipoProducto.getSelectionModel().clearSelection();
        txtPUnit.clear(); txtPUnitOld.clear();
        txtUtilidad.clear(); txtStock.clear(); txtStockOld.clear();
        cbxMarca.getSelectionModel().clearSelection();
        cbxCategoria.getSelectionModel().clearSelection();
        cbxUnidadMedida.getSelectionModel().clearSelection();
        idProductoCE = 0L;
        limpiarError();
    }


}
