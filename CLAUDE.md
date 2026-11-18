# digitalfix-ms-catalog

Contexto global en `../CLAUDE.md`. Aquí va solo lo propio de este servicio.

Catálogo de servicios técnicos (con tarifa) y repuestos (con stock), más el control de stock consumido por las órdenes.
Puerto 8081, Oracle (tablas `servicios_catalogo`, `repuestos_catalogo`, `descuentos_stock_orden`).

Clase principal: `CatalogApplication` (renombrada desde `DigitalfixMsCatalogApplication`).

**Credenciales locales:** `src/main/resources/application.properties` tiene, sin commitear, el endpoint, el usuario y la contraseña de la RDS como valores por defecto de `${DB_URL:...}`, `${DB_USERNAME:...}` y `${DB_PASSWORD:...}`. No los commitees (`git add -p` y omite esas líneas), no los copies a otros archivos y no muestres la contraseña. En Docker los reemplaza `digitalfix-infra/apps/.env`.

Local contra la RDS no necesita otras variables. `ddl-auto=update` ya creó las tablas en la RDS nueva.

## Mapa del código (`cl.digitalfix.catalog`)

| Paquete | Clave |
|---|---|
| `controller` | `ServicioController` (`/api/catalog/services`) y `RepuestoController` (`/api/catalog/spare-parts`) |
| `service` | `ServicioService` y `RepuestoService` (CRUD, `descontarStock`, `ajustarStock`) |
| `entity` | `ServicioCatalogo` (tarifa `precision 12, scale 2`), `Repuesto` y `DescuentoStockOrden` (PK `orden_id` + `firma_solicitud`) |
| `repository` | `RepuestoRepository.buscarTodosParaActualizar(ids)` y `DescuentoStockOrdenRepository.buscarParaActualizar(id)`, ambos con `PESSIMISTIC_WRITE` |
| `mapper` | `ServicioMapper` y `RepuestoMapper` (estáticos) |
| `exception` | `GlobalExceptionHandler`: 404 → `{"mensaje": ...}` y validación → `{campo: mensaje}`. Los `ResponseStatusException` usan el formato por defecto de Spring |

Estilo de DTOs: los request de servicio y repuesto son clases Lombok (`@Getter @Setter`) con Bean Validation. Los de stock son records. Respeta el estilo del archivo que edites.

## Seguridad

- **Catalog no tiene Spring Security**: no valida JWT. Su protección es la red (`digitalfix-backend`, sin puerto publicado) y el BFF, que aplica los roles.
- Nunca publiques su puerto ni lo expongas por el Gateway. Si se agrega validación de JWT, hay que coordinarla con Workorders, que ya reenvía el Bearer en `nombreServicio` pero **no** en `descontarStock` ni en `ajustarStock`.

## Stock: invariantes que no se pueden romper

- `POST /spare-parts/discount-stock` (`DescontarStockRequest {ordenId, repuestos}`):
  - Idempotente por `ordenId`: agrupa cantidades por repuesto y construye una firma ordenada `id:cant|id:cant`.
  - Con la misma firma ya registrada, no hace nada. Con una firma distinta para la misma orden, responde 409.
  - Valida todo antes de modificar: si falta stock en un repuesto, no descuenta ninguno.
- `PUT /spare-parts/adjust-order-stock` (`AjustarStockOrdenRequest {ordenId, anteriores, nuevos}`):
  - Reconcilia por diferencia.
  - Si la firma actual es igual a `nuevos`, es un reintento y no hace nada. Si no es igual a `anteriores`, responde 409.
  - Rechaza duplicados y cantidades ≤ 0 con 400. El stock nunca queda negativo.
- Firma vacía = `"-"`, porque Oracle convierte `''` en NULL. Largo máximo de la firma: 2000.
- Bloqueos pesimistas por `id` en orden ascendente para evitar deadlocks. Mantén ese orden si agregas consultas.
- Estos dos endpoints son **internos** (los llama solo Workorders). El BFF bloquea `discount-stock`.
- El caso pide que el stock baje al asignar: eso lo dispara Workorders y no se cambia aquí.

## Tests

```powershell
.\mvnw.cmd -B test      # H2 en modo Oracle (src/test/resources/application.properties)
```

- `RepuestoControllerTests`, `ServicioControllerTests` y `PersistenciaCatalogoTests`.
- Todo cambio en stock necesita tests de: reintento idempotente, firma distinta → 409, stock insuficiente sin efectos parciales, y duplicados o cantidades inválidas → 400.
- Contrato consumido por Workorders (`CatalogoClient`) y por el BFF (`CatalogoClient` y DTOs `ServicioCatalogoResponse`/`RepuestoCatalogoResponse`): si cambias un DTO, revisa ambos.
