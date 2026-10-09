# DigitalFix catalog: refactor no funcional con Lombok

9 de octubre de 2026. Alcance: tipos Java, código repetitivo y documentación.
La referencia funcional es el checkout inicial, incluidos los cambios locales
previos. No se cambiaron ramas ni se crearon commits durante esta tarea.

## Responsabilidades y decisiones

Catalog mantiene Controller → Service → Repository → JPA/Oracle, con mappers
simples y DTOs independientes. RepuestoSolicitud pasa a RepuestoRequest;
ServicioCatalogoSolicitud a ServicioRequest; los DTOs de descuento también
terminan en Request. Controllers, services y repositories usan los sufijos
Controller, Service y Repository. ServicioCatalogo sigue siendo el nombre de la
entidad; el DTO de salida pasa a ServicioResponse.

Los dos controllers y services usan @RequiredArgsConstructor. Repuesto y
ServicioCatalogo, y los dos requests mutables, usan @Getter/@Setter y constructor
vacío público con @NoArgsConstructor. DescuentoStockOrden usa @Getter y un
constructor vacío protegido; conserva su constructor que registra la fecha.
No se generan equals/hashCode/toString ni se utiliza @Data.

RepuestoRequest conserva sus campos reales id/nombre/descripcion/stock y sigue
siendo una clase mutable: convertirlo al ejemplo repuestoId/cantidad rompería
el contrato CRUD. Omitir stock sigue produciendo cero; null explícito se rechaza.
Los records de descuento y response se conservan como records.

Se preservaron los dos cambios locales anteriores al trabajo: @NotBlank en la
descripción del request de repuesto y application.properties con DB_URL.
En la ejecución inicial, una prueba existente falló porque su fixture omitía
la descripción. Se añadió una descripción válida a los fixtures de stock,
sin cambiar la expectativa 201 ni retirar la validación de producción. Se
agregó una prueba explícita del rechazo 400 cuando falta descripción.
Stock, agrupación de cantidades, descuentos, idempotencia, bloqueos,
transacciones y mensajes HTTP no se modifican.

## Nomenclatura y archivos renombrados

Antes de editar se registraron declaraciones, imports, constructores, referencias,
generics, tests, mappers y dependencias de los tres servicios. Los renombrados
actuaron sobre identificadores Java; literals y nombres externos se conservaron.

| Tipo anterior | Tipo actual | Archivo actual |
|---|---|---|
| RepuestoSolicitud | RepuestoRequest | `src/main/java/cl/digitalfix/catalog/dto/request/RepuestoRequest.java` |
| ServicioCatalogoSolicitud | ServicioRequest | `src/main/java/cl/digitalfix/catalog/dto/request/ServicioRequest.java` |
| RepuestoStockSolicitud | RepuestoStockRequest | `src/main/java/cl/digitalfix/catalog/dto/request/RepuestoStockRequest.java` |
| DescontarStockSolicitud | DescontarStockRequest | `src/main/java/cl/digitalfix/catalog/dto/request/DescontarStockRequest.java` |
| RepuestoControlador | RepuestoController | `src/main/java/cl/digitalfix/catalog/controller/RepuestoController.java` |
| ServicioCatalogoControlador | ServicioController | `src/main/java/cl/digitalfix/catalog/controller/ServicioController.java` |
| RepuestoServicio | RepuestoService | `src/main/java/cl/digitalfix/catalog/service/RepuestoService.java` |
| ServicioCatalogoServicio | ServicioService | `src/main/java/cl/digitalfix/catalog/service/ServicioService.java` |
| RepuestoRepositorio | RepuestoRepository | `src/main/java/cl/digitalfix/catalog/repository/RepuestoRepository.java` |
| ServicioCatalogoRepositorio | ServicioRepository | `src/main/java/cl/digitalfix/catalog/repository/ServicioRepository.java` |
| DescuentoStockOrdenRepositorio | DescuentoStockOrdenRepository | `src/main/java/cl/digitalfix/catalog/repository/DescuentoStockOrdenRepository.java` |
| ServicioCatalogoMapper | ServicioMapper | `src/main/java/cl/digitalfix/catalog/mapper/ServicioMapper.java` |
| ServicioCatalogoResponse | ServicioResponse | `src/main/java/cl/digitalfix/catalog/dto/response/ServicioResponse.java` |
| ManejadorExcepciones | GlobalExceptionHandler | `src/main/java/cl/digitalfix/catalog/exception/GlobalExceptionHandler.java` |
| RepuestoControladorTests | RepuestoControllerTests | `src/test/java/cl/digitalfix/catalog/controller/RepuestoControllerTests.java` |
| ServicioCatalogoControladorTests | ServicioControllerTests | `src/test/java/cl/digitalfix/catalog/controller/ServicioControllerTests.java` |

