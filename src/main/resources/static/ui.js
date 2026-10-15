const panelAccesibilidad = document.querySelector('#panelAccesibilidad');
const sessionToast = document.querySelector('#sessionToast');
const readingGuide = document.querySelector('#readingGuide');

document.querySelector('#btnAccesibilidad').addEventListener('click', () => {
    panelAccesibilidad.classList.toggle('open');
    panelAccesibilidad.setAttribute('aria-hidden', String(!panelAccesibilidad.classList.contains('open')));
    sessionToast.classList.remove('open');
});

document.querySelector('#btnCerrarAccesibilidad').addEventListener('click', () => {
    panelAccesibilidad.classList.remove('open');
    panelAccesibilidad.setAttribute('aria-hidden', 'true');
});

document.querySelector('#btnSesion').addEventListener('click', () => {
    sessionToast.classList.toggle('open');
    sessionToast.setAttribute('aria-hidden', String(!sessionToast.classList.contains('open')));
    panelAccesibilidad.classList.remove('open');
});

document.querySelector('#btnTema').addEventListener('click', () => {
    document.body.classList.toggle('dark-mode');
    localStorage.setItem('bibliotecaTema', document.body.classList.contains('dark-mode') ? 'dark' : 'light');
});

document.querySelectorAll('[data-a11y]').forEach(button => {
    button.addEventListener('click', () => document.body.classList.toggle(button.dataset.a11y === 'large-text' ? 'large-text' : button.dataset.a11y === 'dyslexia' ? 'dyslexia' : button.dataset.a11y === 'contrast' ? 'high-contrast' : 'reading-mask'));
});

document.addEventListener('mousemove', event => {
    if (document.body.classList.contains('reading-mask')) readingGuide.style.top = `${Math.max(0, event.clientY - 27)}px`;
});

document.querySelector('#btnRestablecer').addEventListener('click', () => {
    document.body.classList.remove('large-text', 'dyslexia', 'high-contrast', 'reading-mask');
    document.body.classList.remove('dark-mode');
    localStorage.setItem('bibliotecaTema', 'light');
});

if (localStorage.getItem('bibliotecaTema') === 'dark') document.body.classList.add('dark-mode');
