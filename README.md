# DigitalFix — Catalog

Microservicio encargado de administrar el catálogo de servicios técnicos y repuestos de DigitalFix, además de controlar el stock utilizado por las órdenes de trabajo.

## Integrantes

- Ariel Molina
- Lucas Ferrada

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring Data JPA
- Spring Web MVC
- Jakarta Validation
- Oracle Database en Amazon RDS
- H2 para pruebas
- Lombok
- Docker

## Arquitectura

Catalog funciona como un microservicio interno.

El flujo principal es:

```text
Angular
   ↓
AWS API Gateway
   ↓
BFF
   ↓
Catalog
   ↓
Oracle RDS
```

Catalog también participa en el flujo de asignación de órdenes:

```text
BFF
 ↓
Workorders
 ↓
Catalog
 ↓
Oracle RDS
```

El frontend no se comunica directamente con Catalog.

## Responsabilidades

Catalog se encarga de:

- administrar servicios técnicos;
- administrar tarifas de servicios;
- administrar repuestos;
- administrar stock;
- persistir servicios y repuestos en Oracle RDS;
- validar datos de entrada;
- descontar stock cuando Workorders asigna una orden;
- evitar descuentos duplicados;
- validar reintentos;
- prevenir inconsistencias de stock durante operaciones simultáneas.

## Puerto

El microservicio utiliza:

```text
8081
```

## Servicios técnicos

Catalog expone CRUD completo de servicios.

| Método | Ruta | Función |
|---|---|---|
| GET | `/api/catalog/services` | Lista servicios |
| POST | `/api/catalog/services` | Crea un servicio |
| PUT | `/api/catalog/services/{id}` | Actualiza un servicio |
| DELETE | `/api/catalog/services/{id}` | Elimina un servicio |

### Crear servicio

```http
POST /api/catalog/services
```

Ejemplo:

```json
{
  "nombre": "Mantención eléctrica",
  "descripcion": "Revisión general de instalación eléctrica",
  "tarifa": 25000
}
```

### Actualizar servicio

```http
PUT /api/catalog/services/{id}
```

Ejemplo:

```json
{
  "nombre": "Mantención eléctrica preventiva",
  "descripcion": "Revisión preventiva de la instalación",
  "tarifa": 30000
}
```

### Eliminar servicio

```http
DELETE /api/catalog/services/{id}
```

Si el identificador no existe, Catalog responde:

```text
404 Not Found
```

## Repuestos

Catalog expone CRUD completo de repuestos.

| Método | Ruta | Función |
|---|---|---|
| GET | `/api/catalog/spare-parts` | Lista repuestos |
| POST | `/api/catalog/spare-parts` | Crea un repuesto |
| PUT | `/api/catalog/spare-parts/{id}` | Actualiza un repuesto |
| DELETE | `/api/catalog/spare-parts/{id}` | Elimina un repuesto |

### Crear repuesto

```http
POST /api/catalog/spare-parts
```

Ejemplo:

```json
{
  "nombre": "Interruptor termomagnético",
  "descripcion": "Interruptor para tablero eléctrico",
  "stock": 20
}
```

### Actualizar repuesto

```http
PUT /api/catalog/spare-parts/{id}
```

Ejemplo:

```json
{
  "nombre": "Interruptor termomagnético",
  "descripcion": "Interruptor para tablero eléctrico",
  "stock": 15
}
```

### Eliminar repuesto

```http
DELETE /api/catalog/spare-parts/{id}
```

Si el identificador no existe, responde:

```text
404 Not Found
```

## Validaciones de servicios

Para los servicios se valida:

- `nombre` obligatorio;
- `tarifa` obligatoria;
- `tarifa` mayor o igual a cero.

La descripción puede ser opcional.

Ejemplo inválido:

```json
{
  "nombre": "",
  "tarifa": -100
}
```

Catalog responde:

```text
400 Bad Request
```

## Validaciones de repuestos

Para los repuestos se valida:

- `nombre` obligatorio;
- `descripcion` obligatoria;
- `stock` obligatorio;
- `stock` mayor o igual a cero.

Un stock negativo genera:

