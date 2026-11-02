package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface IClienteService extends ICrudGenericoService<Cliente, Long> {
    Optional<Cliente> buscarPorDniruc(String dniruc);
    boolean existeDniruc(String dniruc, Long idExcluir);
    List<ModeloDataAutocomplet> listAutoCompletCliente();
}
