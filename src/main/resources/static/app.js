const estado = { libros: [], usuarios: [], prestamos: [] };
let filtrosHistorial = {};
let solicitudHistorial = 0;

function escaparHtml(valor) {
    const reemplazos = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
    return String(valor ?? '').replace(/[&<>"']/g, caracter => reemplazos[caracter]);
}

async function consultar(url, opciones) {
    const respuesta = await fetch(url, opciones);
    if (!respuesta.ok) {
        const detalle = await respuesta.json().catch(() => null);
        throw new Error(detalle?.message ?? `La operación falló (${respuesta.status})`);
    }
    return respuesta.status === 204 ? null : respuesta.json();
}

function mostrarLibros() {
    const filtro = document.querySelector('#buscarLibro').value.toLowerCase().trim();
    const librosVisibles = estado.libros.filter(libro => [libro.titulo, libro.autor, libro.isbn].some(valor => (valor ?? '').toLowerCase().includes(filtro)));
    const cuerpo = document.querySelector('#tablaLibros');
    cuerpo.innerHTML = librosVisibles.map(libro => `<tr><td>${escaparHtml(libro.id)}</td><td>${escaparHtml(libro.titulo)}</td><td>${escaparHtml(libro.autor)}</td><td>${escaparHtml(libro.isbn)}</td><td><span class="status ${libro.disponible ? '' : 'unavailable'}">${libro.disponible ? 'Disponible' : 'Prestado'}</span></td></tr>`).join('');
    document.querySelector('#totalLibros').textContent = estado.libros.length;
    document.querySelector('#librosDisponibles').textContent = estado.libros.filter(libro => libro.disponible).length;
    const selector = document.querySelector('#libroId');
    selector.innerHTML = estado.libros.filter(libro => libro.disponible).map(libro => `<option value="${escaparHtml(libro.id)}">${escaparHtml(libro.titulo)}</option>`).join('');
}

function mostrarUsuarios() {
    const filtro = document.querySelector('#buscarUsuario').value.toLowerCase().trim();
    const usuariosVisibles = estado.usuarios.filter(usuario => [usuario.nombre, usuario.apellidos, usuario.dni, usuario.correo, usuario.telefono].some(valor => (valor ?? '').toLowerCase().includes(filtro)));
    document.querySelector('#tablaUsuarios').innerHTML = usuariosVisibles.map(usuario => `<tr><td>${escaparHtml(usuario.id)}</td><td>${escaparHtml(usuario.nombre)}</td><td>${escaparHtml(usuario.apellidos ?? '-')}</td><td>${escaparHtml(usuario.dni ?? '-')}</td><td>${escaparHtml(usuario.correo)}</td><td>${escaparHtml(usuario.telefono ?? '-')}</td><td><button class="secondary-button" onclick="abrirFichaUsuario(${Number(usuario.id)})">Ver ficha</button></td></tr>`).join('');
    document.querySelector('#totalUsuarios').textContent = estado.usuarios.length;
    document.querySelector('#usuarioId').innerHTML = estado.usuarios.map(usuario => `<option value="${escaparHtml(usuario.id)}">${escaparHtml(usuario.nombre)}</option>`).join('');
}

function mostrarFichaUsuario(ficha) {
    const usuario = ficha.usuario;
    const resumen = ficha.resumen;
    const filas = ficha.historial.map(prestamo => `<tr><td>${escaparHtml(prestamo.libroTitulo)}</td><td>${escaparHtml(prestamo.fechaPrestamo ?? '-')}</td><td>${escaparHtml(prestamo.fechaVencimiento ?? '-')}</td><td>${escaparHtml(prestamo.fechaDevolucion ?? '-')}</td><td>${escaparHtml(prestamo.tipoPrestamo)}</td><td>${prestamo.devuelto ? 'Devuelto' : prestamo.vencido ? 'Vencido' : 'Activo'}</td><td>S/ ${Number(prestamo.multa).toFixed(2)}</td></tr>`).join('');
    document.querySelector('#fichaContenido').innerHTML = `
        <div class="ficha-identidad"><h3>${escaparHtml(usuario.nombre)} ${escaparHtml(usuario.apellidos ?? '')}</h3><p>DNI: ${escaparHtml(usuario.dni)} · ${escaparHtml(usuario.correo)} · ${escaparHtml(usuario.telefono)}</p></div>
        <div class="ficha-resumen"><article><span>Total</span><strong>${escaparHtml(resumen.totalPrestamos)}</strong></article><article><span>Activos</span><strong>${escaparHtml(resumen.prestamosActivos)}</strong></article><article><span>Devueltos</span><strong>${escaparHtml(resumen.prestamosDevueltos)}</strong></article><article><span>Vencidos</span><strong>${escaparHtml(resumen.prestamosVencidos)}</strong></article><article><span>Multas</span><strong>S/ ${Number(resumen.multasAcumuladas).toFixed(2)}</strong></article></div>
        ${ficha.historial.length ? `<div class="table-wrapper"><table><thead><tr><th>Libro</th><th>Préstamo</th><th>Vencimiento</th><th>Devolución</th><th>Tipo</th><th>Estado</th><th>Multa</th></tr></thead><tbody>${filas}</tbody></table></div>` : '<p class="message">Este lector no tiene préstamos registrados.</p>'}`;
}

async function abrirFichaUsuario(id) {
    const panel = document.querySelector('#fichaLector');
    const contenido = document.querySelector('#fichaContenido');
    panel.hidden = false;
    contenido.textContent = 'Cargando ficha...';
    try {
        mostrarFichaUsuario(await consultar(`/api/usuarios/${id}/ficha`));
    } catch (error) {
        contenido.textContent = error.message;
    }
}

function cerrarFichaUsuario() {
    document.querySelector('#fichaLector').hidden = true;
}

function mostrarPrestamos(prestamos = estado.prestamos) {
    const nombreLibro = id => estado.libros.find(libro => libro.id === id)?.titulo ?? `Libro ${id}`;
    const nombreUsuario = id => { const usuario = estado.usuarios.find(item => item.id === id); return usuario ? `${usuario.nombre} ${usuario.apellidos ?? ''}`.trim() : `Usuario ${id}`; };
    document.querySelector('#tablaPrestamos').innerHTML = prestamos.map(prestamo => `<tr><td>${escaparHtml(prestamo.id)}</td><td>${escaparHtml(nombreLibro(prestamo.libroId))}</td><td>${escaparHtml(nombreUsuario(prestamo.usuarioId))}</td><td>${escaparHtml(prestamo.fechaVencimiento)}</td><td>${prestamo.devuelto ? 'Sí' : 'No'}</td><td>S/ ${Number(prestamo.multa).toFixed(2)}</td><td>${prestamo.devuelto ? '-' : `<button class="secondary-button" onclick="devolverPrestamo(${Number(prestamo.id)})">Devolver</button>`}</td></tr>`).join('');
    document.querySelector('#totalPrestamos').textContent = estado.prestamos.length;
    document.querySelector('#historialEstado').textContent = prestamos.length
        ? `${prestamos.length} préstamos en el historial mostrado.`
        : 'No hay préstamos que coincidan con los filtros seleccionados.';
    document.querySelector('#historialEstado').className = 'message';
}

async function cargarHistorial() {
    const solicitud = ++solicitudHistorial;
    const parametros = Object.entries(filtrosHistorial)
        .map(([nombre, valor]) => `${encodeURIComponent(nombre)}=${encodeURIComponent(valor)}`).join('&');
    try {
        const prestamos = parametros ? await consultar(`/api/prestamos?${parametros}`) : estado.prestamos;
        if (solicitud === solicitudHistorial) mostrarPrestamos(prestamos);
    } catch (error) {
        if (solicitud !== solicitudHistorial) return;
        document.querySelector('#historialEstado').textContent = error.message;
        document.querySelector('#historialEstado').className = 'message error';
    }
}

async function aplicarFiltrosHistorial(event) {
    event.preventDefault();
    filtrosHistorial = {};
    for (const [nombre, selector] of [['usuarioId', '#filtroUsuarioId'], ['libroId', '#filtroLibroId'], ['devuelto', '#filtroDevuelto']]) {
        const valor = document.querySelector(selector).value.trim();
        if (valor !== '') filtrosHistorial[nombre] = valor;
    }
    await cargarHistorial();
}

async function limpiarFiltrosHistorial() {
    filtrosHistorial = {};
    for (const selector of ['#filtroUsuarioId', '#filtroLibroId', '#filtroDevuelto']) {
        document.querySelector(selector).value = '';
    }
    await cargarHistorial();
}

async function cargarDatos() {
    try {
        [estado.libros, estado.usuarios, estado.prestamos] = await Promise.all([consultar('/api/libros'), consultar('/api/usuarios'), consultar('/api/prestamos')]);
        mostrarLibros(); mostrarUsuarios();
        await cargarHistorial();
        document.querySelector('#librosEstado').textContent = 'Datos actualizados correctamente.';
        document.querySelector('#librosEstado').className = 'message success';
    } catch (error) { document.querySelector('#librosEstado').textContent = error.message; document.querySelector('#librosEstado').className = 'message error'; }
}

async function devolverPrestamo(id) {
    try { await consultar(`/api/prestamos/${id}/devolver`, { method: 'PUT' }); await cargarDatos(); } catch (error) { alert(error.message); }
}

document.querySelector('#formPrestamo').addEventListener('submit', async event => {
    event.preventDefault();
    const libroId = document.querySelector('#libroId').value;
    const usuarioId = document.querySelector('#usuarioId').value;
    const tipo = document.querySelector('#tipoPrestamo').value;
    const mensaje = document.querySelector('#prestamoMensaje');
    const fechaVencimiento = document.querySelector('#fechaVencimiento').value;
    try { await consultar(`/api/prestamos?libroId=${libroId}&usuarioId=${usuarioId}&tipoPrestamo=${tipo}&fechaVencimiento=${fechaVencimiento}`, { method: 'POST' }); mensaje.textContent = 'Préstamo registrado correctamente.'; mensaje.className = 'message success'; await cargarDatos(); } catch (error) { mensaje.textContent = error.message; mensaje.className = 'message error'; }
});

document.querySelector('#btnActualizar').addEventListener('click', cargarDatos);
document.querySelector('#formFiltrosHistorial').addEventListener('submit', aplicarFiltrosHistorial);
document.querySelector('#btnLimpiarFiltros').addEventListener('click', limpiarFiltrosHistorial);
document.querySelector('#btnResumen').addEventListener('click', async () => {
    const mensaje = document.querySelector('#reporteResumen');
    try {
        const resumen = await consultar('/api/reportes/resumen');
        const masPrestado = resumen.librosMasPrestados[0];
        mensaje.textContent = `Préstamos: ${resumen.totalPrestamos} | Activos: ${resumen.prestamosActivos} | Devueltos: ${resumen.prestamosDevueltos} | Multas: S/ ${Number(resumen.totalMultas).toFixed(2)}${masPrestado ? ` | Más prestado: ${masPrestado.libro} (${masPrestado.prestamos})` : ''}`;
        mensaje.className = 'message success';
    } catch (error) { mensaje.textContent = error.message; mensaje.className = 'message error'; }
});
document.querySelector('#buscarLibro').addEventListener('input', mostrarLibros);
document.querySelector('#buscarUsuario').addEventListener('input', mostrarUsuarios);

document.querySelector('#formLibro').addEventListener('submit', async event => {
    event.preventDefault();
    const mensaje = document.querySelector('#libroMensaje');
    const libro = {
        titulo: document.querySelector('#tituloLibro').value,
        autor: document.querySelector('#autorLibro').value,
        isbn: document.querySelector('#isbnLibro').value
    };
    try {
        await consultar('/api/libros', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(libro) });
        event.target.reset(); mensaje.textContent = 'Libro registrado correctamente.'; mensaje.className = 'message success'; await cargarDatos();
    } catch (error) { mensaje.textContent = error.message; mensaje.className = 'message error'; }
});

document.querySelector('#formUsuario').addEventListener('submit', async event => {
    event.preventDefault();
    const mensaje = document.querySelector('#usuarioMensaje');
    const usuario = {
        nombre: document.querySelector('#nombreUsuario').value,
        apellidos: document.querySelector('#apellidosUsuario').value,
        dni: document.querySelector('#dniUsuario').value,
        correo: document.querySelector('#correoUsuario').value,
        telefono: document.querySelector('#telefonoUsuario').value
    };
    try {
        await consultar('/api/usuarios', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(usuario) });
        event.target.reset(); mensaje.textContent = 'Usuario registrado correctamente.'; mensaje.className = 'message success'; await cargarDatos();
    } catch (error) { mensaje.textContent = error.message; mensaje.className = 'message error'; }
});

cargarDatos();

const fechaInicial = new Date();
fechaInicial.setDate(fechaInicial.getDate() + 14);
document.querySelector('#fechaVencimiento').value = fechaInicial.toISOString().slice(0, 10);
document.querySelector('#fechaVencimiento').min = new Date().toLocaleDateString('en-CA');
