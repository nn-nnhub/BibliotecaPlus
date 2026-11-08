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

Consulta el [índice de los 21 RFC](docs/indice-rfc.md) para ver sus documentos, issues, PR y estado. El [formato de documentación](docs/formato-documento-rfc.md) y la [guía de gestión](docs/gestion-rfc-github.md) explican cómo registrar y verificar un cambio.

