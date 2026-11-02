package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Compra;
import pe.edu.upeu.sysventas.model.DetalleCompra;
import pe.edu.upeu.sysventas.model.Proveedor;
import pe.edu.upeu.sysventas.model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CompraRepository extends AbstractJpaRepository<Compra, Long> {

    private static final String SELECT_JOIN =
            "SELECT c.*, p.ruc, p.razonSocial, u.usuario FROM compras c " +
            "JOIN proveedor p ON c.idProveedor = p.idProveedor " +
            "JOIN Usuario u ON c.idUsuario = u.idUsuario ";

    @Override protected String getTableName() { return "compras"; }
    @Override protected String getPkColumn()  { return "idCompra"; }

    @Override
    public List<Compra> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY c.idCompra DESC");
    }

    @Override
    public Optional<Compra> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE c.idCompra = ?", id);
    }

    /** Un mismo comprobante (tipo + serie + número) del mismo proveedor no se registra dos veces. */
    public boolean existeComprobante(Long idProveedor, String tipoDoc, String serie, String numDoc) {
        return executeExists(
                "SELECT 1 FROM compras WHERE idProveedor = ? AND tipoDoc = ? AND serie = ? AND numDoc = ?",
                idProveedor, tipoDoc, serie, numDoc);
    }

    /**
     * Registra la compra completa en UNA sola transacción:
     * 1) cabecera compras, 2) cada detalle_compra, 3) aumenta el stock de cada prenda.
     */
    public Compra registrarCompra(Compra c) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                long idCompra = executeInsertGetKey(conn,
                        "INSERT INTO compras(fechaCompra,tipoDoc,serie,numDoc,netoTotal,igv,precioTotal,idProveedor,idUsuario) " +
                        "VALUES(?,?,?,?,?,?,?,?,?)",
                        Date.valueOf(c.getFechaCompra()), c.getTipoDoc(), c.getSerie(), c.getNumDoc(),
                        c.getNetoTotal(), c.getIgv(), c.getPrecioTotal(),
                        c.getIdProveedor().getIdProveedor(), c.getIdUsuario().getIdUsuario());
                c.setId(idCompra);

                for (DetalleCompra d : c.getDetalles()) {
                    long idDet = executeInsertGetKey(conn,
                            "INSERT INTO detalle_compra(precioCompra,cantidad,subtotal,idCompra,idProducto) VALUES(?,?,?,?,?)",
                            d.getPrecioCompra(), d.getCantidad(), d.getSubtotal(),
                            idCompra, d.getIdProducto().getIdProducto());
                    d.setIdDetalleCompra(idDet);
                    d.setIdCompra(c);

                    executeUpdate(conn,
                            "UPDATE Productos SET stock = stock + ? WHERE idProducto = ?",
                            d.getCantidad(), d.getIdProducto().getIdProducto());
                }
                conn.commit();
                return c;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected Compra insert(Connection conn, Compra e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO compras(fechaCompra,tipoDoc,serie,numDoc,netoTotal,igv,precioTotal,idProveedor,idUsuario) " +
                "VALUES(?,?,?,?,?,?,?,?,?)",
                Date.valueOf(e.getFechaCompra()), e.getTipoDoc(), e.getSerie(), e.getNumDoc(),
                e.getNetoTotal(), e.getIgv(), e.getPrecioTotal(),
                e.getIdProveedor().getIdProveedor(), e.getIdUsuario().getIdUsuario());
        e.setId(id);
        return e;
    }

    @Override
    protected Compra updateRow(Connection conn, Compra e) throws SQLException {
        executeUpdate(conn,
                "UPDATE compras SET fechaCompra=?, tipoDoc=?, serie=?, numDoc=?, netoTotal=?, igv=?, precioTotal=?, " +
                "idProveedor=?, idUsuario=? WHERE idCompra=?",
                Date.valueOf(e.getFechaCompra()), e.getTipoDoc(), e.getSerie(), e.getNumDoc(),
                e.getNetoTotal(), e.getIgv(), e.getPrecioTotal(),
                e.getIdProveedor().getIdProveedor(), e.getIdUsuario().getIdUsuario(), e.getId());
        return e;
    }

    @Override
    protected Compra mapRow(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setId(rs.getLong("idCompra"));
        c.setFechaCompra(rs.getDate("fechaCompra").toLocalDate());
        c.setTipoDoc(rs.getString("tipoDoc"));
        c.setSerie(rs.getString("serie"));
        c.setNumDoc(rs.getString("numDoc"));
        c.setNetoTotal(rs.getDouble("netoTotal"));
        c.setIgv(rs.getDouble("igv"));
        c.setPrecioTotal(rs.getDouble("precioTotal"));
        c.setIdProveedor(Proveedor.builder()
                .idProveedor(rs.getLong("idProveedor"))
                .ruc(rs.getString("ruc"))
                .razonSocial(rs.getString("razonSocial"))
                .build());
        c.setIdUsuario(Usuario.builder()
                .idUsuario(rs.getLong("idUsuario"))
                .usuario(rs.getString("usuario"))
                .build());
        return c;
    }
}
