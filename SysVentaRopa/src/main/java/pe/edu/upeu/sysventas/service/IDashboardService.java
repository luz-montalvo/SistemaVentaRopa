package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.DashboardDto;

public interface IDashboardService {
    /** Resumen para el inicio, según el perfil del usuario que inició sesión. */
    DashboardDto cargar(String perfil, Long idUsuario);
}
