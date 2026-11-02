package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.repository.ClienteRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.service.IClienteService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// CAPA SERVICIO: reglas de negocio de Cliente.
// Hereda de CrudGenericoServiceImp los métodos save, update, findAll, findById y delete.
public class ClienteServiceImp extends CrudGenericoServiceImp<Cliente, Long> implements IClienteService {
    private final ClienteRepository repo;

    // Inyección por constructor: AppContext le pasa el repositorio
    public ClienteServiceImp(ClienteRepository repo) {
        this.repo = repo;
    }

    // Le dice a la clase padre qué repositorio usar
    @Override
    protected ICrudGenericoRepository<Cliente, Long> getRepo() {
        return repo;
    }

    // Optional: puede traer el cliente o venir vacío (evita null)
    @Override
    public Optional<Cliente> buscarPorDniruc(String dniruc) {
        return repo.findByDniruc(dniruc);
    }

    @Override
    public boolean existeDniruc(String dniruc, Long idExcluir) {
        return repo.existeDniruc(dniruc, idExcluir);
    }

    /** idx = idCliente, texto = "DNI/RUC - nombre". */
    // Prepara la lista para el buscador con autocompletado de la pantalla de ventas
    @Override
    public List<ModeloDataAutocomplet> listAutoCompletCliente() {
        List<ModeloDataAutocomplet> lista = new ArrayList<>();
        for (Cliente c : repo.findAll()) {
            ModeloDataAutocomplet data = new ModeloDataAutocomplet();
            data.setIdx(c.getDniruc());
            data.setNameDysplay(c.getNombres());
            data.setOtherData(String.valueOf(c.getIdCliente()));
            lista.add(data);
        }
        return lista;
    }
}
