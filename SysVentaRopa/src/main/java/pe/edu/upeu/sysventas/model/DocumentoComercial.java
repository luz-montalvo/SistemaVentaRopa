package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Clase abstracta (POO - herencia y polimorfismo) para Venta y Compra.
 * Las dos tienen neto, IGV y total; cada una sabe cómo calcular su neto.
 */
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DocumentoComercial {
    public static final double TASA_IGV = 0.18;

    private Long id;
    private double netoTotal;
    private double igv;
    private double precioTotal;

    /** Cada hijo suma sus líneas de detalle. */
    public abstract double calcularNeto();

    /** Neto = suma del detalle, IGV = Neto x 18 %, Total = Neto + IGV. */
    public void calcularMontos() {
        this.netoTotal = redondear(calcularNeto());
        this.igv = redondear(netoTotal * TASA_IGV);
        this.precioTotal = redondear(netoTotal + igv);
    }

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
