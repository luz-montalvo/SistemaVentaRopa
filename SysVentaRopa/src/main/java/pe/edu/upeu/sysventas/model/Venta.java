package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

/** Tabla Venta (cabecera). El id heredado es idVenta. */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Venta extends DocumentoComercial {
    private LocalDate fechaVenta;
    private Cliente idCliente;
    private Usuario idUsuario;
    private List<VentaDetalle> detalles;

    @Override
    public double calcularNeto() {
        if (detalles == null) return 0.0;
        return detalles.stream().mapToDouble(VentaDetalle::getNetoTotal).sum();
    }

    /** Para mostrar en la tabla del historial. */
    public String getClienteNombre() {
        return idCliente == null ? "" : idCliente.getDniruc() + " - " + idCliente.getNombres();
    }

    public String getVendedor() {
        return idUsuario == null ? "" : idUsuario.getUsuario();
    }
}
