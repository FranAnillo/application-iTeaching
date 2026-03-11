import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { mensajesApi, usuariosApi } from '../api/endpoints';
import type { Mensaje, Usuario } from '../types';

export default function MensajesPage() {
  var auth = useAuth();
  var user = auth.user;

  var mensajesState = useState<Mensaje[]>([]);
  var mensajes = mensajesState[0];
  var setMensajes = mensajesState[1];

  var usuariosState = useState<Usuario[]>([]);
  var usuarios = usuariosState[0];
  var setUsuarios = usuariosState[1];

  var selectedUserState = useState<Usuario | null>(null);
  var selectedUser = selectedUserState[0];
  var setSelectedUser = selectedUserState[1];

  var conversacionState = useState<Mensaje[]>([]);
  var conversacion = conversacionState[0];
  var setConversacion = conversacionState[1];

  var nuevoMensajeState = useState('');
  var nuevoMensaje = nuevoMensajeState[0];
  var setNuevoMensaje = nuevoMensajeState[1];

  var loadingState = useState(true);
  var loading = loadingState[0];
  var setLoading = loadingState[1];

  var searchState = useState('');
  var search = searchState[0];
  var setSearch = searchState[1];

  var meState = useState<Usuario | null>(null);
  var me = meState[0];
  var setMe = meState[1];

  useEffect(function () {
    Promise.all([
      mensajesApi.getAll(),
      usuariosApi.getAll(),
      usuariosApi.me(),
    ]).then(function (results) {
      setMensajes(results[0].data);
      setUsuarios(results[1].data);
      setMe(results[2].data);
    }).catch(function () { }).finally(function () { setLoading(false); });
  }, []);

  function selectUser(u: Usuario) {
    setSelectedUser(u);
    if (me) {
      mensajesApi.getConversacion(u.id).then(function (res) {
        setConversacion(res.data);
        mensajesApi.marcarLeidos(u.id).catch(function () { });
      }).catch(function () { });
    }
  }

  function enviar() {
    if (!nuevoMensaje.trim() || !selectedUser || !me) return;
    mensajesApi.enviar({
      contenido: nuevoMensaje,
      destinatarioId: selectedUser.id,
    }).then(function (res) {
      setConversacion(conversacion.concat([res.data]));
      setNuevoMensaje('');
    }).catch(function () { });
  }

  // Build contact list from messages
  function getContacts() {
    if (!me) return [];
    var contactMap: Record<number, { user: Usuario; lastMessage: Mensaje; unread: number }> = {};

    mensajes.forEach(function (m) {
      var contactId = m.remitenteId === me!.id ? m.destinatarioId : m.remitenteId;
      var contactName = m.remitenteId === me!.id ? m.destinatarioNombre : m.remitenteNombre;

      if (!contactMap[contactId]) {
        var usr = usuarios.find(function (u) { return u.id === contactId; });
        contactMap[contactId] = {
          user: usr || { id: contactId, username: '', nombre: contactName, apellido: '', email: '', telefono: '', role: '', puntuacion: 0, avatar: null },
          lastMessage: m,
          unread: 0,
        };
      }
      if (!m.leido && m.destinatarioId === me!.id) {
        contactMap[contactId].unread++;
      }
    });

    return Object.values(contactMap).sort(function (a, b) {
      return a.lastMessage.fechaEnvio > b.lastMessage.fechaEnvio ? -1 : 1;
    });
  }

  var contacts = getContacts();
  var filteredUsuarios = usuarios.filter(function (u) {
    if (!me || u.id === me.id) return false;
    if (!search) return false;
    var full = (u.nombre + ' ' + u.apellido + ' ' + u.username).toLowerCase();
    return full.indexOf(search.toLowerCase()) >= 0;
  });

  function formatTime(dateStr: string) {
    var d = new Date(dateStr);
    var hours = String(d.getHours()).padStart(2, '0');
    var mins = String(d.getMinutes()).padStart(2, '0');
    return hours + ':' + mins;
  }

  function formatDate(dateStr: string) {
    return dateStr.substring(0, 10);
  }

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
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Mensajes</h2>
        <p className="text-gray-500 dark:text-gray-400">Mensajería interna entre miembros de la Asignatura</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4" style={{ height: 'calc(100vh - 220px)' }}>
        {/* Contacts sidebar */}
        <div className="rounded-xl bg-white dark:bg-gray-800 shadow-sm flex flex-col overflow-hidden">
          <div className="p-3 border-b border-gray-200 dark:border-gray-700">
            <input
              type="text"
              placeholder="Buscar usuario..."
              value={search}
              onChange={function (e) { setSearch(e.target.value); }}
              className="w-full rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 px-3 py-2 text-sm text-gray-900 dark:text-white placeholder-gray-400"
            />
          </div>

          {/* Search results */}
          {search && filteredUsuarios.length > 0 && (
            <div className="border-b border-gray-200 dark:border-gray-700 max-h-32 overflow-y-auto">
              {filteredUsuarios.slice(0, 5).map(function (u) {
                return (
                  <div
                    key={'search-' + u.id}
                    onClick={function () { selectUser(u); setSearch(''); }}
                    className="px-4 py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 text-sm"
                  >
                    <span className="font-medium text-gray-900 dark:text-white">{u.nombre} {u.apellido}</span>
                    <span className="text-gray-500 dark:text-gray-400 ml-2">@{u.username}</span>
                  </div>
                );
              })}
            </div>
          )}

          {/* Contact list */}
          <div className="flex-1 overflow-y-auto">
            {contacts.length === 0 && (
              <p className="text-center text-sm text-gray-500 dark:text-gray-400 py-8">
                Busca un usuario para iniciar una conversación
              </p>
            )}
            {contacts.map(function (c) {
              var isActive = selectedUser && selectedUser.id === c.user.id;
              return (
                <div
                  key={c.user.id}
                  onClick={function () { selectUser(c.user); }}
                  className={'px-4 py-3 cursor-pointer border-b border-gray-100 dark:border-gray-700 transition ' +
                    (isActive ? 'bg-indigo-50 dark:bg-indigo-900/20' : 'hover:bg-gray-50 dark:hover:bg-gray-700/50')}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-sm text-gray-900 dark:text-white">
                      {c.user.nombre} {c.user.apellido}
                    </span>
                    {c.unread > 0 && (
                      <span className="bg-indigo-600 text-white text-xs rounded-full px-2 py-0.5">{c.unread}</span>
                    )}
                  </div>
                  <p className="text-xs text-gray-500 dark:text-gray-400 truncate mt-0.5">
                    {c.lastMessage.contenido}
                  </p>
                  <p className="text-xs text-gray-400 dark:text-gray-500 mt-0.5">
                    {formatDate(c.lastMessage.fechaEnvio)}
                  </p>
                </div>
              );
            })}
          </div>
        </div>

        {/* Conversation */}
        <div className="lg:col-span-2 rounded-xl bg-white dark:bg-gray-800 shadow-sm flex flex-col overflow-hidden">
          {!selectedUser ? (
            <div className="flex-1 flex items-center justify-center">
              <div className="text-center">
                <div className="text-4xl mb-2">💬</div>
                <p className="text-gray-500 dark:text-gray-400">Selecciona una conversación o busca un usuario</p>
              </div>
            </div>
          ) : (
            <>
              {/* Chat header */}
              <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-indigo-500 flex items-center justify-center text-white text-sm font-bold">
                  {selectedUser.nombre ? selectedUser.nombre.charAt(0) : '?'}
                </div>
                <div>
                  <h4 className="font-semibold text-gray-900 dark:text-white text-sm">
                    {selectedUser.nombre} {selectedUser.apellido}
                  </h4>
                  <p className="text-xs text-gray-500 dark:text-gray-400">@{selectedUser.username}</p>
                </div>
              </div>

              {/* Messages */}
              <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {conversacion.length === 0 && (
                  <p className="text-center text-sm text-gray-500 dark:text-gray-400 py-8">
                    No hay mensajes aún. ¡Envía el primero!
                  </p>
                )}
                {conversacion.map(function (m) {
                  var isMine = me && m.remitenteId === me.id;
                  return (
                    <div key={m.id} className={'flex ' + (isMine ? 'justify-end' : 'justify-start')}>
                      <div className={'max-w-xs lg:max-w-md px-4 py-2 rounded-2xl text-sm ' +
                        (isMine
                          ? 'bg-indigo-600 text-white rounded-br-sm'
                          : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white rounded-bl-sm')
                      }>
                        <p>{m.contenido}</p>
                        <p className={'text-xs mt-1 ' + (isMine ? 'text-indigo-200' : 'text-gray-400 dark:text-gray-500')}>
                          {formatTime(m.fechaEnvio)}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Input */}
              <div className="p-3 border-t border-gray-200 dark:border-gray-700 flex gap-2">
                <input
                  type="text"
                  placeholder="Escribe un mensaje..."
                  value={nuevoMensaje}
                  onChange={function (e) { setNuevoMensaje(e.target.value); }}
                  onKeyDown={function (e) { if (e.key === 'Enter') enviar(); }}
                  className="flex-1 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 px-3 py-2 text-sm text-gray-900 dark:text-white placeholder-gray-400"
                />
                <button
                  onClick={enviar}
                  disabled={!nuevoMensaje.trim()}
                  className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition"
                >
                  Enviar
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
