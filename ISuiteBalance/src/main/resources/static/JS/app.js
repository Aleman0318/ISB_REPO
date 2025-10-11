// Utilidades y estado
const fmt = (n) => {
  const v = Number(n || 0);
  return '$ ' + v.toLocaleString('es-SV', {minimumFractionDigits:2, maximumFractionDigits:2});
};

const tbody = document.getElementById('tbody-partida');
const totalDebe = document.getElementById('total-debe');
const totalHaber = document.getElementById('total-haber');
const cuentasContainer = document.getElementById('cuentas-container');

let numeroPartida = 1; // N° de partida activo
let cuentaIndex = 1;   // contador visual de tarjetas

// Tabla principal
function agregarFila({fecha, cuenta, desc, lado, monto}) {
  const tr = document.createElement('tr');
  const esDebe = lado === 'debe';

  tr.innerHTML = `
    <td>${String(numeroPartida).padStart(3,'0')}</td>
    <td>${fecha || ''}</td>
    <td>${cuenta || ''}</td>
    <td>${desc || ''}</td>
    <td>${esDebe ? fmt(monto) : ''}</td>
    <td>${!esDebe ? fmt(monto) : ''}</td>
  `;

  tr.dataset.debe = esDebe ? Number(monto) : 0;
  tr.dataset.haber = !esDebe ? Number(monto) : 0;

  tbody.appendChild(tr);
  recalcularTotales();
}

function recalcularTotales(){
  let debe = 0, haber = 0;
  [...tbody.querySelectorAll('tr')].forEach(tr=>{
    debe += Number(tr.dataset.debe || 0);
    haber += Number(tr.dataset.haber || 0);
  });
  totalDebe.textContent = fmt(debe);
  totalHaber.textContent = fmt(haber);
}

// Helpers tarjetas
function cardIsEmpty(card){
  const cuenta = card.querySelector('.sel-cuenta').value.trim();
  const monto = card.querySelector('.inp-monto').value.trim();
  const desc  = card.querySelector('.inp-desc').value.trim();
  const fecha = (card.querySelector('.inp-fecha').value || '').trim();
  return cuenta === '' && monto === '' && desc === '' && fecha === '';
}

function crearTarjetaCuenta(){
  cuentaIndex += 1;
  const idx = cuentaIndex;
  const name = `lado-${idx}`;

  const section = document.createElement('section');
  section.className = 'cuenta-item';
  section.dataset.idx = String(idx);

  section.innerHTML = `
    <div class="cuenta-head">
      <h3>Cuenta afectada ${idx}</h3>
      <div class="head-actions">
        <!-- el botón + se inyecta sólo en la última por ensurePlusOnLast() -->
        <button class="btn-icon danger btn-delete-card" title="Eliminar cuenta">🗑️</button>
      </div>
    </div>

    <div class="cuenta-body">
      <label class="sub">Cuenta</label>
      <select class="input sel-cuenta">
        <option value="" selected>Seleccione la cuenta</option>
        <option>Inventario</option>
        <option>Bancos</option>
        <option>Clientes</option>
        <option>IVA acreditable</option>
        <option>Ventas</option>
        <option>Proveedores</option>
      </select>

      <label class="sub">Fecha</label>
      <input type="date" class="input inp-fecha" />

      <div class="fila-form mt-8">
        <label class="radio"><input type="radio" name="${name}" value="debe" checked> Debe</label>
        <label class="radio"><input type="radio" name="${name}" value="haber"> Haber</label>
      </div>

      <label class="sub">Monto</label>
      <input class="input monto inp-monto" type="number" min="0" step="0.01" placeholder="$ 0.00">

      <label class="sub">Descripción (por cuenta)</label>
      <textarea class="input inp-desc" rows="2" placeholder="Describe esta línea…"></textarea>
    </div>

    <div class="cuenta-foot">
      <button class="btn btn-primary btn-enviar">Añadir a tabla</button>
    </div>
  `;
  return section;
}

/* Poner el "+" sólo en la utima tarjeta */
function ensurePlusOnLast(){
  const items = cuentasContainer.querySelectorAll('.cuenta-item');
  items.forEach((item, i) => {
    const actions = item.querySelector('.head-actions');
    // quita cualquier + existente
    const existing = actions.querySelector('.btn-add-card');
    if (existing) existing.remove();
    // agrega + sólo en la última
    if (i === items.length - 1) {
      const addBtn = document.createElement('button');
      addBtn.className = 'btn-icon btn-add-card';
      addBtn.title = 'Añadir otra cuenta';
      addBtn.textContent = '+';
      actions.prepend(addBtn); // antes del 🗑️
    }
  });
}

/* Mostrar "añadir a tabla" solo en la ultina tarjeta */
function actualizarVisibilidadBotonEnviar(){
  const items = cuentasContainer.querySelectorAll('.cuenta-item');
  items.forEach((item, i) => {
    const btn = item.querySelector('.btn-enviar');
    if (btn) btn.style.display = (i === items.length - 1) ? 'inline-flex' : 'none';
  });
}

// imicializar asegurar + en la última y botón enviar correcto
ensurePlusOnLast();
actualizarVisibilidadBotonEnviar();

