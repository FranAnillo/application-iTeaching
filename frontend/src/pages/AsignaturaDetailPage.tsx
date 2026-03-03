import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  asignaturasApi,
  anunciosApi,
  materialesApi,
  tareasApi,
  entregasApi,
  foroApi,
  valoracionesApi,
  gruposApi,
  carpetasApi,
} from '../api/endpoints';
import type { Asignatura, Anuncio, Material, Tarea, Entrega, ForoTema, ForoRespuesta, Valoracion, Grupo, Carpeta } from '../types';

type Tab = 'info' | 'anuncios' | 'materiales' | 'tareas' | 'foro' | 'grupos' | 'calificaciones';

var TABS: { key: Tab; label: string; icon: string }[] = [
  { key: 'info', label: 'Informacion', icon: 'i' },
  { key: 'anuncios', label: 'Anuncios', icon: '!' },
  { key: 'materiales', label: 'Materiales', icon: '#' },
  { key: 'tareas', label: 'Tareas', icon: 'T' },
  { key: 'foro', label: 'Foro', icon: 'F' },
  { key: 'grupos', label: 'Grupos', icon: 'G' },
  { key: 'calificaciones', label: 'Calificaciones', icon: 'C' },
];

export default function AsignaturaDetailPage() {
  var params = useParams<{ id: string }>();
  var id = params.id;
  var navigate = useNavigate();
  var auth = useAuth();
  var user = auth.user;
  var isNew = id === 'new';

  var asignaturaState = useState<Asignatura | null>(null);
  var asignatura = asignaturaState[0];
  var setAsignatura = asignaturaState[1];

  var tabState = useState<Tab>(isNew ? 'info' : 'anuncios');
  var tab = tabState[0];
  var setTab = tabState[1];

  var loadingState = useState(!isNew);
  var loading = loadingState[0];
  var setLoading = loadingState[1];

  var errorState = useState('');
  var error = errorState[0];
  var setError = errorState[1];

  var formState = useState<Partial<Asignatura>>({ nombre: '', descripcion: '', url: '' });
  var form = formState[0];
  var setForm = formState[1];

  var savingState = useState(false);
  var saving = savingState[0];
  var setSaving = savingState[1];

  // Anuncios
  var anunciosState = useState<Anuncio[]>([]);
  var anuncios = anunciosState[0];
  var setAnuncios = anunciosState[1];
  var showAnuncioFormState = useState(false);
  var showAnuncioForm = showAnuncioFormState[0];
  var setShowAnuncioForm = showAnuncioFormState[1];
  var anuncioFormState = useState({ titulo: '', contenido: '', importante: false });
  var anuncioForm = anuncioFormState[0];
  var setAnuncioForm = anuncioFormState[1];

  // Materiales
  var materialesState = useState<Material[]>([]);
  var materiales = materialesState[0];
  var setMateriales = materialesState[1];
  var showMaterialFormState = useState(false);
  var showMaterialForm = showMaterialFormState[0];
  var setShowMaterialForm = showMaterialFormState[1];
  var materialFormState = useState({ titulo: '', descripcion: '', urlRecurso: '', tipo: 'DOCUMENTO' });
  var materialForm = materialFormState[0];
  var setMaterialForm = materialFormState[1];

  // Tareas
  var tareasState = useState<Tarea[]>([]);
  var tareas = tareasState[0];
  var setTareas = tareasState[1];
  var showTareaFormState = useState(false);
  var showTareaForm = showTareaFormState[0];
  var setShowTareaForm = showTareaFormState[1];
  var tareaFormState = useState({ titulo: '', descripcion: '', fechaEntrega: '', puntuacionMaxima: 10 });
  var tareaForm = tareaFormState[0];
  var setTareaForm = tareaFormState[1];

  // Entregas for selected tarea
  var selectedTareaState = useState<Tarea | null>(null);
  var selectedTarea = selectedTareaState[0];
  var setSelectedTarea = selectedTareaState[1];
  var entregasState = useState<Entrega[]>([]);
  var entregas = entregasState[0];
  var setEntregas = entregasState[1];
  var entregaFormState = useState({ contenido: '', urlAdjunto: '' });
  var entregaForm = entregaFormState[0];
  var setEntregaForm = entregaFormState[1];
  var showEntregaFormState = useState(false);
  var showEntregaForm = showEntregaFormState[0];
  var setShowEntregaForm = showEntregaFormState[1];

  // Foro
  var temasState = useState<ForoTema[]>([]);
  var temas = temasState[0];
  var setTemas = temasState[1];
  var showTemaFormState = useState(false);
  var showTemaForm = showTemaFormState[0];
  var setShowTemaForm = showTemaFormState[1];
  var temaFormState = useState({ titulo: '', contenido: '' });
  var temaForm = temaFormState[0];
  var setTemaForm = temaFormState[1];
  var selectedTemaState = useState<ForoTema | null>(null);
  var selectedTema = selectedTemaState[0];
  var setSelectedTema = selectedTemaState[1];
  var respuestaFormState = useState('');
  var respuestaForm = respuestaFormState[0];
  var setRespuestaForm = respuestaFormState[1];

  // Calificaciones
  var misEntregasState = useState<Entrega[]>([]);
  var misEntregas = misEntregasState[0];
  var setMisEntregas = misEntregasState[1];
  var valoracionesState = useState<Valoracion[]>([]);
  var valoraciones = valoracionesState[0];
  var setValoraciones = valoracionesState[1];

  // Valoracion form (for students)
  var showValFormState = useState(false);
  var showValForm = showValFormState[0];
  var setShowValForm = showValFormState[1];
  var valFormState = useState({ puntuacion: 5, comentario: '', puntosMejora: '', profesorId: 0 });
  var valForm = valFormState[0];
  var setValForm = valFormState[1];
  var valErrorState = useState('');
  var valError = valErrorState[0];
  var setValError = valErrorState[1];
  var valSavingState = useState(false);
  var valSaving = valSavingState[0];
  var setValSaving = valSavingState[1];
  var isEstudiante = user && user.role === 'ROLE_ESTUDIANTE';

  // Grupos
  var gruposState = useState<Grupo[]>([]);
  var grupos = gruposState[0];
  var setGrupos = gruposState[1];
  var showGrupoFormState = useState(false);
  var showGrupoForm = showGrupoFormState[0];
  var setShowGrupoForm = showGrupoFormState[1];
  var grupoFormState = useState({ nombre: '', tipo: 'TEORIA' });
  var grupoForm = grupoFormState[0];
  var setGrupoForm = grupoFormState[1];

  // Carpetas
  var carpetasState = useState<Carpeta[]>([]);
  var carpetas = carpetasState[0];
  var setCarpetas = carpetasState[1];
  var showCarpetaFormState = useState(false);
  var showCarpetaForm = showCarpetaFormState[0];
  var setShowCarpetaForm = showCarpetaFormState[1];
  var carpetaFormState = useState({ nombre: '' });
  var carpetaForm = carpetaFormState[0];
  var setCarpetaForm = carpetaFormState[1];

  // Role check
  var isAdmin = user && user.role === 'ROLE_ADMIN';
  var isProfesor = user && user.role === 'ROLE_PROFESOR';
  var canManageContent = isAdmin || isProfesor;

  useEffect(function () {
    if (!isNew && id) {
      asignaturasApi.getById(Number(id))
        .then(function (r) { setAsignatura(r.data); setForm(r.data); })
        .catch(function () { setError('Asignatura no encontrada'); })
        .finally(function () { setLoading(false); });
    }
  }, [id, isNew]);

  // Load tab data when switching
  useEffect(function () {
    if (!id || isNew) return;
    var numId = Number(id);
    if (tab === 'anuncios') anunciosApi.getByAsignatura(numId).then(function (r) { setAnuncios(r.data); }).catch(function () {});
    if (tab === 'materiales') {
      materialesApi.getByAsignatura(numId).then(function (r) { setMateriales(r.data); }).catch(function () {});
      carpetasApi.getByAsignatura(numId).then(function (r) { setCarpetas(r.data); }).catch(function () {});
    }
    if (tab === 'tareas') tareasApi.getByAsignatura(numId).then(function (r) { setTareas(r.data); }).catch(function () {});
    if (tab === 'foro') foroApi.getTemasByAsignatura(numId).then(function (r) { setTemas(r.data); }).catch(function () {});
    if (tab === 'grupos') gruposApi.getByAsignatura(numId).then(function (r) { setGrupos(r.data); }).catch(function () {});
    if (tab === 'calificaciones') {
      entregasApi.getMisEntregas().then(function (r) { setMisEntregas(r.data); }).catch(function () {});
      valoracionesApi.getByAsignatura(numId).then(function (r) { setValoraciones(r.data); }).catch(function () {});
    }
  }, [tab, id, isNew]);

  function update(field: string, value: any) { setForm(function (prev) { return Object.assign({}, prev, { [field]: value }); }); }

  function handleSubmitAsignatura(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    (isNew ? asignaturasApi.create(form) : asignaturasApi.update(Number(id), form))
      .then(function () { navigate('/asignaturas'); })
      .catch(function (err: any) { setError(err.response && err.response.data && err.response.data.message || 'Error al guardar'); })
      .finally(function () { setSaving(false); });
  }

  // ===== HELPERS =====
  function loadAnuncios() { anunciosApi.getByAsignatura(Number(id)).then(function (r) { setAnuncios(r.data); }); }
  function loadMateriales() { materialesApi.getByAsignatura(Number(id)).then(function (r) { setMateriales(r.data); }); }
  function loadTareas() { tareasApi.getByAsignatura(Number(id)).then(function (r) { setTareas(r.data); }); }
  function loadTemas() { foroApi.getTemasByAsignatura(Number(id)).then(function (r) { setTemas(r.data); }); }
  function loadGrupos() { gruposApi.getByAsignatura(Number(id)).then(function (r) { setGrupos(r.data); }); }
  function loadCarpetas() { carpetasApi.getByAsignatura(Number(id)).then(function (r) { setCarpetas(r.data); }); }

  function handleCreateAnuncio(e: FormEvent) {
    e.preventDefault();
    anunciosApi.create(Object.assign({}, anuncioForm, { asignaturaId: Number(id) }))
      .then(function () { setShowAnuncioForm(false); setAnuncioForm({ titulo: '', contenido: '', importante: false }); loadAnuncios(); });
  }

  function handleCreateMaterial(e: FormEvent) {
    e.preventDefault();
    materialesApi.create(Object.assign({}, materialForm, { asignaturaId: Number(id) }))
      .then(function () { setShowMaterialForm(false); setMaterialForm({ titulo: '', descripcion: '', urlRecurso: '', tipo: 'DOCUMENTO' }); loadMateriales(); });
  }

  function handleCreateTarea(e: FormEvent) {
    e.preventDefault();
    tareasApi.create(Object.assign({}, tareaForm, { asignaturaId: Number(id) }))
      .then(function () { setShowTareaForm(false); setTareaForm({ titulo: '', descripcion: '', fechaEntrega: '', puntuacionMaxima: 10 }); loadTareas(); });
  }

  function handleSelectTarea(t: Tarea) {
    setSelectedTarea(t);
    entregasApi.getByTarea(t.id).then(function (res) { setEntregas(res.data); });
  }

  function handleSubmitEntrega(e: FormEvent) {
    e.preventDefault();
    if (!selectedTarea) return;
    entregasApi.submit(Object.assign({}, entregaForm, { tareaId: selectedTarea.id }))
      .then(function () { setShowEntregaForm(false); setEntregaForm({ contenido: '', urlAdjunto: '' }); if (selectedTarea) handleSelectTarea(selectedTarea); });
  }

  function handleCalificar(entregaId: number) {
    var cal = prompt('Calificacion (0-10):');
    if (!cal) return;
    var comentario = prompt('Comentario (opcional):') || '';
    entregasApi.calificar(entregaId, Number(cal), comentario)
      .then(function () { if (selectedTarea) handleSelectTarea(selectedTarea); });
  }

  function handleCreateTema(e: FormEvent) {
    e.preventDefault();
    foroApi.createTema(Object.assign({}, temaForm, { asignaturaId: Number(id) }))
      .then(function () { setShowTemaForm(false); setTemaForm({ titulo: '', contenido: '' }); loadTemas(); });
  }

  function handleSelectTema(t: ForoTema) {
    foroApi.getTemaById(t.id).then(function (res) { setSelectedTema(res.data); });
  }

  function handleCreateRespuesta(e: FormEvent) {
    e.preventDefault();
    if (!selectedTema) return;
    foroApi.createRespuesta({ contenido: respuestaForm, temaId: selectedTema.id })
      .then(function () { setRespuestaForm(''); handleSelectTema(selectedTema as ForoTema); });
  }

  function handleCreateGrupo(e: FormEvent) {
    e.preventDefault();
    gruposApi.create(Object.assign({}, grupoForm, { asignaturaId: Number(id) }))
      .then(function () { setShowGrupoForm(false); setGrupoForm({ nombre: '', tipo: 'TEORIA' }); loadGrupos(); });
  }

  function handleCreateCarpeta(e: FormEvent) {
    e.preventDefault();
    carpetasApi.create(Object.assign({}, carpetaForm, { asignaturaId: Number(id) }))
      .then(function () { setShowCarpetaForm(false); setCarpetaForm({ nombre: '' }); loadCarpetas(); loadMateriales(); });
  }

  function loadValoraciones() { valoracionesApi.getByAsignatura(Number(id)).then(function (r) { setValoraciones(r.data); }); }

  function handleCreateValoracion(e: FormEvent) {
    e.preventDefault();
    setValSaving(true);
    setValError('');
    valoracionesApi.create({
      puntuacion: valForm.puntuacion,
      comentario: valForm.comentario,
      puntosMejora: valForm.puntosMejora,
      profesorId: valForm.profesorId,
      asignaturaId: Number(id),
    })
      .then(function () { setShowValForm(false); setValForm({ puntuacion: 5, comentario: '', puntosMejora: '', profesorId: 0 }); setValError(''); loadValoraciones(); })
      .catch(function (err: any) { var msg = (err.response && err.response.data && err.response.data.message) || 'Error al enviar valoracion'; setValError(msg); })
      .finally(function () { setValSaving(false); });
  }

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  // ===== NEW ASIGNATURA FORM =====
  if (isNew) {
    return (
      <div className="mx-auto max-w-2xl">
        <h2 className="mb-6 text-2xl font-bold text-gray-900">Nueva asignatura</h2>
        {error && <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</div>}
        <form onSubmit={handleSubmitAsignatura} className="space-y-4 rounded-xl bg-white p-6 shadow-sm">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
            <input type="text" value={form.nombre || ''} onChange={function (e) { update('nombre', e.target.value); }} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Descripcion</label>
            <textarea value={form.descripcion || ''} onChange={function (e) { update('descripcion', e.target.value); }} rows={3} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">URL</label>
            <input type="url" value={form.url || ''} onChange={function (e) { update('url', e.target.value); }} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div className="flex gap-3 pt-4">
            <button type="submit" disabled={saving} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition">{saving ? 'Guardando...' : 'Crear curso'}</button>
            <button type="button" onClick={function () { navigate('/asignaturas'); }} className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium hover:bg-gray-200 transition">Cancelar</button>
          </div>
        </form>
      </div>
    );
  }

  // Helper to render stars
  function renderStars(n: number) {
    var stars = [];
    for (var i = 0; i < 5; i++) {
      stars.push(i < n ? '*' : '-');
    }
    return stars.join('');
  }

  function getMaterialIcon(tipo: string) {
    if (tipo === 'DOCUMENTO') return '[DOC]';
    if (tipo === 'VIDEO') return '[VID]';
    if (tipo === 'ENLACE') return '[URL]';
    if (tipo === 'PRESENTACION') return '[PPT]';
    if (tipo === 'EJERCICIO') return '[EJ]';
    return '[FILE]';
  }

  // ===== VIRTUAL CLASSROOM VIEW =====
  return (
    <div>
      {/* Course header */}
      <div className="mb-6 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 p-6 text-white shadow-lg">
        <h2 className="text-2xl font-bold">{asignatura ? asignatura.nombre : ''}</h2>
        <p className="mt-1 text-indigo-100">{asignatura && asignatura.descripcion ? asignatura.descripcion : 'Sin descripcion'}</p>
        {asignatura && asignatura.creadorNombre && (
          <p className="mt-2 text-sm text-indigo-200">Creador: {asignatura.creadorNombre}</p>
        )}
        <div className="mt-3 flex gap-2">
          <span className="rounded-full bg-white bg-opacity-20 px-3 py-1 text-xs">{asignatura && asignatura.profesorIds ? asignatura.profesorIds.length : 0} profesores</span>
          <span className="rounded-full bg-white bg-opacity-20 px-3 py-1 text-xs">{asignatura && asignatura.estudianteIds ? asignatura.estudianteIds.length : 0} estudiantes</span>
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-6 flex gap-1 overflow-x-auto rounded-lg bg-white p-1 shadow-sm">
        {TABS.map(function (t) {
          return (
            <button
              key={t.key}
              onClick={function () { setTab(t.key); setSelectedTarea(null); setSelectedTema(null); }}
              className={'flex items-center gap-1.5 whitespace-nowrap rounded-md px-3 py-2 text-sm font-medium transition ' +
                (tab === t.key ? 'bg-indigo-600 text-white' : 'text-gray-600 hover:bg-gray-100')}
            >[{t.icon}] {t.label}</button>
          );
        })}
      </div>

      {error && <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</div>}

      {/* ===== INFO TAB ===== */}
      {tab === 'info' && isAdmin && (
        <form onSubmit={handleSubmitAsignatura} className="space-y-4 rounded-xl bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900">Editar curso</h3>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
            <input type="text" value={form.nombre || ''} onChange={function (e) { update('nombre', e.target.value); }} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Descripcion</label>
            <textarea value={form.descripcion || ''} onChange={function (e) { update('descripcion', e.target.value); }} rows={3} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">URL</label>
            <input type="url" value={form.url || ''} onChange={function (e) { update('url', e.target.value); }} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <button type="submit" disabled={saving} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition">{saving ? 'Guardando...' : 'Guardar cambios'}</button>
        </form>
      )}
      {tab === 'info' && !isAdmin && (
        <div className="space-y-4 rounded-xl bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900">Informacion del curso</h3>
          <p className="text-sm text-gray-600"><strong>Nombre:</strong> {asignatura ? asignatura.nombre : ''}</p>
          <p className="text-sm text-gray-600"><strong>Descripcion:</strong> {asignatura && asignatura.descripcion ? asignatura.descripcion : 'Sin descripcion'}</p>
          {asignatura && asignatura.url && <p className="text-sm text-gray-600"><strong>URL:</strong> <a href={asignatura.url} target="_blank" rel="noopener noreferrer" className="text-indigo-600">{asignatura.url}</a></p>}
          <p className="text-sm text-gray-600"><strong>Creador:</strong> {asignatura && asignatura.creadorNombre ? asignatura.creadorNombre : 'N/A'}</p>
          <p className="text-sm text-gray-600"><strong>Profesores:</strong> {asignatura && asignatura.profesorIds ? asignatura.profesorIds.length : 0}</p>
          <p className="text-sm text-gray-600"><strong>Estudiantes:</strong> {asignatura && asignatura.estudianteIds ? asignatura.estudianteIds.length : 0}</p>
        </div>
      )}

      {/* ===== ANUNCIOS TAB ===== */}
      {tab === 'anuncios' && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Anuncios del curso</h3>
            {canManageContent && <button onClick={function () { setShowAnuncioForm(!showAnuncioForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showAnuncioForm ? 'Cancelar' : '+ Nuevo anuncio'}</button>}
          </div>
          {showAnuncioForm && (
            <form onSubmit={handleCreateAnuncio} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <input type="text" value={anuncioForm.titulo} onChange={function (e) { setAnuncioForm(Object.assign({}, anuncioForm, { titulo: e.target.value })); }} placeholder="Titulo del anuncio" required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <textarea value={anuncioForm.contenido} onChange={function (e) { setAnuncioForm(Object.assign({}, anuncioForm, { contenido: e.target.value })); }} placeholder="Contenido..." rows={3} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={anuncioForm.importante} onChange={function (e) { setAnuncioForm(Object.assign({}, anuncioForm, { importante: e.target.checked })); }} /> Marcar como importante</label>
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Publicar</button>
            </form>
          )}
          {anuncios.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay anuncios publicados</p>
          ) : (
            <div className="space-y-3">
              {anuncios.map(function (a) {
                return (
                  <div key={a.id} className={'rounded-xl bg-white p-5 shadow-sm ' + (a.importante ? 'border-l-4 border-amber-400' : '')}>
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-semibold text-gray-900">{a.importante ? '[!] ' : ''}{a.titulo}</h4>
                        <p className="text-xs text-gray-400">{a.autorNombre} - {new Date(a.fechaCreacion).toLocaleString('es-ES')}</p>
                      </div>
                      <button onClick={function () { anunciosApi.delete(a.id).then(loadAnuncios); }} className="text-xs text-red-500 hover:text-red-700">Eliminar</button>
                    </div>
                    <p className="mt-2 text-sm text-gray-600 whitespace-pre-wrap">{a.contenido}</p>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ===== MATERIALES TAB ===== */}
      {tab === 'materiales' && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Material del curso</h3>
            <div className="flex gap-2">
              {canManageContent && <button onClick={function () { setShowCarpetaForm(!showCarpetaForm); }} className="rounded-lg bg-teal-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-teal-700 transition">{showCarpetaForm ? 'Cancelar' : '+ Carpeta'}</button>}
              {canManageContent && <button onClick={function () { setShowMaterialForm(!showMaterialForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showMaterialForm ? 'Cancelar' : '+ Nuevo material'}</button>}
            </div>
          </div>
          {showMaterialForm && (
            <form onSubmit={handleCreateMaterial} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <input type="text" value={materialForm.titulo} onChange={function (e) { setMaterialForm(Object.assign({}, materialForm, { titulo: e.target.value })); }} placeholder="Titulo" required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <textarea value={materialForm.descripcion} onChange={function (e) { setMaterialForm(Object.assign({}, materialForm, { descripcion: e.target.value })); }} placeholder="Descripcion" rows={2} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <input type="text" value={materialForm.urlRecurso} onChange={function (e) { setMaterialForm(Object.assign({}, materialForm, { urlRecurso: e.target.value })); }} placeholder="URL del recurso" className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <select value={materialForm.tipo} onChange={function (e) { setMaterialForm(Object.assign({}, materialForm, { tipo: e.target.value })); }} className="rounded-lg border border-gray-300 px-3 py-2">
                <option value="DOCUMENTO">DOCUMENTO</option>
                <option value="VIDEO">VIDEO</option>
                <option value="ENLACE">ENLACE</option>
                <option value="PRESENTACION">PRESENTACION</option>
                <option value="EJERCICIO">EJERCICIO</option>
                <option value="OTRO">OTRO</option>
              </select>
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Subir material</button>
            </form>
          )}
          {showCarpetaForm && (
            <form onSubmit={handleCreateCarpeta} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm border-l-4 border-teal-400">
              <input type="text" value={carpetaForm.nombre} onChange={function (e) { setCarpetaForm(Object.assign({}, carpetaForm, { nombre: e.target.value })); }} placeholder="Nombre de la carpeta" required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-teal-500 focus:outline-none focus:ring-1 focus:ring-teal-500" />
              <button type="submit" className="rounded-lg bg-teal-600 px-4 py-2 text-sm font-medium text-white hover:bg-teal-700 transition">Crear carpeta</button>
            </form>
          )}
          {/* Carpetas listing */}
          {carpetas.length > 0 && (
            <div className="mb-4">
              <h4 className="mb-2 text-sm font-semibold text-gray-700">Carpetas</h4>
              <div className="flex flex-wrap gap-2">
                {carpetas.map(function (c) {
                  return (
                    <div key={c.id} className="flex items-center gap-1 rounded-lg bg-teal-50 px-3 py-2 text-sm border border-teal-200">
                      <span className="text-teal-600">[DIR]</span>
                      <span className="font-medium text-teal-800">{c.nombre}</span>
                      {canManageContent && <button onClick={function () { carpetasApi.delete(c.id).then(function () { loadCarpetas(); }); }} className="ml-1 text-xs text-red-400 hover:text-red-600">x</button>}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
          {materiales.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay materiales en este curso</p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2">
              {materiales.map(function (m) {
                return (
                  <div key={m.id} className="rounded-xl bg-white p-4 shadow-sm">
                    <div className="flex items-center gap-2">
                      <span className="text-xl">{getMaterialIcon(m.tipo)}</span>
                      <div>
                        <h4 className="font-semibold text-gray-900">{m.titulo}</h4>
                        <p className="text-xs text-gray-400">{m.tipo} - {m.autorNombre}</p>
                      </div>
                    </div>
                    {m.descripcion && <p className="mt-2 text-sm text-gray-500">{m.descripcion}</p>}
                    <div className="mt-2 flex gap-2">
                      {m.urlRecurso && <a href={m.urlRecurso} target="_blank" rel="noopener noreferrer" className="text-sm text-indigo-600 hover:text-indigo-500">Abrir</a>}
                      <button onClick={function () { materialesApi.delete(m.id).then(loadMateriales); }} className="text-sm text-red-500 hover:text-red-700">Eliminar</button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ===== TAREAS TAB ===== */}
      {tab === 'tareas' && !selectedTarea && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Tareas</h3>
            {canManageContent && <button onClick={function () { setShowTareaForm(!showTareaForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showTareaForm ? 'Cancelar' : '+ Nueva tarea'}</button>}
          </div>
          {showTareaForm && (
            <form onSubmit={handleCreateTarea} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <input type="text" value={tareaForm.titulo} onChange={function (e) { setTareaForm(Object.assign({}, tareaForm, { titulo: e.target.value })); }} placeholder="Titulo" required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <textarea value={tareaForm.descripcion} onChange={function (e) { setTareaForm(Object.assign({}, tareaForm, { descripcion: e.target.value })); }} placeholder="Descripcion e instrucciones" rows={3} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Fecha limite</label>
                  <input type="datetime-local" value={tareaForm.fechaEntrega} onChange={function (e) { setTareaForm(Object.assign({}, tareaForm, { fechaEntrega: e.target.value })); }} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Puntuacion maxima</label>
                  <input type="number" value={tareaForm.puntuacionMaxima} onChange={function (e) { setTareaForm(Object.assign({}, tareaForm, { puntuacionMaxima: Number(e.target.value) })); }} min={0} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                </div>
              </div>
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Crear tarea</button>
            </form>
          )}
          {tareas.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay tareas asignadas</p>
          ) : (
            <div className="space-y-3">
              {tareas.map(function (t) {
                var isPast = new Date(t.fechaEntrega) < new Date();
                return (
                  <div key={t.id} className="rounded-xl bg-white p-5 shadow-sm cursor-pointer hover:shadow-md transition" onClick={function () { handleSelectTarea(t); }}>
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-semibold text-gray-900">[T] {t.titulo}</h4>
                        <p className="text-xs text-gray-400">Por: {t.creadorNombre}</p>
                      </div>
                      <div className="text-right">
                        <span className={'rounded-full px-2 py-0.5 text-xs font-medium ' + (isPast ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700')}>
                          {isPast ? 'Cerrada' : 'Abierta'}
                        </span>
                        <p className="mt-1 text-xs text-gray-400">Entrega: {new Date(t.fechaEntrega).toLocaleDateString('es-ES')}</p>
                      </div>
                    </div>
                    {t.descripcion && <p className="mt-2 text-sm text-gray-600">{t.descripcion}</p>}
                    <div className="mt-2 flex gap-3 text-xs text-gray-400">
                      <span>Max: {t.puntuacionMaxima} pts</span>
                      <span>{t.totalEntregas} entregas</span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ===== TAREA DETAIL ===== */}
      {tab === 'tareas' && selectedTarea && (
        <div>
          <button onClick={function () { setSelectedTarea(null); }} className="mb-4 text-sm text-indigo-600 hover:text-indigo-500">&lt;- Volver a tareas</button>
          <div className="mb-4 rounded-xl bg-white p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-gray-900">{selectedTarea.titulo}</h3>
            <p className="text-sm text-gray-500">{selectedTarea.descripcion}</p>
            <div className="mt-2 flex gap-4 text-xs text-gray-400">
              <span>Fecha limite: {new Date(selectedTarea.fechaEntrega).toLocaleString('es-ES')}</span>
              <span>Max: {selectedTarea.puntuacionMaxima} pts</span>
            </div>
          </div>

          <div className="mb-4 flex items-center justify-between">
            <h4 className="font-semibold text-gray-900">Entregas ({entregas.length})</h4>
            <button onClick={function () { setShowEntregaForm(!showEntregaForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showEntregaForm ? 'Cancelar' : 'Enviar entrega'}</button>
          </div>

          {showEntregaForm && (
            <form onSubmit={handleSubmitEntrega} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <textarea value={entregaForm.contenido} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { contenido: e.target.value })); }} placeholder="Tu respuesta..." rows={4} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <input type="text" value={entregaForm.urlAdjunto} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { urlAdjunto: e.target.value })); }} placeholder="URL adjunto (opcional)" className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Enviar</button>
            </form>
          )}

          {entregas.length === 0 ? (
            <p className="rounded-xl bg-white p-6 text-center text-gray-500 shadow-sm">No hay entregas aun</p>
          ) : (
            <div className="space-y-3">
              {entregas.map(function (en) {
                return (
                  <div key={en.id} className="rounded-xl bg-white p-4 shadow-sm">
                    <div className="flex items-start justify-between">
                      <div>
                        <p className="font-medium text-gray-900">{en.estudianteNombre}</p>
                        <p className="text-xs text-gray-400">{new Date(en.fechaEntrega).toLocaleString('es-ES')}</p>
                      </div>
                      {en.calificacion !== null ? (
                        <span className="rounded-full bg-green-100 px-2.5 py-0.5 text-sm font-semibold text-green-800">{en.calificacion}/{selectedTarea.puntuacionMaxima}</span>
                      ) : (
                        <button onClick={function () { handleCalificar(en.id); }} className="rounded bg-amber-50 px-2 py-1 text-xs text-amber-700 hover:bg-amber-100">Calificar</button>
                      )}
                    </div>
                    <p className="mt-2 text-sm text-gray-600 whitespace-pre-wrap">{en.contenido}</p>
                    {en.urlAdjunto && <a href={en.urlAdjunto} target="_blank" rel="noopener noreferrer" className="mt-1 inline-block text-sm text-indigo-600">[Adjunto]</a>}
                    {en.comentarioProfesor && <p className="mt-2 rounded bg-blue-50 p-2 text-sm text-blue-700">{en.comentarioProfesor}</p>}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ===== FORO TAB ===== */}
      {tab === 'foro' && !selectedTema && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Foro de discusion</h3>
            {canManageContent && <button onClick={function () { setShowTemaForm(!showTemaForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showTemaForm ? 'Cancelar' : '+ Nuevo tema'}</button>}
          </div>
          {showTemaForm && (
            <form onSubmit={handleCreateTema} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <input type="text" value={temaForm.titulo} onChange={function (e) { setTemaForm(Object.assign({}, temaForm, { titulo: e.target.value })); }} placeholder="Titulo del tema" required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <textarea value={temaForm.contenido} onChange={function (e) { setTemaForm(Object.assign({}, temaForm, { contenido: e.target.value })); }} placeholder="Contenido..." rows={3} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Publicar tema</button>
            </form>
          )}
          {temas.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay temas en el foro</p>
          ) : (
            <div className="space-y-2">
              {temas.map(function (t) {
                return (
                  <div key={t.id} className="rounded-xl bg-white p-4 shadow-sm cursor-pointer hover:shadow-md transition" onClick={function () { handleSelectTema(t); }}>
                    <div className="flex items-center gap-2">
                      {t.fijado && <span className="text-amber-500">[PIN]</span>}
                      <h4 className="font-semibold text-gray-900">{t.titulo}</h4>
                    </div>
                    <p className="mt-1 text-xs text-gray-400">{t.autorNombre} - {new Date(t.fechaCreacion).toLocaleString('es-ES')} - {t.totalRespuestas} respuestas</p>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ===== FORO TEMA DETAIL ===== */}
      {tab === 'foro' && selectedTema && (
        <div>
          <button onClick={function () { setSelectedTema(null); }} className="mb-4 text-sm text-indigo-600 hover:text-indigo-500">&lt;- Volver al foro</button>
          <div className="mb-4 rounded-xl bg-white p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-gray-900">{selectedTema.fijado ? '[PIN] ' : ''}{selectedTema.titulo}</h3>
            <p className="text-xs text-gray-400">{selectedTema.autorNombre} - {new Date(selectedTema.fechaCreacion).toLocaleString('es-ES')}</p>
            <p className="mt-3 text-sm text-gray-700 whitespace-pre-wrap">{selectedTema.contenido}</p>
          </div>

          <h4 className="mb-3 font-semibold text-gray-900">Respuestas ({selectedTema.respuestas ? selectedTema.respuestas.length : 0})</h4>

          {selectedTema.respuestas && selectedTema.respuestas.map(function (r: ForoRespuesta) {
            return (
              <div key={r.id} className="mb-2 rounded-lg bg-gray-50 p-4">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-gray-900">{r.autorNombre}</p>
                  <p className="text-xs text-gray-400">{new Date(r.fechaCreacion).toLocaleString('es-ES')}</p>
                </div>
                <p className="mt-1 text-sm text-gray-600 whitespace-pre-wrap">{r.contenido}</p>
              </div>
            );
          })}

          <form onSubmit={handleCreateRespuesta} className="mt-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
            <textarea value={respuestaForm} onChange={function (e) { setRespuestaForm(e.target.value); }} placeholder="Escribe tu respuesta..." rows={3} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
            <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Responder</button>
          </form>
        </div>
      )}

      {/* ===== GRUPOS TAB ===== */}
      {tab === 'grupos' && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Grupos de la asignatura</h3>
            {canManageContent && <button onClick={function () { setShowGrupoForm(!showGrupoForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showGrupoForm ? 'Cancelar' : '+ Nuevo grupo'}</button>}
          </div>
          {showGrupoForm && (
            <form onSubmit={handleCreateGrupo} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <input type="text" value={grupoForm.nombre} onChange={function (e) { setGrupoForm(Object.assign({}, grupoForm, { nombre: e.target.value })); }} placeholder="Nombre del grupo" required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <select value={grupoForm.tipo} onChange={function (e) { setGrupoForm(Object.assign({}, grupoForm, { tipo: e.target.value })); }} className="rounded-lg border border-gray-300 px-3 py-2">
                <option value="TEORIA">Teoria</option>
                <option value="PRACTICA">Practica</option>
              </select>
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Crear grupo</button>
            </form>
          )}
          {grupos.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay grupos creados</p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2">
              {grupos.map(function (g) {
                return (
                  <div key={g.id} className="rounded-xl bg-white p-5 shadow-sm">
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-semibold text-gray-900">{g.nombre}</h4>
                        <span className={'mt-1 inline-block rounded-full px-2 py-0.5 text-xs font-medium ' + (g.tipo === 'TEORIA' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700')}>{g.tipo === 'TEORIA' ? 'Teoria' : 'Practica'}</span>
                      </div>
                      {canManageContent && <button onClick={function () { gruposApi.delete(g.id).then(loadGrupos); }} className="text-xs text-red-500 hover:text-red-700">Eliminar</button>}
                    </div>
                    <p className="mt-2 text-xs text-gray-400">{g.miembroIds ? g.miembroIds.length : 0} miembros</p>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ===== CALIFICACIONES TAB ===== */}
      {tab === 'calificaciones' && (
        <div>
          <h3 className="mb-4 text-lg font-semibold text-gray-900">Mis calificaciones</h3>
          {misEntregas.filter(function (e) { return e.calificacion !== null; }).length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay calificaciones disponibles</p>
          ) : (
            <div className="overflow-hidden rounded-xl bg-white shadow-sm">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Tarea</th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Fecha entrega</th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Calificacion</th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Comentario</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {misEntregas.filter(function (e) { return e.calificacion !== null; }).map(function (en) {
                    return (
                      <tr key={en.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 text-sm font-medium text-gray-900">{en.tareaTitulo}</td>
                        <td className="px-4 py-3 text-sm text-gray-500">{new Date(en.fechaEntrega).toLocaleDateString('es-ES')}</td>
                        <td className="px-4 py-3 text-sm font-semibold text-indigo-600">{en.calificacion}</td>
                        <td className="px-4 py-3 text-sm text-gray-500">{en.comentarioProfesor || '-'}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          {/* ===== VALORACIONES DE PROFESORES (ANONIMAS) ===== */}
          <div className="mt-8 flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Valoraciones de profesores</h3>
            {isEstudiante && (
              <button onClick={function () { setShowValForm(!showValForm); setValError(''); }} className="rounded-lg bg-amber-500 px-4 py-2 text-sm font-medium text-white hover:bg-amber-600 transition">
                {showValForm ? 'Cancelar' : '+ Valorar profesor'}
              </button>
            )}
          </div>

          {showValForm && isEstudiante && (
            <form onSubmit={handleCreateValoracion} className="mb-6 space-y-4 rounded-xl bg-white p-6 shadow-sm border border-amber-100">
              <div className="flex items-center gap-2 rounded-lg bg-blue-50 p-3 text-sm text-blue-700">
                <span>&#128274;</span>
                <span>Tu valoracion es <strong>anonima</strong>. Solo los estudiantes matriculados pueden valorar.</span>
              </div>

              {valError && (
                <div className="rounded-lg bg-red-50 p-3 text-sm text-red-700 border border-red-200">{valError}</div>
              )}

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Profesor a valorar</label>
                <select value={valForm.profesorId} onChange={function (e) { setValForm(Object.assign({}, valForm, { profesorId: Number(e.target.value) })); }} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500">
                  <option value={0} disabled>Seleccionar profesor...</option>
                  {asignatura && asignatura.profesorIds && asignatura.profesorIds.map(function (pid) {
                    return <option key={pid} value={pid}>{'Profesor #' + pid}</option>;
                  })}
                </select>
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Puntuacion: <span className="text-amber-500">{renderStars(valForm.puntuacion)}</span> ({valForm.puntuacion}/5)</label>
                <input type="range" min={1} max={5} value={valForm.puntuacion} onChange={function (e) { setValForm(Object.assign({}, valForm, { puntuacion: Number(e.target.value) })); }} className="w-full accent-amber-500" />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Comentario</label>
                <textarea value={valForm.comentario} onChange={function (e) { setValForm(Object.assign({}, valForm, { comentario: e.target.value })); }} rows={3} placeholder="Describe tu experiencia de forma constructiva..." className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Puntos de mejora</label>
                <textarea value={valForm.puntosMejora} onChange={function (e) { setValForm(Object.assign({}, valForm, { puntosMejora: e.target.value })); }} rows={3} placeholder="Sugiere aspectos concretos a mejorar (metodologia, materiales, comunicacion...)" className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              </div>

              <div className="flex items-center gap-2 rounded-lg bg-amber-50 p-3 text-xs text-amber-700">
                <span>&#9888;&#65039;</span>
                <span>Los comentarios son revisados por IA. No se permiten insultos ni ataques personales.</span>
              </div>

              <button type="submit" disabled={valSaving} className="rounded-lg bg-amber-500 px-4 py-2 text-sm font-medium text-white hover:bg-amber-600 disabled:opacity-50 transition">
                {valSaving ? 'Enviando...' : 'Enviar valoracion anonima'}
              </button>
            </form>
          )}

          {valoraciones.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay valoraciones de profesores</p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2">
              {valoraciones.map(function (v) {
                return (
                  <div key={v.id} className="rounded-xl bg-white p-4 shadow-sm border border-gray-100">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <p className="font-semibold text-gray-900">{v.profesorNombre}</p>
                        <p className="text-xs text-gray-400">{v.fechaCreacion ? new Date(v.fechaCreacion).toLocaleDateString('es-ES') : ''}</p>
                      </div>
                      <span className="text-amber-400">{renderStars(v.puntuacion)}</span>
                    </div>
                    {v.comentario && <p className="text-sm text-gray-600 mb-2">{v.comentario}</p>}
                    {v.puntosMejora && (
                      <div className="rounded-lg bg-amber-50 p-2 mb-2">
                        <p className="text-xs font-medium text-amber-700 mb-0.5">Puntos de mejora:</p>
                        <p className="text-xs text-amber-600">{v.puntosMejora}</p>
                      </div>
                    )}
                    <p className="text-xs text-gray-400 italic border-t border-gray-100 pt-2">Valoracion anonima</p>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
