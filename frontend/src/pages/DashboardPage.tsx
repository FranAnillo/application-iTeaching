import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { asignaturasApi, clasesApi, estudiantesApi, profesoresApi } from '../api/endpoints';

interface Stats {
  asignaturas: number;
  estudiantes: number;
  profesores: number;
  clases: number;
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState<Stats>({ asignaturas: 0, estudiantes: 0, profesores: 0, clases: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [a, e, p, c] = await Promise.all([
          asignaturasApi.getAll(),
          estudiantesApi.getAll(),
          profesoresApi.getAll(),
          clasesApi.getAll(),
        ]);
        setStats({
          asignaturas: a.data.length,
          estudiantes: e.data.length,
          profesores: p.data.length,
          clases: c.data.length,
        });
      } catch {
        // silently fail
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const cards = [
    { label: 'Asignaturas', value: stats.asignaturas, to: '/asignaturas', color: 'bg-blue-500', icon: '📚' },
    { label: 'Profesores', value: stats.profesores, to: '/profesores', color: 'bg-green-500', icon: '👨‍🏫' },
    { label: 'Estudiantes', value: stats.estudiantes, to: '/estudiantes', color: 'bg-amber-500', icon: '🎓' },
    { label: 'Clases', value: stats.clases, to: '/clases', color: 'bg-purple-500', icon: '📅' },
  ];

  return (
    <div>
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-gray-900">
          ¡Hola, {user?.username}!
        </h2>
        <p className="text-gray-500">Bienvenido al panel de iTeaching</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {cards.map((card) => (
            <Link
              key={card.label}
              to={card.to}
              className="rounded-xl bg-white p-6 shadow-sm hover:shadow-md transition"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">{card.label}</p>
                  <p className="mt-1 text-3xl font-bold text-gray-900">{card.value}</p>
                </div>
                <div className={`flex h-12 w-12 items-center justify-center rounded-lg ${card.color} text-2xl text-white`}>
                  {card.icon}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
