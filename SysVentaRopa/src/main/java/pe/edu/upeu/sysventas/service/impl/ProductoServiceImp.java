package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.ProductoRepository;
import pe.edu.upeu.sysventas.service.IProductoService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ProductoServiceImp extends CrudGenericoServiceImp<Producto, Long> implements IProductoService {
    private final ProductoRepository productoRepository;

    @Override
    protected ICrudGenericoRepository<Producto, Long> getRepo() {
        return productoRepository;
    }

    /** idx = idProducto, texto = nombre (para el buscador de prendas). */
    @Override
    public List<ModeloDataAutocomplet> listAutoCompletProducto() {
        List<ModeloDataAutocomplet> lista = new ArrayList<>();
        for (Producto p : productoRepository.findAll()) {
            ModeloDataAutocomplet data = new ModeloDataAutocomplet();
            data.setIdx(String.valueOf(p.getIdProducto()));
            data.setNameDysplay(p.getNombre());
            data.setOtherData(p.getPrecioVenta() + ":" + p.getStock());
            lista.add(data);
        }
        return lista;
    }
}
