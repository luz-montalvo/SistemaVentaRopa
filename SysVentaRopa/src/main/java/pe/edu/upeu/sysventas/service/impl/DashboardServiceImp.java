package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.DashboardDto;
import pe.edu.upeu.sysventas.repository.DashboardRepository;
import pe.edu.upeu.sysventas.service.IDashboardService;

public class DashboardServiceImp implements IDashboardService {

    private final DashboardRepository repo;

    public DashboardServiceImp(DashboardRepository repo) {
        this.repo = repo;
    }

    @Override
    public DashboardDto cargar(String perfil, Long idUsuario) {
        // Regla de negocio: el cajero solo ve sus propias ventas; los demás perfiles ven todo
        boolean soloSusVentas = "Vendedor de Tienda / Cajero".equals(perfil);
        return repo.cargar(soloSusVentas ? idUsuario : null);
    }
}
