package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Tabla detalle_compra: cada prenda comprada al proveedor. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompra {
    private Long idDetalleCompra;
    private Double precioCompra;
    private Integer cantidad;
    private Double subtotal;     // precioCompra x cantidad
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compra idCompra;
    private Producto idProducto;

    public String getNombreProducto() {
        return idProducto == null ? "" : idProducto.getNombre();
    }
}
