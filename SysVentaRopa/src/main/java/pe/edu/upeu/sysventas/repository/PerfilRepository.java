package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Perfil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PerfilRepository extends AbstractJpaRepository<Perfil, Long> {
    @Override protected String getTableName() { return "Perfil"; }
    @Override protected String getPkColumn()  { return "idPerfil"; }

    @Override
    protected Perfil insert(Connection conn, Perfil e) throws SQLException {
        long id = executeInsertGetKey(conn, "INSERT INTO Perfil(nombre) VALUES(?)", e.getNombre());
        e.setIdPerfil(id);
        return e;
    }

    @Override
    protected Perfil updateRow(Connection conn, Perfil e) throws SQLException {
        executeUpdate(conn, "UPDATE Perfil SET nombre=? WHERE idPerfil=?", e.getNombre(), e.getIdPerfil());
        return e;
    }

    @Override
    protected Perfil mapRow(ResultSet rs) throws SQLException {
        return Perfil.builder()
                .idPerfil(rs.getLong("idPerfil"))
                .nombre(rs.getString("nombre"))
                .build();
    }
}
