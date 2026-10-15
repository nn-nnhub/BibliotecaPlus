const estado = { libros: [], usuarios: [], prestamos: [] };

async function consultar(url, opciones) {
    const respuesta = await fetch(url, opciones);
    if (!respuesta.ok) throw new Error(`La operación falló (${respuesta.status})`);
    return respuesta.status === 204 ? null : respuesta.json();
}

function mostrarLibros() {
    const cuerpo = document.querySelector('#tablaLibros');
    cuerpo.innerHTML = estado.libros.map(libro => `<tr><td>${libro.id}</td><td>${libro.titulo}</td><td>${libro.autor}</td><td>${libro.isbn}</td><td><span class="status ${libro.disponible ? '' : 'unavailable'}">${libro.disponible ? 'Disponible' : 'Prestado'}</span></td></tr>`).join('');
    document.querySelector('#totalLibros').textContent = estado.libros.length;
    document.querySelector('#librosDisponibles').textContent = estado.libros.filter(libro => libro.disponible).length;
    const selector = document.querySelector('#libroId');
    selector.innerHTML = estado.libros.filter(libro => libro.disponible).map(libro => `<option value="${libro.id}">${libro.titulo}</option>`).join('');
}

function mostrarUsuarios() {
    document.querySelector('#tablaUsuarios').innerHTML = estado.usuarios.map(usuario => `<tr><td>${usuario.id}</td><td>${usuario.nombre}</td><td>${usuario.correo}</td></tr>`).join('');
    document.querySelector('#totalUsuarios').textContent = estado.usuarios.length;
    document.querySelector('#usuarioId').innerHTML = estado.usuarios.map(usuario => `<option value="${usuario.id}">${usuario.nombre}</option>`).join('');
}

function mostrarPrestamos() {
    document.querySelector('#tablaPrestamos').innerHTML = estado.prestamos.map(prestamo => `<tr><td>${prestamo.id}</td><td>${prestamo.libroId}</td><td>${prestamo.usuarioId}</td><td>${prestamo.fechaPrestamo}</td><td>${prestamo.devuelto ? 'Devuelto' : 'Activo'}</td><td>${prestamo.devuelto ? '-' : `<button class="secondary-button" onclick="devolverPrestamo(${prestamo.id})">Devolver</button>`}</td></tr>`).join('');
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
    try { await consultar(`/api/prestamos/${id}/devolver?tipoPrestamo=DOMICILIO`, { method: 'PUT' }); await cargarDatos(); } catch (error) { alert(error.message); }
}

document.querySelector('#formPrestamo').addEventListener('submit', async event => {
    event.preventDefault();
    const libroId = document.querySelector('#libroId').value;
    const usuarioId = document.querySelector('#usuarioId').value;
    const tipo = document.querySelector('#tipoPrestamo').value;
    const mensaje = document.querySelector('#prestamoMensaje');
    try { await consultar(`/api/prestamos?libroId=${libroId}&usuarioId=${usuarioId}&tipoPrestamo=${tipo}`, { method: 'POST' }); mensaje.textContent = 'Préstamo registrado correctamente.'; mensaje.className = 'message success'; await cargarDatos(); } catch (error) { mensaje.textContent = error.message; mensaje.className = 'message error'; }
});

document.querySelector('#btnActualizar').addEventListener('click', cargarDatos);
cargarDatos();
