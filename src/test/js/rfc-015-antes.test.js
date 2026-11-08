// Especificación ejecutable ANTES del RFC-015.
// Los fallos documentan capacidades que todavía no existen en la interfaz.
const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const path = require('node:path');
const { cargarApp } = require('./cargar-app');

const rutaHtml = path.join(__dirname, '..', '..', 'main', 'resources', 'static', 'index.html');
const html = fs.readFileSync(rutaHtml, 'utf8');
const respuestaOk = () => Promise.resolve({
    ok: true,
    status: 200,
    json: () => Promise.resolve([])
});

test('conserva el comportamiento existente: muestra libros y usuarios', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.libros = [{ id: 1, titulo: 'Clean Code', autor: 'Robert C. Martin', isbn: '9780132350884', disponible: true }];
              estado.usuarios = [{ id: 2, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@x.com', telefono: '999' }];
              mostrarLibros(); mostrarUsuarios();`);

    assert.match(nodos['#tablaLibros'].innerHTML, /Clean Code/);
    assert.match(nodos['#tablaUsuarios'].innerHTML, /Ana/);
});

test('cada libro muestra botones Editar y Eliminar', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.libros = [{ id: 1, titulo: 'Clean Code', autor: 'Robert C. Martin', isbn: '9780132350884', disponible: true }]; mostrarLibros();`);

    const fila = nodos['#tablaLibros'].innerHTML;
    assert.ok(fila.includes('Editar'), 'falta el botón Editar en la fila del libro');
    assert.ok(fila.includes('Eliminar'), 'falta el botón Eliminar en la fila del libro');
    assert.match(fila, /iniciarEdicionLibro\(1\)/);
    assert.match(fila, /eliminarLibro\(1\)/);
});

test('cada usuario muestra botones Editar y Eliminar', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.usuarios = [{ id: 2, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@x.com', telefono: '999' }]; mostrarUsuarios();`);

    const fila = nodos['#tablaUsuarios'].innerHTML;
    assert.ok(fila.includes('Editar'), 'falta el botón Editar en la fila del usuario');
    assert.ok(fila.includes('Eliminar'), 'falta el botón Eliminar en la fila del usuario');
    assert.match(fila, /iniciarEdicionUsuario\(2\)/);
    assert.match(fila, /eliminarUsuario\(2\)/);
});

test('los formularios permiten cancelar la edición de libros y usuarios', () => {
    assert.ok(html.includes('id="btnCancelarEdicionLibro"'), 'falta el control para cancelar la edición del libro');
    assert.ok(html.includes('id="btnCancelarEdicionUsuario"'), 'falta el control para cancelar la edición del usuario');

    const { ejecutar } = cargarApp();
    assert.strictEqual(ejecutar('typeof cancelarEdicionLibro'), 'function');
    assert.strictEqual(ejecutar('typeof cancelarEdicionUsuario'), 'function');
});

test('editar un libro usa PUT y actualiza tablas, selectores y contadores', async () => {
    const { ejecutar, disparar, llamadas } = cargarApp({ fetch: respuestaOk });
    await ejecutar('cargarDatos()');
    llamadas.length = 0;
    ejecutar(`estado.libros = [{ id: 1, titulo: 'Clean Code', autor: 'Robert C. Martin', isbn: '9780132350884', disponible: false }]; iniciarEdicionLibro(1);`);

    await disparar('#formLibro', 'submit');

    assert.ok(llamadas.some(({ url, metodo }) => url === '/api/libros/1' && metodo === 'PUT'));
    assert.ok(llamadas.filter(({ metodo }) => metodo === 'GET').length >= 3,
        'después de editar debe recargar libros, usuarios y préstamos');
});

test('editar un usuario usa PUT y actualiza las vistas', async () => {
    const { ejecutar, disparar, llamadas } = cargarApp({ fetch: respuestaOk });
    await ejecutar('cargarDatos()');
    llamadas.length = 0;
    ejecutar(`estado.usuarios = [{ id: 2, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@x.com', telefono: '999' }]; iniciarEdicionUsuario(2);`);

    await disparar('#formUsuario', 'submit');

    assert.ok(llamadas.some(({ url, metodo }) => url === '/api/usuarios/2' && metodo === 'PUT'));
    assert.ok(llamadas.filter(({ metodo }) => metodo === 'GET').length >= 3,
        'después de editar debe recargar libros, usuarios y préstamos');
});

test('cancelar la confirmación impide eliminar libros y usuarios', async () => {
    const { ejecutar, llamadas, confirmaciones } = cargarApp({ fetch: respuestaOk, confirm: () => false });
    await ejecutar('eliminarLibro(1)');
    await ejecutar('eliminarUsuario(2)');

    assert.strictEqual(confirmaciones.length, 2);
    assert.ok(!llamadas.some(({ metodo }) => metodo === 'DELETE'));
});

test('confirmar la eliminación usa DELETE y actualiza las vistas', async () => {
    const { ejecutar, llamadas } = cargarApp({ fetch: respuestaOk, confirm: () => true });
    await ejecutar('eliminarLibro(1)');
    await ejecutar('eliminarUsuario(2)');

    assert.ok(llamadas.some(({ url, metodo }) => url === '/api/libros/1' && metodo === 'DELETE'));
    assert.ok(llamadas.some(({ url, metodo }) => url === '/api/usuarios/2' && metodo === 'DELETE'));
    assert.ok(llamadas.filter(({ metodo }) => metodo === 'GET').length >= 6,
        'cada eliminación debe recargar libros, usuarios y préstamos');
});