```text
400 Bad Request
```

## Descuento de stock

Catalog posee un endpoint interno utilizado por Workorders:

```http
POST /api/catalog/spare-parts/discount-stock
```

Este endpoint no corresponde a una operación administrativa del frontend.

Su objetivo es descontar los repuestos utilizados durante la asignación de una orden.

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

- `ordenId` exista en la solicitud;
- `ordenId` sea positivo;
- exista al menos un repuesto;
- los identificadores de repuesto sean válidos;
- las cantidades sean mayores que cero;
- todos los repuestos existan;
- exista stock suficiente.

## Flujo de descuento

El flujo implementado es:

```text
Orden pasa a ASIGNADA
        ↓
Workorders recibe los repuestos
        ↓
Workorders llama a Catalog
        ↓
Catalog agrupa cantidades
        ↓
Catalog bloquea los repuestos
        ↓
Catalog valida existencia
        ↓
Catalog valida stock
        ↓
Catalog descuenta cantidades
        ↓
Catalog registra el descuento de la orden
        ↓
Oracle RDS
```

Si Catalog rechaza la operación, Workorders no finaliza la asignación con esos repuestos.

## Idempotencia

Catalog evita descontar stock dos veces para la misma orden.

Cada operación de descuento queda asociada al:

```text
ordenId
```

Catalog genera una firma basada en los repuestos y cantidades.

Ejemplo:

```text
4:2|7:1
```

Esto permite distinguir dos situaciones.

### Mismo reintento

```text
misma orden
+
mismos repuestos
+
mismas cantidades
```

Resultado:

```text
no vuelve a descontar stock
```

### Reintento diferente

```text
misma orden
+
repuestos o cantidades diferentes
```

Resultado:

```text
409 Conflict
```

## Agrupación de repuestos

Si una solicitud contiene varias veces el mismo repuesto, Catalog agrupa las cantidades antes de procesarlas.

Ejemplo:

```json
{
  "ordenId": 20,
  "repuestos": [
    {
      "repuestoId": 4,
      "cantidad": 2
    },
    {
      "repuestoId": 4,
      "cantidad": 3
    }
  ]
}
```

Catalog procesa el resultado como:

```text
repuesto 4
cantidad total 5
```

## Control de concurrencia

Durante el descuento de stock se utiliza bloqueo pesimista:

```text
PESSIMISTIC_WRITE
```

Los repuestos solicitados son bloqueados mientras se realiza la transacción.

Esto evita que dos órdenes descuenten simultáneamente las mismas unidades disponibles y generen un stock inconsistente.

## Transacción de stock

El descuento se ejecuta dentro de una transacción.

Antes de guardar los cambios se valida que:

- todos los repuestos existan;
- todos tengan stock suficiente.

Solo después se realiza el descuento.

Esto evita aplicar parcialmente una solicitud donde algunos repuestos son válidos y otros no.

## Manejo de errores de stock

### Repuesto inexistente

```text
404 Not Found
```

### Stock insuficiente

```text
409 Conflict
```

### Orden reutilizada con contenido diferente

```text
409 Conflict
```

### Solicitud inválida

```text
400 Bad Request
```

## Persistencia

Catalog utiliza Oracle Database en Amazon RDS.

Persiste información relacionada con:

```text
servicios
repuestos
stock
descuentos realizados por orden
```

La información de órdenes de trabajo no pertenece a Catalog.

Catalog solo almacena el identificador necesario para controlar la idempotencia del descuento.

## Configuración de Oracle

La conexión utiliza:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Ejemplo de formato:

```text
jdbc:oracle:thin:@//HOST:1521/SERVICIO
```

Las credenciales reales no deben almacenarse en Git.

En el despliegue Docker se entregan mediante:

```text
digitalfix-infra/apps/.env
```

## Hibernate

Actualmente se utiliza:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate crea o actualiza las estructuras necesarias sin eliminar los datos existentes durante esta etapa del proyecto.

También se configura:

```properties
spring.jpa.open-in-view=false
```

para evitar mantener la sesión de persistencia abierta durante toda la respuesta HTTP.

## Configuración principal

El archivo:

