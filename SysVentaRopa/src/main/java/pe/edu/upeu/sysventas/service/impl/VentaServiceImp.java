package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.VentaDetalleRepository;
import pe.edu.upeu.sysventas.repository.VentaRepository;
import pe.edu.upeu.sysventas.service.IVentaService;

import java.time.LocalDate;
import java.util.List;

public class VentaServiceImp extends CrudGenericoServiceImp<Venta, Long> implements IVentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository detalleRepository;

    public VentaServiceImp(VentaRepository ventaRepository, VentaDetalleRepository detalleRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleRepository = detalleRepository;
    }

    @Override
    protected ICrudGenericoRepository<Venta, Long> getRepo() {
        return ventaRepository;
    }

    // REGLA DE NEGOCIO de la venta: valida, calcula montos y manda a guardar
    @Override
    public Venta registrarVenta(Venta venta) {
        if (venta.getIdCliente() == null) throw new IllegalArgumentException("Seleccione un cliente");
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty())
            throw new IllegalArgumentException("Agregue al menos una prenda");
        if (venta.getFechaVenta() == null) venta.setFechaVenta(LocalDate.now());
        // Polimorfismo: Venta sabe sumar su detalle (método heredado de DocumentoComercial)
        venta.calcularMontos();          // neto, IGV 18 % y total
        return ventaRepository.registrarVenta(venta);
    }

    @Override
    public List<VentaDetalle> listarDetalle(Long idVenta) {
        return detalleRepository.findByVenta(idVenta);
    }
}
