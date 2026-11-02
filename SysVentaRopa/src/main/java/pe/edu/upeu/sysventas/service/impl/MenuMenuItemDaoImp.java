package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.MenuMenuItenTO;
import pe.edu.upeu.sysventas.service.IMenuMenuItemDao;

import java.util.*;

/**
 * Arma el menú según el perfil del usuario.
 * Cada opción: id, fxml, sección del menú, texto de la opción, título de la pantalla, tipo.
 * Tipo "T" = abre pantalla, "C" = cerrar sesión.
 */
public class MenuMenuItemDaoImp implements IMenuMenuItemDao {

    @Override
    public List<MenuMenuItenTO> listaAccesos(String perfil, Properties idioma) {
        String ventas = idioma.getProperty("menu.ventas", "Ventas");
        String inventario = idioma.getProperty("menu.inventario", "Inventario");
        String compras = idioma.getProperty("menu.compras", "Compras");
        String admin = idioma.getProperty("menu.admin", "Administración");

        // Todas las opciones posibles del menú (id -> pantalla FXML, sección y texto según el idioma)
        Map<String, MenuMenuItenTO> op = new LinkedHashMap<>();
        op.put("venta", item("venta", "/view/main_venta.fxml", ventas, idioma.getProperty("item.venta", "Registrar venta")));
        op.put("cliente", item("cliente", "/view/main_cliente.fxml", ventas, idioma.getProperty("item.cliente", "Clientes")));
        op.put("historialventa", item("historialventa", "/view/main_historial_venta.fxml", ventas, idioma.getProperty("item.historialventa", "Historial de ventas")));
        op.put("producto", item("producto", "/view/main_producto.fxml", inventario, idioma.getProperty("item.producto", "Prendas")));
        op.put("catalogo", item("catalogo", "/view/main_catalogo.fxml", inventario, idioma.getProperty("item.catalogo", "Categorías y unidades")));
        op.put("compra", item("compra", "/view/main_compra.fxml", compras, idioma.getProperty("item.compra", "Registrar compra")));
        op.put("proveedor", item("proveedor", "/view/main_proveedor.fxml", compras, idioma.getProperty("item.proveedor", "Proveedores")));
        op.put("historialcompra", item("historialcompra", "/view/main_historial_compra.fxml", compras, idioma.getProperty("item.historialcompra", "Historial de compras")));
        op.put("usuario", item("usuario", "/view/main_usuario.fxml", admin, idioma.getProperty("item.usuario", "Usuarios")));

        // PERMISOS: según el perfil del usuario se eligen las opciones que puede ver
        List<String> permitidos = switch (perfil) {
            case "Administrador de Tienda" -> new ArrayList<>(op.keySet());
            case "Vendedor de Tienda / Cajero" -> List.of("venta", "cliente", "historialventa");
            case "Encargado de Almacén" -> List.of("producto", "catalogo", "compra", "proveedor", "historialcompra");
            default -> List.of();
        };

        List<MenuMenuItenTO> lista = new ArrayList<>();
        for (String id : permitidos) lista.add(op.get(id));
        // Cerrar sesión siempre está disponible
        String salir = idioma.getProperty("item.salir", "Cerrar sesión");
        // Cerrar sesión siempre aparece
        lista.add(new MenuMenuItenTO("salir", "/view/login.fxml", "", salir, salir, "C"));
        return lista;
    }

    private MenuMenuItenTO item(String id, String fxml, String menu, String opcion) {
        return new MenuMenuItenTO(id, fxml, menu, opcion, opcion, "T");
    }

    @Override
    public Map<String, String[]> accesosAutorizados(List<MenuMenuItenTO> accesos) {
        Map<String, String[]> menuConfig = new HashMap<>();
        for (MenuMenuItenTO menu : accesos) {
            menuConfig.put("mi" + menu.getIdNombreObj(),
                    new String[]{menu.getRutaFile(), menu.getNombreTab(), menu.getTipoTab()});
        }
        return menuConfig;
    }

    @Override
    public int[] contarMenuMunuItem(List<MenuMenuItenTO> data) {
        int menui = 0, menuitem = 0;
        String menuN = null;
        for (MenuMenuItenTO mmiItem : data) {
            if (!mmiItem.getMenunombre().equals(menuN)) {
                menuN = mmiItem.getMenunombre();
                menui++;
            }
            if (!mmiItem.getMenuitemnombre().isEmpty()) menuitem++;
        }
        return new int[]{menui, menuitem};
    }
}
