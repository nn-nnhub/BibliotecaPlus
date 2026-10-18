const estado = { libros: [], usuarios: [], prestamos: [], edicionLibroId: null, edicionUsuarioId: null };

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
    cuerpo.innerHTML = librosVisibles.map(libro => {
        const id = Number(libro.id);
        return `<tr><td>${escaparHtml(libro.id)}</td><td>${escaparHtml(libro.titulo)}</td><td>${escaparHtml(libro.autor)}</td><td>${escaparHtml(libro.isbn)}</td><td><span class="status ${libro.disponible ? '' : 'unavailable'}">${libro.disponible ? 'Disponible' : 'Prestado'}</span></td><td><div class="row-actions"><button class="secondary-button" type="button" onclick="iniciarEdicionLibro(${id})">Editar</button><button class="danger-button" type="button" onclick="eliminarLibro(${id})">Eliminar</button></div></td></tr>`;
    }).join('');
    document.querySelector('#totalLibros').textContent = estado.libros.length;
    document.querySelector('#librosDisponibles').textContent = estado.libros.filter(libro => libro.disponible).length;
    const selector = document.querySelector('#libroId');
    selector.innerHTML = estado.libros.filter(libro => libro.disponible).map(libro => `<option value="${escaparHtml(libro.id)}">${escaparHtml(libro.titulo)}</option>`).join('');
}

function mostrarUsuarios() {
    const filtro = document.querySelector('#buscarUsuario').value.toLowerCase().trim();
    const usuariosVisibles = estado.usuarios.filter(usuario => [usuario.nombre, usuario.apellidos, usuario.dni, usuario.correo, usuario.telefono].some(valor => (valor ?? '').toLowerCase().includes(filtro)));
    document.querySelector('#tablaUsuarios').innerHTML = usuariosVisibles.map(usuario => {
        const id = Number(usuario.id);
        return `<tr><td>${escaparHtml(usuario.id)}</td><td>${escaparHtml(usuario.nombre)}</td><td>${escaparHtml(usuario.apellidos ?? '-')}</td><td>${escaparHtml(usuario.dni ?? '-')}</td><td>${escaparHtml(usuario.correo)}</td><td>${escaparHtml(usuario.telefono ?? '-')}</td><td><div class="row-actions"><button class="secondary-button" type="button" onclick="iniciarEdicionUsuario(${id})">Editar</button><button class="danger-button" type="button" onclick="eliminarUsuario(${id})">Eliminar</button></div></td></tr>`;
    }).join('');
    document.querySelector('#totalUsuarios').textContent = estado.usuarios.length;
    document.querySelector('#usuarioId').innerHTML = estado.usuarios.map(usuario => `<option value="${escaparHtml(usuario.id)}">${escaparHtml(usuario.nombre)}</option>`).join('');
}

function mostrarPrestamos() {
    const nombreLibro = id => estado.libros.find(libro => libro.id === id)?.titulo ?? `Libro ${id}`;
    const nombreUsuario = id => { const usuario = estado.usuarios.find(item => item.id === id); return usuario ? `${usuario.nombre} ${usuario.apellidos ?? ''}`.trim() : `Usuario ${id}`; };
    document.querySelector('#tablaPrestamos').innerHTML = estado.prestamos.map(prestamo => `<tr><td>${escaparHtml(prestamo.id)}</td><td>${escaparHtml(nombreLibro(prestamo.libroId))}</td><td>${escaparHtml(nombreUsuario(prestamo.usuarioId))}</td><td>${escaparHtml(prestamo.fechaVencimiento)}</td><td>${prestamo.devuelto ? 'Sí' : 'No'}</td><td>S/ ${Number(prestamo.multa).toFixed(2)}</td><td>${prestamo.devuelto ? '-' : `<button class="secondary-button" onclick="devolverPrestamo(${Number(prestamo.id)})">Devolver</button>`}</td></tr>`).join('');
    document.querySelector('#totalPrestamos').textContent = estado.prestamos.length;
}

async function cargarDatos() {
    try {
        [estado.libros, estado.usuarios, estado.prestamos] = await Promise.all([consultar('/api/libros'), consultar('/api/usuarios'), consultar('/api/prestamos')]);
        mostrarLibros(); mostrarUsuarios(); mostrarPrestamos();
        document.querySelector('#librosEstado').textContent = 'Datos actualizados correctamente.';
        document.querySelector('#librosEstado').className = 'message success';
    } catch (error) { document.querySelector('#librosEstado').textContent = error.message; document.querySelector('#librosEstado').className = 'message error'; }
}

async function devolverPrestamo(id) {
    try { await consultar(`/api/prestamos/${id}/devolver`, { method: 'PUT' }); await cargarDatos(); } catch (error) { alert(error.message); }
}

function iniciarEdicionLibro(id) {
    const libro = estado.libros.find(item => item.id === id);
    if (!libro) return;
    estado.edicionLibroId = id;
    document.querySelector('#tituloLibro').value = libro.titulo ?? '';
    document.querySelector('#autorLibro').value = libro.autor ?? '';
    document.querySelector('#isbnLibro').value = libro.isbn ?? '';
    document.querySelector('#tituloFormLibro').textContent = 'Editar libro';
    document.querySelector('#btnGuardarLibro').textContent = 'Guardar cambios';
    document.querySelector('#btnCancelarEdicionLibro').hidden = false;
}

