package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.UnidadMedida;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.UnidadMedidaRepository;
import pe.edu.upeu.sysventas.service.IUnidadMedidaService;

import java.util.List;

@RequiredArgsConstructor
public class UnidadMedidaServiceImp extends CrudGenericoServiceImp<UnidadMedida, Long> implements IUnidadMedidaService {
    private final UnidadMedidaRepository unidadMedidaRepository;

    @Override
    protected ICrudGenericoRepository<UnidadMedida, Long> getRepo() {
        return unidadMedidaRepository;
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        return unidadMedidaRepository.findAll().stream()
                .map(u -> new ComboBoxOption(String.valueOf(u.getIdUnidadMed()), u.getNombre()))
                .toList();
    }
}
