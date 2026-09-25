package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.model.Prenda;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.PrendaRepository;
import pe.edu.upeu.sysventas.service.IPrendaService;

public class PrendaServiceImp extends CrudGenericoServiceImp<Prenda, Long>
        implements IPrendaService {

    private final PrendaRepository repo;

    public PrendaServiceImp(PrendaRepository repo) {
        this.repo = repo;
    }

    @Override
    protected ICrudGenericoRepository<Prenda, Long> getRepo() {
        return repo;
    }
}