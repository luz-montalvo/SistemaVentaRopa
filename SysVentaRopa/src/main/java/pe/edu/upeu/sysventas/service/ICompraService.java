package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.Compra;
import pe.edu.upeu.sysventas.model.DetalleCompra;

import java.util.List;

public interface ICompraService extends ICrudGenericoService<Compra, Long> {
    /** Guarda cabecera + detalle y aumenta stock. */
    Compra registrarCompra(Compra compra);
    List<DetalleCompra> listarDetalle(Long idCompra);
    boolean existeComprobante(Long idProveedor, String tipoDoc, String serie, String numDoc);
}
