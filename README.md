# DigitalFix - Microservicio Catalog

Microservicio encargado de administrar el catálogo de servicios técnicos y repuestos de DigitalFix.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring Data JPA
- Oracle Database 19c en Amazon RDS

## Integrantes

- Ariel Molina
- Lucas Ferrada

## Funcionalidades

Permite:

- Listar, crear, actualizar y eliminar servicios técnicos.
- Administrar las tarifas de los servicios.
- Listar, crear, actualizar y eliminar repuestos.
- Administrar el stock de repuestos.
- Descontar stock asociado a una orden de trabajo.
- Evitar descuentos duplicados para una misma orden.
- Validar reintentos de descuento.
- Evitar consumos simultáneos inconsistentes de stock.
- Persistir los datos en Oracle Database en Amazon RDS.
- Validar nombre obligatorio, tarifa no negativa y stock de repuestos no negativo.

## Endpoints

### Servicios

#### Listar servicios

```http
GET /api/catalog/services
```

#### Crear servicio

```http
POST /api/catalog/services
```

Ejemplo:

```json
{
  "nombre": "Mantencion electrica",
  "descripcion": "Revision general de instalacion electrica",
  "tarifa": 25000
}
```

#### Actualizar servicio

```http
PUT /api/catalog/services/{id}
```

Ejemplo:

```json
{
  "nombre": "Mantencion electrica preventiva",
  "descripcion": "Revision preventiva de la instalacion",
  "tarifa": 30000
}
```

#### Eliminar servicio

```http
DELETE /api/catalog/services/{id}
```

Elimina el servicio indicado.

Si el identificador no existe, retorna `404 Not Found`.

### Repuestos

#### Listar repuestos

```http
GET /api/catalog/spare-parts
```

#### Crear repuesto

```http
POST /api/catalog/spare-parts
```

Ejemplo:

```json
{
  "nombre": "Interruptor termomagnetico",
  "descripcion": "Interruptor para tablero electrico",
  "stock": 20
}
```

#### Actualizar repuesto

```http
PUT /api/catalog/spare-parts/{id}
```

Ejemplo:

```json
{
  "nombre": "Interruptor termomagnetico",
  "descripcion": "Interruptor para tablero electrico",
  "stock": 15
}
```

#### Eliminar repuesto

```http
DELETE /api/catalog/spare-parts/{id}
```

Elimina el repuesto indicado.

Si el identificador no existe, retorna `404 Not Found`.

#### Descontar stock por orden

```http
POST /api/catalog/spare-parts/discount-stock
```

Endpoint interno destinado a ser utilizado por Workorders cuando una orden es asignada.

Ejemplo:

```json
{
  "ordenId": 15,
  "repuestos": [
    {
      "repuestoId": 4,
      "cantidad": 2
    }
  ]
}
```

Catalog valida que:

- todos los repuestos existan;
- exista stock suficiente para todos;
- una misma orden no descuente stock más de una vez;
- un reintento de la misma orden corresponda a los mismos repuestos y cantidades.

Si una orden ya descontó stock y recibe nuevamente exactamente la misma solicitud, la operación se considera un reintento válido y no vuelve a descontar.

Si la misma orden intenta descontar repuestos o cantidades diferentes, responde `409 Conflict`.

Si algún repuesto no existe, responde `404 Not Found`.

Si no existe stock suficiente, responde `409 Conflict`.

El descuento se ejecuta dentro de una transacción.

Durante la operación, los repuestos involucrados se bloquean para actualización mediante bloqueo pesimista, evitando que dos órdenes consuman simultáneamente las mismas existencias.

Los identificadores y cantidades repetidos dentro de una misma solicitud se agrupan antes de validar y descontar el stock.

## Idempotencia del descuento

Cada descuento queda asociado al identificador de la orden.

Catalog guarda una firma de la solicitud procesada, basada en los identificadores de repuesto y sus cantidades.

Ejemplo:

```text
4:2|7:1
```

Esto permite distinguir entre:

