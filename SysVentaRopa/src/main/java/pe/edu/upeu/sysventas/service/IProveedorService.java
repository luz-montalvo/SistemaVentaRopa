package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Proveedor;

import java.util.List;

public interface IProveedorService extends ICrudGenericoService<Proveedor, Long> {
    List<ComboBoxOption> listarCombobox();
    boolean existeRuc(String ruc, Long idExcluir);
}
