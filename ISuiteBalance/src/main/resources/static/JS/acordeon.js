
    document.addEventListener('click', function(e){
      const btn = e.target.closest('.accordion-toggle');
      if(!btn) return;

      // Si fuera <a>, evita navegación (se usa en la Opción B)
      if(btn.tagName === 'A') e.preventDefault();

      const panel = document.getElementById(btn.getAttribute('aria-controls'));
      const open = btn.getAttribute('aria-expanded') === 'true';
      btn.setAttribute('aria-expanded', String(!open));
      panel.hidden = open;
    });

