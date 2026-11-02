package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Tabla VentaDetalle: cada prenda vendida dentro de una venta. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {
    private Long idVentaDetalle;
    private Double precioUnit;   // costo de la prenda al momento de vender
    private Double porceUtil;    // % de utilidad aplicado
    private Integer cantidad;
    private Double netoTotal;    // precio de venta x cantidad (sin IGV)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Venta idVenta;
    private Producto idProducto;

    public Double getPrecioVenta() {
        return Producto.calcularPrecioVenta(precioUnit, porceUtil);
    }

    public String getNombreProducto() {
        return idProducto == null ? "" : idProducto.getNombre();
    }
}
