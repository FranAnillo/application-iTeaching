import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { tareasApi, clasesApi, anunciosApi, asignaturasApi } from '../api/endpoints';
import type { Tarea, Clase, Anuncio, Asignatura } from '../types';

interface CalendarEvent {
  id: string;
  title: string;
  date: string;
  type: string;
  color: string;
  asignatura: string;
}

export default function CalendarioPage() {
  var auth = useAuth();
  var user = auth.user;

  var eventsState = useState<CalendarEvent[]>([]);
  var events = eventsState[0];
  var setEvents = eventsState[1];

  var loadingState = useState(true);
  var loading = loadingState[0];
  var setLoading = loadingState[1];

  var currentDateState = useState(new Date());
  var currentDate = currentDateState[0];
  var setCurrentDate = currentDateState[1];

  var selectedDateState = useState<string | null>(null);
  var selectedDate = selectedDateState[0];
  var setSelectedDate = selectedDateState[1];

  useEffect(function () {
    loadEvents();
  }, []);

  function loadEvents() {
    setLoading(true);
    asignaturasApi.getAll().then(function (res) {
      var asignaturas = res.data;
      var allPromises: Promise<any>[] = [];
      var allEvents: CalendarEvent[] = [];

      asignaturas.forEach(function (asig: Asignatura) {
        allPromises.push(
          tareasApi.getByAsignatura(asig.id).then(function (r) {
            r.data.forEach(function (tarea: Tarea) {
              var dateStr = tarea.fechaEntrega ? tarea.fechaEntrega.substring(0, 10) : '';
              if (dateStr) {
                var typeLabel = tarea.tipoTarea === 'EVALUACION' ? 'Evaluación' :
                                tarea.tipoTarea === 'SIMULACRO' ? 'Simulacro' : 'Tarea';
                var color = tarea.tipoTarea === 'EVALUACION' ? 'bg-red-500' :
                            tarea.tipoTarea === 'SIMULACRO' ? 'bg-yellow-500' : 'bg-blue-500';
                allEvents.push({
                  id: 'tarea-' + tarea.id,
                  title: typeLabel + ': ' + tarea.titulo,
                  date: dateStr,
                  type: tarea.tipoTarea,
                  color: color,
                  asignatura: asig.nombre,
                });
              }
            });
          }).catch(function () {})
        );

        allPromises.push(
          anunciosApi.getByAsignatura(asig.id).then(function (r) {
            r.data.forEach(function (anuncio: Anuncio) {
              var dateStr = anuncio.fechaCreacion ? anuncio.fechaCreacion.substring(0, 10) : '';
              if (dateStr) {
                allEvents.push({
                  id: 'anuncio-' + anuncio.id,
                  title: '📢 ' + anuncio.titulo,
                  date: dateStr,
                  type: 'ANUNCIO',
                  color: anuncio.importante ? 'bg-orange-500' : 'bg-green-500',
                  asignatura: asig.nombre,
                });
              }
            });
          }).catch(function () {})
        );
      });

      Promise.all(allPromises).then(function () {
        setEvents(allEvents);
      }).finally(function () {
        setLoading(false);
      });
    }).catch(function () { setLoading(false); });
  }

  function getDaysInMonth(year: number, month: number) {
    return new Date(year, month + 1, 0).getDate();
  }

  function getFirstDayOfMonth(year: number, month: number) {
    var day = new Date(year, month, 1).getDay();
    return day === 0 ? 6 : day - 1; // Monday = 0
  }

  var year = currentDate.getFullYear();
  var month = currentDate.getMonth();
  var daysInMonth = getDaysInMonth(year, month);
  var firstDay = getFirstDayOfMonth(year, month);
  var monthNames = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
  var dayNames = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom'];

  function prevMonth() {
    setCurrentDate(new Date(year, month - 1, 1));
  }

  function nextMonth() {
    setCurrentDate(new Date(year, month + 1, 1));
  }

  function getEventsForDay(day: number) {
    var dateStr = year + '-' + String(month + 1).padStart(2, '0') + '-' + String(day).padStart(2, '0');
    return events.filter(function (e) { return e.date === dateStr; });
  }

  var today = new Date();
  var todayStr = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0');

  var selectedEvents = selectedDate ? events.filter(function (e) { return e.date === selectedDate; }) : [];

  var cells: (number | null)[] = [];
  for (var i = 0; i < firstDay; i++) cells.push(null);
  for (var d = 1; d <= daysInMonth; d++) cells.push(d);
  while (cells.length % 7 !== 0) cells.push(null);

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Calendario</h2>
        <p className="text-gray-500 dark:text-gray-400">Deadlines de tareas, evaluaciones y anuncios</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Calendar grid */}
          <div className="lg:col-span-2">
            <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm p-4">
              {/* Header */}
              <div className="flex items-center justify-between mb-4">
                <button onClick={prevMonth} className="rounded-lg p-2 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300">
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
                </button>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                  {monthNames[month]} {year}
                </h3>
                <button onClick={nextMonth} className="rounded-lg p-2 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300">
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
                </button>
              </div>

              {/* Day names */}
              <div className="grid grid-cols-7 mb-1">
                {dayNames.map(function (dn) {
                  return <div key={dn} className="text-center text-xs font-medium text-gray-500 dark:text-gray-400 py-2">{dn}</div>;
                })}
              </div>

              {/* Days grid */}
              <div className="grid grid-cols-7">
                {cells.map(function (day, idx) {
                  if (day === null) {
                    return <div key={'empty-' + idx} className="p-1 min-h-[80px]" />;
                  }
                  var dateStr = year + '-' + String(month + 1).padStart(2, '0') + '-' + String(day).padStart(2, '0');
                  var dayEvents = getEventsForDay(day);
                  var isToday = dateStr === todayStr;
                  var isSelected = dateStr === selectedDate;

                  return (
                    <div
                      key={'day-' + day}
                      onClick={function () { setSelectedDate(dateStr); }}
                      className={'p-1 min-h-[80px] border border-gray-100 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50 transition ' +
                        (isSelected ? 'bg-indigo-50 dark:bg-indigo-900/20 ring-2 ring-indigo-500' : '') +
                        (isToday ? ' ring-1 ring-indigo-400' : '')}
                    >
                      <div className={'text-sm font-medium mb-1 ' + (isToday ? 'text-indigo-600 dark:text-indigo-400 font-bold' : 'text-gray-700 dark:text-gray-300')}>
                        {day}
                      </div>
                      {dayEvents.slice(0, 3).map(function (ev) {
                        return (
                          <div key={ev.id} className={'text-xs px-1 py-0.5 mb-0.5 rounded text-white truncate ' + ev.color}>
                            {ev.title}
                          </div>
                        );
                      })}
                      {dayEvents.length > 3 && (
                        <div className="text-xs text-gray-500 dark:text-gray-400">+{dayEvents.length - 3} más</div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Sidebar - selected day details + legend */}
          <div className="space-y-4">
            {/* Legend */}
            <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm p-4">
              <h4 className="font-semibold text-gray-900 dark:text-white mb-3">Leyenda</h4>
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-blue-500"></span><span className="text-gray-600 dark:text-gray-400">Tarea</span></div>
                <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-red-500"></span><span className="text-gray-600 dark:text-gray-400">Evaluación</span></div>
                <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-yellow-500"></span><span className="text-gray-600 dark:text-gray-400">Simulacro</span></div>
                <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-green-500"></span><span className="text-gray-600 dark:text-gray-400">Anuncio</span></div>
                <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-orange-500"></span><span className="text-gray-600 dark:text-gray-400">Anuncio importante</span></div>
              </div>
            </div>

            {/* Selected day events */}
            <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm p-4">
              <h4 className="font-semibold text-gray-900 dark:text-white mb-3">
                {selectedDate ? 'Eventos del ' + selectedDate : 'Selecciona un día'}
              </h4>
              {selectedDate && selectedEvents.length === 0 && (
                <p className="text-sm text-gray-500 dark:text-gray-400">No hay eventos este día</p>
              )}
              <div className="space-y-2">
                {selectedEvents.map(function (ev) {
                  return (
                    <div key={ev.id} className="rounded-lg border border-gray-200 dark:border-gray-700 p-3">
                      <div className={'inline-block text-xs px-2 py-0.5 rounded text-white mb-1 ' + ev.color}>
                        {ev.type}
                      </div>
                      <p className="text-sm font-medium text-gray-900 dark:text-white">{ev.title}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">{ev.asignatura}</p>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Upcoming events */}
            <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm p-4">
              <h4 className="font-semibold text-gray-900 dark:text-white mb-3">Próximos eventos</h4>
              <div className="space-y-2">
                {events
                  .filter(function (e) { return e.date >= todayStr; })
                  .sort(function (a, b) { return a.date < b.date ? -1 : 1; })
                  .slice(0, 5)
                  .map(function (ev) {
                    return (
                      <div key={ev.id} className="flex items-start gap-2">
                        <span className={'w-2 h-2 rounded-full mt-1.5 flex-shrink-0 ' + ev.color}></span>
                        <div>
                          <p className="text-sm font-medium text-gray-900 dark:text-white">{ev.title}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">{ev.date} · {ev.asignatura}</p>
                        </div>
                      </div>
                    );
                  })}
                {events.filter(function (e) { return e.date >= todayStr; }).length === 0 && (
                  <p className="text-sm text-gray-500 dark:text-gray-400">No hay eventos próximos</p>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
