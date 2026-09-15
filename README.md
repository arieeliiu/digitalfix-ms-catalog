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
- Actualizar servicios.
- Persistir los datos en Oracle Database en Amazon RDS.
- Validar nombre obligatorio y tarifa no negativa.

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
  "nombre": "Mantencion electrica preventiva",
  "descripcion": "Revision preventiva de la instalacion",
  "tarifa": 30000
}
```

## Validaciones

- `nombre` es obligatorio.
- `tarifa` es obligatoria.
- `tarifa` no puede ser negativa.
- Datos inválidos retornan `400 Bad Request`.
- Un identificador inexistente retorna `404 Not Found`.

## Variables de entorno

La conexión a la base de datos se configura externamente mediante:

```text
DB_HOST
DB_PORT (opcional, 1521)
DB_SERVICE (opcional, ORCL)
DB_USERNAME
DB_PASSWORD
```

Ejemplo de URL JDBC:

```text
jdbc:oracle:thin:@//<endpoint-rds>:1521/ORCL
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

Sin puertos publicados; usar el compose del BFF. Ver [guía](../digitalfix-ms-bff/DEPLOYMENT.md).
Las pruebas incluyen persistencia JPA con H2; Oracle remoto se verifica manualmente.
