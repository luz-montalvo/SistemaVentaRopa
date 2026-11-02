package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.config.DatabaseConfig;
import pe.edu.upeu.sysventas.dto.DashboardDto;
import pe.edu.upeu.sysventas.dto.DashboardDto.Par;
import pe.edu.upeu.sysventas.dto.DashboardDto.UltimaVenta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas de resumen (SUM, COUNT, GROUP BY) para el Dashboard.
 * No es un repositorio de una tabla: solo lee datos de varias tablas, no guarda nada.
 */
public class DashboardRepository {

    /** Una prenda con este stock o menos se considera "stock bajo". */
    public static final int UMBRAL_STOCK_BAJO = 20;

    /**
     * @param idUsuario si no es null, las cifras de ventas solo cuentan las ventas de ese usuario
     *                  (así el cajero ve únicamente lo que él vendió).
     */
    public DashboardDto cargar(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        LocalDate iniMes = hoy.withDayOfMonth(1);
        LocalDate finMes = iniMes.plusMonths(1);
        LocalDate ini7 = hoy.minusDays(6);
        // idUsuario es un número (Long), por eso se puede concatenar sin riesgo de inyección SQL
        String fu = idUsuario == null ? "" : " AND v.idUsuario = " + idUsuario;

        try (Connection c = DatabaseConfig.getConnection()) {
            double[] vHoy = sumaYCuenta(c,
                    "SELECT COALESCE(SUM(v.precioTotal),0), COUNT(*) FROM Venta v WHERE v.fechaVenta = ?" + fu,
                    d(hoy));
            double[] vMes = sumaYCuenta(c,
                    "SELECT COALESCE(SUM(v.precioTotal),0), COUNT(*) FROM Venta v " +
                            "WHERE v.fechaVenta >= ? AND v.fechaVenta < ?" + fu, d(iniMes), d(finMes));
            // Utilidad = (precio de venta - costo) x cantidad = costo x %utilidad/100 x cantidad
            double[] utilidad = sumaYCuenta(c,
                    "SELECT COALESCE(SUM(d.precioUnit * d.porceUtil / 100 * d.cantidad),0), COUNT(*) " +
                            "FROM VentaDetalle d JOIN Venta v ON d.idVenta = v.idVenta " +
                            "WHERE v.fechaVenta >= ? AND v.fechaVenta < ?" + fu, d(iniMes), d(finMes));
            double[] cMes = sumaYCuenta(c,
                    "SELECT COALESCE(SUM(precioTotal),0), COUNT(*) FROM compras " +
                            "WHERE fechaCompra >= ? AND fechaCompra < ?", d(iniMes), d(finMes));

            int stockBajo = (int) sumaYCuenta(c,
                    "SELECT COUNT(*), COUNT(*) FROM Productos WHERE stock <= ?", UMBRAL_STOCK_BAJO)[0];
            int clientes = (int) sumaYCuenta(c, "SELECT COUNT(*), COUNT(*) FROM Cliente")[0];
            int prendas = (int) sumaYCuenta(c, "SELECT COUNT(*), COUNT(*) FROM Productos")[0];
            int proveedores = (int) sumaYCuenta(c, "SELECT COUNT(*), COUNT(*) FROM proveedor")[0];

            List<Par> ventas7 = ventasUltimosDias(c, ini7, hoy, fu);

            List<Par> porCategoria = pares(c,
                    "SELECT cat.nombre, SUM(d.netoTotal) AS sumaTotal FROM VentaDetalle d " +
                            "JOIN Venta v ON d.idVenta = v.idVenta " +
                            "JOIN Productos p ON d.idProducto = p.idProducto " +
                            "JOIN Categoria cat ON p.idCateg = cat.idCateg " +
                            "WHERE v.fechaVenta >= ? AND v.fechaVenta < ?" + fu +
                            " GROUP BY cat.nombre ORDER BY sumaTotal DESC", d(iniMes), d(finMes));

            List<Par> top = pares(c,
                    "SELECT p.nombre, SUM(d.cantidad) AS cant FROM VentaDetalle d " +
                            "JOIN Venta v ON d.idVenta = v.idVenta " +
                            "JOIN Productos p ON d.idProducto = p.idProducto " +
                            "WHERE v.fechaVenta >= ? AND v.fechaVenta < ?" + fu +
                            " GROUP BY p.nombre ORDER BY cant DESC LIMIT 5", d(iniMes), d(finMes));

            List<Par> critico = pares(c,
                    "SELECT nombre, stock FROM Productos WHERE stock <= ? ORDER BY stock ASC, nombre LIMIT 6",
                    UMBRAL_STOCK_BAJO);

            List<Par> cajeros = pares(c,
                    "SELECT u.usuario, COALESCE(SUM(v.precioTotal),0) AS sumaTotal FROM Venta v " +
                            "JOIN Usuario u ON v.idUsuario = u.idUsuario " +
                            "WHERE v.fechaVenta >= ? AND v.fechaVenta < ? " +
                            "GROUP BY u.usuario ORDER BY sumaTotal DESC LIMIT 5", d(iniMes), d(finMes));

            List<UltimaVenta> ultimas = ultimasVentas(c, fu);

            return new DashboardDto(vHoy[0], (int) vHoy[1], vMes[0], (int) vMes[1], utilidad[0],
                    cMes[0], (int) cMes[1], stockBajo, clientes, prendas, proveedores,
                    ventas7, porCategoria, top, critico, cajeros, ultimas);
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar el dashboard: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static Date d(LocalDate f) {
        return Date.valueOf(f);
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }

    /** SELECT que devuelve una sola fila con dos números: [0] = suma (o conteo), [1] = cantidad. */
    private double[] sumaYCuenta(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new double[]{rs.getDouble(1), rs.getDouble(2)};
                return new double[]{0, 0};
            }
        }
    }

