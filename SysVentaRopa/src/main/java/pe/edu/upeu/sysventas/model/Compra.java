package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

/** Tabla compras (cabecera). El id heredado es idCompra. */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Compra extends DocumentoComercial {
    private LocalDate fechaCompra;
    private String tipoDoc;      // FACTURA / BOLETA / GUIA
    private String serie;        // ej. F001
    private String numDoc;       // ej. 0000123
    private Proveedor idProveedor;
    private Usuario idUsuario;
    private List<DetalleCompra> detalles;

    @Override
    public double calcularNeto() {
        if (detalles == null) return 0.0;
        return detalles.stream().mapToDouble(DetalleCompra::getSubtotal).sum();
    }

    public String getComprobante() {
        return tipoDoc + " " + serie + "-" + numDoc;
    }

    public String getProveedorNombre() {
        return idProveedor == null ? "" : idProveedor.getRuc() + " - " + idProveedor.getRazonSocial();
    }

    public String getComprador() {
        return idUsuario == null ? "" : idUsuario.getUsuario();
    }
}
