package ni.uam.edu.distribuidoragueguense.Controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import ni.uam.edu.distribuidoragueguense.Dao.DistribuidoraDao;
import ni.uam.edu.distribuidoragueguense.Modelo.Trabajador;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DistribuidoraController {

    private final DistribuidoraDao dao = new DistribuidoraDao();
    private final ObservableList<Trabajador> datosTabla = FXCollections.observableArrayList();
    private Trabajador trabajadorEnEdicion = null;

    // --- CONTROLES DEL FORMULARIO ---
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbCargo;
    @FXML private ListView<String> lvArea;
    @FXML private DatePicker dpFechaContratacion;
    @FXML private RadioButton rbIndefinido;
    @FXML private RadioButton rbTemporal;
    @FXML private CheckBox chkSeguroMedico;
    @FXML private CheckBox chkViaticos;
    @FXML private CheckBox chkBonoAlimentacion;
    @FXML private CheckBox chkCapacitaciones;
    @FXML private ImageView imgInstitucional;

    // --- BOTONES ---
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEliminar;

    // --- TABLA Y COLUMNAS ---
    @FXML private TableView<Trabajador> tblTrabajadores;
    @FXML private TableColumn<Trabajador, String> colNombreCompleto;
    @FXML private TableColumn<Trabajador, String> colCargo;
    @FXML private TableColumn<Trabajador, String> colArea;
    @FXML private TableColumn<Trabajador, String> colFechaContratacion;
    @FXML private TableColumn<Trabajador, String> colTipoContrato;
    @FXML private TableColumn<Trabajador, String> colBeneficios;

    private ToggleGroup tgTipoContrato;

    @FXML
    public void initialize() {
        inicializarCargos();
        inicializarAreas();
        configurarTipoContrato();
        configurarTabla();
        configurarEventosTabla();
    }

    private void inicializarCargos() {
        if (cmbCargo != null) {
            cmbCargo.getItems().setAll(
                    "Gerente General",
                    "Supervisor de Ventas",
                    "Ejecutivo de Cuentas",
                    "Contador General",
                    "Analista de Inventario",
                    "Conductor / Repartidor",
                    "Auxiliar de Bodega"
            );
        }
    }

    private void inicializarAreas() {
        if (lvArea != null) {
            lvArea.getItems().setAll(
                    "Ventas y Distribución",
                    "Logística y Bodega",
                    "Administración y Finanzas",
                    "Recursos Humanos",
                    "Tecnología e Informática"
            );
        }
    }

    private void configurarTipoContrato() {
        tgTipoContrato = new ToggleGroup();
        if (rbIndefinido != null) rbIndefinido.setToggleGroup(tgTipoContrato);
        if (rbTemporal != null) rbTemporal.setToggleGroup(tgTipoContrato);
    }

    private void configurarTabla() {
        if (tblTrabajadores == null) return;

        if (colNombreCompleto != null) {
            colNombreCompleto.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getNombreCompleto()));
        }
        if (colCargo != null) {
            colCargo.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getCargo()));
        }
        if (colArea != null) {
            colArea.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getArea()));
        }
        if (colFechaContratacion != null) {
            colFechaContratacion.setCellValueFactory(f -> {
                LocalDate fecha = f.getValue().getFechaContratacion();
                return new SimpleStringProperty(fecha == null ? "" : fecha.toString());
            });
        }
        if (colTipoContrato != null) {
            colTipoContrato.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getTipoContrato()));
        }
        if (colBeneficios != null) {
            colBeneficios.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBeneficios()));
        }

        tblTrabajadores.setItems(datosTabla);

        // --- CONTEXT MENU SOBRE TABLEVIEW ---
        MenuItem miEditar = new MenuItem("Editar");
        MenuItem miEliminar = new MenuItem("Eliminar");

        miEditar.setOnAction(e -> cargarDatosSeleccionados());
        miEliminar.setOnAction(e -> eliminarTrabajador());

        ContextMenu contextMenu = new ContextMenu(miEditar, miEliminar);
        tblTrabajadores.setContextMenu(contextMenu);
    }

    private void configurarEventosTabla() {
        if (tblTrabajadores == null) return;

        // Doble clic en una fila para cargar datos
        tblTrabajadores.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                cargarDatosSeleccionados();
            }
        });
    }

    // =========================================================
    // EVENTOS ACTIONEVENT (Botones, Toolbar, Menús)
    // =========================================================

    @FXML
    public void btnGuardarOnAction(ActionEvent event) {
        if (trabajadorEnEdicion != null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Modo Edición Activo",
                    "Está editando un trabajador existente. Presione 'Actualizar' para guardar cambios o 'Limpiar' para cancelar.");
            return;
        }

        if (!validarFormulario()) return;

        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        String cargo = cmbCargo.getValue();
        String area = lvArea.getSelectionModel().getSelectedItem();
        LocalDate fechaContratacion = dpFechaContratacion.getValue();
        RadioButton rbSeleccionado = (RadioButton) tgTipoContrato.getSelectedToggle();
        String tipoContrato = rbSeleccionado != null ? rbSeleccionado.getText() : "";
        String beneficios = obtenerBeneficiosSeleccionados();

        if (dao.existeUsuario(usuario)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Usuario duplicado",
                    "El nombre de usuario '" + usuario + "' ya pertenece a otro colaborador.");
            return;
        }

        Trabajador nuevo = new Trabajador(nombres, apellidos, usuario, password,
                cargo, area, fechaContratacion, tipoContrato, beneficios);

        dao.agregar(nuevo);
        actualizarVistaTabla();
        limpiarCampos();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso",
                "El colaborador fue registrado correctamente.");
    }

    @FXML
    public void btnActualizarOnAction(ActionEvent event) {
        if (trabajadorEnEdicion == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ningún colaborador en edición",
                    "Haga doble clic en una fila de la tabla o use el menú contextual 'Editar' para cargar los datos antes de actualizar.");
            return;
        }

        if (!validarFormulario()) return;

        trabajadorEnEdicion.setNombres(txtNombres.getText().trim());
        trabajadorEnEdicion.setApellidos(txtApellidos.getText().trim());
        trabajadorEnEdicion.setPassword(txtPassword.getText());
        trabajadorEnEdicion.setCargo(cmbCargo.getValue());
        trabajadorEnEdicion.setArea(lvArea.getSelectionModel().getSelectedItem());
        trabajadorEnEdicion.setFechaContratacion(dpFechaContratacion.getValue());

        RadioButton rbSeleccionado = (RadioButton) tgTipoContrato.getSelectedToggle();
        if (rbSeleccionado != null) {
            trabajadorEnEdicion.setTipoContrato(rbSeleccionado.getText());
        }
        trabajadorEnEdicion.setBeneficios(obtenerBeneficiosSeleccionados());

        dao.actualizar(trabajadorEnEdicion);
        actualizarVistaTabla();
        limpiarCampos();
        trabajadorEnEdicion = null;

        mostrarAlerta(Alert.AlertType.INFORMATION, "Actualización Exitosa",
                "Los datos del colaborador han sido actualizados correctamente.");
    }

    @FXML
    public void btnLimpiarOnAction(ActionEvent event) {
        limpiarCampos();
        trabajadorEnEdicion = null;
    }

    @FXML
    public void btnEliminarOnAction(ActionEvent event) {
        eliminarTrabajador();
    }

    // =========================================================
    // EVENTOS MOUSEEVENT Y KEYEVENT
    // =========================================================

    @FXML
    public void tblTrabajadoresOnMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            cargarDatosSeleccionados();
        }
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (trabajadorEnEdicion != null) {
                btnActualizarOnAction(null);
            } else {
                btnGuardarOnAction(null);
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            btnLimpiarOnAction(null);
        }
    }

    // =========================================================
    // EVENTOS DE MENÚ (MenuBar)
    // =========================================================

    @FXML
    public void menuNuevoOnAction(ActionEvent event) {
        btnLimpiarOnAction(event);
    }

    @FXML
    public void menuSalirOnAction(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void menuAcercaDeOnAction(ActionEvent event) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Acerca de",
                "Distribuidora El Güegüense\nSistema de Gestión y Registro de Colaboradores\nVersión 1.0 - 2026\nDesarrollado con JavaFX.");
    }

    // =========================================================
    // MÉTODOS DE SOPORTE Y VALIDACIONES
    // =========================================================

    private void cargarDatosSeleccionados() {
        if (tblTrabajadores == null) return;
        Trabajador seleccionado = tblTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Seleccione un colaborador de la tabla.");
            return;
        }

        trabajadorEnEdicion = seleccionado;

        if (txtNombres != null) txtNombres.setText(seleccionado.getNombres());
        if (txtApellidos != null) txtApellidos.setText(seleccionado.getApellidos());
        if (txtUsuario != null) {
            txtUsuario.setText(seleccionado.getUsuario());
            txtUsuario.setDisable(true); // El usuario no se modifica porque es la clave única
        }
        if (txtPassword != null) txtPassword.setText(seleccionado.getPassword());
        if (cmbCargo != null) cmbCargo.setValue(seleccionado.getCargo());
        if (lvArea != null) lvArea.getSelectionModel().select(seleccionado.getArea());
        if (dpFechaContratacion != null) dpFechaContratacion.setValue(seleccionado.getFechaContratacion());

        if (tgTipoContrato != null && seleccionado.getTipoContrato() != null) {
            if (rbIndefinido != null && rbIndefinido.getText().equalsIgnoreCase(seleccionado.getTipoContrato())) {
                rbIndefinido.setSelected(true);
            } else if (rbTemporal != null && rbTemporal.getText().equalsIgnoreCase(seleccionado.getTipoContrato())) {
                rbTemporal.setSelected(true);
            }
        }

        // Marcar los beneficios
        String ben = seleccionado.getBeneficios() != null ? seleccionado.getBeneficios() : "";
        if (chkSeguroMedico != null) chkSeguroMedico.setSelected(ben.contains("Seguro Médico"));
        if (chkViaticos != null) chkViaticos.setSelected(ben.contains("Viáticos"));
        if (chkBonoAlimentacion != null) chkBonoAlimentacion.setSelected(ben.contains("Bono de Alimentación"));
        if (chkCapacitaciones != null) chkCapacitaciones.setSelected(ben.contains("Capacitaciones"));
    }

    private void eliminarTrabajador() {
        if (tblTrabajadores == null) return;
        Trabajador seleccionado = tblTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Seleccione el colaborador que desea eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro de eliminar al colaborador: " + seleccionado.getNombreCompleto() + "?");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                dao.eliminar(seleccionado);
                actualizarVistaTabla();
                if (trabajadorEnEdicion == seleccionado) {
                    limpiarCampos();
                    trabajadorEnEdicion = null;
                }
                mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Colaborador eliminado correctamente.");
            }
        });
    }

    private boolean validarFormulario() {
        if (txtNombres == null || txtNombres.getText() == null || txtNombres.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe ingresar los nombres del colaborador.");
            return false;
        }

        if (txtApellidos == null || txtApellidos.getText() == null || txtApellidos.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe ingresar los apellidos del colaborador.");
            return false;
        }

        if (txtUsuario == null || txtUsuario.getText() == null || txtUsuario.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe ingresar un nombre de usuario.");
            return false;
        }

        if (txtUsuario.getText().trim().length() < 5) {
            mostrarAlerta(Alert.AlertType.ERROR, "Longitud Inválida", "El usuario debe tener al menos 5 caracteres.");
            return false;
        }

        if (txtPassword == null || txtPassword.getText() == null || txtPassword.getText().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe ingresar una contraseña temporal.");
            return false;
        }

        if (txtPassword.getText().length() < 8) {
            mostrarAlerta(Alert.AlertType.ERROR, "Longitud Inválida", "La contraseña temporal debe tener al menos 8 caracteres.");
            return false;
        }

        if (cmbCargo == null || cmbCargo.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe seleccionar un cargo.");
            return false;
        }

        if (lvArea == null || lvArea.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe seleccionar un área de trabajo de la lista.");
            return false;
        }

        if (dpFechaContratacion == null || dpFechaContratacion.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe seleccionar la fecha de contratación.");
            return false;
        }

        // VALIDACIÓN DE FECHA: No puede ser posterior a la fecha actual
        if (dpFechaContratacion.getValue().isAfter(LocalDate.now())) {
            mostrarAlerta(Alert.AlertType.ERROR, "Fecha Inválida",
                    "La fecha de contratación no puede ser posterior a la fecha actual (" + LocalDate.now() + ").");
            return false;
        }

        if (tgTipoContrato == null || tgTipoContrato.getSelectedToggle() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe seleccionar un tipo de contrato.");
            return false;
        }

        if (!hayBeneficioSeleccionado()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Vacío", "Debe seleccionar al menos un beneficio.");
            return false;
        }

        return true;
    }

    private boolean hayBeneficioSeleccionado() {
        return (chkSeguroMedico != null && chkSeguroMedico.isSelected())
                || (chkViaticos != null && chkViaticos.isSelected())
                || (chkBonoAlimentacion != null && chkBonoAlimentacion.isSelected())
                || (chkCapacitaciones != null && chkCapacitaciones.isSelected());
    }

    private String obtenerBeneficiosSeleccionados() {
        List<String> beneficios = new ArrayList<>();
        if (chkSeguroMedico != null && chkSeguroMedico.isSelected()) beneficios.add("Seguro Médico");
        if (chkViaticos != null && chkViaticos.isSelected()) beneficios.add("Viáticos");
        if (chkBonoAlimentacion != null && chkBonoAlimentacion.isSelected()) beneficios.add("Bono de Alimentación");
        if (chkCapacitaciones != null && chkCapacitaciones.isSelected()) beneficios.add("Capacitaciones");
        return beneficios.isEmpty() ? "Ninguno" : String.join(", ", beneficios);
    }

    private void actualizarVistaTabla() {
        if (tblTrabajadores != null) {
            datosTabla.setAll(dao.obtenerRegistros());
            tblTrabajadores.refresh();
        }
    }

    private void limpiarCampos() {
        if (txtNombres != null) txtNombres.clear();
        if (txtApellidos != null) txtApellidos.clear();
        if (txtUsuario != null) {
            txtUsuario.clear();
            txtUsuario.setDisable(false);
        }
        if (txtPassword != null) txtPassword.clear();
        if (cmbCargo != null) cmbCargo.getSelectionModel().clearSelection();
        if (lvArea != null) lvArea.getSelectionModel().clearSelection();
        if (dpFechaContratacion != null) dpFechaContratacion.setValue(null);
        if (tgTipoContrato != null && tgTipoContrato.getSelectedToggle() != null) {
            tgTipoContrato.getSelectedToggle().setSelected(false);
        }
        if (chkSeguroMedico != null) chkSeguroMedico.setSelected(false);
        if (chkViaticos != null) chkViaticos.setSelected(false);
        if (chkBonoAlimentacion != null) chkBonoAlimentacion.setSelected(false);
        if (chkCapacitaciones != null) chkCapacitaciones.setSelected(false);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}