const test = require('node:test');
const assert = require('node:assert');
const { cargarApp } = require('./cargar-app');

const respuestaOk = () => Promise.resolve({
    ok: true,
    status: 200,
    json: () => Promise.resolve([])
});

test('cancelar una edición restaura ambos formularios sin enviar peticiones', () => {
    const { nodos, llamadas, ejecutar } = cargarApp({ fetch: respuestaOk });
    llamadas.length = 0;
    ejecutar(`estado.libros = [{ id: 1, titulo: 'Libro', autor: 'Autor', isbn: 'ISBN', disponible: true }];
              estado.usuarios = [{ id: 2, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@x.com', telefono: '999' }];
              iniciarEdicionLibro(1); iniciarEdicionUsuario(2);`);

    assert.strictEqual(nodos['#tituloFormLibro'].textContent, 'Editar libro');
    assert.strictEqual(nodos['#tituloFormUsuario'].textContent, 'Editar usuario');
    assert.strictEqual(nodos['#btnCancelarEdicionLibro'].hidden, false);
    assert.strictEqual(nodos['#btnCancelarEdicionUsuario'].hidden, false);

    ejecutar('cancelarEdicionLibro(); cancelarEdicionUsuario();');

    assert.strictEqual(ejecutar('estado.edicionLibroId'), null);
    assert.strictEqual(ejecutar('estado.edicionUsuarioId'), null);
    assert.strictEqual(nodos['#tituloLibro'].value, '');
    assert.strictEqual(nodos['#nombreUsuario'].value, '');
    assert.strictEqual(nodos['#tituloFormLibro'].textContent, 'Registrar libro');
    assert.strictEqual(nodos['#tituloFormUsuario'].textContent, 'Registrar usuario');
    assert.strictEqual(nodos['#btnCancelarEdicionLibro'].hidden, true);
    assert.strictEqual(nodos['#btnCancelarEdicionUsuario'].hidden, true);
    assert.ok(!llamadas.some(({ metodo }) => metodo === 'PUT' || metodo === 'DELETE'));
});

test('muestra los mensajes reales del backend al rechazar eliminaciones', async () => {
    const fetch = (url, opciones) => {
        if (opciones?.method === 'DELETE') {
            const message = url.startsWith('/api/libros/')
                ? 'No se puede eliminar el libro porque tiene préstamos asociados'
                : 'No se puede eliminar el usuario porque tiene préstamos asociados';
            return Promise.resolve({ ok: false, status: 400, json: () => Promise.resolve({ message }) });
        }
        return respuestaOk();
    };
    const { nodos, llamadas, ejecutar } = cargarApp({ fetch, confirm: () => true });
    llamadas.length = 0;

    await ejecutar('eliminarLibro(1)');
    await ejecutar('eliminarUsuario(2)');

    assert.strictEqual(nodos['#libroMensaje'].textContent,
        'No se puede eliminar el libro porque tiene préstamos asociados');
    assert.strictEqual(nodos['#usuarioMensaje'].textContent,
        'No se puede eliminar el usuario porque tiene préstamos asociados');
    assert.strictEqual(nodos['#libroMensaje'].className, 'message error');
    assert.strictEqual(nodos['#usuarioMensaje'].className, 'message error');
    assert.strictEqual(llamadas.filter(({ metodo }) => metodo === 'DELETE').length, 2);
    assert.strictEqual(llamadas.filter(({ metodo }) => metodo === 'GET').length, 0,
        'un DELETE rechazado no debe recargar las vistas');
});

test('editar un libro no envía el campo de disponibilidad', async () => {
    const { nodos, llamadas, ejecutar, disparar } = cargarApp({ fetch: respuestaOk });
    llamadas.length = 0;
    ejecutar(`estado.libros = [{ id: 1, titulo: 'Libro', autor: 'Autor', isbn: 'ISBN', disponible: false }]; iniciarEdicionLibro(1);`);
    nodos['#tituloLibro'].value = 'Libro actualizado';

    await disparar('#formLibro', 'submit');

    const peticion = llamadas.find(({ metodo }) => metodo === 'PUT');
    const cuerpo = JSON.parse(peticion.opciones.body);
    assert.deepStrictEqual(cuerpo, { titulo: 'Libro actualizado', autor: 'Autor', isbn: 'ISBN' });
    assert.ok(!Object.hasOwn(cuerpo, 'disponible'));
});
