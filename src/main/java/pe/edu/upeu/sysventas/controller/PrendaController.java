package pe.edu.upeu.sysventas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.model.Prenda;
import pe.edu.upeu.sysventas.service.IPrendaService;

@RequiredArgsConstructor
public class PrendaController {

    private final IPrendaService ps;

    // =========================
    // CAMPOS DEL FORMULARIO
    // =========================

    @FXML
    private TextField txtNombre;

    @FXML
    private ComboBox<String> cbxCategoria;

    @FXML
    private ComboBox<String> cbxTalla;

    @FXML
    private ComboBox<String> cbxColor;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtBuscar;


    // =========================
    // MENSAJES DE ERROR
    // =========================

    @FXML
    private Label lblNombreError;

    @FXML
    private Label lblCategoriaError;

    @FXML
    private Label lblTallaError;

    @FXML
    private Label lblColorError;

    @FXML
    private Label lblPrecioError;

    @FXML
    private Label lblStockError;

    @FXML
    private Label lblMensaje;


    // =========================
    // TABLA
    // =========================

    @FXML
    private TableView<Prenda> tableView;

    @FXML
    private TableColumn<Prenda, Long> colId;

    @FXML
    private TableColumn<Prenda, String> colNombre;

    @FXML
    private TableColumn<Prenda, String> colCategoria;

    @FXML
    private TableColumn<Prenda, String> colTalla;

    @FXML
    private TableColumn<Prenda, String> colColor;

    @FXML
    private TableColumn<Prenda, Double> colPrecio;

    @FXML
    private TableColumn<Prenda, Integer> colStock;

    private ObservableList<Prenda> listaPrendas;


    // =========================
    // INICIALIZAR
    // =========================

