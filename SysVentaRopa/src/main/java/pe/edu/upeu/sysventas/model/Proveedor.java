package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tabla proveedor: empresas que nos venden las prendas. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {
    private Long idProveedor;
    private String ruc;
    private String razonSocial;
    private String contacto;
    private String direccion;
    private String celular;
    private String email;
}
