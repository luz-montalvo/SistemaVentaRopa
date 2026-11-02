package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;

import java.util.List;

public interface IVentaService extends ICrudGenericoService<Venta, Long> {
    /** Guarda cabecera + detalle y descuenta stock. */
    Venta registrarVenta(Venta venta);
    List<VentaDetalle> listarDetalle(Long idVenta);
}
