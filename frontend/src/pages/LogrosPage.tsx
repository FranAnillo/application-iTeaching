import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { logrosApi } from '../api/endpoints';
import type { Logro } from '../types';

export default function LogrosPage() {
  var auth = useAuth();
  var user = auth.user;

  var logrosState = useState<Logro[]>([]);
  var logros = logrosState[0];
  var setLogros = logrosState[1];

  var loadingState = useState(true);
  var loading = loadingState[0];
  var setLoading = loadingState[1];

  var filterState = useState('TODOS');
  var filter = filterState[0];
  var setFilter = filterState[1];

  useEffect(function () {
    logrosApi.getAll().then(function (res) {
      setLogros(res.data);
    }).catch(function () {}).finally(function () { setLoading(false); });
  }, []);

  var obtenidos = logros.filter(function (l) { return l.obtenido; }).length;
  var total = logros.length;
  var porcentaje = total > 0 ? Math.round(obtenidos * 100 / total) : 0;

  var categorias = ['TODOS', 'ACADEMICO', 'SOCIAL', 'ASISTENCIA', 'ESPECIAL'];
  var categoriaNombres: Record<string, string> = {
    TODOS: 'Todos',
    ACADEMICO: 'Académicos',
    SOCIAL: 'Sociales',
    ASISTENCIA: 'Asistencia',
    ESPECIAL: 'Especiales',
  };

  var filtered = filter === 'TODOS' ? logros : logros.filter(function (l) { return l.categoria === filter; });

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
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Logros e Insignias</h2>
        <p className="text-gray-500 dark:text-gray-400">Desbloquea logros completando actividades en la plataforma</p>
      </div>

      {/* Progress summary */}
      <div className="rounded-xl bg-gradient-to-r from-indigo-500 to-purple-600 p-6 mb-8 text-white">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold">Tu Progreso de Logros</h3>
            <p className="text-indigo-100 mt-1">{obtenidos} de {total} logros desbloqueados</p>
          </div>
          <div className="text-4xl font-bold">{porcentaje}%</div>
        </div>
        <div className="mt-4 w-full bg-indigo-300/30 rounded-full h-3">
          <div className="bg-white h-3 rounded-full transition-all" style={{ width: porcentaje + '%' }}></div>
        </div>
      </div>

      {/* Category filter */}
      <div className="flex flex-wrap gap-2 mb-6">
        {categorias.map(function (cat) {
          var isActive = filter === cat;
          return (
            <button
              key={cat}
              onClick={function () { setFilter(cat); }}
              className={'rounded-lg px-4 py-2 text-sm font-medium transition ' +
                (isActive
                  ? 'bg-indigo-600 text-white'
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 border border-gray-200 dark:border-gray-700')}
            >
              {categoriaNombres[cat]}
            </button>
          );
        })}
      </div>

      {/* Logros grid */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {filtered.map(function (logro) {
          return (
            <div
              key={logro.id}
              className={'rounded-xl shadow-sm p-5 transition ' +
                (logro.obtenido
                  ? 'bg-white dark:bg-gray-800 ring-2 ring-indigo-500'
                  : 'bg-gray-100 dark:bg-gray-800/50 opacity-60')}
            >
              <div className="flex items-start gap-3">
                <span className="text-3xl">{logro.icono}</span>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <h4 className="font-semibold text-gray-900 dark:text-white">{logro.nombre}</h4>
                    {logro.obtenido && (
                      <span className="bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 text-xs px-2 py-0.5 rounded-full font-medium">
                        ✓ Desbloqueado
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">{logro.descripcion}</p>
                  <div className="mt-2 flex items-center gap-2">
                    <span className={'text-xs px-2 py-0.5 rounded-full ' +
                      (logro.categoria === 'ACADEMICO' ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400' :
                       logro.categoria === 'SOCIAL' ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400' :
                       logro.categoria === 'ASISTENCIA' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' :
                       'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400')}>
                      {categoriaNombres[logro.categoria] || logro.categoria}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {filtered.length === 0 && (
        <p className="text-center text-gray-500 dark:text-gray-400 py-8">No hay logros en esta categoría</p>
      )}
    </div>
  );
}