```text
src/main/resources/application.properties
```

configura:

- puerto `8081`;
- URL de Oracle RDS;
- usuario de Oracle;
- contraseña de Oracle;
- driver JDBC;
- configuración de Hibernate.

## Comunicación con BFF

Para operaciones del catálogo utilizadas desde el frontend, el flujo es:

```text
Frontend
→ API Gateway
→ BFF
→ Catalog
```

El BFF controla los permisos según el rol antes de reenviar las operaciones.

Actualmente:

```text
Admin
```

puede crear, editar y eliminar servicios y repuestos.

```text
Operador
```

puede consultar servicios y repuestos.

```text
Cliente
```

puede consultar servicios.

Catalog permanece como servicio interno y no se expone directamente desde el frontend.

## Comunicación con Workorders

Para el descuento de stock:

```text
Workorders
→ Catalog
```

Workorders utiliza:

```http
POST /api/catalog/spare-parts/discount-stock
```

cuando una orden pasa a estado `ASIGNADA` y contiene repuestos.

Catalog es responsable de validar y modificar el stock.

## Ejecución local

Configurar las variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Luego ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

El servicio queda disponible en:

```text
http://localhost:8081
```

## Pruebas

Ejecutar:

```powershell
.\mvnw.cmd verify
```

Las pruebas actuales cubren principalmente:

- listado de servicios;
- creación de servicios;
- actualización de servicios;
- eliminación de servicios;
- operaciones de repuestos;
- validaciones de entrada;
- respuestas HTTP;
- persistencia mediante H2.

La integración real de stock también se comprueba mediante las pruebas funcionales del sistema desplegado:

```text
BFF
→ Workorders
→ Catalog
→ Oracle RDS
```

## Docker

Catalog se construye mediante:

```text
Dockerfile
```

El despliegue conjunto se administra desde:

```text
digitalfix-infra/docker-compose.yml
```

El Compose central ejecuta:

```text
BFF
Catalog
Workorders
```

Catalog utiliza internamente el puerto:

```text
8081
```

pero no lo publica directamente hacia Internet.

Los servicios se comunican mediante la red Docker.

## Despliegue actual

Actualmente Catalog se ejecuta en contenedor dentro de la instancia EC2.

El flujo desplegado es:

```text
Angular
   ↓
AWS API Gateway
   ↓
BFF en EC2
   ├──→ Catalog en EC2
   │       ↓
   │    Oracle RDS
   │
   └──→ Workorders en EC2
            ↓
          Catalog
            ↓
         Oracle RDS
```

La conexión real con Oracle RDS está integrada y es utilizada por los CRUD y por el control de stock.

## Estructura principal

El proyecto se organiza por responsabilidades:

```text
controller
service
repository
entity
dto
mapper
exception
```

Los controllers exponen los endpoints.

Los services contienen las reglas de negocio.

Los repositories administran la persistencia.

Las entities representan los datos almacenados.

Los DTOs representan solicitudes y respuestas.

Los mappers separan las entidades JPA de los contratos HTTP.

## Estado actual

Actualmente se encuentran operativos:

- listado de servicios;
- creación de servicios;
- actualización de servicios;
- eliminación de servicios;
- listado de repuestos;
- creación de repuestos;
- actualización de repuestos;
- eliminación de repuestos;
- persistencia en Oracle RDS;
- administración real de stock;
- descuento de stock desde Workorders;
- agrupación de repuestos repetidos;
- idempotencia de descuentos;
- validación de stock suficiente;
- bloqueo pesimista durante el descuento;
- prevención de descuentos duplicados;
- integración con BFF;
- integración con Workorders;
- integración con Docker en EC2.

El CRUD del catálogo y la integración con Oracle RDS fueron probados funcionalmente desde el frontend administrativo.

## Documentación técnica

Los archivos de refactor existentes conservan información histórica sobre reorganización por capas, convenciones técnicas y uso de Lombok.

El `README.md` representa el estado funcional actual de Catalog.

## Flujo de trabajo

Los cambios se desarrollan en ramas independientes y se integran a `main` mediante Pull Request.

La rama `main` representa la versión estable utilizada para la entrega.