package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Compra;
import pe.edu.upeu.sysventas.model.DetalleCompra;
import pe.edu.upeu.sysventas.model.Producto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DetalleCompraRepository extends AbstractJpaRepository<DetalleCompra, Long> {
    @Override protected String getTableName() { return "detalle_compra"; }
    @Override protected String getPkColumn()  { return "idDetalleCompra"; }

    private static final String SELECT_JOIN =
            "SELECT d.*, p.nombre AS prodNombre FROM detalle_compra d " +
            "JOIN Productos p ON d.idProducto = p.idProducto ";

    @Override
    public List<DetalleCompra> findAll() {
        return executeQuery(SELECT_JOIN);
    }

    /** Detalle (prendas) de una compra. */
    public List<DetalleCompra> findByCompra(Long idCompra) {
        return executeQuery(SELECT_JOIN + "WHERE d.idCompra = ? ORDER BY d.idDetalleCompra", idCompra);
    }

    @Override
    protected DetalleCompra insert(Connection conn, DetalleCompra e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO detalle_compra(precioCompra,cantidad,subtotal,idCompra,idProducto) VALUES(?,?,?,?,?)",
                e.getPrecioCompra(), e.getCantidad(), e.getSubtotal(),
                e.getIdCompra().getId(), e.getIdProducto().getIdProducto());
        e.setIdDetalleCompra(id);
        return e;
    }

    @Override
    protected DetalleCompra updateRow(Connection conn, DetalleCompra e) throws SQLException {
        executeUpdate(conn,
                "UPDATE detalle_compra SET precioCompra=?, cantidad=?, subtotal=?, idCompra=?, idProducto=? WHERE idDetalleCompra=?",
                e.getPrecioCompra(), e.getCantidad(), e.getSubtotal(),
                e.getIdCompra().getId(), e.getIdProducto().getIdProducto(), e.getIdDetalleCompra());
        return e;
    }

    @Override
    protected DetalleCompra mapRow(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setId(rs.getLong("idCompra"));
        return DetalleCompra.builder()
                .idDetalleCompra(rs.getLong("idDetalleCompra"))
                .precioCompra(rs.getDouble("precioCompra"))
                .cantidad(rs.getInt("cantidad"))
                .subtotal(rs.getDouble("subtotal"))
                .idCompra(c)
                .idProducto(Producto.builder()
                        .idProducto(rs.getLong("idProducto"))
                        .nombre(rs.getString("prodNombre")).build())
                .build();
    }
}
