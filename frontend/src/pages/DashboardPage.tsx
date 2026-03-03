import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { asignaturasApi, clasesApi, usuariosApi, materialesApi } from '../api/endpoints';
import type { Asignatura } from '../types';

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
    }).catch(function () {}).finally(function () { setLoading(false); });
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
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Mis Cursos</h3>
            {user && user.role === 'ROLE_ADMIN' && (
              <Link to="/asignaturas/new" className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 transition">+ Nuevo curso</Link>
            )}
          </div>
          {cursos.length === 0 ? (
            <p className="rounded-xl bg-white dark:bg-gray-800 p-8 text-center text-gray-500 dark:text-gray-400 shadow-sm">No hay cursos disponibles. Crea tu primer curso.</p>
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
        </div>
      )}
    </div>
  );
}
