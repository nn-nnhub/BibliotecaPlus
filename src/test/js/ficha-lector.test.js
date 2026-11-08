const test = require('node:test');
const assert = require('node:assert/strict');
const { cargarApp } = require('./cargar-app');

const respuesta = datos => Promise.resolve({ ok: true, status: 200, json: async () => datos });

const ficha = {
    usuario: { id: 1, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@example.com', telefono: '999111222' },
    resumen: { totalPrestamos: 2, prestamosActivos: 1, prestamosDevueltos: 1, prestamosVencidos: 0, multasAcumuladas: 8 },
    historial: [
        { id: 10, libroId: 2, libroTitulo: 'Libro activo', fechaPrestamo: '2026-10-01', fechaVencimiento: '2026-11-01', fechaDevolucion: null, tipoPrestamo: 'DOMICILIO', devuelto: false, vencido: false, multa: 0 },
        { id: 9, libroId: 3, libroTitulo: 'Libro devuelto', fechaPrestamo: '2026-09-01', fechaVencimiento: '2026-09-10', fechaDevolucion: '2026-09-14', tipoPrestamo: 'ESPECIAL', devuelto: true, vencido: false, multa: 8 }
    ]
};

function servidor(url) {
    if (url === '/api/usuarios/1/ficha') return respuesta(ficha);
    return respuesta([]);
}

function solicitudControlada() {
    let resolver;
    let rechazar;
    const promesa = new Promise((resolve, reject) => { resolver = resolve; rechazar = reject; });
    return { promesa, resolver: datos => resolver({ ok: true, status: 200, json: async () => datos }),
        resolverHttp: () => resolver({ ok: false, status: 400, json: async () => ({ message: 'HTTP anterior' }) }), rechazar };
}

test('cerrar invalida una respuesta de ficha pendiente sin cambiar su contenido', async () => {
    const pendiente = solicitudControlada();
    const app = cargarApp({ fetch: url => url.endsWith('/ficha') ? pendiente.promesa : respuesta([]) });
    const apertura = app.ejecutar('abrirFichaUsuario(1)');
    app.ejecutar('cerrarFichaUsuario()');
    const html = app.nodos['#fichaContenido'].innerHTML;
    const texto = app.nodos['#fichaContenido'].textContent;

    pendiente.resolver(ficha);
    await apertura;

    assert.equal(app.nodos['#fichaLector'].open, false);
    assert.equal(app.nodos['#fichaContenido'].innerHTML, html);
    assert.equal(app.nodos['#fichaContenido'].textContent, texto);
});

test('la respuesta tardía de A no reemplaza la ficha de B', async () => {
    const a = solicitudControlada();
    const b = solicitudControlada();
    const app = cargarApp({ fetch: url => url === '/api/usuarios/1/ficha' ? a.promesa
        : url === '/api/usuarios/2/ficha' ? b.promesa : respuesta([]) });
    const aperturaA = app.ejecutar('abrirFichaUsuario(1)');
    const aperturaB = app.ejecutar('abrirFichaUsuario(2)');
    b.resolver({ ...ficha, usuario: { ...ficha.usuario, id: 2, nombre: 'Beatriz' } });
    await aperturaB;
    const htmlB = app.nodos['#fichaContenido'].innerHTML;
    assert.match(htmlB, /Beatriz Torres/);

    a.resolver(ficha);
    await aperturaA;

    assert.equal(app.nodos['#fichaLector'].open, true);
    assert.equal(app.nodos['#fichaContenido'].innerHTML, htmlB);
});

test('un error tardío invalidado al cerrar no modifica la ficha', async () => {
    const pendiente = solicitudControlada();
    const app = cargarApp({ fetch: url => url.endsWith('/ficha') ? pendiente.promesa : respuesta([]) });
    const apertura = app.ejecutar('abrirFichaUsuario(1)');
    app.ejecutar('cerrarFichaUsuario()');
    const texto = app.nodos['#fichaContenido'].textContent;
    pendiente.rechazar(new Error('Error tardío'));
    await apertura;

    assert.equal(app.nodos['#fichaLector'].open, false);
    assert.equal(app.nodos['#fichaContenido'].textContent, texto);
});

test('cada usuario muestra un botón Ver ficha', () => {
    const app = cargarApp();
    app.ejecutar("estado.usuarios = [{ id: 1, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@example.com', telefono: '999' }]; mostrarUsuarios();");

    assert.match(app.nodos['#tablaUsuarios'].innerHTML, /Ver ficha/);
    assert.match(app.nodos['#tablaUsuarios'].innerHTML, /abrirFichaUsuario\(1, this\)/);
});

test('abre la ficha consultando el endpoint del usuario', async () => {
    const app = cargarApp({ fetch: servidor });
    app.ejecutar("document.querySelector('#fichaLector').open = false");
    app.llamadas.length = 0;

    await app.ejecutar('abrirFichaUsuario(1)');

    assert.equal(app.llamadas[0].url, '/api/usuarios/1/ficha');
    assert.equal(app.nodos['#fichaLector'].open, true);
    assert.match(app.nodos['#fichaContenido'].innerHTML, /Ana Torres/);
});

test('cierra la ficha sin alterar los datos cargados', () => {
    const app = cargarApp();
    app.ejecutar("document.querySelector('#fichaLector').showModal(); cerrarFichaUsuario()");

    assert.equal(app.nodos['#fichaLector'].open, false);
});

test('muestra resumen e historial de préstamos', () => {
    const app = cargarApp();
    app.ejecutar(`mostrarFichaUsuario(${JSON.stringify(ficha)})`);
    const html = app.nodos['#fichaContenido'].innerHTML;

    assert.match(html, /Activos[^0-9]*1/);
    assert.match(html, /Devueltos[^0-9]*1/);
    assert.match(html, /S\/ 8\.00/);
    assert.match(html, /Libro activo/);
    assert.match(html, /Libro devuelto/);
});

test('muestra un estado vacío para un lector sin préstamos', () => {
    const app = cargarApp();
    const vacia = { ...ficha, resumen: { totalPrestamos: 0, prestamosActivos: 0, prestamosDevueltos: 0, prestamosVencidos: 0, multasAcumuladas: 0 }, historial: [] };

    app.ejecutar(`mostrarFichaUsuario(${JSON.stringify(vacia)})`);

    assert.match(app.nodos['#fichaContenido'].innerHTML, /no tiene préstamos/i);
});

test('escapa los datos del lector y del historial para evitar XSS', () => {
    const app = cargarApp();
    const peligrosa = {
        ...ficha,
        usuario: { ...ficha.usuario, nombre: '<img src=x onerror=alert(1)>', correo: '<script>alert(2)</script>' },
        historial: [{ ...ficha.historial[0], libroTitulo: '<svg onload=alert(3)>' }]
    };

    app.ejecutar(`mostrarFichaUsuario(${JSON.stringify(peligrosa)})`);
    const html = app.nodos['#fichaContenido'].innerHTML;

    assert.doesNotMatch(html, /<img|<script|<svg/i);
    assert.match(html, /&lt;img/);
    assert.match(html, /&lt;script/);
    assert.match(html, /&lt;svg/);
});


function escape(app) {
    const evento = { type: 'cancel', defaultPrevented: false,
        preventDefault() { this.defaultPrevented = true; } };
    app.nodos['#fichaLector'].dispatchEvent(evento);
    return evento;
}
function origen(app, selector = '#verFicha1') {
    const boton = app.documento.querySelector(selector);
    boton.focus();
    return boton;
}

test('el HTML utiliza dialog modal nativo, nombre accesible y cierre enfocable', () => {
    const fs = require('node:fs');
    const path = require('node:path');
    const html = fs.readFileSync(path.join(__dirname, '../../main/resources/static/index.html'), 'utf8');
    assert.match(html, /<dialog[^>]*id="fichaLector"[^>]*aria-labelledby="fichaTitulo"/);
    assert.match(html, /<button[^>]*id="btnCerrarFicha"[^>]*autofocus[^>]*onclick="cerrarFichaUsuario\(\)"/);
    assert.doesNotMatch(html, /id="fichaLector"[^>]*hidden/);
});

test('abre modal y enfoca Cerrar antes de recibir la ficha; Cerrar restaura el origen', async () => {
    const pendiente = solicitudControlada();
    const app = cargarApp({ fetch: url => url.endsWith('/ficha') ? pendiente.promesa : respuesta([]) });
    const boton = origen(app);
    const apertura = app.ejecutar('abrirFichaUsuario(1)');
    assert.equal(app.nodos['#fichaLector'].open, true);
    assert.equal(app.nodos['#fichaLector'].modal, true);
    assert.equal(app.documento.activeElement, app.nodos['#btnCerrarFicha']);
    app.ejecutar('cerrarFichaUsuario()');
    assert.equal(app.nodos['#fichaLector'].open, false);
    assert.equal(app.documento.activeElement, boton);
    pendiente.resolver(ficha);
    await apertura;
});

for (const resultado of ['exito', 'error de red', 'error HTTP']) {
    test(`Escape cierra, restaura foco e invalida ${resultado} pendiente`, async () => {
        const pendiente = solicitudControlada();
        const app = cargarApp({ fetch: url => url.endsWith('/ficha') ? pendiente.promesa : respuesta([]) });
        const boton = origen(app);
        const apertura = app.ejecutar('abrirFichaUsuario(1)');
        const version = app.ejecutar('solicitudFicha');
        const evento = escape(app);
        assert.equal(evento.defaultPrevented, true);
        assert.equal(app.ejecutar('solicitudFicha'), version + 1);
        assert.equal(app.nodos['#fichaLector'].open, false);
        assert.equal(app.documento.activeElement, boton);
        const texto = app.nodos['#fichaContenido'].textContent;
        const html = app.nodos['#fichaContenido'].innerHTML;
        if (resultado === 'exito') pendiente.resolver(ficha);
        else if (resultado === 'error HTTP') pendiente.resolverHttp();
        else pendiente.rechazar(new Error('Red anterior'));
        await apertura;
        assert.equal(app.nodos['#fichaContenido'].textContent, texto);
        assert.equal(app.nodos['#fichaContenido'].innerHTML, html);
        assert.equal(app.nodos['#fichaLector'].open, false);
        assert.equal(app.documento.activeElement, boton);
    });
}

test('no intenta enfocar el boton original si fue retirado de la pagina', async () => {
    const app = cargarApp({ fetch: servidor });
    const boton = origen(app);
    await app.ejecutar('abrirFichaUsuario(1)');
    boton.isConnected = false;
    boton.focus = () => assert.fail('No debe enfocar un nodo desconectado');
    escape(app);
    assert.equal(app.nodos['#fichaLector'].open, false);
});

for (const resultado of ['exito', 'error']) {
    test(`otra ficha pendiente conserva modal, foco y nuevo origen ante ${resultado} de A`, async () => {
        const a = solicitudControlada(), b = solicitudControlada();
        const app = cargarApp({ fetch: url => url === '/api/usuarios/1/ficha' ? a.promesa
            : url === '/api/usuarios/2/ficha' ? b.promesa : respuesta([]) });
        origen(app);
        const aperturaA = app.ejecutar('abrirFichaUsuario(1)');
        const botonB = app.documento.querySelector('#verFicha2');
        const aperturaB = app.ejecutar("abrirFichaUsuario(2, document.querySelector('#verFicha2'))");
        b.resolver({ ...ficha, usuario: { ...ficha.usuario, nombre: 'Beatriz' } });
        await aperturaB;
        const html = app.nodos['#fichaContenido'].innerHTML;
        if (resultado === 'exito') a.resolver(ficha); else a.rechazar(new Error('Error anterior'));
        await aperturaA;
        assert.equal(app.nodos['#fichaContenido'].innerHTML, html);
        assert.equal(app.nodos['#fichaLector'].modal, true);
        assert.equal(app.documento.activeElement, app.nodos['#btnCerrarFicha']);
        escape(app);
        assert.equal(app.documento.activeElement, botonB);
    });
}

test('reabrir tras Escape conserva la ficha nueva pese al error anterior', async () => {
    const pendiente = solicitudControlada();
    const app = cargarApp({ fetch: url => url === '/api/usuarios/2/ficha' ? respuesta(ficha)
        : url.endsWith('/ficha') ? pendiente.promesa : respuesta([]) });
    origen(app);
    const anterior = app.ejecutar('abrirFichaUsuario(1)');
    escape(app);
    await app.ejecutar('abrirFichaUsuario(2)');
    const html = app.nodos['#fichaContenido'].innerHTML;
    pendiente.rechazar(new Error('Error obsoleto'));
    await anterior;
    assert.equal(app.nodos['#fichaLector'].open, true);
    assert.equal(app.nodos['#fichaContenido'].innerHTML, html);
    assert.equal(app.documento.activeElement, app.nodos['#btnCerrarFicha']);
});
