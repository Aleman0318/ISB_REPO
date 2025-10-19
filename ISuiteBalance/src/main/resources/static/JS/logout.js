document.addEventListener('DOMContentLoaded', () => {
      const btn = document.getElementById('logoutBtn');
      if (!btn) return;

      btn.addEventListener('click', async (e) => {
        e.preventDefault();
        const { isConfirmed } = await Swal.fire({
          title: '¿Cerrar sesión?',
          text: 'Se cerrará tu sesión actual.',
          icon: 'warning',
          showCancelButton: true,
          confirmButtonText: 'Sí, cerrar',
          cancelButtonText: 'Cancelar',
          confirmButtonColor: '#e63946'
        });

        if (isConfirmed) {
          const f = document.getElementById('logoutForm');
          if (f) f.submit();  // POST /logout con CSRF
        }
      });
    });