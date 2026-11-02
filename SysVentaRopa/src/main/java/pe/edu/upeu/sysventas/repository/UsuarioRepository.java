package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Perfil;
import pe.edu.upeu.sysventas.model.Usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends AbstractJpaRepository<Usuario, Long> {

    private static final String SELECT_JOIN =
            "SELECT u.*, p.nombre AS perfilNombre FROM Usuario u " +
            "JOIN Perfil p ON u.idPerfil = p.idPerfil ";

    @Override protected String getTableName() { return "Usuario"; }
    @Override protected String getPkColumn()  { return "idUsuario"; }

    @Override
    public List<Usuario> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY u.idUsuario");
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE u.idUsuario = ?", id);
    }

    /** Login: busca por nombre de usuario (la clave se compara en el servicio con PasswordUtil). */
    public Optional<Usuario> findByUsuario(String usuario) {
        return executeQueryOne(SELECT_JOIN + "WHERE u.usuario = ?", usuario);
    }

    /** Guarda solo la clave (ya encriptada). Se usa para migrar claves en texto plano. */
    public void actualizarClave(Long idUsuario, String claveEncriptada) {
        executeUpdateStandalone("UPDATE Usuario SET clave=? WHERE idUsuario=?", claveEncriptada, idUsuario);
    }

    /** Para validar que el nombre de usuario no se repita (UQ_Usuario_usuario). */
    public boolean existeUsuario(String usuario, Long idExcluir) {
        return executeExists("SELECT 1 FROM Usuario WHERE usuario = ? AND idUsuario <> ?",
                usuario, idExcluir == null ? 0L : idExcluir);
    }

    @Override
    protected Usuario insert(Connection conn, Usuario e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO Usuario(usuario,clave,idPerfil) VALUES(?,?,?)",
                e.getUsuario(), e.getClave(), e.getIdPerfil().getIdPerfil());
        e.setIdUsuario(id);
        return e;
    }

    @Override
    protected Usuario updateRow(Connection conn, Usuario e) throws SQLException {
        executeUpdate(conn,
                "UPDATE Usuario SET usuario=?, clave=?, idPerfil=? WHERE idUsuario=?",
                e.getUsuario(), e.getClave(), e.getIdPerfil().getIdPerfil(), e.getIdUsuario());
        return e;
    }

    @Override
    protected Usuario mapRow(ResultSet rs) throws SQLException {
        return Usuario.builder()
                .idUsuario(rs.getLong("idUsuario"))
                .usuario(rs.getString("usuario"))
                .clave(rs.getString("clave"))
                .idPerfil(Perfil.builder()
                        .idPerfil(rs.getLong("idPerfil"))
                        .nombre(rs.getString("perfilNombre"))
                        .build())
                .build();
    }
}
