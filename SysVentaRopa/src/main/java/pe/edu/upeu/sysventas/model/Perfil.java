package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tabla Perfil: rol del usuario (Administrador, Cajero, Almacén). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {
    private Long idPerfil;
    private String nombre;
}
