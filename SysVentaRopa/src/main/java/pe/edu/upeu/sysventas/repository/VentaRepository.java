package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class VentaRepository extends AbstractJpaRepository<Venta, Long> {

    // Consulta base con JOIN para traer también los datos del cliente y del vendedor
    private static final String SELECT_JOIN =
            "SELECT v.*, c.dniruc, c.nombres, c.tipoDocumento, c.direccion, u.usuario FROM Venta v " +
            "JOIN Cliente c ON v.idCliente = c.idCliente " +
            "JOIN Usuario u ON v.idUsuario = u.idUsuario ";

    @Override protected String getTableName() { return "Venta"; }
    @Override protected String getPkColumn()  { return "idVenta"; }

    @Override
    public List<Venta> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY v.idVenta DESC");
    }

    @Override
    public Optional<Venta> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE v.idVenta = ?", id);
    }

    /**
     * Registra la venta completa en UNA sola transacción:
     * 1) cabecera Venta, 2) cada VentaDetalle, 3) descuenta el stock.
     * Si alguna prenda no tiene stock suficiente se hace rollback y no se guarda nada.
     */
    public Venta registrarVenta(Venta v) {
        try (Connection conn = openConnection()) {
            // Inicia la TRANSACCIÓN: nada se guarda hasta el commit
            conn.setAutoCommit(false);
            try {
                // 1) Inserta la cabecera y obtiene el idVenta generado
                long idVenta = executeInsertGetKey(conn,
                        "INSERT INTO Venta(fechaVenta,netoTotal,igv,precioTotal,idCliente,idUsuario) VALUES(?,?,?,?,?,?)",
                        Date.valueOf(v.getFechaVenta()), v.getNetoTotal(), v.getIgv(), v.getPrecioTotal(),
                        v.getIdCliente().getIdCliente(), v.getIdUsuario().getIdUsuario());
                v.setId(idVenta);

                // 2) Inserta cada prenda del detalle
                for (VentaDetalle d : v.getDetalles()) {
                    long idDet = executeInsertGetKey(conn,
                            "INSERT INTO VentaDetalle(precioUnit,porceUtil,cantidad,netoTotal,idVenta,idProducto) VALUES(?,?,?,?,?,?)",
                            d.getPrecioUnit(), d.getPorceUtil(), d.getCantidad(), d.getNetoTotal(),
                            idVenta, d.getIdProducto().getIdProducto());
                    d.setIdVentaDetalle(idDet);
                    d.setIdVenta(v);

                    // 3) Descuenta el stock (solo si alcanza: stock >= cantidad)
                    int filas = executeUpdate(conn,
                            "UPDATE Productos SET stock = stock - ? WHERE idProducto = ? AND stock >= ?",
                            d.getCantidad(), d.getIdProducto().getIdProducto(), d.getCantidad());
                    if (filas == 0) {
                        throw new SQLException("Stock insuficiente para: " + d.getNombreProducto());
                    }
                }
                // Todo salió bien: confirma los cambios en la BD
                conn.commit();
                return v;
            } catch (SQLException e) {
                // Algo falló: deshace todo (no queda la venta a medias)
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected Venta insert(Connection conn, Venta e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO Venta(fechaVenta,netoTotal,igv,precioTotal,idCliente,idUsuario) VALUES(?,?,?,?,?,?)",
                Date.valueOf(e.getFechaVenta()), e.getNetoTotal(), e.getIgv(), e.getPrecioTotal(),
                e.getIdCliente().getIdCliente(), e.getIdUsuario().getIdUsuario());
        e.setId(id);
        return e;
    }

    @Override
    protected Venta updateRow(Connection conn, Venta e) throws SQLException {
        executeUpdate(conn,
                "UPDATE Venta SET fechaVenta=?, netoTotal=?, igv=?, precioTotal=?, idCliente=?, idUsuario=? WHERE idVenta=?",
                Date.valueOf(e.getFechaVenta()), e.getNetoTotal(), e.getIgv(), e.getPrecioTotal(),
                e.getIdCliente().getIdCliente(), e.getIdUsuario().getIdUsuario(), e.getId());
        return e;
    }

    // Convierte cada fila del SELECT en un objeto Venta con su Cliente y Usuario
    @Override
    protected Venta mapRow(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setId(rs.getLong("idVenta"));
        v.setFechaVenta(rs.getDate("fechaVenta").toLocalDate());
        v.setNetoTotal(rs.getDouble("netoTotal"));
        v.setIgv(rs.getDouble("igv"));
        v.setPrecioTotal(rs.getDouble("precioTotal"));
        v.setIdCliente(Cliente.builder()
                .idCliente(rs.getLong("idCliente"))
                .dniruc(rs.getString("dniruc"))
                .nombres(rs.getString("nombres"))
                .tipoDocumento(TipoDocumento.valueOf(rs.getString("tipoDocumento")))
                .direccion(rs.getString("direccion"))
                .build());
        v.setIdUsuario(Usuario.builder()
                .idUsuario(rs.getLong("idUsuario"))
                .usuario(rs.getString("usuario"))
                .build());
        return v;
    }
}
