package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prenda {

    private Long idPrenda;
    private String nombre;
    private String categoria;
    private String talla;
    private String color;
    private Double precio;
    private Integer stock;
}