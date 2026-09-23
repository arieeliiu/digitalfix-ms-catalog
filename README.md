# DigitalFix - Microservicio Catalog

Microservicio encargado de administrar el catálogo básico de servicios de mantenimiento eléctrico de DigitalFix.

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

- Listar servicios.
- Crear servicios.
- Actualizar servicios, tarifas y stock.
- Eliminar servicios.
- Persistir los datos en Oracle Database en Amazon RDS.
- Validar nombre obligatorio, tarifa no negativa y stock no negativo.

## Endpoints

### Listar servicios

```http
GET /api/catalog/services
```

### Crear servicio

```http
POST /api/catalog/services
```

Ejemplo:

```json
{
  "nombre": "Mantencion electrica",
  "descripcion": "Revision general de instalacion electrica",
  "tarifa": 25000,
  "stock": 10
}
```

### Actualizar servicio

```http
PUT /api/catalog/services/{id}
```

Ejemplo:

```json
{
  "nombre": "Mantencion electrica preventiva",
  "descripcion": "Revision preventiva de la instalacion",
  "tarifa": 30000,
  "stock": 8
}
```
### Eliminar servicio

```http
DELETE /api/catalog/services/{id}
```

Elimina el servicio indicado. Si el identificador no existe, retorna 404 Not Found.

## Validaciones

- `nombre` es obligatorio.
- `tarifa` es obligatoria.
- `tarifa` no puede ser negativa.
- `stock` es obligatorio.
- `stock` no puede ser negativo.
- Datos inválidos retornan `400 Bad Request`.
- Un identificador inexistente retorna `404 Not Found`.

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
DB_PORT (opcional, por defecto 1521)
DB_SERVICE (opcional, por defecto ORCL)
DB_USERNAME
DB_PASSWORD
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

## Pruebas

Ejecutar:

```powershell
.\mvnw.cmd verify
```

Las pruebas cubren:

- Consulta correcta de servicios.
- Creación correcta de servicios.
- Actualización correcta de servicios.
- Validaciones de entrada.
- Respuestas `400 Bad Request`.
- Respuestas `404 Not Found`.

## Despliegue integrado

Catalog se ejecuta como un servicio interno y no expone su puerto directamente a Internet.

La orquestación del despliegue se mantiene en el repositorio `digitalfix-infra`, desde donde se configuran las variables de entorno y la comunicación con el BFF.

La conexión real con Oracle RDS debe verificarse nuevamente al desplegar los cambios de esquema, especialmente la incorporación del campo `stock`.

