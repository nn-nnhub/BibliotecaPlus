# RFC-011: Seguridad y mejora de reportes

## Solicitud de cambio (RFC)

Título del cambio: RFC-011 — Seguridad y mejora de reportes.

Issue: #11.

## Descripción del cambio

- Escapar el contenido dinámico antes de insertarlo como HTML en la interfaz (`app.js`).
- Neutralizar las fórmulas y escapar el separador, las comillas y los saltos de línea en el CSV de préstamos.

## Justificación

Defectos verificados con pruebas en un entorno aislado (H2 en memoria, sin tocar `./data`):

| ID | Evidencia antes de la corrección |
|---|---|
| H2 | Título, autor, ISBN y nombres se insertaban con `innerHTML` sin escapar. En la prueba, `<img src=x onerror=alert(1)>`, `<script>…` y `"><svg onload=…>` llegaban intactos al HTML (4 de 5 pruebas fallaban). Un libro con título nulo rompía además el filtro de búsqueda. |
| H9 | El CSV exportaba `1;=HYPERLINK("x";"y");Ana Torres;…`: la fórmula quedaba intacta y el `;` del título desplazaba las columnas. |

## Impacto en el sistema

`app.js`, `ReporteController`, `ExportadorCsv` (nuevo) y las pruebas asociadas. No se modifica el esquema ni los datos.

## Tipo de mantenimiento

Correctivo / Preventivo

## Riesgo

Bajo

## Prioridad

Media

## Costo estimado

Medio

## Responsable

Pendiente de asignación

## Fecha propuesta

17/10/2026

## Etiquetas

RFC · Media · Mejora

## Implementación

- `app.js`: la función `escaparHtml` convierte `& < > " '` y se aplica a todos los valores dinámicos de las cinco asignaciones a `innerHTML` (tabla y selector de libros, tabla y selector de usuarios, historial de préstamos). El identificador del botón "Devolver" pasa por `Number()`. El filtro de libros tolera valores nulos. Los mensajes de estado ya usaban `textContent`.
- `ExportadorCsv.java`: antepone `'` a los textos que empiezan por `=`, `+`, `-`, `@`, tabulación o retorno de carro; entrecomilla los valores con `;`, comillas o saltos de línea y duplica las comillas. Solo se neutralizan las cadenas, no los números. Se conservan el encabezado y el BOM UTF-8.
- `ReporteController.java`: delega la generación del CSV en `ExportadorCsv`.

## Pruebas

- `ExportacionCsvTest` (integración): reproduce H9 de extremo a extremo; falló antes del cambio.
- `ExportadorCsvTest` (5 casos): texto normal, fórmulas, separador, comillas y saltos de línea, combinación y estructura del archivo.
- `src/test/js/xss.test.js` (5 casos): ejecuta `app.js` en un contexto `vm` de Node con un `document` simulado. Cubre libros, usuarios e historial con HTML malicioso, el formato visible de los datos normales y los campos nulos.

Resultados:
- Java: 61 pruebas, 0 fallos (`mvn test`, JDK 17 portable).
- JavaScript: 5 pruebas, 0 fallos (`node --test "src/test/js/*.test.js"`).
- Antes de corregir H2, 4 de las 5 pruebas JavaScript fallaban.

## Limitaciones

- Las pruebas JavaScript no se ejecutan con `mvn test`; se lanzan por separado con Node. Integrarlas en Maven exigiría un plugin adicional.
- El `document` de la prueba es simulado: no se verificó el comportamiento en un navegador real.
- No existe una política de seguridad de contenido (CSP) ni un saneamiento en el servidor; el escape se hace solo al mostrar.
- No se comprobó la apertura del CSV en Excel con datos reales.

## Decisiones pendientes

- **Apóstrofo en el CSV:** neutralizar con `'` cambia el texto visible de una celda que empiece por esos caracteres (se verá `'=…`). Es la práctica habitual, pero queda por decidir si se acepta o se prefiere otro tratamiento.
- **Pruebas JavaScript en Maven:** decidir si se integran en el ciclo de `mvn test` o se mantienen como comando independiente.
- **Saneamiento en el servidor:** decidir si además de escapar al mostrar se debe restringir el contenido al guardar.

## Fuera de alcance

Quedan sin abordar en este RFC, por no estar verificados o aprobados: H10 (consultas repetidas por fila en `ReporteService`) y H11 (consola H2 y registro de SQL habilitados siempre).

## Evidencia

- `docs/capturas/rfc-011-csv-despues.png`: 61 pruebas Java aprobadas tras corregir el CSV.
- `docs/capturas/rfc-011-xss-despues.png`: 5 pruebas JavaScript aprobadas tras escapar el HTML.
- `docs/capturas/rfc-011-commit.png`: commit de la corrección de H2.

No existen capturas "antes" de las pruebas fallidas; la evidencia previa a la corrección es la salida registrada en la sesión de trabajo y descrita en la tabla de justificación.
