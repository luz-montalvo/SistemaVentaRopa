package pe.edu.upeu.sysventas.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Perfil {
    Long idPerfil;
    String nombre;
    String codigo;
    List<Acceso> accesos; //Agregacion
}
