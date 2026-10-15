# Pruebas funcionales de BibliotecaPlus

## Objetivo

Verificar que los usuarios puedan gestionar libros, usuarios y préstamos desde la interfaz web y que la información mostrada coincida con la API REST.

## Precondiciones

1. Tener Java 17 o superior y Maven configurados.
2. Ejecutar `Iniciar-BibliotecaPlus.bat`.
3. Abrir `http://localhost:8080`.
4. La base H2 se carga con datos iniciales al iniciar la aplicación.

## Casos de prueba

| ID | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| PF-01 | Visualizar libros | Abrir el panel principal | Se muestran los libros registrados y su disponibilidad. |
| PF-02 | Registrar libro | Completar título, autor e ISBN y presionar Guardar libro | El libro aparece en el catálogo y aumenta el contador. |
| PF-03 | Visualizar usuarios | Revisar la sección Usuarios registrados | Se muestran ID, nombre y correo. |
| PF-04 | Registrar usuario | Completar nombre y correo y presionar Guardar usuario | El usuario aparece en la tabla y en el selector de préstamos. |
| PF-05 | Registrar préstamo | Seleccionar libro disponible, usuario y tipo; presionar Registrar préstamo | El préstamo aparece como Activo y el libro pasa a Prestado. |
| PF-06 | Mostrar nombres en historial | Revisar la tabla Préstamos | Se muestra el título del libro y el nombre del usuario, no solo sus ID. |
| PF-07 | Devolver préstamo | Presionar Devolver en un préstamo activo | El préstamo cambia a Devuelto y el libro vuelve a Disponible. |
| PF-08 | Modo oscuro | Presionar el botón con icono de luna | La interfaz cambia a modo oscuro. |
| PF-09 | Accesibilidad | Abrir el menú y activar Visión baja, Dislexia, Contraste o Máscara de lectura | Se aplica el ajuste visual seleccionado. |
| PF-10 | Restablecer accesibilidad | Presionar Restablecer | Se eliminan los ajustes visuales y se vuelve al estado normal. |
| PF-11 | Actualizar datos | Presionar Actualizar datos | Las tablas y contadores se sincronizan con la API. |
| PF-12 | Buscar libros | Escribir parte del título, autor o ISBN | La tabla muestra solo los libros coincidentes. |
| PF-13 | Buscar usuarios | Escribir parte del nombre o correo | La tabla muestra solo los usuarios coincidentes. |
| PF-14 | Desplazar libros | Cargar muchos registros y desplazar la tabla de libros | La tabla se desplaza sin deformar el dashboard. |
| PF-15 | Desplazar usuarios | Cargar muchos registros y desplazar la tabla de usuarios | La tabla se desplaza dentro de su propio contenedor. |
| PF-16 | Registrar vencimiento | Seleccionar una fecha de vencimiento y registrar un préstamo | El historial muestra la fecha límite. |
| PF-17 | Calcular multa | Devolver un préstamo con fecha vencida | Se muestran los días de retraso implícitos y la multa correcta. |
| PF-18 | Registrar usuario completo | Completar nombres, apellidos, DNI, correo y teléfono | El usuario aparece con todos sus datos. |
| PF-19 | Buscar usuario ampliado | Buscar por apellido o DNI | La tabla muestra el usuario coincidente. |
| PF-20 | Rechazar DNI duplicado | Intentar registrar un usuario con un DNI existente | La API rechaza el registro y muestra un mensaje. |

## Evidencia de ejecución

La prueba automatizada de compilación se ejecuta con:

```powershell
& "C:\Users\USER\Tools\apache-maven-3.9.9\bin\mvn.cmd" test
```

Resultado registrado durante el desarrollo: `BUILD SUCCESS`.
