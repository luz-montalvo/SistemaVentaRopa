package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.service.ICrudGenericoService;

import java.util.List;

// Servicio GENÉRICO (con <T, ID>): el CRUD básico se escribe una sola vez
// y lo heredan todos los servicios (Cliente, Producto, Usuario, etc.)
public abstract class CrudGenericoServiceImp<T, ID> implements ICrudGenericoService<T, ID> {
    // Cada servicio hijo indica cuál es su repositorio
    protected abstract ICrudGenericoRepository<T,ID> getRepo();

    @Override
    public T save(T t) {
        return getRepo().save(t);
    }

    // Antes de actualizar o eliminar se verifica que el registro exista
    @Override
    public T update(ID id, T t) {
        if(!getRepo().existsById(id)){
            throw new ModelNotFoundException("ID no existe: "+id);
        }
        return getRepo().update(t);
    }

    @Override
    public List<T> findAll() {
        return getRepo().findAll();
    }

    @Override
    public T findById(ID id) {
        return getRepo().findById(id).orElseThrow(()->new ModelNotFoundException("El Id no existe:"+id));
    }

    @Override
    public void delete(ID id) {
        if(!getRepo().existsById(id)){
            throw new ModelNotFoundException("ID no existe: "+id);
        }
        getRepo().deleteById(id);
    }
}
