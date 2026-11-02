package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tabla Categoria: tipo de prenda (casacas, polos, pantalones...). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    private Long idCateg;
    private String nombre;
}
