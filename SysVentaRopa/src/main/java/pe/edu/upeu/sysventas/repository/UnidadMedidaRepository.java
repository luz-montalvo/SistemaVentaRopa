package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.UnidadMedida;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnidadMedidaRepository extends AbstractJpaRepository<UnidadMedida, Long> {
    @Override protected String getTableName() { return "UnidadMedida"; }
    @Override protected String getPkColumn()  { return "idUnidadMed"; }

    @Override
    protected UnidadMedida insert(Connection conn, UnidadMedida e) throws SQLException {
        long id = executeInsertGetKey(conn, "INSERT INTO UnidadMedida(nombre) VALUES(?)", e.getNombre());
        e.setIdUnidadMed(id);
        return e;
    }

    @Override
    protected UnidadMedida updateRow(Connection conn, UnidadMedida e) throws SQLException {
        executeUpdate(conn, "UPDATE UnidadMedida SET nombre=? WHERE idUnidadMed=?", e.getNombre(), e.getIdUnidadMed());
        return e;
    }

    @Override
    protected UnidadMedida mapRow(ResultSet rs) throws SQLException {
        return UnidadMedida.builder()
                .idUnidadMed(rs.getLong("idUnidadMed"))
                .nombre(rs.getString("nombre"))
                .build();
    }
}
