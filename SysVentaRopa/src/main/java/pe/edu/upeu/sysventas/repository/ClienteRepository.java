package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// CAPA REPOSITORIO: aquí está el SQL de la tabla Cliente (JDBC).
// Los métodos genéricos (save, update, delete, findById) vienen de AbstractJpaRepository.
public class ClienteRepository extends AbstractJpaRepository<Cliente, Long> {
    // Nombre de la tabla y de su llave primaria, usados por los métodos genéricos
    @Override protected String getTableName() { return "Cliente"; }
    @Override protected String getPkColumn()  { return "idCliente"; }

    // Lista todos los clientes ordenados por nombre
    @Override
    public List<Cliente> findAll() {
        return executeQuery("SELECT * FROM Cliente ORDER BY nombres");
    }

    /** Busca un cliente por su DNI o RUC (columna única dniruc). */
    public Optional<Cliente> findByDniruc(String dniruc) {
        return executeQueryOne("SELECT * FROM Cliente WHERE dniruc = ?", dniruc);
    }

    // ¿Existe otro cliente con ese DNI/RUC? (idExcluir evita compararse consigo mismo al editar)
    public boolean existeDniruc(String dniruc, Long idExcluir) {
        return executeExists("SELECT 1 FROM Cliente WHERE dniruc = ? AND idCliente <> ?",
                dniruc, idExcluir == null ? 0L : idExcluir);
    }

    // INSERT con parámetros "?" (evita inyección SQL). Devuelve el id generado por la BD
    @Override
    protected Cliente insert(Connection conn, Cliente e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO Cliente(dniruc,nombres,tipoDocumento,repLegal,direccion,celular) VALUES(?,?,?,?,?,?)",
                e.getDniruc(), e.getNombres(), e.getTipoDocumento().name(),
                e.getRepLegal(), e.getDireccion(), e.getCelular());
        e.setIdCliente(id);
        return e;
    }

    // UPDATE de todos los campos del cliente según su idCliente
    @Override
    protected Cliente updateRow(Connection conn, Cliente e) throws SQLException {
        executeUpdate(conn,
                "UPDATE Cliente SET dniruc=?, nombres=?, tipoDocumento=?, repLegal=?, direccion=?, celular=? WHERE idCliente=?",
                e.getDniruc(), e.getNombres(), e.getTipoDocumento().name(),
                e.getRepLegal(), e.getDireccion(), e.getCelular(), e.getIdCliente());
        return e;
    }

    // Convierte una fila del ResultSet (resultado SQL) en un objeto Cliente
    @Override
    protected Cliente mapRow(ResultSet rs) throws SQLException {
        return Cliente.builder()
                .idCliente(rs.getLong("idCliente"))
                .dniruc(rs.getString("dniruc"))
                .nombres(rs.getString("nombres"))
                .tipoDocumento(TipoDocumento.valueOf(rs.getString("tipoDocumento")))
                .repLegal(rs.getString("repLegal"))
                .direccion(rs.getString("direccion"))
                .celular(rs.getString("celular"))
                .build();
    }
}
