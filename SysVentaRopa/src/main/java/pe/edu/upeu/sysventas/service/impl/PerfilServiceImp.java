package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Perfil;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.PerfilRepository;
import pe.edu.upeu.sysventas.service.IPerfilService;

import java.util.List;

public class PerfilServiceImp extends CrudGenericoServiceImp<Perfil, Long> implements IPerfilService {
    private final PerfilRepository repo;

    public PerfilServiceImp(PerfilRepository repo) {
        this.repo = repo;
    }

    @Override
    protected ICrudGenericoRepository<Perfil, Long> getRepo() {
        return repo;
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        return repo.findAll().stream()
                .map(p -> new ComboBoxOption(String.valueOf(p.getIdPerfil()), p.getNombre()))
                .toList();
    }
}
