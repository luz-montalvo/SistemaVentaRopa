package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Categoria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoriaRepository extends AbstractJpaRepository<Categoria, Long> {
    @Override protected String getTableName() { return "Categoria"; }
    @Override protected String getPkColumn()  { return "idCateg"; }

    @Override
    protected Categoria insert(Connection conn, Categoria e) throws SQLException {
        long id = executeInsertGetKey(conn, "INSERT INTO Categoria(nombre) VALUES(?)", e.getNombre());
        e.setIdCateg(id);
        return e;
    }

    @Override
    protected Categoria updateRow(Connection conn, Categoria e) throws SQLException {
        executeUpdate(conn, "UPDATE Categoria SET nombre=? WHERE idCateg=?", e.getNombre(), e.getIdCateg());
        return e;
    }

    @Override
    protected Categoria mapRow(ResultSet rs) throws SQLException {
        return Categoria.builder()
                .idCateg(rs.getLong("idCateg"))
                .nombre(rs.getString("nombre"))
                .build();
    }
}
