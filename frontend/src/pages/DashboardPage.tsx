import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { asignaturasApi, clasesApi, usuariosApi, materialesApi, tareasApi, anunciosApi, progresoApi } from '../api/endpoints';
import type { Asignatura, Tarea, Anuncio, Progreso } from '../types';

interface Stats {
  asignaturas: number;
  usuarios: number;
  clases: number;
  materiales: number;
}

export default function DashboardPage() {
  var auth = useAuth();
  var user = auth.user;
  var statsState = useState<Stats>({ asignaturas: 0, usuarios: 0, clases: 0, materiales: 0 });
  var stats = statsState[0];
  var setStats = statsState[1];
  var loadingState = useState(true);
  var loading = loadingState[0];
  var setLoading = loadingState[1];
  var cursosState = useState<Asignatura[]>([]);
  var cursos = cursosState[0];
  var setCursos = cursosState[1];

  var upcomingState = useState<{ titulo: string; fecha: string; tipo: string; asignatura: string }[]>([]);
  var upcoming = upcomingState[0];
  var setUpcoming = upcomingState[1];

  var recentAnunciosState = useState<Anuncio[]>([]);
  var recentAnuncios = recentAnunciosState[0];
  var setRecentAnuncios = recentAnunciosState[1];

  var progresoState = useState<Progreso[]>([]);
  var progresoList = progresoState[0];
  var setProgresoList = progresoState[1];

  useEffect(function () {
    Promise.all([
      asignaturasApi.getAll(),
      usuariosApi.getAll(),
      clasesApi.getAll(),
      materialesApi.getAll(),
    ]).then(function (results) {
      var allCursos = results[0].data;
      setStats({
        asignaturas: allCursos.length,
        usuarios: results[1].data.length,
        clases: results[2].data.length,
        materiales: results[3].data.length,
      });
      setCursos(allCursos);

      // Load upcoming deadlines & announcements per course
      var deadlines: { titulo: string; fecha: string; tipo: string; asignatura: string }[] = [];
      var anuncios: Anuncio[] = [];
      var tareaPromises: Promise<any>[] = [];

      allCursos.forEach(function (asig: Asignatura) {
        tareaPromises.push(
          tareasApi.getByAsignatura(asig.id).then(function (r) {
            var now = new Date().toISOString();
            r.data.forEach(function (t: Tarea) {
              if (t.fechaEntrega && t.fechaEntrega >= now.substring(0, 10)) {
                deadlines.push({
                  titulo: t.titulo,
                  fecha: t.fechaEntrega.substring(0, 10),
                  tipo: t.tipoTarea,
                  asignatura: asig.nombre,
                });
              }
            });
          }).catch(function () { })
        );
        tareaPromises.push(
          anunciosApi.getByAsignatura(asig.id).then(function (r) {
            anuncios = anuncios.concat(r.data);
          }).catch(function () { })
        );
      });

      Promise.all(tareaPromises).then(function () {
        deadlines.sort(function (a, b) { return a.fecha < b.fecha ? -1 : 1; });
        setUpcoming(deadlines.slice(0, 5));
        anuncios.sort(function (a, b) { return a.fechaCreacion > b.fechaCreacion ? -1 : 1; });
        setRecentAnuncios(anuncios.slice(0, 5));
      });

      // Load progress
      progresoApi.getGlobal().then(function (r) {
        setProgresoList(r.data);
      }).catch(function () { });

    }).catch(function () { }).finally(function () { setLoading(false); });
  }, []);

  var cards = [
    { label: 'Cursos', value: stats.asignaturas, to: '/asignaturas', color: 'bg-blue-500' },
    { label: 'Usuarios', value: stats.usuarios, to: '/usuarios', color: 'bg-green-500' },
    { label: 'Clases', value: stats.clases, to: '/clases', color: 'bg-purple-500' },
    { label: 'Materiales', value: stats.materiales, to: '/materiales', color: 'bg-teal-500' },
  ];

  return (
    <div>
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
          Hola, {user ? user.username : ''}!
        </h2>
        <p className="text-gray-500 dark:text-gray-400">Bienvenido al Aula Virtual iTeaching 2.0</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : (
        <div>
          {/* Stats cards */}
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8">
            {cards.map(function (card) {
              return (
                <Link
                  key={card.label}
                  to={card.to}
                  className="rounded-xl bg-white dark:bg-gray-800 p-5 shadow-sm hover:shadow-md transition"
                >
                  <p className="text-sm font-medium text-gray-500 dark:text-gray-400">{card.label}</p>
                  <p className="mt-1 text-3xl font-bold text-gray-900 dark:text-white">{card.value}</p>
                </Link>
              );
            })}
          </div>

          {/* Mis Cursos */}
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Mis Asignaturas</h3>
            {user && user.role === 'ROLE_ADMIN' && (
              <Link to="/asignaturas/new" className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">+ Nueva asignatura</Link>
            )}
          </div>
          {cursos.length === 0 ? (
            <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 dark:text-gray-400 shadow-sm">No hay asignaturas disponibles. Crea tu primera asignatura.</p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {cursos.map(function (c) {
                return (
                  <Link key={c.id} to={'/asignaturas/' + c.id} className="rounded-xl bg-white dark:bg-gray-800 shadow-sm hover:shadow-md transition overflow-hidden">
                    <div className="bg-gradient-to-r from-indigo-500 to-purple-500 p-4">
                      <h4 className="font-bold text-white text-lg">{c.nombre}</h4>
                      {c.creadorNombre && <p className="text-indigo-100 text-sm">{c.creadorNombre}</p>}
                    </div>
                    <div className="p-4">
                      <p className="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">{c.descripcion || 'Sin descripcion'}</p>
                      <div className="mt-3 flex items-center justify-between text-xs text-gray-400 dark:text-gray-500">
                        <span>{c.estudianteIds ? c.estudianteIds.length : 0} estudiantes</span>
                        <span className="text-indigo-600 font-medium">Entrar &gt;</span>
                      </div>
                    </div>
                  </Link>
                );
              })}
            </div>
          )}

          {/* Enhanced sections row */}
          <div className="grid gap-4 lg:grid-cols-2 mt-8">
            {/* Upcoming deadlines */}
            <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm p-5">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">📅 Próximos Plazos</h3>
                <Link to="/calendario" className="text-sm text-indigo-600 dark:text-indigo-400 hover:underline">Ver calendario</Link>
              </div>
              {upcoming.length === 0 ? (
                <p className="text-sm text-gray-500 dark:text-gray-400">No hay plazos próximos</p>
              ) : (
                <div className="space-y-3">
                  {upcoming.map(function (item, idx) {
                    var typeColor = item.tipo === 'EVALUACION' ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400' :
                      item.tipo === 'SIMULACRO' ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400' :
                        'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400';
                    return (
                      <div key={idx} className="flex items-center gap-3">
                        <span className={'text-xs px-2 py-0.5 rounded-full font-medium ' + typeColor}>
                          {item.tipo}
                        </span>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{item.titulo}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">{item.asignatura}</p>
                        </div>
                        <span className="text-xs text-gray-500 dark:text-gray-400 flex-shrink-0">{item.fecha}</span>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Recent announcements */}
            <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm p-5">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">📢 Anuncios Recientes</h3>
              {recentAnuncios.length === 0 ? (
                <p className="text-sm text-gray-500 dark:text-gray-400">No hay anuncios recientes</p>
              ) : (
                <div className="space-y-3">
                  {recentAnuncios.map(function (a) {
                    return (
                      <Link key={a.id} to={'/asignaturas/' + a.asignaturaId} className="block">
                        <div className="flex items-start gap-2">
                          {a.importante && <span className="text-orange-500 text-xs mt-0.5">⚠️</span>}
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{a.titulo}</p>
                            <p className="text-xs text-gray-500 dark:text-gray-400">{a.asignaturaNombre} · {a.fechaCreacion ? a.fechaCreacion.substring(0, 10) : ''}</p>
                          </div>
                        </div>
                      </Link>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* Progress summary */}
          {progresoList.length > 0 && (
            <div className="mt-8">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">📊 Mi Progreso</h3>
                <Link to="/calificaciones" className="text-sm text-indigo-600 dark:text-indigo-400 hover:underline">Ver calificaciones</Link>
              </div>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {progresoList.map(function (p) {
                  return (
                    <div key={p.asignaturaId} className="rounded-xl bg-white dark:bg-gray-800 shadow-sm p-4">
                      <h4 className="font-medium text-gray-900 dark:text-white text-sm mb-2 truncate">{p.asignaturaNombre}</h4>
                      <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2 mb-2">
                        <div className="bg-indigo-500 h-2 rounded-full" style={{ width: p.porcentajeProgreso + '%' }}></div>
                      </div>
                      <div className="flex justify-between text-xs text-gray-500 dark:text-gray-400">
                        <span>{Math.round(p.porcentajeProgreso)}% completado</span>
                        <span>Promedio: {p.promedioCalificaciones || '-'}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
