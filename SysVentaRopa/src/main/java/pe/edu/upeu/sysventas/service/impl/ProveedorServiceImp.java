package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Proveedor;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.ProveedorRepository;
import pe.edu.upeu.sysventas.service.IProveedorService;

import java.util.List;

public class ProveedorServiceImp extends CrudGenericoServiceImp<Proveedor, Long> implements IProveedorService {
    private final ProveedorRepository repo;

    public ProveedorServiceImp(ProveedorRepository repo) {
        this.repo = repo;
    }

    @Override
    protected ICrudGenericoRepository<Proveedor, Long> getRepo() {
        return repo;
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        return repo.findAll().stream()
                .map(p -> new ComboBoxOption(String.valueOf(p.getIdProveedor()), p.getRuc() + " - " + p.getRazonSocial()))
                .toList();
    }

    @Override
    public boolean existeRuc(String ruc, Long idExcluir) {
        return repo.existeRuc(ruc, idExcluir);
    }
}