```text
Misma orden + mismos repuestos y cantidades
→ reintento válido
→ no vuelve a descontar
```

y:

```text
Misma orden + repuestos o cantidades diferentes
→ conflicto
→ 409 Conflict
```

## Validaciones

- `nombre` es obligatorio.
- `tarifa` es obligatoria para los servicios.
- `tarifa` no puede ser negativa.
- `stock` es obligatorio para los repuestos.
- `stock` no puede ser negativo.
- `ordenId` debe ser válido para una solicitud de descuento.
- `repuestoId` debe ser positivo.
- `cantidad` debe ser mayor que cero.
- Una solicitud de descuento debe contener al menos un repuesto.
- Datos inválidos retornan `400 Bad Request`.
- Un identificador inexistente retorna `404 Not Found`.
- Stock insuficiente o un reintento inconsistente retorna `409 Conflict`.

## Variables de entorno

La conexión a Oracle se configura mediante variables de entorno.

Puede utilizarse una URL completa:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

O construirla a partir de:

```text
DB_HOST
DB_PORT
DB_SERVICE
DB_USERNAME
DB_PASSWORD
```

Valores por defecto:

```text
DB_PORT=1521
DB_SERVICE=ORCL
```

Formato esperado:

```text
jdbc:oracle:thin:@//HOST:1521/SERVICIO
```

Las credenciales no deben almacenarse en el repositorio.

## Ejecución local

Configurar primero las variables de entorno y luego ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

El servicio queda disponible por defecto en:

```text
http://localhost:8081
```

## Persistencia

Durante el desarrollo, Hibernate administra la actualización del esquema mediante:

```text
spring.jpa.hibernate.ddl-auto=update
```

Catalog persiste actualmente información relacionada con:

- servicios técnicos;
- repuestos y stock;
- órdenes que ya realizaron un descuento de stock.

## Pruebas

Ejecutar:

```powershell
.\mvnw.cmd verify
```

Las pruebas automatizadas actuales cubren principalmente:

- consulta correcta de servicios;
- creación correcta de servicios;
- actualización correcta de servicios;
- validaciones de entrada;
- respuestas `400 Bad Request`;
- respuestas `404 Not Found`;
- persistencia mediante H2 en el entorno de pruebas existente.

Además, se verificó con `mvnw verify` que la lógica de descuento de stock, idempotencia por orden y bloqueo de repuestos compila correctamente sin afectar las pruebas automatizadas existentes.

Actualmente no existen pruebas automatizadas específicas para:

- descuento de stock;
- reintentos idempotentes;
- conflictos por contenido diferente;
- concurrencia sobre el mismo stock.

Estos comportamientos deben comprobarse mediante pruebas específicas o durante la integración entre Workorders y Catalog.

## Despliegue integrado

Catalog se ejecuta como un servicio interno y no debe exponerse directamente a Internet.

La arquitectura esperada es:

```text
Frontend
→ API Gateway
→ BFF
→ Workorders
→ Catalog
→ Oracle RDS
```

El descuento de stock será solicitado por Workorders al asignar una orden.

La orquestación del despliegue se mantiene en el repositorio `digitalfix-infra`, desde donde se configuran las variables de entorno y la comunicación entre los servicios.

La conexión real con Oracle RDS debe verificarse nuevamente después de desplegar esta versión.

## Refactor por capas (5 de octubre de 2026)

Los controllers utilizan DTOs independientes en `dto/request` y `dto/response`.
La capa service coordina persistencia y mapeo mediante `mapper`, sin exponer
entidades JPA en los contratos HTTP. Las validaciones, rutas, códigos, estados,
reglas de dominio y configuración existentes se conservan.
Las solicitudes de repuestos conservan stock cero cuando se omite el campo y rechazan un valor null explícito.

La revisión y las decisiones integradas se documentan en
[REFACTOR.md del BFF](../digitalfix-ms-bff/REFACTOR.md), disponible en el workspace
con los repositorios hermanos. No se incorpora RabbitMQ ni Kafka.
