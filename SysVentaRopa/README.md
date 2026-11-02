# SysVentaRopa – Sistema de Gestión de Ventas de Ropa (JavaFX + H2)

Proyecto basado en el caso de estudio "Venta de Ropa". La base de datos sigue el modelo final
`db_modelo/modelo_venta_de_ropa.architect` (11 tablas).

## Caso de estudio
Una tienda de ropa necesita un sistema de escritorio para registrar sus ventas y compras,
controlar el stock de prendas y saber qué vendió cada cajero. Cada usuario entra con su
clave y solo ve las opciones de su perfil.

## Lista de requerimientos
| N° | Requerimiento | Pantalla |
|---|---|---|
| RF01 | Iniciar sesión con usuario y clave (clave encriptada) | Login |
| RF02 | Mostrar el menú según el perfil (Administrador de Tienda, Vendedor/Cajero, Encargado de Almacén) | Principal |
| RF03 | CRUD de usuarios y asignación de perfil | Usuarios |
| RF04 | CRUD de clientes (DNI/RUC), con consulta automática de DNI (RENIEC) y RUC (SUNAT) | Clientes |
| RF05 | CRUD de categorías y unidades de medida | Categorías y unidades |
| RF06 | CRUD de prendas con precio, % de utilidad y stock | Prendas |
| RF07 | Registrar una venta con carrito, IGV 18 % y descuento de stock | Registrar venta |
| RF08 | Consultar el historial de ventas con su detalle | Historial de ventas |
| RF09 | CRUD de proveedores (RUC) | Proveedores |
| RF10 | Registrar una compra a proveedor y aumentar el stock | Registrar compra |
| RF11 | Consultar el historial de compras con su detalle | Historial de compras |
| RF12 | Generar e imprimir la Boleta (cliente con DNI) o Factura (cliente con RUC) de cada venta | Registrar venta, Historial de ventas |
| RNF01 | Validar los campos de cada formulario y mostrar un mensaje del resultado | Todas |
| RNF02 | Cambiar el tema (Claro, Oscuro, Rosa) y el idioma (ES, EN, FR) | Principal |

## Investigación adicional (Punto 6): encriptación de claves
Las claves de la tabla `Usuario` ya no se guardan en texto plano. Se guarda un **hash SHA-256 con sal**
(`utils/PasswordUtil.java`):
- **Hash SHA-256**: convierte la clave en un código que no se puede revertir.
- **Sal**: 16 bytes aleatorios por usuario, así dos usuarios con la misma clave tienen hashes distintos.
- En la BD se guarda `sal:hash` (69 caracteres). En el login se vuelve a calcular el hash de lo escrito y se compara.
- Al iniciar la app, las claves que aún estén en texto plano (datos de prueba del SQL) se encriptan solas.
- En **Usuarios** se ve la columna "Clave guardada" con el hash. Al editar, si deja la clave vacía se conserva la anterior.

## Boleta / Factura
- Al registrar una venta se pregunta si desea imprimir el comprobante.
- Cliente con **DNI** → BOLETA DE VENTA serie B001; con **RUC** → FACTURA DE VENTA serie F001 (N° = id de la venta, ej. B001-00000012).
- Se puede reimprimir desde **Historial de ventas** → seleccionar venta → *Imprimir boleta / factura*.
- Usa `javafx.print.PrinterJob` (`components/Comprobante.java`). Para guardarlo como PDF elija "Microsoft Print to PDF" en el diálogo.

## Consulta DNI / RUC
- **DNI** (8 dígitos): nombres desde eldni.com (`utils/ConsultaDNI.java`).
- **RUC 10...** (persona con negocio): usa el DNI que va dentro del RUC; no necesita nada más.
- **RUC 20...** (empresa): API de Decolecta (`utils/ConsultaRUC.java`). Registrarse gratis en https://decolecta.com,
  copiar el token y ponerlo en `application.properties` → `api.ruc.token=TU_TOKEN`.
- En **Registrar venta → Nuevo cliente** se llena solo al terminar de escribir el número.
  En **Clientes** se usa el botón *RENIEC / SUNAT*. Necesita internet.

## Ejecutar
```
mvn clean compile javafx:run
```
En IntelliJ: elegir arriba la configuración **SysVentaRopa (Maven)** y darle ▶ (ya hace `clean compile javafx:run`).
Ojo: `javafx:run` solo NO compila; si se usa sin `compile` abre la versión vieja o no abre nada.
o desde IntelliJ: ejecutar la clase `pe.edu.upeu.sysventas.App`.

La base H2 se crea sola en `./data/ventaropa_db` con los datos de prueba de
`src/main/resources/schema_ropa.sql`. Si cambias el esquema, borra el archivo `data/ventaropa_db.mv.db`.

## Usuarios de prueba
| Usuario | Clave | Perfil | Qué ve en el menú |
|---|---|---|---|
| ventita123 | 123456 | Administrador de Tienda | Todo |
| Emmanuel Mantilla (Admin) | 654321 | Administrador de Tienda | Todo |
| Ana Quispe (Cajera Turno M) | 123456 | Vendedor de Tienda / Cajero | Ventas, Clientes, Historial de ventas |

(El perfil "Encargado de Almacén" ve Prendas, Categorías y unidades, Compras, Proveedores e Historial de compras.
Se puede crear un usuario con ese perfil desde **Usuarios**.)

## Tablas del modelo → pantalla que las usa
| Tabla | Pantalla |
|---|---|
| Perfil, Usuario | Login, Usuarios |
| Cliente | Clientes (y registro rápido dentro de Ventas) |
| Categoria, UnidadMedida | Categorías y unidades |
| Productos | Prendas |
| Venta, VentaDetalle | Registrar venta, Historial de ventas |
| proveedor | Proveedores |
| compras, detalle_compra | Registrar compra, Historial de compras |

## Reglas de cálculo
- Precio de venta = Costo (preUnitario) × (1 + porceUtil / 100)  → `porceUtil` se guarda como porcentaje (25 = 25 %).
- Venta: Neto = Σ (precio de venta × cantidad), IGV = Neto × 18 %, Total = Neto + IGV.
- Compra: Neto = Σ (precioCompra × cantidad), IGV = Neto × 18 %, Total = Neto + IGV.
- Al registrar una **venta** el stock baja; al registrar una **compra** el stock sube.
  Cabecera + detalle + stock se guardan en una sola transacción (si algo falla, no se guarda nada).

## Arquitectura (capas)
```
model/         Clases de cada tabla (Venta y Compra heredan de DocumentoComercial)
repository/    SQL con JDBC (un repositorio por tabla)
service/       Reglas de negocio (interfaces + impl)
controller/    Controladores JavaFX (uno por pantalla)
config/        AppContext (inyección de dependencias) y DatabaseConfig (H2 + HikariCP)
components/    Utilidades de la interfaz: tabla, validación, mensajes, temas
resources/view Pantallas FXML    resources/css  Estilos (temas Claro, Oscuro, Rosa)
```