    /** SELECT de dos columnas (texto, número) -> lista de pares. */
    private List<Par> pares(Connection c, String sql, Object... params) throws SQLException {
        List<Par> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new Par(rs.getString(1), rs.getDouble(2)));
            }
        }
        return lista;
    }

    /** Ventas de cada uno de los últimos 7 días; los días sin ventas salen con 0. */
    private List<Par> ventasUltimosDias(Connection c, LocalDate ini, LocalDate fin, String fu) throws SQLException {
        Map<LocalDate, Double> porDia = new LinkedHashMap<>();
        for (LocalDate f = ini; !f.isAfter(fin); f = f.plusDays(1)) porDia.put(f, 0.0);

        String sql = "SELECT v.fechaVenta, COALESCE(SUM(v.precioTotal),0) FROM Venta v " +
                "WHERE v.fechaVenta >= ? AND v.fechaVenta <= ?" + fu + " GROUP BY v.fechaVenta";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, d(ini), d(fin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) porDia.put(rs.getDate(1).toLocalDate(), rs.getDouble(2));
            }
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        List<Par> lista = new ArrayList<>();
        porDia.forEach((f, total) -> lista.add(new Par(f.format(fmt), total)));
        return lista;
    }

    private List<UltimaVenta> ultimasVentas(Connection c, String fu) throws SQLException {
        String sql = "SELECT v.idVenta, v.fechaVenta, cl.nombres, cl.tipoDocumento, v.precioTotal " +
                "FROM Venta v JOIN Cliente cl ON v.idCliente = cl.idCliente " +
                "WHERE 1 = 1" + fu + " ORDER BY v.idVenta DESC LIMIT 6";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<UltimaVenta> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong(1);
                // Cliente con RUC -> Factura (F001); con DNI -> Boleta (B001)
                String serie = "RUC".equalsIgnoreCase(rs.getString(4)) ? "F001" : "B001";
                lista.add(new UltimaVenta(id, rs.getDate(2).toLocalDate().format(fmt), rs.getString(3),
                        String.format("%s-%08d", serie, id), rs.getDouble(5)));
            }
        }
        return lista;
    }
}