    @FXML
    public void initialize() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("idPrenda")
        );

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>("categoria")
        );

        colTalla.setCellValueFactory(
                new PropertyValueFactory<>("talla")
        );

        colColor.setCellValueFactory(
                new PropertyValueFactory<>("color")
        );

        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio")
        );

        colStock.setCellValueFactory(
                new PropertyValueFactory<>("stock")
        );


        // =========================
        // CATEGORÍAS
        // =========================

        cbxCategoria.setItems(
                FXCollections.observableArrayList(
                        "Polo",
                        "Camisa",
                        "Pantalón",
                        "Falda",
                        "Vestido",
                        "Casaca"
                )
        );


        // =========================
        // TALLAS
        // =========================

        cbxTalla.setItems(
                FXCollections.observableArrayList(
                        "XS",
                        "S",
                        "M",
                        "L",
                        "XL"
                )
        );


        // =========================
        // COLORES
        // =========================

        cbxColor.setItems(
                FXCollections.observableArrayList(
                        "Negro",
                        "Blanco",
                        "Rojo",
                        "Azul",
                        "Verde"
                )
        );


        // =========================
        // MOSTRAR PRENDAS
        // =========================

        listar();


        // =========================
        // RESALTAR BÚSQUEDA
        // =========================

        configurarResaltado();


        // =========================
        // DETECTAR SELECCIÓN
        // =========================

        tableView.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, seleccionado) -> {
                            seleccionar();
                        }
                );
    }


    // =========================
    // LISTAR
    // =========================

    @FXML
    public void listar() {

        listaPrendas =
                FXCollections.observableArrayList(
                        ps.findAll()
                );

        tableView.getItems().clear();

        tableView.getItems().addAll(
                listaPrendas
        );

        tableView.refresh();
    }


    // =========================
    // BUSCAR POR NOMBRE
    // =========================

    @FXML
    public void buscar() {

        String texto =
                txtBuscar.getText()
                        .trim()
                        .toLowerCase();


        // =========================
        // MOSTRAR TODAS LAS PRENDAS
        // =========================

        listaPrendas =
                FXCollections.observableArrayList(
                        ps.findAll()
                );

        tableView.getItems().clear();

        tableView.getItems().addAll(
                listaPrendas
        );


        // =========================
        // SI NO ESCRIBIÓ NADA
        // =========================

        if (texto.isEmpty()) {

            lblMensaje.setText("");

            tableView.refresh();

            return;
        }


        // =========================
        // BUSCAR COINCIDENCIA
        // =========================

        Prenda encontrada = null;

        for (Prenda prenda : listaPrendas) {

            if (prenda.getNombre()
                    .toLowerCase()
                    .contains(texto)) {

                encontrada = prenda;

                break;
            }
        }


        // =========================
        // SI ENCONTRÓ
        // =========================

        if (encontrada != null) {

            tableView.getSelectionModel()
                    .select(encontrada);

            tableView.scrollTo(encontrada);

            lblMensaje.setText(
                    "Prenda encontrada."
            );

            lblMensaje.setStyle(
                    "-fx-text-fill: green; -fx-font-weight: bold;"
            );

        } else {

            lblMensaje.setText(
                    "No se encontró una prenda con ese nombre."
            );

            lblMensaje.setStyle(
                    "-fx-text-fill: red; -fx-font-weight: bold;"
            );
        }


        // Actualizar colores
        tableView.refresh();
    }


    // =========================
    // RESALTAR RESULTADO
    // =========================

    private void configurarResaltado() {

        tableView.setRowFactory(tv -> {

            TableRow<Prenda> fila =
                    new TableRow<>();

            fila.itemProperty()
                    .addListener(
                            (observable, anterior, prenda) -> {

                                if (prenda == null) {

                                    fila.setStyle("");

                                    return;
                                }

                                String texto =
                                        txtBuscar.getText()
                                                .trim()
                                                .toLowerCase();

                                if (!texto.isEmpty()
                                        && prenda.getNombre()
                                        .toLowerCase()
                                        .contains(texto)) {

                                    fila.setStyle(
                                            "-fx-background-color: yellow;"
                                    );

                                } else {

                                    fila.setStyle("");
                                }
                            }
                    );


            txtBuscar.textProperty()
                    .addListener(
                            (observable, anterior, nuevoTexto) -> {

                                String texto =
                                        nuevoTexto
                                                .trim()
                                                .toLowerCase();

                                if (fila.getItem() != null
                                        && !texto.isEmpty()
                                        && fila.getItem()
                                        .getNombre()
                                        .toLowerCase()
                                        .contains(texto)) {

                                    fila.setStyle(
                                            "-fx-background-color: yellow;"
                                    );

                                } else {

                                    fila.setStyle("");
                                }
                            }
                    );

            return fila;
        });
    }


    // =========================
    // GUARDAR
    // =========================

    @FXML
    public void guardar() {

        limpiarErrores();

        lblMensaje.setText("");

        boolean hayError = false;


        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {

            lblNombreError.setText(
                    "Falta completar el nombre."
            );

            hayError = true;
        }


        // Validar categoría
        if (cbxCategoria.getValue() == null) {

            lblCategoriaError.setText(
                    "Seleccione una categoría."
            );

            hayError = true;
        }


        // Validar talla
        if (cbxTalla.getValue() == null) {

            lblTallaError.setText(
                    "Seleccione una talla."
            );

            hayError = true;
        }


        // Validar color
        if (cbxColor.getValue() == null) {

            lblColorError.setText(
                    "Seleccione un color."
            );

            hayError = true;
        }


        // Validar precio
        if (txtPrecio.getText().trim().isEmpty()) {

            lblPrecioError.setText(
                    "Falta completar el precio."
            );

            hayError = true;
        }


        // Validar stock
        if (txtStock.getText().trim().isEmpty()) {

            lblStockError.setText(
                    "Falta completar el stock."
            );

            hayError = true;
        }


        if (hayError) {
            return;
        }


        double precio = 0;
        int stock = 0;


        // Convertir precio
        try {

            precio = Double.parseDouble(
                    txtPrecio.getText().trim()
            );

        } catch (NumberFormatException e) {

            lblPrecioError.setText(
                    "Ingrese un precio válido."
            );

            hayError = true;
        }


        // Convertir stock
        try {

            stock = Integer.parseInt(
                    txtStock.getText().trim()
            );

        } catch (NumberFormatException e) {

            lblStockError.setText(
                    "Ingrese un stock válido."
            );

            hayError = true;
        }


        // Precio negativo
        if (!hayError && precio < 0) {

            lblPrecioError.setText(
                    "El precio no puede ser negativo."
            );

            hayError = true;
        }


        // Stock negativo
        if (!hayError && stock < 0) {

            lblStockError.setText(
                    "El stock no puede ser negativo."
            );

            hayError = true;
        }


        if (hayError) {
            return;
        }


        // =========================
        // CREAR PRENDA
        // =========================

        Prenda prenda = Prenda.builder()
                .nombre(txtNombre.getText().trim())
                .categoria(cbxCategoria.getValue())
                .talla(cbxTalla.getValue())
                .color(cbxColor.getValue())
                .precio(precio)
                .stock(stock)
                .build();


        // =========================
        // GUARDAR
        // =========================

        ps.save(prenda);


        // Actualizar tabla
        listar();


        // Limpiar formulario
        limpiar();


        // Mensaje
        lblMensaje.setText(
                "¡Registro completado correctamente!"
        );

        lblMensaje.setStyle(
                "-fx-text-fill: green; -fx-font-weight: bold;"
        );
    }


    // =========================
    // EDITAR
    // =========================

    @FXML
    public void editar() {

        limpiarErrores();

        lblMensaje.setText("");

        Prenda prenda =
                tableView.getSelectionModel()
                        .getSelectedItem();


        if (prenda == null) {

            lblMensaje.setText(
                    "Seleccione una prenda para editar."
            );

            lblMensaje.setStyle(
                    "-fx-text-fill: red; -fx-font-weight: bold;"
            );

            return;
        }


        boolean hayError = false;


        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {

            lblNombreError.setText(
                    "Falta completar el nombre."
            );

            hayError = true;
        }


        // Validar categoría
        if (cbxCategoria.getValue() == null) {

            lblCategoriaError.setText(
                    "Seleccione una categoría."
            );

            hayError = true;
        }


        // Validar talla
        if (cbxTalla.getValue() == null) {

            lblTallaError.setText(
                    "Seleccione una talla."
            );

            hayError = true;
        }


        // Validar color
        if (cbxColor.getValue() == null) {

            lblColorError.setText(
                    "Seleccione un color."
            );

            hayError = true;
        }


        // Validar precio
        if (txtPrecio.getText().trim().isEmpty()) {

            lblPrecioError.setText(
                    "Falta completar el precio."
            );

            hayError = true;
        }


        // Validar stock
        if (txtStock.getText().trim().isEmpty()) {

            lblStockError.setText(
                    "Falta completar el stock."
            );

            hayError = true;
        }


        if (hayError) {
            return;
        }


        double precio = 0;
        int stock = 0;


        // Convertir precio
        try {

            precio = Double.parseDouble(
                    txtPrecio.getText().trim()
            );

        } catch (NumberFormatException e) {

            lblPrecioError.setText(
                    "Ingrese un precio válido."
            );

            hayError = true;
        }


        // Convertir stock
        try {

            stock = Integer.parseInt(
                    txtStock.getText().trim()
            );

        } catch (NumberFormatException e) {

            lblStockError.setText(
                    "Ingrese un stock válido."
            );

            hayError = true;
        }


        // Precio negativo
        if (!hayError && precio < 0) {

            lblPrecioError.setText(
                    "El precio no puede ser negativo."
            );

            hayError = true;
        }


        // Stock negativo
        if (!hayError && stock < 0) {

            lblStockError.setText(
                    "El stock no puede ser negativo."
            );

            hayError = true;
        }


        if (hayError) {
            return;
        }


        // =========================
        // ACTUALIZAR PRENDA
        // =========================

        prenda.setNombre(
                txtNombre.getText().trim()
        );

        prenda.setCategoria(
                cbxCategoria.getValue()
        );

        prenda.setTalla(
                cbxTalla.getValue()
        );

        prenda.setColor(
                cbxColor.getValue()
        );

        prenda.setPrecio(precio);

        prenda.setStock(stock);


        ps.update(
                prenda.getIdPrenda(),
                prenda
        );


        // Actualizar tabla
        listar();

        // Limpiar
        limpiar();


        // Mensaje
        lblMensaje.setText(
                "¡Prenda actualizada correctamente!"
        );

        lblMensaje.setStyle(
                "-fx-text-fill: green; -fx-font-weight: bold;"
        );
    }


    // =========================
    // SELECCIONAR
    // =========================

    @FXML
    public void seleccionar() {

        Prenda prenda =
                tableView.getSelectionModel()
                        .getSelectedItem();


        if (prenda != null) {

            txtNombre.setText(
                    prenda.getNombre()
            );

            cbxCategoria.setValue(
                    prenda.getCategoria()
            );

            cbxTalla.setValue(
                    prenda.getTalla()
            );

            cbxColor.setValue(
                    prenda.getColor()
            );

            txtPrecio.setText(
                    String.valueOf(
                            prenda.getPrecio()
                    )
            );

            txtStock.setText(
                    String.valueOf(
                            prenda.getStock()
                    )
            );

            limpiarErrores();

            lblMensaje.setText("");
        }
    }


    // =========================
    // ELIMINAR
    // =========================

    @FXML
    public void eliminar() {

        Prenda prenda =
                tableView.getSelectionModel()
                        .getSelectedItem();


        if (prenda != null) {

            ps.delete(
                    prenda.getIdPrenda()
            );

            listar();

            limpiar();

        } else {

            lblMensaje.setText(
                    "Seleccione una prenda para eliminar."
            );

            lblMensaje.setStyle(
                    "-fx-text-fill: red; -fx-font-weight: bold;"
            );
        }
    }


    // =========================
    // LIMPIAR
    // =========================

    @FXML
    public void limpiar() {

        txtNombre.clear();

        cbxCategoria.setValue(null);

        cbxTalla.setValue(null);

        cbxColor.setValue(null);

        txtPrecio.clear();

        txtStock.clear();

        limpiarErrores();

        lblMensaje.setText("");

        tableView.getSelectionModel()
                .clearSelection();
    }


    // =========================
    // LIMPIAR ERRORES
    // =========================

    private void limpiarErrores() {

        lblNombreError.setText("");

        lblCategoriaError.setText("");

        lblTallaError.setText("");

        lblColorError.setText("");

        lblPrecioError.setText("");

        lblStockError.setText("");
    }
}