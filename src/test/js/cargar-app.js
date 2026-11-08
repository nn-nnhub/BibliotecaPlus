// Ejecuta app.js en un contexto aislado de Node con un `document` simulado.
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const rutaApp = path.join(__dirname, '..', '..', 'main', 'resources', 'static', 'app.js');

function cargarApp({ fetch } = {}) {
    const nodos = {};
    const llamadas = [];
    const nodo = () => ({
        value: '', textContent: '', innerHTML: '', className: '', min: '',
        listeners: {}, open: false, isConnected: true,
        focus() { documento.activeElement = this; },
        contains(elemento) { return this === elemento || (this === nodos['#fichaLector'] && elemento === nodos['#btnCerrarFicha']); },
        showModal() { if (this.open) throw new Error('Dialogo ya abierto'); this.open = true; this.modal = true; },
        close() { this.open = false; this.modal = false; },
        dispatchEvent(evento) {
            this.listeners[evento.type]?.(evento);
            if (evento.type === 'cancel' && !evento.defaultPrevented) this.close();
            return !evento.defaultPrevented;
        },
        addEventListener(tipo, accion) { this.listeners[tipo] = accion; }, reset() {}
    });
    const documento = { activeElement: null, querySelector: selector => (nodos[selector] ??= nodo()) };
    const fetchSimulado = fetch ?? (() => Promise.reject(new Error('sin red en las pruebas')));
    const contexto = vm.createContext({
        document: documento,
        fetch: (url, opciones) => { llamadas.push({ url, metodo: opciones?.method ?? 'GET' }); return fetchSimulado(url, opciones); },
        alert() {},
        console
    });
    vm.runInContext(fs.readFileSync(rutaApp, 'utf8'), contexto);
    return { nodos, llamadas, documento, ejecutar: codigo => vm.runInContext(codigo, contexto) };
}

module.exports = { cargarApp };
