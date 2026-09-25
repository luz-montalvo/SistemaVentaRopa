package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Prenda;

public class PrendaRepository extends AbstractJpaRepository<Prenda, Long> {

    private long sequence = 1;

    @Override
    protected Long getId(Prenda entity) {
        return entity.getIdPrenda();
    }

    @Override
    protected void setId(Prenda entity, Long id) {
        entity.setIdPrenda(id);
    }

    @Override
    protected Long generateId() {
        return sequence++;
    }
}