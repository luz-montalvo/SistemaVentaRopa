package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tabla UnidadMedida: unidad, docena o pack. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedida {
    private Long idUnidadMed;
    private String nombre;
}