function cancelarEdicionLibro() {
    estado.edicionLibroId = null;
    document.querySelector('#formLibro').reset();
    document.querySelector('#tituloLibro').value = '';
    document.querySelector('#autorLibro').value = '';
    document.querySelector('#isbnLibro').value = '';
    document.querySelector('#tituloFormLibro').textContent = 'Registrar libro';
    document.querySelector('#btnGuardarLibro').textContent = 'Guardar libro';
    document.querySelector('#btnCancelarEdicionLibro').hidden = true;
}

function iniciarEdicionUsuario(id) {
    const usuario = estado.usuarios.find(item => item.id === id);
    if (!usuario) return;
    estado.edicionUsuarioId = id;
    document.querySelector('#nombreUsuario').value = usuario.nombre ?? '';
    document.querySelector('#apellidosUsuario').value = usuario.apellidos ?? '';
    document.querySelector('#dniUsuario').value = usuario.dni ?? '';
    document.querySelector('#correoUsuario').value = usuario.correo ?? '';
    document.querySelector('#telefonoUsuario').value = usuario.telefono ?? '';
    document.querySelector('#tituloFormUsuario').textContent = 'Editar usuario';
    document.querySelector('#btnGuardarUsuario').textContent = 'Guardar cambios';
    document.querySelector('#btnCancelarEdicionUsuario').hidden = false;
}

function cancelarEdicionUsuario() {
    estado.edicionUsuarioId = null;
    document.querySelector('#formUsuario').reset();
    document.querySelector('#nombreUsuario').value = '';
    document.querySelector('#apellidosUsuario').value = '';
    document.querySelector('#dniUsuario').value = '';
    document.querySelector('#correoUsuario').value = '';
    document.querySelector('#telefonoUsuario').value = '';
    document.querySelector('#tituloFormUsuario').textContent = 'Registrar usuario';
    document.querySelector('#btnGuardarUsuario').textContent = 'Guardar usuario';
    document.querySelector('#btnCancelarEdicionUsuario').hidden = true;
}

async function eliminarLibro(id) {
    if (!confirm('¿Deseas eliminar este libro?')) return;
    const mensaje = document.querySelector('#libroMensaje');
    try {
        await consultar(`/api/libros/${id}`, { method: 'DELETE' });
        if (estado.edicionLibroId === id) cancelarEdicionLibro();
        mensaje.textContent = 'Libro eliminado correctamente.';
        mensaje.className = 'message success';
        await cargarDatos();
    } catch (error) {
        mensaje.textContent = error.message;
        mensaje.className = 'message error';
    }
}

async function eliminarUsuario(id) {
    if (!confirm('¿Deseas eliminar este usuario?')) return;
    const mensaje = document.querySelector('#usuarioMensaje');
    try {
        await consultar(`/api/usuarios/${id}`, { method: 'DELETE' });
        if (estado.edicionUsuarioId === id) cancelarEdicionUsuario();
        mensaje.textContent = 'Usuario eliminado correctamente.';
        mensaje.className = 'message success';
        await cargarDatos();
    } catch (error) {
        mensaje.textContent = error.message;
        mensaje.className = 'message error';
    }
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
document.querySelector('#btnCancelarEdicionLibro').addEventListener('click', cancelarEdicionLibro);
document.querySelector('#btnCancelarEdicionUsuario').addEventListener('click', cancelarEdicionUsuario);

document.querySelector('#formLibro').addEventListener('submit', async event => {
    event.preventDefault();
    const mensaje = document.querySelector('#libroMensaje');
    const libro = {
        titulo: document.querySelector('#tituloLibro').value,
        autor: document.querySelector('#autorLibro').value,
        isbn: document.querySelector('#isbnLibro').value
    };
    try {
        const editando = estado.edicionLibroId !== null;
        const url = editando ? `/api/libros/${estado.edicionLibroId}` : '/api/libros';
        await consultar(url, { method: editando ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(libro) });
        cancelarEdicionLibro();
        mensaje.textContent = editando ? 'Libro actualizado correctamente.' : 'Libro registrado correctamente.';
        mensaje.className = 'message success';
        await cargarDatos();
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
        const editando = estado.edicionUsuarioId !== null;
        const url = editando ? `/api/usuarios/${estado.edicionUsuarioId}` : '/api/usuarios';
        await consultar(url, { method: editando ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(usuario) });
        cancelarEdicionUsuario();
        mensaje.textContent = editando ? 'Usuario actualizado correctamente.' : 'Usuario registrado correctamente.';
        mensaje.className = 'message success';
        await cargarDatos();
    } catch (error) { mensaje.textContent = error.message; mensaje.className = 'message error'; }
});

cargarDatos();

const fechaInicial = new Date();
fechaInicial.setDate(fechaInicial.getDate() + 14);
document.querySelector('#fechaVencimiento').value = fechaInicial.toISOString().slice(0, 10);
document.querySelector('#fechaVencimiento').min = new Date().toLocaleDateString('en-CA');
