import { useEffect, useState, type FormEvent } from 'react';
import { useAuth } from '../context/AuthContext';
import { valoracionesApi, asignaturasApi } from '../api/endpoints';
import type { Valoracion, Asignatura } from '../types';

export default function ValoracionesPage() {
  var auth = useAuth();
  var user = auth.user;
  var isEstudiante = user && user.role === 'ROLE_ESTUDIANTE';
  var isAdmin = user && user.role === 'ROLE_ADMIN';

  var itemsState = useState<Valoracion[]>([]);
  var items = itemsState[0];
  var setItems = itemsState[1];

  var loadingState = useState(true);
  var loading = loadingState[0];
  var setLoading = loadingState[1];

  var showFormState = useState(false);
  var showForm = showFormState[0];
  var setShowForm = showFormState[1];

  var asignaturasState = useState<Asignatura[]>([]);
  var asignaturas = asignaturasState[0];
  var setAsignaturas = asignaturasState[1];

  var formState = useState({
    puntuacion: 5,
    comentario: '',
    puntosMejora: '',
    profesorId: 0,
    asignaturaId: 0,
  });
  var form = formState[0];
  var setForm = formState[1];

  var savingState = useState(false);
  var saving = savingState[0];
  var setSaving = savingState[1];

  var errorState = useState('');
  var formError = errorState[0];
  var setFormError = errorState[1];

  // Profesores de la asignatura seleccionada
  var profesoresState = useState<Array<{ id: number; nombre: string }>>([]);
  var profesores = profesoresState[0];
  var setProfesores = profesoresState[1];

  function loadValoraciones() {
    valoracionesApi
      .getAll()
      .then(function (r) { setItems(r.data); })
      .catch(function () {})
      .finally(function () { setLoading(false); });
  }

  useEffect(function () {
    loadValoraciones();
    asignaturasApi.getAll().then(function (r) { setAsignaturas(r.data); });
  }, []);

  // Cuando cambia la asignatura seleccionada, cargar sus profesores
  useEffect(function () {
    if (form.asignaturaId > 0) {
      var asig = asignaturas.find(function (a) { return a.id === form.asignaturaId; });
      if (asig && asig.profesorIds && asig.profesorIds.length > 0) {
        // Obtener nombres de profesores — usar endpoint de usuarios
        var profList: Array<{ id: number; nombre: string }> = [];
        asig.profesorIds.forEach(function (pid) {
          // Simple: usamos los datos disponibles
          profList.push({ id: pid, nombre: 'Profesor #' + pid });
        });
        setProfesores(profList);
      } else {
        setProfesores([]);
      }
    }
  }, [form.asignaturaId, asignaturas]);

  function handleCreate(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setFormError('');
    valoracionesApi.create({
      puntuacion: form.puntuacion,
      comentario: form.comentario,
      puntosMejora: form.puntosMejora,
      profesorId: form.profesorId,
      asignaturaId: form.asignaturaId,
    })
      .then(function () {
        setShowForm(false);
        setForm({ puntuacion: 5, comentario: '', puntosMejora: '', profesorId: 0, asignaturaId: 0 });
        setFormError('');
        loadValoraciones();
      })
      .catch(function (err: any) {
        var msg = (err.response && err.response.data && err.response.data.message) || 'Error al enviar la valoracion';
        setFormError(msg);
      })
      .finally(function () { setSaving(false); });
  }

  function handleDelete(id: number) {
    if (!confirm('Eliminar esta valoracion?')) return;
    valoracionesApi.delete(id)
      .then(function () { setItems(function (prev) { return prev.filter(function (v) { return v.id !== id; }); }); })
      .catch(function () { alert('Error al eliminar'); });
  }

  function renderStars(n: number) {
    var stars = [];
    for (var i = 0; i < 5; i++) {
      stars.push(i < n ? '\u2605' : '\u2606');
    }
    return stars.join('');
  }

  function formatDate(d: string) {
    if (!d) return '';
    try { return new Date(d).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: 'numeric' }); }
    catch (e) { return d; }
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Valoraciones de Profesores</h2>
          <p className="mt-1 text-sm text-gray-500">Valoraciones anonimas realizadas por estudiantes matriculados</p>
        </div>
        {isEstudiante && (
          <button onClick={function () { setShowForm(!showForm); setFormError(''); }} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">
            {showForm ? 'Cancelar' : '+ Valorar profesor'}
          </button>
        )}
      </div>

      {showForm && isEstudiante && (
        <form onSubmit={handleCreate} className="mb-6 space-y-4 rounded-xl bg-white p-6 shadow-sm border border-indigo-100">
          <div className="flex items-center gap-2 rounded-lg bg-blue-50 p-3 text-sm text-blue-700">
            <span className="text-lg">&#128274;</span>
            <span>Tu valoracion es completamente <strong>anonima</strong>. Solo los estudiantes matriculados en la asignatura pueden valorar.</span>
          </div>

          {formError && (
            <div className="rounded-lg bg-red-50 p-3 text-sm text-red-700 border border-red-200">
              <strong>Error:</strong> {formError}
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Asignatura</label>
              <select value={form.asignaturaId} onChange={function (e) { setForm(Object.assign({}, form, { asignaturaId: Number(e.target.value), profesorId: 0 })); }} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500">
                <option value={0} disabled>Seleccionar asignatura...</option>
                {asignaturas.map(function (a) { return <option key={a.id} value={a.id}>{a.nombre}</option>; })}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Profesor</label>
              <select value={form.profesorId} onChange={function (e) { setForm(Object.assign({}, form, { profesorId: Number(e.target.value) })); }} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" disabled={profesores.length === 0}>
                <option value={0} disabled>{profesores.length === 0 ? 'Selecciona asignatura primero' : 'Seleccionar profesor...'}</option>
                {profesores.map(function (p) { return <option key={p.id} value={p.id}>{p.nombre}</option>; })}
              </select>
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Puntuacion: <span className="text-amber-500">{renderStars(form.puntuacion)}</span> ({form.puntuacion}/5)</label>
            <input type="range" min={1} max={5} value={form.puntuacion} onChange={function (e) { setForm(Object.assign({}, form, { puntuacion: Number(e.target.value) })); }} className="w-full accent-indigo-600" />
            <div className="flex justify-between text-xs text-gray-400">
              <span>Muy mejorable</span><span>Excelente</span>
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Comentario</label>
            <textarea value={form.comentario} onChange={function (e) { setForm(Object.assign({}, form, { comentario: e.target.value })); }} rows={3} placeholder="Describe tu experiencia con este profesor de forma constructiva..." className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Puntos de mejora</label>
            <textarea value={form.puntosMejora} onChange={function (e) { setForm(Object.assign({}, form, { puntosMejora: e.target.value })); }} rows={3} placeholder="Sugiere aspectos concretos que el profesor podria mejorar (material, metodologia, comunicacion...)" className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>

          <div className="flex items-center gap-2 rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
            <span className="text-base">&#9888;&#65039;</span>
            <span>Los comentarios son moderados por un sistema de IA. No se permiten insultos, lenguaje ofensivo ni ataques personales. Las valoraciones deben ser constructivas.</span>
          </div>

          <button type="submit" disabled={saving} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition">
            {saving ? 'Enviando...' : 'Enviar valoracion anonima'}
          </button>
        </form>
      )}

      {loading ? (
        <div className="flex justify-center py-12"><div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" /></div>
      ) : items.length === 0 ? (
        <p className="py-12 text-center text-gray-500">No hay valoraciones aun</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {items.map(function (v) {
            return (
              <div key={v.id} className="rounded-xl bg-white p-5 shadow-sm border border-gray-100">
                <div className="mb-3 flex items-start justify-between">
                  <div>
                    <p className="font-semibold text-gray-900">{v.profesorNombre}</p>
                    <p className="text-xs text-gray-400">{v.asignaturaNombre}</p>
                  </div>
                  <div className="text-right">
                    <span className="text-lg text-amber-400">{renderStars(v.puntuacion)}</span>
                    <p className="text-xs text-gray-400">{formatDate(v.fechaCreacion)}</p>
                  </div>
                </div>
                {v.comentario && <p className="mb-2 text-sm text-gray-600">{v.comentario}</p>}
                {v.puntosMejora && (
                  <div className="mb-2 rounded-lg bg-amber-50 p-2">
                    <p className="text-xs font-medium text-amber-700 mb-1">Puntos de mejora:</p>
                    <p className="text-xs text-amber-600">{v.puntosMejora}</p>
                  </div>
                )}
                <div className="flex items-center justify-between border-t border-gray-100 pt-2">
                  <p className="text-xs text-gray-400 italic">Valoracion anonima</p>
                  {isAdmin && (
                    <button onClick={function () { handleDelete(v.id); }} className="rounded bg-red-50 px-2 py-1 text-xs text-red-600 hover:bg-red-100 transition">Eliminar</button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