Los archivos antiguos se sustituyen por los renombrados; no quedan clases
Java duplicadas ni referencias a los tipos anteriores. Las clases Application
conservan sus nombres para mantener las referencias de arranque de Docker/IDE.
No se crean nuevas capas, clases de negocio ni abstracciones. Se crean tres
informes REFACTOR_LOMBOK.md, uno por servicio, y se actualizan los README.

## Incorporación de Lombok

pom.xml añade org.projectlombok:lombok con scope provided y optional=true,
utilizando la versión 1.18.46 gestionada por Spring Boot 4.1.1. Maven Compiler
configura annotationProcessorPaths explícitamente con ${lombok.version}.
El plugin de Spring Boot excluye Lombok del JAR de ejecución. Los Dockerfile no
necesitan cambios: ya copian pom.xml y src y compilan con JDK 21.

La integración sigue la [documentación oficial de Lombok para Maven](https://projectlombok.org/setup/maven).
No se cambia la versión de Java, Spring Boot ni de dependencias existentes.
No se añade @Data, @Builder, @EqualsAndHashCode ni @ToString.

## Compatibilidad verificada

- Mismos endpoints y métodos HTTP: 9 en BFF, 6 en Workorders y 9 en Catalog.
- Mismos campos y contratos JSON, incluyendo status, oid y repuestos.
- Misma seguridad, Bearer, roles, scopes, claims y autorizaciones.
- Mismas reglas de dominio, validaciones, consultas y transacciones.
- Mismos nombres y anotaciones JPA de tablas, columnas, colecciones y generación de IDs.
- Properties, recursos de prueba, Dockerfile, Compose y .env.example idénticos
  byte por byte al inicio de la tarea (incluidos cambios locales anteriores).
- Entidades y requests mutables conservan las firmas y visibilidad de métodos,
  getters/setters y constructores, comparadas con javap antes/después.
- Comparación de producción sin diferencias fuera de nombres, imports, formato
  y getters/setters/constructores simples reemplazados por Lombok.
- Sin ciclos de dependencias; services sin RestClient/WebClient ni records.
- Ninguna implementación nueva de RabbitMQ, Kafka o Zookeeper. Sus menciones
  documentales en Notify/Audit/Report permanecen fuera de este alcance.

## Validación

Se utilizó Maven Wrapper con Temurin JDK 21.0.12.1 temporal. El wrapper del
checkout no es ejecutable: se invocó con bash, sin modificar su contenido/permisos.
Las dependencias estaban disponibles, por lo que las comprobaciones usaron -o.

```bash
JAVA_HOME=/tmp/digitalfix-jdk21 bash ./mvnw -o clean test
JAVA_HOME=/tmp/digitalfix-jdk21 bash ./mvnw -o -DskipTests package
```

Resultado: **10 tests, 0 fallos, 0 errores, 0 omitidos; package correcto**.

Se verificó procesamiento de anotaciones, inyección de dependencias, contextos
Spring, persistencia con H2, validaciones y serialización/deserialización Jackson.
Los JAR tienen bytecode Java 21 (major 65) y no contienen Lombok en BOOT-INF/lib.
Las pruebas de seguridad del BFF usan JWT firmados y servidores HTTP locales.
Estas comprobaciones no certifican un despliegue remoto en AWS/Entra/Oracle.

## Deuda técnica detectada

| Archivo | Problema | Riesgo | Mejora futura recomendada |
|---|---|---|---|
| `src/main/resources/application.properties; ../digitalfix-ms-bff/docker-compose.yml` | La configuración local exige DB_URL; Compose entrega DB_HOST/DB_PORT/DB_SERVICE. | El arranque de Catalog puede fallar si DB_URL no se entrega por otro mecanismo. | Acordar variables de despliegue o un fallback explícito antes de modificar properties. |
| `src/main/java/cl/digitalfix/catalog/dto/request/RepuestoRequest.java; entity/Repuesto.java` | La descripción se valida como obligatoria en el request local, pero no en la entidad. | Entradas directas al service y entradas HTTP tienen límites de validación diferentes. | Decidir si la regla debe ser autoritativa en dominio; no ampliar validaciones en este refactor. |
| `src/main/java/cl/digitalfix/catalog/controller/RepuestoController.java` | Catalog no incorpora Resource Server y la llamada desde Workorders no envía Bearer. | El acceso depende del aislamiento de red vigente. | Definir autenticación entre servicios en una tarea funcional explícita. |
