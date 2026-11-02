package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.model.Compra;
import pe.edu.upeu.sysventas.model.DetalleCompra;
import pe.edu.upeu.sysventas.repository.CompraRepository;
import pe.edu.upeu.sysventas.repository.DetalleCompraRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.service.ICompraService;

import java.time.LocalDate;
import java.util.List;

public class CompraServiceImp extends CrudGenericoServiceImp<Compra, Long> implements ICompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleRepository;

    public CompraServiceImp(CompraRepository compraRepository, DetalleCompraRepository detalleRepository) {
        this.compraRepository = compraRepository;
        this.detalleRepository = detalleRepository;
    }

    @Override
    protected ICrudGenericoRepository<Compra, Long> getRepo() {
        return compraRepository;
    }

    @Override
    public Compra registrarCompra(Compra compra) {
        if (compra.getIdProveedor() == null) throw new IllegalArgumentException("Seleccione un proveedor");
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty())
            throw new IllegalArgumentException("Agregue al menos una prenda");
        if (existeComprobante(compra.getIdProveedor().getIdProveedor(), compra.getTipoDoc(),
                compra.getSerie(), compra.getNumDoc()))
            throw new IllegalArgumentException("Ese comprobante ya fue registrado para este proveedor");
        if (compra.getFechaCompra() == null) compra.setFechaCompra(LocalDate.now());
        compra.calcularMontos();         // neto, IGV 18 % y total
        return compraRepository.registrarCompra(compra);
    }

    @Override
    public List<DetalleCompra> listarDetalle(Long idCompra) {
        return detalleRepository.findByCompra(idCompra);
    }

    @Override
    public boolean existeComprobante(Long idProveedor, String tipoDoc, String serie, String numDoc) {
        return compraRepository.existeComprobante(idProveedor, tipoDoc, serie, numDoc);
    }
}
