package pe.edu.upeu.sysventas.dto;

import java.util.List;

/** Datos que muestra el Dashboard de la pantalla de inicio (ya calculados desde la BD). */
public record DashboardDto(
        double ventasHoy, int numVentasHoy,
        double ventasMes, int numVentasMes,
        double utilidadMes,
        double comprasMes, int numComprasMes,
        int stockBajo, int totalClientes, int totalPrendas, int totalProveedores,
        List<Par> ventas7Dias,
        List<Par> ventasPorCategoria,
        List<Par> topPrendas,
        List<Par> stockCritico,
        List<Par> ventasPorCajero,
        List<UltimaVenta> ultimasVentas) {

    /** Pareja etiqueta - valor (sirve para gráficos y listas). */
    public record Par(String etiqueta, double valor) {
    }

    /** Fila de la lista "Últimas ventas". */
    public record UltimaVenta(long idVenta, String fecha, String cliente, String comprobante, double total) {
    }
}
