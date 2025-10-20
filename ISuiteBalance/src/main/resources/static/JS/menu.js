(function(){
      const btn = document.getElementById('menuBtn');
      const sidebar = document.querySelector('.sidebar');
      const backdrop = document.getElementById('backdrop');

      if(!btn || !sidebar || !backdrop) return;

      function openMenu(){
        sidebar.classList.add('sidebar--open');
        backdrop.classList.add('backdrop--show');
        document.body.classList.add('no-scroll');
        btn.setAttribute('aria-expanded','true');
      }
      function closeMenu(){
        sidebar.classList.remove('sidebar--open');
        backdrop.classList.remove('backdrop--show');
        document.body.classList.remove('no-scroll');
        btn.setAttribute('aria-expanded','false');
      }
      function toggleMenu(){
        const isOpen = sidebar.classList.contains('sidebar--open');
        isOpen ? closeMenu() : openMenu();
      }

      btn.addEventListener('click', (e)=>{ e.preventDefault(); toggleMenu(); });
      backdrop.addEventListener('click', closeMenu);
      document.addEventListener('keydown', (e)=>{ if(e.key === 'Escape') closeMenu(); });

      // Si cambias a desktop, cierra el panel
      window.addEventListener('resize', ()=>{ if (window.innerWidth > 767) closeMenu(); });
})();