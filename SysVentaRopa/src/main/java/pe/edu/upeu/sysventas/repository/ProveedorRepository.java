package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Proveedor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProveedorRepository extends AbstractJpaRepository<Proveedor, Long> {
    @Override protected String getTableName() { return "proveedor"; }
    @Override protected String getPkColumn()  { return "idProveedor"; }

    @Override
    public List<Proveedor> findAll() {
        return executeQuery("SELECT * FROM proveedor ORDER BY razonSocial");
    }

    public boolean existeRuc(String ruc, Long idExcluir) {
        return executeExists("SELECT 1 FROM proveedor WHERE ruc = ? AND idProveedor <> ?",
                ruc, idExcluir == null ? 0L : idExcluir);
    }

    @Override
    protected Proveedor insert(Connection conn, Proveedor e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO proveedor(ruc,razonSocial,contacto,direccion,celular,email) VALUES(?,?,?,?,?,?)",
                e.getRuc(), e.getRazonSocial(), e.getContacto(), e.getDireccion(), e.getCelular(), e.getEmail());
        e.setIdProveedor(id);
        return e;
    }

    @Override
    protected Proveedor updateRow(Connection conn, Proveedor e) throws SQLException {
        executeUpdate(conn,
                "UPDATE proveedor SET ruc=?, razonSocial=?, contacto=?, direccion=?, celular=?, email=? WHERE idProveedor=?",
                e.getRuc(), e.getRazonSocial(), e.getContacto(), e.getDireccion(), e.getCelular(), e.getEmail(),
                e.getIdProveedor());
        return e;
    }

    @Override
    protected Proveedor mapRow(ResultSet rs) throws SQLException {
        return Proveedor.builder()
                .idProveedor(rs.getLong("idProveedor"))
                .ruc(rs.getString("ruc"))
                .razonSocial(rs.getString("razonSocial"))
                .contacto(rs.getString("contacto"))
                .direccion(rs.getString("direccion"))
                .celular(rs.getString("celular"))
                .email(rs.getString("email"))
                .build();
    }
}
