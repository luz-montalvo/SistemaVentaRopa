package pe.edu.upeu.sysventas.config;

import pe.edu.upeu.sysventas.controller.*;
import pe.edu.upeu.sysventas.repository.*;
import pe.edu.upeu.sysventas.service.*;
import pe.edu.upeu.sysventas.service.impl.*;
import pe.edu.upeu.sysventas.utils.ConsultaDNI;
import pe.edu.upeu.sysventas.utils.ConsultaRUC;

import java.util.HashMap;
import java.util.Map;

/**
 * Contenedor de dependencias (inyección manual, como lo haría Spring).
 * Capa 1: Repositorios -> Capa 2: Servicios -> Capa 3: Controladores.
 */
public class AppContext {

    private static AppContext instance;

    // Singleton: siempre se usa el mismo contenedor en toda la app
    public static synchronized AppContext getInstance() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    // Aquí se guardan todos los objetos creados: clave = clase, valor = objeto
    private final Map<Class<?>, Object> contenedor = new HashMap<>();

    private AppContext() {
        registrarRepositorios();
        registrarServicios();
        registrarControladores();
    }

    // CAPA 1 — REPOSITORIOS (uno por tabla)
    private void registrarRepositorios() {
        registrar(PerfilRepository.class, new PerfilRepository());
        registrar(UsuarioRepository.class, new UsuarioRepository());
        registrar(ClienteRepository.class, new ClienteRepository());
        registrar(UnidadMedidaRepository.class, new UnidadMedidaRepository());
        registrar(CategoriaRepository.class, new CategoriaRepository());
        registrar(ProductoRepository.class, new ProductoRepository());
        registrar(VentaRepository.class, new VentaRepository());
        registrar(VentaDetalleRepository.class, new VentaDetalleRepository());
        registrar(ProveedorRepository.class, new ProveedorRepository());
        registrar(CompraRepository.class, new CompraRepository());
        registrar(DetalleCompraRepository.class, new DetalleCompraRepository());
        registrar(DashboardRepository.class, new DashboardRepository());
    }

    // CAPA 2 — SERVICIOS (reciben su repositorio por constructor)
    private void registrarServicios() {
        registrar(IPerfilService.class, new PerfilServiceImp(getBean(PerfilRepository.class)));
        registrar(IUsuarioService.class, new UsuarioServiceImp(getBean(UsuarioRepository.class)));
        registrar(IClienteService.class, new ClienteServiceImp(getBean(ClienteRepository.class)));
        registrar(IUnidadMedidaService.class, new UnidadMedidaServiceImp(getBean(UnidadMedidaRepository.class)));
        registrar(ICategoriaService.class, new CategoriaServiceImp(getBean(CategoriaRepository.class)));
        registrar(IProductoService.class, new ProductoServiceImp(getBean(ProductoRepository.class)));
        registrar(IProveedorService.class, new ProveedorServiceImp(getBean(ProveedorRepository.class)));
        registrar(IVentaService.class, new VentaServiceImp(
                getBean(VentaRepository.class), getBean(VentaDetalleRepository.class)));
        registrar(ICompraService.class, new CompraServiceImp(
                getBean(CompraRepository.class), getBean(DetalleCompraRepository.class)));
        registrar(IDashboardService.class, new DashboardServiceImp(getBean(DashboardRepository.class)));
        registrar(IMenuMenuItemDao.class, new MenuMenuItemDaoImp());
        registrar(ConsultaDNI.class, new ConsultaDNI());
        // Consulta de RUC (SUNAT); usa ConsultaDNI para los RUC 10
        registrar(ConsultaRUC.class, new ConsultaRUC(getBean(ConsultaDNI.class)));
    }

    // CAPA 3 — CONTROLADORES JavaFX (el FXMLLoader los pide con getBean)
    private void registrarControladores() {
        registrar(LoginController.class, new LoginController(getBean(IUsuarioService.class)));
        // El Dashboard se registra antes porque la ventana principal lo necesita
        registrar(DashboardController.class, new DashboardController(getBean(IDashboardService.class)));
        registrar(MainguiController.class, new MainguiController(
                getBean(IMenuMenuItemDao.class), getBean(DashboardController.class)));
        registrar(ProductoController.class, new ProductoController(
                getBean(ICategoriaService.class), getBean(IProductoService.class), getBean(IUnidadMedidaService.class)));
        registrar(CatalogoController.class, new CatalogoController(
                getBean(ICategoriaService.class), getBean(IUnidadMedidaService.class)));
        registrar(ClienteController.class, new ClienteController(
                getBean(IClienteService.class), getBean(ConsultaDNI.class), getBean(ConsultaRUC.class)));
        registrar(VentaController.class, new VentaController(
                getBean(IProductoService.class), getBean(IClienteService.class), getBean(IVentaService.class),
                getBean(ConsultaDNI.class), getBean(ConsultaRUC.class)));
        registrar(HistorialVentaController.class, new HistorialVentaController(getBean(IVentaService.class)));
        registrar(ProveedorController.class, new ProveedorController(getBean(IProveedorService.class)));
        registrar(CompraController.class, new CompraController(
                getBean(IProductoService.class), getBean(IProveedorService.class), getBean(ICompraService.class)));
        registrar(HistorialCompraController.class, new HistorialCompraController(getBean(ICompraService.class)));
        registrar(UsuarioController.class, new UsuarioController(
                getBean(IUsuarioService.class), getBean(IPerfilService.class)));
    }

    private void registrar(Class<?> tipo, Object bean) {
        contenedor.put(tipo, bean);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> tipo) {
        Object bean = contenedor.get(tipo);
        if (bean == null) {
            bean = contenedor.values().stream()
                    .filter(b -> tipo.isAssignableFrom(b.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Bean no encontrado: " + tipo.getName() + "\n→ ¿Lo registraste en AppContext?"));
        }
        return (T) bean;
    }
}
