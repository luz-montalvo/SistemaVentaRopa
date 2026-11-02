package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class VentaDetalleRepository extends AbstractJpaRepository<VentaDetalle, Long> {
    @Override protected String getTableName() { return "VentaDetalle"; }
    @Override protected String getPkColumn()  { return "idVentaDetalle"; }

    private static final String SELECT_JOIN =
            "SELECT d.*, p.nombre AS prodNombre FROM VentaDetalle d " +
            "JOIN Productos p ON d.idProducto = p.idProducto ";

    @Override
    public List<VentaDetalle> findAll() {
        return executeQuery(SELECT_JOIN);
    }

    /** Detalle (prendas) de una venta. */
    public List<VentaDetalle> findByVenta(Long idVenta) {
        return executeQuery(SELECT_JOIN + "WHERE d.idVenta = ? ORDER BY d.idVentaDetalle", idVenta);
    }

    @Override
    protected VentaDetalle insert(Connection conn, VentaDetalle e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO VentaDetalle(precioUnit,porceUtil,cantidad,netoTotal,idVenta,idProducto) VALUES(?,?,?,?,?,?)",
                e.getPrecioUnit(), e.getPorceUtil(), e.getCantidad(), e.getNetoTotal(),
                e.getIdVenta().getId(), e.getIdProducto().getIdProducto());
        e.setIdVentaDetalle(id);
        return e;
    }

    @Override
    protected VentaDetalle updateRow(Connection conn, VentaDetalle e) throws SQLException {
        executeUpdate(conn,
                "UPDATE VentaDetalle SET precioUnit=?, porceUtil=?, cantidad=?, netoTotal=?, idVenta=?, idProducto=? WHERE idVentaDetalle=?",
                e.getPrecioUnit(), e.getPorceUtil(), e.getCantidad(), e.getNetoTotal(),
                e.getIdVenta().getId(), e.getIdProducto().getIdProducto(), e.getIdVentaDetalle());
        return e;
    }

    @Override
    protected VentaDetalle mapRow(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setId(rs.getLong("idVenta"));
        return VentaDetalle.builder()
                .idVentaDetalle(rs.getLong("idVentaDetalle"))
                .precioUnit(rs.getDouble("precioUnit"))
                .porceUtil(rs.getDouble("porceUtil"))
                .cantidad(rs.getInt("cantidad"))
                .netoTotal(rs.getDouble("netoTotal"))
                .idVenta(v)
                .idProducto(Producto.builder()
                        .idProducto(rs.getLong("idProducto"))
                        .nombre(rs.getString("prodNombre")).build())
                .build();
    }
}
