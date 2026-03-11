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
  clasesApi,
  usuariosApi,
  asistenciaApi,
  progresoApi,
  rubricasApi,
  gradosApi,
  archivosApi,
} from '../api/endpoints';
import type { Asignatura, Anuncio, Material, Tarea, Entrega, ForoTema, ForoRespuesta, Valoracion, Grupo, Carpeta, Clase, AsistenciaRecord, Progreso, Rubrica, CriterioRubrica, Grado } from '../types';

type Tab = 'info' | 'anuncios' | 'materiales' | 'horarios' | 'tareas' | 'evaluaciones' | 'simulacros' | 'foro' | 'grupos' | 'calificaciones' | 'asistencia' | 'progreso' | 'rubricas';

var ALL_TABS: { key: Tab; label: string; icon: string; roles: string[] | null }[] = [
  { key: 'info', label: 'Informacion', icon: 'i', roles: null },
  { key: 'anuncios', label: 'Anuncios', icon: '!', roles: null },
  { key: 'materiales', label: 'Materiales', icon: '#', roles: null },
  { key: 'horarios', label: 'Horarios', icon: 'H', roles: null },
  { key: 'tareas', label: 'Tareas', icon: 'T', roles: null },
  { key: 'evaluaciones', label: 'Evaluaciones', icon: 'E', roles: null },
  { key: 'simulacros', label: 'Simulacros', icon: 'S', roles: null },
  { key: 'foro', label: 'Foro', icon: 'F', roles: null },
  { key: 'grupos', label: 'Grupos', icon: 'G', roles: null },
  { key: 'calificaciones', label: 'Calificaciones', icon: 'C', roles: null },
  { key: 'asistencia', label: 'Asistencia', icon: 'A', roles: null },
  { key: 'progreso', label: 'Progreso', icon: 'P', roles: null },
  { key: 'rubricas', label: 'Rubricas', icon: 'R', roles: null },
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

  var formState = useState<Partial<Asignatura>>({ nombre: '', descripcion: '', url: '', aula: '', siglas: '', gradoId: 0 });
  var form = formState[0];
  var setForm = formState[1];

  var gradosListState = useState<Grado[]>([]);
  var gradosList = gradosListState[0];
  var setGradosList = gradosListState[1];

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
  var tareaFormState = useState({ titulo: '', descripcion: '', fechaEntrega: '', puntuacionMaxima: 10, tipoTarea: 'TAREA' });
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
  var selectedFileState = useState<File | null>(null);
  var selectedFile = selectedFileState[0];
  var setSelectedFile = selectedFileState[1];

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
  var grupoFormState = useState({ nombre: '', tipo: 'TEORIA', inscribible: true });
  var grupoForm = grupoFormState[0];
  var setGrupoForm = grupoFormState[1];

  // Horarios (clases de esta asignatura)
  var clasesState = useState<Clase[]>([]);
  var clases = clasesState[0];
  var setClases = clasesState[1];

  // My user ID (for grupo membership checks)
  var meIdState = useState<number | null>(null);
  var meId = meIdState[0];
  var setMeId = meIdState[1];

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

  // Asistencia
  var asistenciaState = useState<AsistenciaRecord[]>([]);
  var asistenciaList = asistenciaState[0];
  var setAsistenciaList = asistenciaState[1];
  var asistenciaFechaState = useState(new Date().toISOString().split('T')[0]);
  var asistenciaFecha = asistenciaFechaState[0];
  var setAsistenciaFecha = asistenciaFechaState[1];

  // Progreso
  var progresoState = useState<Progreso | null>(null);
  var progreso = progresoState[0];
  var setProgreso = progresoState[1];

  // Rubricas
  var rubricasState = useState<Rubrica[]>([]);
  var rubricasList = rubricasState[0];
  var setRubricasList = rubricasState[1];
  var showRubricaFormState = useState(false);
  var showRubricaForm = showRubricaFormState[0];
  var setShowRubricaForm = showRubricaFormState[1];
  var rubricaFormState = useState({ nombre: '', descripcion: '', tareaId: 0, criterios: [] as CriterioRubrica[] });
  var rubricaForm = rubricaFormState[0];
  var setRubricaForm = rubricaFormState[1];

  // Role check
  var isAdmin = user && user.role === 'ROLE_ADMIN';
  var isProfesor = user && user.role === 'ROLE_PROFESOR';
  var canManageContent = isAdmin || isProfesor;

  // Filter tabs by role
  var TABS = ALL_TABS.filter(function (t) {
    if (!t.roles) return true;
    return user && t.roles.indexOf(user.role) !== -1;
  });

  useEffect(function () {
    if (!isNew && id) {
      asignaturasApi.getById(Number(id))
        .then(function (r) { setAsignatura(r.data); setForm(r.data); })
        .catch(function () { setError('Asignatura no encontrada'); })
        .finally(function () { setLoading(false); });
    }
    // Fetch current user ID (for grupo membership checks)
    usuariosApi.me().then(function (r) { setMeId(r.data.id); }).catch(function () {});
    // Fetch degrees for the dropdown
    gradosApi.getAll().then(function (r) { 
      setGradosList(r.data); 
      if (isNew && r.data.length > 0) {
        update('gradoId', r.data[0].id);
      }
    }).catch(function () {});
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
    if (tab === 'evaluaciones') tareasApi.getByAsignatura(numId).then(function (r) { setTareas(r.data); }).catch(function () {});
    if (tab === 'simulacros') tareasApi.getByAsignatura(numId).then(function (r) { setTareas(r.data); }).catch(function () {});
    if (tab === 'horarios') clasesApi.getAll().then(function (r) {
      setClases(r.data.filter(function (c) { return c.asignaturaId === numId; }));
    }).catch(function () {});
    if (tab === 'foro') foroApi.getTemasByAsignatura(numId).then(function (r) { setTemas(r.data); }).catch(function () {});
    if (tab === 'grupos') gruposApi.getByAsignatura(numId).then(function (r) { setGrupos(r.data); }).catch(function () {});
    if (tab === 'calificaciones') {
      entregasApi.getMisEntregas().then(function (r) { setMisEntregas(r.data); }).catch(function () {});
      valoracionesApi.getByAsignatura(numId).then(function (r) { setValoraciones(r.data); }).catch(function () {});
    }
    if (tab === 'asistencia') {
      asistenciaApi.getByAsignatura(numId).then(function (r) { setAsistenciaList(r.data); }).catch(function () {});
    }
    if (tab === 'progreso') {
      progresoApi.getByAsignatura(numId).then(function (r) { setProgreso(r.data); }).catch(function () {});
    }
    if (tab === 'rubricas') {
      tareasApi.getByAsignatura(numId).then(function (r) { setTareas(r.data); }).catch(function () {});
      loadRubricas(numId);
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
  function loadClases() { clasesApi.getAll().then(function (r) { setClases(r.data.filter(function (c) { return c.asignaturaId === Number(id); })); }); }
  function loadAsistencia() { asistenciaApi.getByAsignatura(Number(id)).then(function (r) { setAsistenciaList(r.data); }).catch(function () {}); }
  function loadRubricas(asigId: number) {
    tareasApi.getByAsignatura(asigId).then(function (res) {
      var allRubricas: Rubrica[] = [];
      var promises = res.data.map(function (t) {
        return rubricasApi.getByTarea(t.id).then(function (rr) {
          if (rr.data) allRubricas.push(rr.data);
        }).catch(function () {});
      });
      Promise.all(promises).then(function () { setRubricasList(allRubricas); });
    }).catch(function () {});
  }

  function handleRegistrarAsistencia(estudianteId: number, estado: string) {
    asistenciaApi.registrar({ estudianteId: estudianteId, asignaturaId: Number(id), fecha: asistenciaFecha, estado: estado, observacion: '' })
      .then(function () { loadAsistencia(); })
      .catch(function () {});
  }

  function handleCreateRubrica(e: FormEvent) {
    e.preventDefault();
    rubricasApi.crear(Object.assign({}, rubricaForm, { criterios: rubricaForm.criterios.map(function (c, i) { return Object.assign({}, c, { orden: i + 1 }); }) }))
      .then(function () { setShowRubricaForm(false); setRubricaForm({ nombre: '', descripcion: '', tareaId: 0, criterios: [] }); loadRubricas(Number(id)); });
  }

  function addCriterio() {
    setRubricaForm(Object.assign({}, rubricaForm, { criterios: rubricaForm.criterios.concat([{ nombre: '', descripcion: '', puntuacionMaxima: 10, nivelExcelente: '', nivelBueno: '', nivelSuficiente: '', nivelInsuficiente: '' }]) }));
  }

  function updateCriterio(index: number, field: string, value: any) {
    var newCriterios = rubricaForm.criterios.map(function (c, i) {
      if (i === index) return Object.assign({}, c, { [field]: value });
      return c;
    });
    setRubricaForm(Object.assign({}, rubricaForm, { criterios: newCriterios }));
  }

  function removeCriterio(index: number) {
    setRubricaForm(Object.assign({}, rubricaForm, { criterios: rubricaForm.criterios.filter(function (_, i) { return i !== index; }) }));
  }

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
      .then(function () { setShowTareaForm(false); setTareaForm({ titulo: '', descripcion: '', fechaEntrega: '', puntuacionMaxima: 10, tipoTarea: 'TAREA' }); loadTareas(); });
  }

  function handleSelectTarea(t: Tarea) {
    setSelectedTarea(t);
    entregasApi.getByTarea(t.id).then(function (res) { setEntregas(res.data); });
  }

  function handleSubmitEntrega(e: FormEvent) {
    e.preventDefault();
    const currentTarea = selectedTarea;
    if (!currentTarea) return;

    var promise = selectedFile 
      ? archivosApi.upload(selectedFile).then(function(res) { return res.data.url; })
      : Promise.resolve(entregaForm.urlAdjunto);

    promise.then(function(finalUrl) {
      entregasApi.submit(Object.assign({}, entregaForm, { tareaId: currentTarea.id, urlAdjunto: finalUrl }))
        .then(function () { 
          setShowEntregaForm(false); 
          setEntregaForm({ contenido: '', urlAdjunto: '' }); 
          setSelectedFile(null);
          handleSelectTarea(currentTarea); 
        });
    }).catch(function() {
      alert('Error al subir el archivo o enviar la entrega');
    });
  }

  function handleCalificar(entregaId: number) {
    var cal = prompt('Calificacion (0-10):');
    if (!cal) return;
    const currentTarea = selectedTarea;
    var comentario = prompt('Comentario (opcional):') || '';
    entregasApi.calificar(entregaId, Number(cal), comentario)
      .then(function () { if (currentTarea) handleSelectTarea(currentTarea); });
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
    const currentTema = selectedTema;
    if (!currentTema) return;
    foroApi.createRespuesta({ contenido: respuestaForm, temaId: currentTema.id })
      .then(function () { setRespuestaForm(''); handleSelectTema(currentTema); });
  }

  function handleCreateGrupo(e: FormEvent) {
    e.preventDefault();
    gruposApi.create(Object.assign({}, grupoForm, { asignaturaId: Number(id) }))
      .then(function () { setShowGrupoForm(false); setGrupoForm({ nombre: '', tipo: 'TEORIA', inscribible: true }); loadGrupos(); });
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
            <label className="mb-1 block text-sm font-medium text-gray-700">Siglas</label>
            <input type="text" value={form.siglas || ''} onChange={function (e) { update('siglas', e.target.value); }} required maxLength={20} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Descripcion</label>
            <textarea value={form.descripcion || ''} onChange={function (e) { update('descripcion', e.target.value); }} rows={3} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">URL</label>
            <input type="url" value={form.url || ''} onChange={function (e) { update('url', e.target.value); }} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Aula</label>
            <input type="text" value={form.aula || ''} onChange={function (e) { update('aula', e.target.value); }} placeholder="Ej: Aula 1.1, Laboratorio 2, etc." className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Grado</label>
            <select 
              value={form.gradoId || ''} 
              onChange={function (e) { update('gradoId', Number(e.target.value)); }} 
              required 
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="" disabled>Selecciona un grado</option>
              {gradosList.map(function (g) {
                return <option key={g.id} value={g.id}>{g.nombre} ({g.cursoAcademico})</option>;
              })}
            </select>
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
        <h2 className="text-2xl font-bold">{asignatura ? asignatura.nombre : ''} <span className="text-xs text-indigo-300">[{asignatura?.siglas}]</span></h2>
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
      {tab === 'info' && isAdmin && (!asignatura || !asignatura.gradoId) && (
        <form onSubmit={handleSubmitAsignatura} className="space-y-4 rounded-xl bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900">Editar curso</h3>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
            <input type="text" value={form.nombre || ''} onChange={function (e) { update('nombre', e.target.value); }} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Siglas</label>
            <input type="text" value={form.siglas || ''} onChange={function (e) { update('siglas', e.target.value); }} required maxLength={20} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Descripcion</label>
            <textarea value={form.descripcion || ''} onChange={function (e) { update('descripcion', e.target.value); }} rows={3} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">URL</label>
            <input type="url" value={form.url || ''} onChange={function (e) { update('url', e.target.value); }} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Aula</label>
            <input type="text" value={form.aula || ''} onChange={function (e) { update('aula', e.target.value); }} className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Grado</label>
            <select 
              value={form.gradoId || ''} 
              onChange={function (e) { update('gradoId', Number(e.target.value)); }} 
              required 
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="" disabled>Selecciona un grado</option>
              {gradosList.map(function (g) {
                return <option key={g.id} value={g.id}>{g.nombre} ({g.cursoAcademico})</option>;
              })}
            </select>
          </div>
          <button type="submit" disabled={saving} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition">{saving ? 'Guardando...' : 'Guardar cambios'}</button>
        </form>
      )}
      {tab === 'info' && (!isAdmin || (asignatura && asignatura.gradoId)) && (
        <div className="space-y-4 rounded-xl bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900">Informacion del curso</h3>
          <p className="text-sm text-gray-600"><strong>Nombre:</strong> {asignatura ? asignatura.nombre : ''}</p>
          <p className="text-sm text-gray-600"><strong>Siglas:</strong> {asignatura ? asignatura.siglas : ''}</p>
          <p className="text-sm text-gray-600"><strong>Descripcion:</strong> {asignatura && asignatura.descripcion ? asignatura.descripcion : 'Sin descripcion'}</p>
          {asignatura && asignatura.aula && <p className="text-sm text-gray-600"><strong>Aula:</strong> {asignatura.aula}</p>}
          {asignatura && asignatura.url && <p className="text-sm text-gray-600"><strong>URL:</strong> <a href={asignatura.url} target="_blank" rel="noopener noreferrer" className="text-indigo-600">{asignatura.url}</a></p>}
          <p className="text-sm text-gray-600"><strong>Grado:</strong> {asignatura && asignatura.gradoNombre ? asignatura.gradoNombre : 'N/A'}</p>
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
                      {canManageContent && <button onClick={function () { anunciosApi.delete(a.id).then(loadAnuncios); }} className="text-xs text-red-500 hover:text-red-700">Eliminar</button>}
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
                      {canManageContent && <button onClick={function () { materialesApi.delete(m.id).then(loadMateriales); }} className="text-sm text-red-500 hover:text-red-700">Eliminar</button>}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ===== HORARIOS TAB (read-only for students) ===== */}
      {tab === 'horarios' && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Horarios de clase</h3>
            <button
              onClick={function () {
                var numId = Number(id);
                var endpoint = canManageContent
                  ? clasesApi.descargarHorarioCompletoPdf(numId)
                  : clasesApi.descargarHorarioPdf(numId);
                endpoint.then(function (response) {
                  var blob = new Blob([response.data], { type: 'application/pdf' });
                  var url = window.URL.createObjectURL(blob);
                  var a = document.createElement('a');
                  a.href = url;
                  a.download = 'horario_' + numId + '.pdf';
                  document.body.appendChild(a);
                  a.click();
                  window.URL.revokeObjectURL(url);
                  document.body.removeChild(a);
                }).catch(function () {
                  alert('Error al descargar el PDF de horario');
                });
              }}
              className="flex items-center gap-2 rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M12 10v6m0 0l-3-3m3 3l3-3M3 17v3a2 2 0 002 2h14a2 2 0 002-2v-3" /></svg>
              Descargar PDF
            </button>
          </div>
          {clases.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay clases programadas para esta asignatura</p>
          ) : (
            <div className="overflow-hidden rounded-xl bg-white shadow-sm">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Profesor</th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Aula</th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Hora inicio</th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Hora fin</th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">Estado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {clases.map(function (c) {
                    var estadoColor = 'bg-gray-100 text-gray-700';
                    if (c.estadoClase === 'ACEPTADA') estadoColor = 'bg-green-100 text-green-700';
                    if (c.estadoClase === 'SOLICITADA') estadoColor = 'bg-yellow-100 text-yellow-700';
                    if (c.estadoClase === 'RECHAZADA') estadoColor = 'bg-red-100 text-red-700';
                    if (c.estadoClase === 'COMPLETADA') estadoColor = 'bg-blue-100 text-blue-700';
                    return (
                      <tr key={c.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 text-sm font-medium text-gray-900">{c.profesorNombre}</td>
                        <td className="px-4 py-3 text-sm text-gray-500">{asignatura ? asignatura.aula : '-'}</td>
                        <td className="px-4 py-3 text-sm text-gray-500">{c.horaComienzo}</td>
                        <td className="px-4 py-3 text-sm text-gray-500">{c.horaFin}</td>
                        <td className="px-4 py-3"><span className={'rounded-full px-2 py-0.5 text-xs font-medium ' + estadoColor}>{c.estadoClase}</span></td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ===== TAREAS TAB (admin/profesor only) ===== */}
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
              <select value={tareaForm.tipoTarea} onChange={function (e) { setTareaForm(Object.assign({}, tareaForm, { tipoTarea: e.target.value })); }} className="rounded-lg border border-gray-300 px-3 py-2">
                <option value="TAREA">Tarea</option>
                <option value="EVALUACION">Evaluacion</option>
                <option value="SIMULACRO">Simulacro</option>
              </select>
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
            {isEstudiante && (
              <button onClick={function () { setShowEntregaForm(!showEntregaForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">
                {showEntregaForm ? 'Cancelar' : 'Enviar entrega'}
              </button>
            )}
          </div>

          {showEntregaForm && (
            <form onSubmit={handleSubmitEntrega} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <textarea value={entregaForm.contenido} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { contenido: e.target.value })); }} placeholder="Tu respuesta..." rows={4} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <div className="space-y-1">
                <label className="block text-xs font-medium text-gray-500">Archivo adjunto</label>
                <input 
                  type="file" 
                  onChange={function(e) { if(e.target.files) setSelectedFile(e.target.files[0]); }} 
                  className="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100" 
                />
              </div>
              <div className="text-xs text-gray-400 text-center">O bien, indica una URL:</div>
              <input type="text" value={entregaForm.urlAdjunto} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { urlAdjunto: e.target.value })); }} placeholder="URL adjunto (opcional)" className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Enviar entrega</button>
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
                        <span className="rounded-full bg-green-100 px-2.5 py-0.5 text-sm font-semibold text-green-800">
                          {en.calificacion}/{selectedTarea!.puntuacionMaxima}
                        </span>
                      ) : (
                        canManageContent && (
                          <button onClick={function () { handleCalificar(en.id); }} className="rounded bg-amber-50 px-2 py-1 text-xs text-amber-700 hover:bg-amber-100">
                            Calificar
                          </button>
                        )
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

      {/* ===== EVALUACIONES TAB (all users, read-only for students) ===== */}
      {tab === 'evaluaciones' && !selectedTarea && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Pruebas de evaluacion</h3>
          </div>
          {tareas.filter(function (t) { return t.tipoTarea === 'EVALUACION'; }).length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay pruebas de evaluacion</p>
          ) : (
            <div className="space-y-3">
              {tareas.filter(function (t) { return t.tipoTarea === 'EVALUACION'; }).map(function (t) {
                var isPast = new Date(t.fechaEntrega) < new Date();
                return (
                  <div key={t.id} className="rounded-xl bg-white p-5 shadow-sm cursor-pointer hover:shadow-md transition" onClick={function () { handleSelectTarea(t); }}>
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-semibold text-gray-900">[E] {t.titulo}</h4>
                        <p className="text-xs text-gray-400">Por: {t.creadorNombre}</p>
                      </div>
                      <div className="text-right">
                        <span className={'rounded-full px-2 py-0.5 text-xs font-medium ' + (isPast ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700')}>
                          {isPast ? 'Cerrada' : 'Abierta'}
                        </span>
                        <p className="mt-1 text-xs text-gray-400">Fecha: {new Date(t.fechaEntrega).toLocaleDateString('es-ES')}</p>
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

      {/* Evaluacion detail view (reuses selectedTarea) */}
      {tab === 'evaluaciones' && selectedTarea && (
        <div>
          <button onClick={function () { setSelectedTarea(null); }} className="mb-4 text-sm text-indigo-600 hover:text-indigo-500">&lt;- Volver a evaluaciones</button>
          <div className="mb-4 rounded-xl bg-white p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-gray-900">{selectedTarea.titulo}</h3>
            <p className="text-sm text-gray-500">{selectedTarea.descripcion}</p>
            <div className="mt-2 flex gap-4 text-xs text-gray-400">
              <span>Fecha limite: {new Date(selectedTarea.fechaEntrega).toLocaleString('es-ES')}</span>
              <span>Max: {selectedTarea.puntuacionMaxima} pts</span>
            </div>
          </div>

          {isEstudiante && (
            <div>
              <div className="mb-4 flex items-center justify-between">
                <h4 className="font-semibold text-gray-900">Tu entrega</h4>
                <button onClick={function () { setShowEntregaForm(!showEntregaForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showEntregaForm ? 'Cancelar' : 'Enviar entrega'}</button>
              </div>
              {showEntregaForm && (
                <form onSubmit={handleSubmitEntrega} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
                  <textarea value={entregaForm.contenido} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { contenido: e.target.value })); }} placeholder="Tu respuesta..." rows={4} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                  <input type="text" value={entregaForm.urlAdjunto} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { urlAdjunto: e.target.value })); }} placeholder="URL adjunto (opcional)" className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                  <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Enviar</button>
                </form>
              )}
            </div>
          )}

          {canManageContent && (
            <div>
              <h4 className="mb-3 font-semibold text-gray-900">Entregas ({entregas.length})</h4>
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
                            <span className="rounded-full bg-green-100 px-2.5 py-0.5 text-sm font-semibold text-green-800">{en.calificacion}/{selectedTarea!.puntuacionMaxima}</span>
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
        </div>
      )}

      {/* ===== SIMULACROS TAB ===== */}
      {tab === 'simulacros' && !selectedTarea && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900">Simulacros de examen</h3>
            <p className="text-xs text-gray-400">Estos tests no cuentan como examen oficial</p>
          </div>
          {tareas.filter(function (t) { return t.tipoTarea === 'SIMULACRO'; }).length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay simulacros disponibles</p>
          ) : (
            <div className="space-y-3">
              {tareas.filter(function (t) { return t.tipoTarea === 'SIMULACRO'; }).map(function (t) {
                var isPast = new Date(t.fechaEntrega) < new Date();
                return (
                  <div key={t.id} className="rounded-xl bg-white p-5 shadow-sm cursor-pointer hover:shadow-md transition border-l-4 border-purple-300" onClick={function () { handleSelectTarea(t); }}>
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-semibold text-gray-900">[S] {t.titulo}</h4>
                        <p className="text-xs text-gray-400">Por: {t.creadorNombre}</p>
                      </div>
                      <div className="text-right">
                        <span className={'rounded-full px-2 py-0.5 text-xs font-medium ' + (isPast ? 'bg-red-100 text-red-700' : 'bg-purple-100 text-purple-700')}>
                          {isPast ? 'Cerrado' : 'Disponible'}
                        </span>
                        <p className="mt-1 text-xs text-gray-400">Hasta: {new Date(t.fechaEntrega).toLocaleDateString('es-ES')}</p>
                      </div>
                    </div>
                    {t.descripcion && <p className="mt-2 text-sm text-gray-600">{t.descripcion}</p>}
                    <div className="mt-2 flex gap-3 text-xs text-gray-400">
                      <span>Max: {t.puntuacionMaxima} pts</span>
                      <span>{t.totalEntregas} intentos</span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Simulacro detail view */}
      {tab === 'simulacros' && selectedTarea && (
        <div>
          <button onClick={function () { setSelectedTarea(null); }} className="mb-4 text-sm text-purple-600 hover:text-purple-500">&lt;- Volver a simulacros</button>
          <div className="mb-4 rounded-xl bg-white p-5 shadow-sm border-l-4 border-purple-300">
            <h3 className="text-lg font-semibold text-gray-900">{selectedTarea.titulo}</h3>
            <p className="text-sm text-gray-500">{selectedTarea.descripcion}</p>
            <div className="mt-2 flex gap-4 text-xs text-gray-400">
              <span>Fecha limite: {new Date(selectedTarea.fechaEntrega).toLocaleString('es-ES')}</span>
              <span>Max: {selectedTarea.puntuacionMaxima} pts</span>
            </div>
            <p className="mt-2 text-xs text-purple-600 italic">Este simulacro no tiene impacto en la calificacion final</p>
          </div>

          <div className="mb-4 flex items-center justify-between">
            <h4 className="font-semibold text-gray-900">Entregas ({entregas.length})</h4>
            <button onClick={function () { setShowEntregaForm(!showEntregaForm); }} className="rounded-lg bg-purple-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-purple-700 transition">{showEntregaForm ? 'Cancelar' : 'Enviar intento'}</button>
          </div>

          {showEntregaForm && (
            <form onSubmit={handleSubmitEntrega} className="mb-4 space-y-3 rounded-xl bg-white p-5 shadow-sm">
              <textarea value={entregaForm.contenido} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { contenido: e.target.value })); }} placeholder="Tu respuesta..." rows={4} required className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-purple-500 focus:outline-none focus:ring-1 focus:ring-purple-500" />
              <input type="text" value={entregaForm.urlAdjunto} onChange={function (e) { setEntregaForm(Object.assign({}, entregaForm, { urlAdjunto: e.target.value })); }} placeholder="URL adjunto (opcional)" className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-purple-500 focus:outline-none focus:ring-1 focus:ring-purple-500" />
              <button type="submit" className="rounded-lg bg-purple-600 px-4 py-2 text-sm font-medium text-white hover:bg-purple-700 transition">Enviar</button>
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
                        <span className="rounded-full bg-purple-100 px-2.5 py-0.5 text-sm font-semibold text-purple-800">{en.calificacion}/{selectedTarea!.puntuacionMaxima}</span>
                      ) : canManageContent ? (
                        <button onClick={function () { handleCalificar(en.id); }} className="rounded bg-amber-50 px-2 py-1 text-xs text-amber-700 hover:bg-amber-100">Calificar</button>
                      ) : (
                        <span className="text-xs text-gray-400">Pendiente</span>
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

              <div className="flex items-center gap-2 px-1">
                <input 
                  type="checkbox" 
                  id="inscribible-check" 
                  checked={grupoForm.inscribible} 
                  onChange={function(e) { setGrupoForm(Object.assign({}, grupoForm, { inscribible: e.target.checked })); }} 
                  className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                />
                <label htmlFor="inscribible-check" className="text-sm text-gray-700">Libre acceso (estudiantes pueden apuntarse solos)</label>
              </div>
              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Crear grupo</button>
            </form>
          )}
          {grupos.length === 0 ? (
            <p className="rounded-xl bg-white p-8 text-center text-gray-500 shadow-sm">No hay grupos creados</p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2">
              {grupos.map(function (g) {
                var isMember = meId !== null && g.miembroIds && g.miembroIds.indexOf(meId) !== -1;
                return (
                  <div key={g.id} className={'rounded-xl bg-white p-5 shadow-sm' + (g.inscribible ? ' border-l-4 border-green-400' : '')}>
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-semibold text-gray-900">{g.nombre}</h4>
                        <span className={'mt-1 inline-block rounded-full px-2 py-0.5 text-xs font-medium ' + (g.tipo === 'TEORIA' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700')}>{g.tipo === 'TEORIA' ? 'Teoria' : 'Practica'}</span>
                        {g.inscribible && <span className="ml-1 inline-block rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-green-700">Libre acceso</span>}
                        {!g.inscribible && <span className="ml-1 inline-block rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-500">Restringido</span>}
                      </div>
                      <div className="flex flex-col items-end gap-1">
                        {canManageContent && (
                          <button onClick={function () { gruposApi.toggleInscribible(g.id).then(loadGrupos); }} className={'rounded px-2 py-1 text-xs transition ' + (g.inscribible ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-green-50 text-green-600 hover:bg-green-100')}>
                            {g.inscribible ? 'Cambiar a Restringido' : 'Cambiar a Libre acceso'}
                          </button>
                        )}
                        {canManageContent && <button onClick={function () { gruposApi.delete(g.id).then(loadGrupos); }} className="text-xs text-red-500 hover:text-red-700">Eliminar</button>}
                      </div>
                    </div>
                    <p className="mt-2 text-xs text-gray-400">{g.miembroIds ? g.miembroIds.length : 0} miembros</p>
                    {/* Student self-enrollment */}
                    {isEstudiante && g.inscribible && !isMember && (
                      <button onClick={function () { gruposApi.selfEnrol(g.id).then(loadGrupos).catch(function (err) { alert(err.response && err.response.data && err.response.data.message || 'Error al inscribirse'); }); }} className="mt-2 rounded-lg bg-green-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-green-700 transition">
                        Inscribirse
                      </button>
                    )}
                    {isEstudiante && isMember && (
                      <div className="mt-2 flex items-center gap-2">
                        <span className="rounded-full bg-indigo-100 px-2 py-0.5 text-xs font-medium text-indigo-700">Inscrito</span>
                        <button onClick={function () { gruposApi.selfUnenrol(g.id).then(loadGrupos); }} className="text-xs text-red-500 hover:text-red-700">Desinscribirse</button>
                      </div>
                    )}
                    {isEstudiante && !g.inscribible && !isMember && (
                      <p className="mt-2 text-xs text-gray-400 italic">Acceso restringido: solo un profesor puede asignarte a este grupo</p>
                    )}
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

      {/* ===== ASISTENCIA TAB ===== */}
      {tab === 'asistencia' && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Control de asistencia</h3>
            {canManageContent && (
              <div className="flex items-center gap-2">
                <input type="date" value={asistenciaFecha} onChange={function (e) { setAsistenciaFecha(e.target.value); }} className="rounded-lg border border-gray-300 px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none dark:bg-gray-700 dark:border-gray-600 dark:text-white" />
              </div>
            )}
          </div>

          {canManageContent && asignatura && asignatura.estudianteIds && asignatura.estudianteIds.length > 0 ? (
            <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm overflow-hidden">
              <table className="w-full">
                <thead className="bg-gray-50 dark:bg-gray-700">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300">Estudiante</th>
                    <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-300">Estado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                  {asignatura.estudianteIds.map(function (estId) {
                    var registro = asistenciaList.find(function (a) { return a.estudianteId === estId && a.fecha === asistenciaFecha; });
                    var estadoActual = registro ? registro.estado : '';
                    return (
                      <tr key={estId} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td className="px-4 py-3 text-sm font-medium text-gray-900 dark:text-white">{registro ? registro.estudianteNombre : 'Estudiante #' + estId}</td>
                        <td className="px-4 py-3">
                          <div className="flex justify-center gap-1">
                            {['PRESENTE', 'AUSENTE', 'TARDANZA', 'JUSTIFICADO'].map(function (est) {
                              var colors: Record<string, string> = { PRESENTE: 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300', AUSENTE: 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300', TARDANZA: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-300', JUSTIFICADO: 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300' };
                              var labels: Record<string, string> = { PRESENTE: 'P', AUSENTE: 'A', TARDANZA: 'T', JUSTIFICADO: 'J' };
                              return (
                                <button key={est} onClick={function () { handleRegistrarAsistencia(estId, est); }} className={'rounded-md px-2 py-1 text-xs font-medium transition ' + (estadoActual === est ? colors[est] + ' ring-2 ring-indigo-500' : 'bg-gray-100 text-gray-400 hover:bg-gray-200 dark:bg-gray-600 dark:text-gray-400')} title={est}>{labels[est]}</button>
                              );
                            })}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : isEstudiante ? (
            <div>
              <div className="grid grid-cols-4 gap-3 mb-6">
                {(function () {
                  var miAsistencia = asistenciaList.filter(function (a) { return a.estudianteId === meId; });
                  var total = miAsistencia.length;
                  var presentes = miAsistencia.filter(function (a) { return a.estado === 'PRESENTE'; }).length;
                  var ausentes = miAsistencia.filter(function (a) { return a.estado === 'AUSENTE'; }).length;
                  var tardanzas = miAsistencia.filter(function (a) { return a.estado === 'TARDANZA'; }).length;
                  return [
                    { label: 'Total clases', value: total, color: 'bg-indigo-100 text-indigo-700' },
                    { label: 'Presente', value: presentes, color: 'bg-green-100 text-green-700' },
                    { label: 'Ausente', value: ausentes, color: 'bg-red-100 text-red-700' },
                    { label: 'Tardanza', value: tardanzas, color: 'bg-yellow-100 text-yellow-700' },
                  ].map(function (s) {
                    return (
                      <div key={s.label} className="rounded-xl bg-white dark:bg-gray-800 p-4 shadow-sm text-center">
                        <p className={'text-2xl font-bold ' + s.color}>{s.value}</p>
                        <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{s.label}</p>
                      </div>
                    );
                  });
                })()}
              </div>
              {asistenciaList.filter(function (a) { return a.estudianteId === meId; }).length === 0 ? (
                <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 shadow-sm">No hay registros de asistencia</p>
              ) : (
                <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm overflow-hidden">
                  <table className="w-full">
                    <thead className="bg-gray-50 dark:bg-gray-700">
                      <tr>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300">Fecha</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300">Estado</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300">Observacion</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                      {asistenciaList.filter(function (a) { return a.estudianteId === meId; }).map(function (a) {
                        var estadoColors: Record<string, string> = { PRESENTE: 'bg-green-100 text-green-700', AUSENTE: 'bg-red-100 text-red-700', TARDANZA: 'bg-yellow-100 text-yellow-700', JUSTIFICADO: 'bg-blue-100 text-blue-700' };
                        return (
                          <tr key={a.id}>
                            <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">{a.fecha}</td>
                            <td className="px-4 py-3"><span className={'rounded-full px-2 py-0.5 text-xs font-medium ' + (estadoColors[a.estado] || '')}>{a.estado}</span></td>
                            <td className="px-4 py-3 text-sm text-gray-500 dark:text-gray-400">{a.observacion || '-'}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          ) : (
            <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 shadow-sm">No hay datos de asistencia</p>
          )}
        </div>
      )}

      {/* ===== PROGRESO TAB ===== */}
      {tab === 'progreso' && (
        <div>
          <h3 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">Seguimiento de progreso</h3>
          {progreso ? (
            <div className="space-y-6">
              {/* Overall progress bar */}
              <div className="rounded-xl bg-white dark:bg-gray-800 p-6 shadow-sm">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Progreso general</span>
                  <span className="text-sm font-bold text-indigo-600">{progreso.porcentajeProgreso.toFixed(0)}%</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-4">
                  <div className="bg-indigo-600 h-4 rounded-full transition-all" style={{ width: progreso.porcentajeProgreso + '%' }}></div>
                </div>
              </div>

              {/* Stats grid */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {[
                  { label: 'Tareas totales', value: progreso.totalTareas, icon: '📝' },
                  { label: 'Entregadas', value: progreso.tareasEntregadas, icon: '✅' },
                  { label: 'Calificadas', value: progreso.tareasCalificadas, icon: '📊' },
                  { label: 'Promedio', value: progreso.promedioCalificaciones.toFixed(1), icon: '⭐' },
                ].map(function (s) {
                  return (
                    <div key={s.label} className="rounded-xl bg-white dark:bg-gray-800 p-4 shadow-sm text-center">
                      <p className="text-2xl mb-1">{s.icon}</p>
                      <p className="text-xl font-bold text-gray-900 dark:text-white">{s.value}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{s.label}</p>
                    </div>
                  );
                })}
              </div>

              {/* Attendance stats */}
              <div className="rounded-xl bg-white dark:bg-gray-800 p-6 shadow-sm">
                <h4 className="text-sm font-semibold text-gray-900 dark:text-white mb-3">Asistencia</h4>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600 dark:text-gray-400">{progreso.clasesAsistidas} de {progreso.totalClases} clases</span>
                  <span className="text-sm font-bold text-green-600">{progreso.porcentajeAsistencia.toFixed(0)}%</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-3">
                  <div className={'h-3 rounded-full transition-all ' + (progreso.porcentajeAsistencia >= 80 ? 'bg-green-500' : progreso.porcentajeAsistencia >= 60 ? 'bg-yellow-500' : 'bg-red-500')} style={{ width: progreso.porcentajeAsistencia + '%' }}></div>
                </div>
              </div>
            </div>
          ) : (
            <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 shadow-sm">Cargando datos de progreso...</p>
          )}
        </div>
      )}

      {/* ===== RUBRICAS TAB ===== */}
      {tab === 'rubricas' && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Rubricas de evaluacion</h3>
            {canManageContent && <button onClick={function () { setShowRubricaForm(!showRubricaForm); }} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">{showRubricaForm ? 'Cancelar' : '+ Nueva rubrica'}</button>}
          </div>

          {showRubricaForm && canManageContent && (
            <form onSubmit={handleCreateRubrica} className="mb-6 space-y-4 rounded-xl bg-white dark:bg-gray-800 p-6 shadow-sm">
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Nombre</label>
                <input type="text" value={rubricaForm.nombre} onChange={function (e) { setRubricaForm(Object.assign({}, rubricaForm, { nombre: e.target.value })); }} required className="w-full rounded-lg border border-gray-300 dark:border-gray-600 px-3 py-2 dark:bg-gray-700 dark:text-white focus:border-indigo-500 focus:outline-none" />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Descripcion</label>
                <textarea value={rubricaForm.descripcion} onChange={function (e) { setRubricaForm(Object.assign({}, rubricaForm, { descripcion: e.target.value })); }} rows={2} className="w-full rounded-lg border border-gray-300 dark:border-gray-600 px-3 py-2 dark:bg-gray-700 dark:text-white focus:border-indigo-500 focus:outline-none" />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Tarea asociada</label>
                <select value={rubricaForm.tareaId} onChange={function (e) { setRubricaForm(Object.assign({}, rubricaForm, { tareaId: Number(e.target.value) })); }} required className="w-full rounded-lg border border-gray-300 dark:border-gray-600 px-3 py-2 dark:bg-gray-700 dark:text-white focus:border-indigo-500 focus:outline-none">
                  <option value={0} disabled>Seleccionar tarea...</option>
                  {tareas.map(function (t) { return <option key={t.id} value={t.id}>{t.titulo}</option>; })}
                </select>
              </div>

              {/* Criterios */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">Criterios</label>
                  <button type="button" onClick={addCriterio} className="rounded-lg bg-teal-600 px-2 py-1 text-xs font-medium text-white hover:bg-teal-700 transition">+ Criterio</button>
                </div>
                {rubricaForm.criterios.map(function (c, idx) {
                  return (
                    <div key={idx} className="mb-4 rounded-lg border border-gray-200 dark:border-gray-600 p-4 space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-semibold text-gray-700 dark:text-gray-300">Criterio {idx + 1}</span>
                        <button type="button" onClick={function () { removeCriterio(idx); }} className="text-xs text-red-500 hover:text-red-700">Eliminar</button>
                      </div>
                      <input type="text" value={c.nombre} onChange={function (e) { updateCriterio(idx, 'nombre', e.target.value); }} placeholder="Nombre del criterio" className="w-full rounded-lg border border-gray-300 dark:border-gray-600 px-3 py-1.5 text-sm dark:bg-gray-700 dark:text-white" />
                      <input type="number" value={c.puntuacionMaxima} onChange={function (e) { updateCriterio(idx, 'puntuacionMaxima', Number(e.target.value)); }} placeholder="Puntuacion maxima" className="w-32 rounded-lg border border-gray-300 dark:border-gray-600 px-3 py-1.5 text-sm dark:bg-gray-700 dark:text-white" />
                      <div className="grid grid-cols-2 gap-2">
                        <input type="text" value={c.nivelExcelente} onChange={function (e) { updateCriterio(idx, 'nivelExcelente', e.target.value); }} placeholder="Nivel excelente" className="rounded-lg border border-gray-300 dark:border-gray-600 px-2 py-1 text-xs dark:bg-gray-700 dark:text-white" />
                        <input type="text" value={c.nivelBueno} onChange={function (e) { updateCriterio(idx, 'nivelBueno', e.target.value); }} placeholder="Nivel bueno" className="rounded-lg border border-gray-300 dark:border-gray-600 px-2 py-1 text-xs dark:bg-gray-700 dark:text-white" />
                        <input type="text" value={c.nivelSuficiente} onChange={function (e) { updateCriterio(idx, 'nivelSuficiente', e.target.value); }} placeholder="Nivel suficiente" className="rounded-lg border border-gray-300 dark:border-gray-600 px-2 py-1 text-xs dark:bg-gray-700 dark:text-white" />
                        <input type="text" value={c.nivelInsuficiente} onChange={function (e) { updateCriterio(idx, 'nivelInsuficiente', e.target.value); }} placeholder="Nivel insuficiente" className="rounded-lg border border-gray-300 dark:border-gray-600 px-2 py-1 text-xs dark:bg-gray-700 dark:text-white" />
                      </div>
                    </div>
                  );
                })}
              </div>

              <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition">Crear rubrica</button>
            </form>
          )}

          {rubricasList.length === 0 ? (
            <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 shadow-sm">No hay rubricas definidas</p>
          ) : (
            <div className="space-y-4">
              {rubricasList.map(function (r) {
                return (
                  <div key={r.id} className="rounded-xl bg-white dark:bg-gray-800 p-5 shadow-sm">
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <h4 className="font-semibold text-gray-900 dark:text-white">{r.nombre}</h4>
                        {r.tareaTitulo && <p className="text-xs text-indigo-500 mt-0.5">Tarea: {r.tareaTitulo}</p>}
                        {r.descripcion && <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">{r.descripcion}</p>}
                      </div>
                      {canManageContent && <button onClick={function () { if (confirm('¿Eliminar rubrica?')) rubricasApi.eliminar(r.id).then(function () { loadRubricas(Number(id)); }); }} className="text-xs text-red-500 hover:text-red-700">Eliminar</button>}
                    </div>
                    {r.criterios && r.criterios.length > 0 && (
                      <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                          <thead>
                            <tr className="border-b border-gray-200 dark:border-gray-700">
                              <th className="py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Criterio</th>
                              <th className="py-2 text-center text-xs font-medium text-gray-500 dark:text-gray-400">Max</th>
                              <th className="py-2 text-center text-xs font-medium text-green-600">Excelente</th>
                              <th className="py-2 text-center text-xs font-medium text-blue-600">Bueno</th>
                              <th className="py-2 text-center text-xs font-medium text-yellow-600">Suficiente</th>
                              <th className="py-2 text-center text-xs font-medium text-red-600">Insuficiente</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                            {r.criterios.map(function (c) {
                              return (
                                <tr key={c.id}>
                                  <td className="py-2 font-medium text-gray-900 dark:text-white">{c.nombre}</td>
                                  <td className="py-2 text-center text-gray-600 dark:text-gray-400">{c.puntuacionMaxima}</td>
                                  <td className="py-2 text-center text-xs text-green-700 dark:text-green-400">{c.nivelExcelente || '-'}</td>
                                  <td className="py-2 text-center text-xs text-blue-700 dark:text-blue-400">{c.nivelBueno || '-'}</td>
                                  <td className="py-2 text-center text-xs text-yellow-700 dark:text-yellow-400">{c.nivelSuficiente || '-'}</td>
                                  <td className="py-2 text-center text-xs text-red-700 dark:text-red-400">{c.nivelInsuficiente || '-'}</td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    )}
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
