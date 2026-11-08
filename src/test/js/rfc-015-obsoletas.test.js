const test = require('node:test');
const assert = require('node:assert/strict');
const { cargarApp } = require('./cargar-app');

function controlada() {
    let resolve, reject;
    const promise = new Promise((si, no) => { resolve = si; reject = no; });
    return { promise, resolve, reject };
}
const ok = () => ({ ok: true, status: 200, json: async () => [] });

for (const tipo of ['Libro', 'Usuario']) {
    const libro = tipo === 'Libro';
    const campo = libro ? '#tituloLibro' : '#nombreUsuario';
    const mensaje = libro ? '#libroMensaje' : '#usuarioMensaje';
    const id = `estado.edicion${tipo}Id`;
    for (const resultado of ['exito', 'error HTTP', 'error de red']) {
        for (const cambio of ['otro registro', 'mismo registro', 'cancelar', 'nuevo envio', 'input', 'alta a edicion']) {
            test(`${tipo}: ${resultado} tardio conserva formulario y mensaje tras ${cambio}`, async () => {
                const pendiente = controlada();
                let escrituras = 0;
                const app = cargarApp({ fetch: (url, opciones) => {
                    if (['PUT', 'POST'].includes(opciones?.method)) {
                        return ++escrituras === 1 ? pendiente.promise : Promise.resolve(ok());
                    }
                    return Promise.resolve(ok());
                }});
                // Esperar la carga inicial antes de preparar los registros.
                await app.ejecutar('cargarDatos()');
                app.ejecutar(`estado.libros = [1, 2].map(id => ({ id, titulo: 'Libro ' + id, autor: 'Autor', isbn: 'ISBN' + id }));
                    estado.usuarios = [1, 2].map(id => ({ id, nombre: 'Usuario ' + id, apellidos: 'Apellido', dni: '7000000' + id, correo: 'a@b.com', telefono: '999' }));`);
                if (cambio !== 'alta a edicion') app.ejecutar(`iniciarEdicion${tipo}(1)`);
                const envio = app.disparar(`#form${tipo}`, 'submit');
                const peticion = app.llamadas.find(x => ['PUT', 'POST'].includes(x.metodo));
                assert.equal(peticion.metodo, cambio === 'alta a edicion' ? 'POST' : 'PUT');
                if (cambio === 'cancelar') app.ejecutar(`cancelarEdicion${tipo}()`);
                else if (cambio === 'input') {
                    app.nodos[campo].value = 'Borrador nuevo';
                    app.disparar(`#form${tipo}`, 'input');
                } else if (cambio === 'nuevo envio') await app.disparar(`#form${tipo}`, 'submit');
                else app.ejecutar(`iniciarEdicion${tipo}(${cambio === 'mismo registro' ? 1 : 2})`);
                app.nodos[mensaje].textContent = 'Mensaje de la nueva edicion';
                app.nodos[mensaje].className = 'message success';
                const antes = {
                    id: app.ejecutar(id),
                    campos: Object.fromEntries(Object.entries(app.nodos).map(([k, v]) => [k, v.value])),
                    titulo: app.nodos[`#tituloForm${tipo}`].textContent,
                    boton: app.nodos[`#btnGuardar${tipo}`].textContent,
                    cancelar: app.nodos[`#btnCancelarEdicion${tipo}`].hidden
                };
                if (resultado === 'exito') pendiente.resolve(ok());
                else if (resultado === 'error HTTP') pendiente.resolve({ ok: false, status: 400, json: async () => ({ message: 'Error anterior' }) });
                else pendiente.reject(new Error('Red anterior'));
                await envio;
                assert.equal(app.ejecutar(id), antes.id);
                for (const [selector, valor] of Object.entries(antes.campos)) {
                    assert.equal(app.nodos[selector].value, valor, selector);
                }
                assert.equal(app.nodos[`#tituloForm${tipo}`].textContent, antes.titulo);
                assert.equal(app.nodos[`#btnGuardar${tipo}`].textContent, antes.boton);
                assert.equal(app.nodos[`#btnCancelarEdicion${tipo}`].hidden, antes.cancelar);
                assert.equal(app.nodos[mensaje].textContent, 'Mensaje de la nueva edicion');
                assert.equal(app.nodos[mensaje].className, 'message success');
            });
        }
    }
    test(`${tipo}: un error vigente conserva los datos y muestra el error`, async () => {
        const app = cargarApp({ fetch: (url, opciones) => opciones?.method
            ? Promise.reject(new Error('Error vigente')) : Promise.resolve(ok()) });
        await app.ejecutar('cargarDatos()');
        app.ejecutar(`document.querySelector('${campo}').value = 'Datos vigentes'`);
        await app.disparar(`#form${tipo}`, 'submit');
        assert.equal(app.nodos[campo].value, 'Datos vigentes');
        assert.equal(app.nodos[mensaje].textContent, 'Error vigente');
        assert.equal(app.nodos[mensaje].className, 'message error');
    });
}
