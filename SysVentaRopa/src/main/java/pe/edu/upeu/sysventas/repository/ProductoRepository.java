package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Categoria;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.UnidadMedida;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductoRepository extends AbstractJpaRepository<Producto, Long> {

    private static final String SELECT_JOIN =
            "SELECT p.*, c.nombre AS categNombre, u.nombre AS unidadNombre FROM Productos p " +
            "JOIN Categoria c ON p.idCateg = c.idCateg " +
            "JOIN UnidadMedida u ON p.idUnidadMed = u.idUnidadMed ";

    @Override protected String getTableName() { return "Productos"; }
    @Override protected String getPkColumn()  { return "idProducto"; }

    @Override
    public List<Producto> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY p.nombre");
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE p.idProducto = ?", id);
    }

    @Override
    protected Producto insert(Connection conn, Producto e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO Productos(nombre,preUnitario,porceUtil,stock,idUnidadMed,idCateg) VALUES(?,?,?,?,?,?)",
                e.getNombre(), e.getPreUnitario(), e.getPorceUtil(), e.getStock(),
                e.getIdUnidadMed().getIdUnidadMed(), e.getIdCateg().getIdCateg());
        e.setIdProducto(id);
        return e;
    }

    @Override
    protected Producto updateRow(Connection conn, Producto e) throws SQLException {
        executeUpdate(conn,
                "UPDATE Productos SET nombre=?, preUnitario=?, porceUtil=?, stock=?, idUnidadMed=?, idCateg=? WHERE idProducto=?",
                e.getNombre(), e.getPreUnitario(), e.getPorceUtil(), e.getStock(),
                e.getIdUnidadMed().getIdUnidadMed(), e.getIdCateg().getIdCateg(), e.getIdProducto());
        return e;
    }

    @Override
    protected Producto mapRow(ResultSet rs) throws SQLException {
        return Producto.builder()
                .idProducto(rs.getLong("idProducto"))
                .nombre(rs.getString("nombre"))
                .preUnitario(rs.getDouble("preUnitario"))
                .porceUtil(rs.getDouble("porceUtil"))
                .stock(rs.getInt("stock"))
                .idCateg(Categoria.builder()
                        .idCateg(rs.getLong("idCateg"))
                        .nombre(rs.getString("categNombre")).build())
                .idUnidadMed(UnidadMedida.builder()
                        .idUnidadMed(rs.getLong("idUnidadMed"))
                        .nombre(rs.getString("unidadNombre")).build())
                .build();
    }
}
