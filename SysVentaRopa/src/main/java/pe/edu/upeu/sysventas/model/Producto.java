package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tabla Productos: prendas de ropa del catálogo. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Long idProducto;
    private String nombre;
    private Double preUnitario;   // costo de la prenda
    private Double porceUtil;     // % de utilidad (25 = 25 %)
    private Integer stock;
    private UnidadMedida idUnidadMed;
    private Categoria idCateg;

    /** Precio de venta (sin IGV) = costo + % de utilidad. */
    public Double getPrecioVenta() {
        return calcularPrecioVenta(preUnitario, porceUtil);
    }

    public static double calcularPrecioVenta(Double costo, Double porce) {
        if (costo == null) return 0.0;
        double util = porce == null ? 0.0 : porce;
        return DocumentoComercial.redondear(costo * (1 + util / 100.0));
    }
}
