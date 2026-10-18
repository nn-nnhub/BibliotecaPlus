# BibliotecaPlus

BibliotecaPlus es una aplicación web en Java y Spring Boot para administrar libros, usuarios y préstamos de una biblioteca. El proyecto aplica mantenimiento evolutivo, refactorización, validaciones, persistencia y gestión formal de solicitudes RFC.

## Tecnologías

- Java 17 o superior
- Spring Boot 3.3.5
- Maven y Spring Data JPA
- H2 Database
- HTML, CSS y JavaScript
- JUnit 5 y Mockito

## Requisitos e inicio

En Windows, la forma recomendada es ejecutar `Iniciar-BibliotecaPlus.bat`. El archivo busca primero un JDK y Maven dentro de `.tools/`; si no los encuentra, ejecuta `bootstrap-portable.ps1` para preparar herramientas locales y después inicia Spring Boot. La primera ejecución portable requiere Internet. No modifica el PATH global ni necesita instalar herramientas para todo Windows. La carpeta `.tools/` está excluida de Git.

También se puede iniciar desde una terminal con Java y Maven instalados:

```powershell
mvn spring-boot:run
```

Luego se abre [http://localhost:8080](http://localhost:8080).

## Inicialización

Al iniciar, Spring Boot:

1. Carga `src/main/resources/application.properties`.
2. Se conecta a H2 mediante `jdbc:h2:file:./data/bibliotecadb`.
3. Crea o actualiza las tablas con `spring.jpa.hibernate.ddl-auto=update`.
4. Ejecuta `DataInitializer`, que agrega datos de ejemplo solo cuando corresponde y evita duplicar registros.
5. Publica los controladores REST que utiliza la interfaz web.

La consola H2 opcional está en `/h2-console`, con URL `jdbc:h2:file:./data/bibliotecadb`, usuario `sa` y contraseña vacía.

## Cómo se guardan los datos

La base de datos se guarda en la carpeta del proyecto:

```text
data/bibliotecadb.mv.db
```

Los libros, usuarios y préstamos permanecen después de cerrar y volver a abrir la aplicación. `data/` está en `.gitignore` porque contiene información local y no debe subirse a GitHub. Para limpiar datos durante una prueba, se detiene la aplicación y se elimina únicamente ese archivo; al iniciar nuevamente se recrean las tablas y los datos iniciales.

## Funcionalidades

- Registro y búsqueda de libros por título, autor o ISBN.
- Validación de ISBN duplicado.
- Registro y búsqueda de usuarios por nombre, DNI o correo.
- Usuarios con nombres, apellidos, DNI, correo y teléfono.
- Validación de DNI duplicado.
- Registro, devolución e historial de préstamos.
- Fecha de vencimiento y cálculo de multas.
- Resumen de préstamos y libro más prestado.
- Exportación del historial a CSV compatible con Excel.
- Modo oscuro, accesibilidad y tablas con desplazamiento.
- Tarifas: Domicilio S/ 1.00 por día, Especial S/ 2.00 y Sala/General S/ 0.50.

## Arquitectura y estructura

La interfaz está separada de la lógica de negocio:

```text
src/main/java/com/bibliotecaplus/
├── model/          Entidades Libro, Usuario y Prestamo
├── repository/     Acceso a datos mediante JPA
├── service/        Reglas de negocio
├── controller/     Endpoints REST
├── validation/     Validaciones reutilizables
├── notification/   Mensajes de resultado
└── config/         Inicialización y configuración

src/main/resources/static/
├── index.html      Estructura visual
├── styles.css      Diseño y accesibilidad
├── app.js          Estado y comunicación con la API
└── ui.js           Comportamiento visual
```

El navegador llama a los controladores; los servicios aplican reglas; los repositorios leen y escriben en H2.

## API principal

- `GET/POST /api/libros`: consultar y registrar libros.
- `GET/POST /api/usuarios`: consultar y registrar usuarios.
- `GET/POST /api/prestamos`: consultar y registrar préstamos.
- `PUT /api/prestamos/{id}/devolver`: devolver un préstamo.
- `GET /api/reportes/resumen`: obtener indicadores.
- `GET /api/reportes/prestamos.csv`: descargar el reporte.

## Pruebas automáticas

Las pruebas están en `src/test/java/` y cubren validaciones, tarifas, multas, préstamos y reportes. Ejecutar:

```powershell
mvn test
mvn clean test
```

Los resultados quedan en `target/surefire-reports/`.

Las pruebas de la interfaz están en `src/test/js/` y **no forman parte de `mvn test`**. Se ejecutan con Node.js:

```powershell
node --test "src/test/js/*.test.js"
```

El detalle de cada suite está en [docs/pruebas-automatizadas.md](docs/pruebas-automatizadas.md).

## RFC y GitHub

La plantilla está en `.github/ISSUE_TEMPLATE/rfc.md` y la guía en [docs/gestion-rfc-github.md](docs/gestion-rfc-github.md).

- [RFC-001: refactorización exhaustiva](docs/RFC-001-refactorizacion.md) — [Issue #1](https://github.com/nn-nnhub/BibliotecaPlus/issues/1)
- [RFC-002: validación de ISBN](docs/RFC-002-validacion-isbn.md) — [Issue #2](https://github.com/nn-nnhub/BibliotecaPlus/issues/2)
- [RFC-003: reportes y exportación](docs/RFC-003-reportes-exportacion.md) — [Issue #3](https://github.com/nn-nnhub/BibliotecaPlus/issues/3)
- [RFC-004: vencimientos y multas](docs/RFC-004-vencimientos-multas.md) — [Issue #4](https://github.com/nn-nnhub/BibliotecaPlus/issues/4)
- [RFC-005: mejora del registro de usuarios](docs/RFC-005-mejora-usuarios.md) — [Issue #5](https://github.com/nn-nnhub/BibliotecaPlus/issues/5)
- [RFC-006: validación de DNI](docs/RFC-006-validacion-dni.md) — [Issue #6](https://github.com/nn-nnhub/BibliotecaPlus/issues/6)
- [RFC-007: ejecución portable](docs/RFC-007-ejecucion-portable.md) — [Issue #7](https://github.com/nn-nnhub/BibliotecaPlus/issues/7)
- [RFC-008: persistencia local](docs/RFC-008-persistencia-base-datos.md) — [Issue #8](https://github.com/nn-nnhub/BibliotecaPlus/issues/8)
- [RFC-009: corrección de multas y reglas de préstamo](docs/RFC-009.md) — [Issue #9](https://github.com/nn-nnhub/BibliotecaPlus/issues/9)
- [RFC-010: validación de libros y usuarios](docs/RFC-010.md) — [Issue #10](https://github.com/nn-nnhub/BibliotecaPlus/issues/10)
- [RFC-011: seguridad y mejora de reportes](docs/RFC-011.md) — [Issue #11](https://github.com/nn-nnhub/BibliotecaPlus/issues/11)

Flujo: crear RFC, definir prioridad y riesgo, implementar por capas, probar, documentar, crear commit descriptivo, subir a `main` y cerrar la Issue.

Repositorio: [github.com/nn-nnhub/BibliotecaPlus](https://github.com/nn-nnhub/BibliotecaPlus)

