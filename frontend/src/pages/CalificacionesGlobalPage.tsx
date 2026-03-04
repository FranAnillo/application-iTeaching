import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { progresoApi, entregasApi, asignaturasApi } from '../api/endpoints';
import type { Progreso, Entrega, Asignatura } from '../types';

export default function CalificacionesGlobalPage() {
  var auth = useAuth();
  var user = auth.user;

  var progresoState = useState<Progreso[]>([]);
  var progreso = progresoState[0];
  var setProgreso = progresoState[1];

  var entregasState = useState<Entrega[]>([]);
  var entregas = entregasState[0];
  var setEntregas = entregasState[1];

  var loadingState = useState(true);
  var loading = loadingState[0];
  var setLoading = loadingState[1];

  useEffect(function () {
    Promise.all([
      progresoApi.getGlobal().catch(function () { return { data: [] }; }),
      entregasApi.getMisEntregas().catch(function () { return { data: [] }; }),
    ]).then(function (results) {
      setProgreso(results[0].data);
      setEntregas(results[1].data);
    }).finally(function () { setLoading(false); });
  }, []);

  // Calculate global averages
  var totalCalificadas = 0;
  var sumaCalificaciones = 0;
  progreso.forEach(function (p) {
    if (p.tareasCalificadas > 0) {
      totalCalificadas += p.tareasCalificadas;
      sumaCalificaciones += p.promedioCalificaciones * p.tareasCalificadas;
    }
  });
  var promedioGlobal = totalCalificadas > 0 ? Math.round(sumaCalificaciones / totalCalificadas * 100) / 100 : 0;

  // Recent graded submissions
  var calificadas = entregas
    .filter(function (e) { return e.calificacion !== null && e.calificacion !== undefined; })
    .sort(function (a, b) { return a.fechaEntrega > b.fechaEntrega ? -1 : 1; });

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Libro de Calificaciones</h2>
        <p className="text-gray-500 dark:text-gray-400">Resumen global de tus calificaciones en todos los cursos</p>
      </div>

      {/* Summary cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8">
        <div className="rounded-xl bg-white dark:bg-gray-800 p-5 shadow-sm">
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Promedio Global</p>
          <p className="mt-1 text-3xl font-bold text-indigo-600 dark:text-indigo-400">{promedioGlobal}</p>
        </div>
        <div className="rounded-xl bg-white dark:bg-gray-800 p-5 shadow-sm">
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Cursos Inscritos</p>
          <p className="mt-1 text-3xl font-bold text-gray-900 dark:text-white">{progreso.length}</p>
        </div>
        <div className="rounded-xl bg-white dark:bg-gray-800 p-5 shadow-sm">
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Entregas Totales</p>
          <p className="mt-1 text-3xl font-bold text-gray-900 dark:text-white">{entregas.length}</p>
        </div>
        <div className="rounded-xl bg-white dark:bg-gray-800 p-5 shadow-sm">
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Calificadas</p>
          <p className="mt-1 text-3xl font-bold text-green-600 dark:text-green-400">{totalCalificadas}</p>
        </div>
      </div>

      {/* Per-course progress */}
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Progreso por Curso</h3>
      {progreso.length === 0 ? (
        <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 dark:text-gray-400 shadow-sm">
          No estás inscrito en ningún curso
        </p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 mb-8">
          {progreso.map(function (p) {
            var color = p.promedioCalificaciones >= 7 ? 'text-green-600 dark:text-green-400' :
                        p.promedioCalificaciones >= 5 ? 'text-yellow-600 dark:text-yellow-400' :
                        p.promedioCalificaciones > 0 ? 'text-red-600 dark:text-red-400' : 'text-gray-400';
            return (
              <Link key={p.asignaturaId} to={'/asignaturas/' + p.asignaturaId} className="rounded-xl bg-white dark:bg-gray-800 shadow-sm hover:shadow-md transition p-5">
                <h4 className="font-semibold text-gray-900 dark:text-white mb-3">{p.asignaturaNombre}</h4>

                {/* Progress bar */}
                <div className="mb-3">
                  <div className="flex justify-between text-xs text-gray-500 dark:text-gray-400 mb-1">
                    <span>Progreso</span>
                    <span>{Math.round(p.porcentajeProgreso)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                    <div className="bg-indigo-500 h-2 rounded-full transition-all" style={{ width: p.porcentajeProgreso + '%' }}></div>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-2 text-sm">
                  <div>
                    <span className="text-gray-500 dark:text-gray-400">Promedio: </span>
                    <span className={'font-bold ' + color}>{p.promedioCalificaciones || '-'}</span>
                  </div>
                  <div>
                    <span className="text-gray-500 dark:text-gray-400">Entregas: </span>
                    <span className="font-medium text-gray-900 dark:text-white">{p.tareasEntregadas}/{p.totalTareas}</span>
                  </div>
                  <div>
                    <span className="text-gray-500 dark:text-gray-400">Asistencia: </span>
                    <span className="font-medium text-gray-900 dark:text-white">{Math.round(p.porcentajeAsistencia)}%</span>
                  </div>
                  <div>
                    <span className="text-gray-500 dark:text-gray-400">Calificadas: </span>
                    <span className="font-medium text-gray-900 dark:text-white">{p.tareasCalificadas}</span>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      )}

      {/* Recent grades */}
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Calificaciones Recientes</h3>
      {calificadas.length === 0 ? (
        <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 dark:text-gray-400 shadow-sm">
          Aún no tienes calificaciones
        </p>
      ) : (
        <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 dark:bg-gray-700">
                <th className="text-left px-4 py-3 font-medium text-gray-500 dark:text-gray-300">Tarea</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500 dark:text-gray-300">Fecha</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500 dark:text-gray-300">Calificación</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500 dark:text-gray-300">Comentario</th>
              </tr>
            </thead>
            <tbody>
              {calificadas.slice(0, 20).map(function (e) {
                var noteColor = (e.calificacion || 0) >= 7 ? 'text-green-600 dark:text-green-400' :
                                (e.calificacion || 0) >= 5 ? 'text-yellow-600 dark:text-yellow-400' : 'text-red-600 dark:text-red-400';
                return (
                  <tr key={e.id} className="border-t border-gray-100 dark:border-gray-700">
                    <td className="px-4 py-3 font-medium text-gray-900 dark:text-white">{e.tareaTitulo}</td>
                    <td className="px-4 py-3 text-gray-500 dark:text-gray-400">{e.fechaEntrega ? e.fechaEntrega.substring(0, 10) : ''}</td>
                    <td className={'px-4 py-3 font-bold ' + noteColor}>{e.calificacion}</td>
                    <td className="px-4 py-3 text-gray-500 dark:text-gray-400 truncate max-w-xs">{e.comentarioProfesor || '-'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