// eventos en el contenedor
cuentasContainer.addEventListener('click', (e)=>{

  // 1) Click en "+" (siempre en la última)
  const addBtn = e.target.closest('.btn-add-card');
  if (addBtn && cuentasContainer.contains(addBtn)) {
    const nueva = crearTarjetaCuenta();
    cuentasContainer.appendChild(nueva);
    actualizarVisibilidadBotonEnviar();
    ensurePlusOnLast();
    const sel = nueva.querySelector('.sel-cuenta');
    if (sel) sel.focus();
    cuentasContainer.scrollTop = cuentasContainer.scrollHeight;
    return;
  }

  // 2) Click pa (eliminar tarjeta)
  const delBtn = e.target.closest('.btn-delete-card');
  if (delBtn && cuentasContainer.contains(delBtn)) {
    const card = delBtn.closest('.cuenta-item');
    const all = cuentasContainer.querySelectorAll('.cuenta-item');
    if (!card) return;

    if (!cardIsEmpty(card)) {
      alert('Sólo puedes eliminar una cuenta vacía (sin cuenta, sin monto, sin descripción y sin fecha).');
      return;
    }
    if (all.length === 1) {
      // Si es la unica, limpiala y conserva el +
      card.querySelector('.sel-cuenta').value = '';
      card.querySelector('.inp-fecha').value = '';
      card.querySelector('.inp-monto').value = '';
      card.querySelector('.inp-desc').value  = '';
      ensurePlusOnLast();
      actualizarVisibilidadBotonEnviar();
      return;
    }
    card.remove();
    // Al borrar, el + debe pasar a la nueva última:
    ensurePlusOnLast();
    actualizarVisibilidadBotonEnviar();
    return;
  }

  // 3) Click en "Añadir a tabla" (solo visible en la ultima)
  const sendBtn = e.target.closest('.btn-enviar');
  if (sendBtn && cuentasContainer.contains(sendBtn)) {
    const fechaGeneralEl = document.getElementById('fecha-partida');
    const fechaFallback = fechaGeneralEl ? (fechaGeneralEl.value || '') : '';

    const items = [...cuentasContainer.querySelectorAll('.cuenta-item')];
    let agregadas = 0;

    for (const item of items) {
      const cuentaEl = item.querySelector('.sel-cuenta');
      const fechaEl  = item.querySelector('.inp-fecha');
      const montoEl  = item.querySelector('.inp-monto');
      const descEl   = item.querySelector('.inp-desc');
      const radios   = item.querySelectorAll('input[type="radio"]');
      const lado     = [...radios].find(r=>r.checked)?.value || 'debe';

      let ok = true;
      if (!cuentaEl.value) { cuentaEl.style.borderColor = '#e03131'; ok = false; } else cuentaEl.style.borderColor = '#ccc';
      if (!montoEl.value)  { montoEl.style.borderColor  = '#e03131'; ok = false; } else montoEl.style.borderColor  = '#ccc';

      const fechaUse = (fechaEl.value || fechaFallback);
      if (!ok) continue;

      agregarFila({
        fecha: fechaUse,
        cuenta: cuentaEl.value,
        desc: descEl.value || '',
        lado,
        monto: montoEl.value
      });
      agregadas++;
    }

    if (agregadas === 0) {
      alert('Completa al menos una cuenta con su monto.');
      return;
    }

    // Reiniciar el contenedor a tarjeta 1 limpia (sin + fijo; lo añade)
    cuentasContainer.innerHTML = `
      <section class="cuenta-item" data-idx="1">
        <div class="cuenta-head">
          <h3>Cuenta afectada 1</h3>
          <div class="head-actions">
            <button class="btn-icon danger btn-delete-card" title="Eliminar cuenta">🗑️</button>
          </div>
        </div>

        <div class="cuenta-body">
          <label class="sub">Cuenta</label>
          <select class="input sel-cuenta">
            <option value="" selected>Seleccione la cuenta</option>
            <option>Inventario</option>
            <option>Bancos</option>
            <option>Clientes</option>
            <option>IVA acreditable</option>
            <option>Ventas</option>
            <option>Proveedores</option>
          </select>

          <label class="sub">Fecha</label>
          <input type="date" class="input inp-fecha" />

          <div class="fila-form mt-8">
            <label class="radio"><input type="radio" name="lado-1" value="debe" checked> Debe</label>
            <label class="radio"><input type="radio" name="lado-1" value="haber"> Haber</label>
          </div>

          <label class="sub">Monto</label>
          <input class="input monto inp-monto" type="number" min="0" step="0.01" placeholder="$ 0.00">

          <label class="sub">Descripción (por cuenta)</label>
          <textarea class="input inp-desc" rows="2" placeholder="Describe esta línea…"></textarea>
        </div>

        <div class="cuenta-foot">
          <button class="btn btn-primary btn-enviar">Añadir a tabla</button>
        </div>
      </section>
    `;
    cuentaIndex = 1;
    // normalizar visibilidad y colocar + en la nueva última
    ensurePlusOnLast();
    actualizarVisibilidadBotonEnviar();
  }
});

// Guardar / Cancelar / Siguiente partida
document.getElementById('btn-guardar').addEventListener('click', () => {
  const cuadrada = (totalDebe.textContent === totalHaber.textContent);
  if (!cuadrada) {
    alert('La partida no está cuadrada. Verifica los montos.');
    return;
  }
  alert('Partida guardada correctamente.');
  numeroPartida++;
  tbody.innerHTML = '';
  recalcularTotales();
});

document.getElementById('btn-cerrar').addEventListener('click', () => {
  if (confirm('¿Deseas cancelar esta partida?')) {
    tbody.innerHTML = '';
    recalcularTotales();
  }
});

document.getElementById('btn-siguiente').addEventListener('click', () => {
  numeroPartida++;
  tbody.innerHTML = '';
  recalcularTotales();
  alert(`Iniciando partida N° ${String(numeroPartida).padStart(3,'0')}`);
});
