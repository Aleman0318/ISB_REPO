// JS/acordeon.js
document.addEventListener('DOMContentLoaded', () => {
    const accordions = document.querySelectorAll('.accordion');

    accordions.forEach(acc => {
        const toggle = acc.querySelector('.accordion-toggle');
        const panel  = acc.querySelector('.accordion-panel');

        if (!toggle || !panel) return;

        // Quitamos el hidden del HTML para que la animación funcione
        panel.hidden = false;
        panel.style.maxHeight = '0px';
        panel.style.overflow  = 'hidden';

        // Si alguna opción dentro está "active", lo abrimos por defecto
        const hasActiveLink = panel.querySelector('a.active');
        if (hasActiveLink) {
            acc.classList.add('is-open');
            toggle.setAttribute('aria-expanded', 'true');
            panel.style.maxHeight = panel.scrollHeight + 'px';
        } else {
            toggle.setAttribute('aria-expanded', 'false');
        }

        // Click en el botón del acordeón
        toggle.addEventListener('click', () => {
            const isOpen = acc.classList.contains('is-open');

            if (isOpen) {
                // Cerrar
                acc.classList.remove('is-open');
                toggle.setAttribute('aria-expanded', 'false');

                // Truco para animar de alto actual -> 0
                panel.style.maxHeight = panel.scrollHeight + 'px';
                requestAnimationFrame(() => {
                    panel.style.maxHeight = '0px';
                });

            } else {
                // Abrir
                acc.classList.add('is-open');
                toggle.setAttribute('aria-expanded', 'true');
                panel.style.maxHeight = panel.scrollHeight + 'px';
            }
        });

        // Si se cambia el tamaño de la ventana, ajustamos la altura del panel abierto
        window.addEventListener('resize', () => {
            if (acc.classList.contains('is-open')) {
                panel.style.maxHeight = panel.scrollHeight + 'px';
            }
        });
    });
});
