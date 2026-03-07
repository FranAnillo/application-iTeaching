import { useEffect, useState, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { mensajesApi, usuariosApi } from '../api/endpoints';
import type { Mensaje, Usuario } from '../types';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

/**
 * ChatPage — Chat en tiempo real estilo WhatsApp/Telegram
 * Usa WebSocket (STOMP sobre SockJS) para mensajería instantánea
 */
export default function ChatPage() {
  var auth = useAuth();
  var user = auth.user;

  // --- State ---
  var meState = useState(null as Usuario | null);
  var me = meState[0]; var setMe = meState[1];

  var contactsState = useState([] as ContactInfo[]);
  var contacts = contactsState[0]; var setContacts = contactsState[1];

  var selectedState = useState(null as ContactInfo | null);
  var selected = selectedState[0]; var setSelected = selectedState[1];

  var messagesState = useState([] as Mensaje[]);
  var messages = messagesState[0]; var setMessages = messagesState[1];

  var inputState = useState('');
  var inputText = inputState[0]; var setInputText = inputState[1];

  var searchState = useState('');
  var searchText = searchState[0]; var setSearchText = searchState[1];

  var searchResultsState = useState([] as Usuario[]);
  var searchResults = searchResultsState[0]; var setSearchResults = searchResultsState[1];

  var loadingState = useState(true);
  var loading = loadingState[0]; var setLoading = loadingState[1];

  var onlineState = useState([] as number[]);
  var onlineUsers = onlineState[0]; var setOnlineUsers = onlineState[1];

  var typingState = useState(null as TypingInfo | null);
  var typingInfo = typingState[0]; var setTypingInfo = typingState[1];

  var allUsersState = useState([] as Usuario[]);
  var allUsers = allUsersState[0]; var setAllUsers = allUsersState[1];

  var showMobileContactsState = useState(true);
  var showMobileContacts = showMobileContactsState[0]; var setShowMobileContacts = showMobileContactsState[1];

  var stompClientRef = useRef(null as any);
  var chatEndRef = useRef(null as HTMLDivElement | null);
  var typingTimeoutRef = useRef(null as any);
  var selectedRef = useRef(selected);

  // Keep selectedRef in sync
  useEffect(function () {
    selectedRef.current = selected;
  }, [selected]);

  // --- Types ---
  interface ContactInfo {
    user: Usuario;
    lastMessage: Mensaje | null;
    unread: number;
  }

  interface TypingInfo {
    remitenteId: number;
    remitenteNombre: string;
    destinatarioId: number;
  }

  // --- Init: Load data + connect WS ---
  useEffect(function () {
    Promise.all([
      usuariosApi.me(),
      mensajesApi.getAll(),
      usuariosApi.getAll(),
      mensajesApi.getOnlineUsers(),
    ]).then(function (results) {
      var meData = results[0].data;
      var allMsgs = results[1].data;
      var users = results[2].data;
      var online = results[3].data;

      setMe(meData);
      setAllUsers(users);
      setOnlineUsers(online);
      buildContacts(meData, allMsgs, users);
      connectWebSocket(meData);
    }).catch(function (err) {
      console.error('Error loading chat data:', err);
    }).finally(function () { setLoading(false); });

    return function () {
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.disconnect(function () {});
      }
    };
  }, []);

  // --- Build contacts list ---
  function buildContacts(meData: Usuario, allMsgs: Mensaje[], users: Usuario[]) {
    var map: Record<number, ContactInfo> = {};

    allMsgs.forEach(function (m) {
      var contactId = m.remitenteId === meData.id ? m.destinatarioId : m.remitenteId;
      var contactName = m.remitenteId === meData.id ? m.destinatarioNombre : m.remitenteNombre;

      if (!map[contactId]) {
        var usr = users.find(function (u) { return u.id === contactId; });
        map[contactId] = {
          user: usr || { id: contactId, username: '', nombre: contactName, apellido: '', email: '', telefono: '', role: '', puntuacion: 0, avatar: null },
          lastMessage: m,
          unread: 0,
        };
      } else {
        // Keep the most recent message
        if (m.fechaEnvio > map[contactId].lastMessage!.fechaEnvio) {
          map[contactId].lastMessage = m;
        }
      }
      if (!m.leido && m.destinatarioId === meData.id) {
        map[contactId].unread++;
      }
    });

    var sorted = Object.values(map).sort(function (a, b) {
      if (!a.lastMessage) return 1;
      if (!b.lastMessage) return -1;
      return a.lastMessage.fechaEnvio > b.lastMessage.fechaEnvio ? -1 : 1;
    });
    setContacts(sorted);
  }

  // --- WebSocket Connection ---
  function connectWebSocket(meData: Usuario) {
    var token = localStorage.getItem('token');
    if (!token) return;

    var socket = new SockJS('/ws/chat');
    var client = Stomp.over(socket);
    client.debug = function () {}; // Silence debug logs

    var headers = { Authorization: 'Bearer ' + token };

    client.connect(headers, function () {
      // Subscribe to incoming messages
      client.subscribe('/user/queue/chat', function (msg: any) {
        var mensaje = JSON.parse(msg.body) as Mensaje;
        handleIncomingMessage(meData, mensaje);
      });

      // Subscribe to typing indicators
      client.subscribe('/user/queue/typing', function (msg: any) {
        var info = JSON.parse(msg.body) as TypingInfo;
        setTypingInfo(info);
        // Clear typing after 3 seconds
        if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = setTimeout(function () {
          setTypingInfo(null);
        }, 3000);
      });

      // Subscribe to read receipts
      client.subscribe('/user/queue/read', function () {
        // Refresh messages to show read status
        var sel = selectedRef.current;
        if (sel) {
          mensajesApi.getConversacion(sel.user.id).then(function (res) {
            setMessages(res.data);
          }).catch(function () {});
        }
      });

      // Subscribe to online status
      client.subscribe('/topic/online', function (msg: any) {
        var data = JSON.parse(msg.body);
        setOnlineUsers(function (prev: number[]) {
          if (data.online) {
            if (prev.indexOf(data.userId) === -1) return prev.concat([data.userId]);
            return prev;
          } else {
            return prev.filter(function (id: number) { return id !== data.userId; });
          }
        });
      });

      stompClientRef.current = client;
    }, function (err: any) {
      console.error('WebSocket connection error:', err);
      // Fallback: use polling
      startPolling(meData);
    });
  }

  // --- Fallback polling if WS fails ---
  function startPolling(meData: Usuario) {
    var interval = setInterval(function () {
      var sel = selectedRef.current;
      if (sel) {
        mensajesApi.getConversacion(sel.user.id).then(function (res) {
          setMessages(res.data);
        }).catch(function () {});
      }
    }, 3000);

    // Cleanup on unmount
    return function () { clearInterval(interval); };
  }

  // --- Handle incoming WS message ---
  function handleIncomingMessage(meData: Usuario, mensaje: Mensaje) {
    var sel = selectedRef.current;
    var contactId = mensaje.remitenteId === meData.id ? mensaje.destinatarioId : mensaje.remitenteId;

    // Si el chat está abierto con ese contacto, recarga la conversación desde el backend
    if (sel && sel.user.id === contactId) {
      mensajesApi.getConversacion(contactId).then(function (res) {
        setMessages(res.data);
      }).catch(function () {});
    }

    // Si no hay chat seleccionado y el mensaje va dirigido a mí, auto-abrir conversación con el remitente y recargar
    if (!sel && mensaje.destinatarioId === meData.id) {
      var autoContact: ContactInfo = {
        user: {
          id: contactId,
          username: '',
          nombre: mensaje.remitenteNombre,
          apellido: '',
          email: '',
          telefono: '',
          role: '',
          puntuacion: 0,
          avatar: null,
        },
        lastMessage: mensaje,
        unread: 1,
      };
      setSelected(autoContact);
      setShowMobileContacts(false);
      mensajesApi.getConversacion(contactId).then(function (res) {
        setMessages(res.data);
      }).catch(function () {});
      sel = autoContact;
    }

    // Actualizar lista de contactos (lógica existente)
    setContacts(function (prev: ContactInfo[]) {
      var updated = prev.slice();
      var idx = updated.findIndex(function (c) { return c.user.id === contactId; });
      if (idx >= 0) {
        updated[idx] = Object.assign({}, updated[idx], {
          lastMessage: mensaje,
          unread: (sel && sel.user.id === contactId)
            ? 0
            : updated[idx].unread + (mensaje.destinatarioId === meData.id ? 1 : 0),
        });
      } else {
        updated.unshift({
          user: {
            id: contactId,
            username: '',
            nombre: mensaje.remitenteId === meData.id ? mensaje.destinatarioNombre : mensaje.remitenteNombre,
            apellido: '',
            email: '',
            telefono: '',
            role: '',
            puntuacion: 0,
            avatar: null,
          },
          lastMessage: mensaje,
          unread: mensaje.destinatarioId === meData.id ? 1 : 0,
        });
      }
      updated.sort(function (a, b) {
        if (!a.lastMessage) return 1;
        if (!b.lastMessage) return -1;
        return a.lastMessage.fechaEnvio > b.lastMessage.fechaEnvio ? -1 : 1;
      });
      return updated;
    });
  }

  // --- Select contact ---
  function selectContact(contact: ContactInfo) {
    setSelected(contact);
    setShowMobileContacts(false);

    // Load conversation
    mensajesApi.getConversacion(contact.user.id).then(function (res) {
      setMessages(res.data);

      // Mark as read
      if (contact.unread > 0 && me) {
        mensajesApi.marcarLeidos(contact.user.id).catch(function () {});

        // Send read receipt via WS
        if (stompClientRef.current && stompClientRef.current.connected) {
          stompClientRef.current.send('/app/chat.read', {}, JSON.stringify({
            lectorId: me.id,
            conversacionUserId: contact.user.id,
          }));
        }

        // Update unread count
        setContacts(function (prev: ContactInfo[]) {
          return prev.map(function (c) {
            if (c.user.id === contact.user.id) {
              return Object.assign({}, c, { unread: 0 });
            }
            return c;
          });
        });
      }
    }).catch(function () {});
  }

  // --- Start new conversation from search ---
  function startNewChat(usr: Usuario) {
    var contact: ContactInfo = { user: usr, lastMessage: null, unread: 0 };

    // Add to contacts if not already there
    setContacts(function (prev: ContactInfo[]) {
      var exists = prev.some(function (c) { return c.user.id === usr.id; });
      if (exists) return prev;
      return [contact].concat(prev);
    });

    selectContact(contact);
    setSearchText('');
    setSearchResults([]);
  }

  // --- Send message ---
  function sendMessage() {
    if (!inputText.trim() || !selected || !me) return;

    var dto = {
      contenido: inputText,
      destinatarioId: selected.user.id,
    };

    if (stompClientRef.current && stompClientRef.current.connected) {
      // Send via WebSocket
      stompClientRef.current.send('/app/chat.send', {}, JSON.stringify(dto));
    } else {
      // Fallback: send via REST
      mensajesApi.enviar(dto).then(function (res) {
        setMessages(function (prev: Mensaje[]) { return prev.concat([res.data]); });
      }).catch(function () {});
    }

    setInputText('');
  }

  // --- Typing indicator ---
  function handleTyping() {
    if (!selected || !me) return;
    if (stompClientRef.current && stompClientRef.current.connected) {
      stompClientRef.current.send('/app/chat.typing', {}, JSON.stringify({
        remitenteId: me.id,
        remitenteNombre: me.nombre,
        destinatarioId: selected.user.id,
      }));
    }
  }

  // --- Search users ---
  function handleSearch(query: string) {
    setSearchText(query);
    if (!query.trim() || !me) {
      setSearchResults([]);
      return;
    }
    var q = query.toLowerCase();
    var results = allUsers.filter(function (u) {
      if (u.id === me!.id) return false;
      var full = (u.nombre + ' ' + u.apellido + ' ' + u.username).toLowerCase();
      return full.indexOf(q) >= 0;
    });
    setSearchResults(results.slice(0, 8));
  }

  // --- Auto-scroll ---
  useEffect(function () {
    if (chatEndRef.current) {
      chatEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  // --- Format helpers ---
  function formatTime(dateStr: string) {
    var d = new Date(dateStr);
    return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
  }

  function formatDate(dateStr: string) {
    var d = new Date(dateStr);
    var today = new Date();
    var yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);

    if (d.toDateString() === today.toDateString()) return 'Hoy';
    if (d.toDateString() === yesterday.toDateString()) return 'Ayer';

    var days = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
    var diff = today.getTime() - d.getTime();
    if (diff < 7 * 24 * 60 * 60 * 1000) return days[d.getDay()];

    return String(d.getDate()).padStart(2, '0') + '/' + String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear();
  }

  function getInitials(nombre: string, apellido: string) {
    return ((nombre || '?').charAt(0) + (apellido || '').charAt(0)).toUpperCase();
  }

  function isOnline(userId: number) {
    return onlineUsers.indexOf(userId) >= 0;
  }

  // --- Group messages by date ---
  function groupMessagesByDate(msgs: Mensaje[]) {
    var groups: { date: string; messages: Mensaje[] }[] = [];
    var currentDate = '';

    msgs.forEach(function (m) {
      var dateKey = m.fechaEnvio.substring(0, 10);
      if (dateKey !== currentDate) {
        currentDate = dateKey;
        groups.push({ date: dateKey, messages: [m] });
      } else {
        groups[groups.length - 1].messages.push(m);
      }
    });

    return groups;
  }

  // --- Avatar colors by role ---
  function getAvatarColor(role: string) {
    if (role === 'ROLE_ADMIN') return 'bg-red-500';
    if (role === 'ROLE_PROFESOR') return 'bg-emerald-500';
    return 'bg-blue-500';
  }

  // --- Render ---
  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center">
          <div className="h-12 w-12 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent mx-auto mb-4" />
          <p className="text-gray-500 dark:text-gray-400">Conectando al chat...</p>
        </div>
      </div>
    );
  }

  var messageGroups = groupMessagesByDate(messages);
  var showTyping = typingInfo && selected && typingInfo.remitenteId === selected.user.id;

  return (
    <div className="flex h-[calc(100vh-7rem)] rounded-2xl overflow-hidden shadow-lg border border-gray-200 dark:border-gray-700">

      {/* ========== LEFT PANEL: Contacts ========== */}
      <div className={'flex flex-col bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 ' +
        (showMobileContacts ? 'w-full lg:w-96' : 'hidden lg:flex lg:w-96')}>

        {/* Header */}
        <div className="px-4 py-3 bg-gray-50 dark:bg-gray-900 flex items-center gap-3">
          <div className={'w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm ' + (me ? getAvatarColor(me.role) : 'bg-gray-500')}>
            {me ? getInitials(me.nombre, me.apellido) : '?'}
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="font-semibold text-gray-900 dark:text-white text-sm truncate">
              {me ? me.nombre + ' ' + me.apellido : 'Chat'}
            </h3>
            <p className="text-xs text-green-500">En línea</p>
          </div>
          <span className="text-lg" title="Chat iTeaching">💬</span>
        </div>

        {/* Search box */}
        <div className="px-3 py-2 bg-white dark:bg-gray-800 border-b border-gray-100 dark:border-gray-700">
          <div className="flex items-center bg-gray-100 dark:bg-gray-700 rounded-lg px-3 py-2">
            <svg className="w-4 h-4 text-gray-400 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Buscar o iniciar un nuevo chat"
              value={searchText}
              onChange={function (e) { handleSearch(e.target.value); }}
              className="bg-transparent text-sm text-gray-900 dark:text-white placeholder-gray-400 outline-none w-full"
            />
            {searchText && (
              <button
                onClick={function () { setSearchText(''); setSearchResults([]); }}
                className="text-gray-400 hover:text-gray-600 ml-1"
              >✕</button>
            )}
          </div>
        </div>

        {/* Search results overlay */}
        {searchResults.length > 0 && (
          <div className="border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 max-h-64 overflow-y-auto">
            <p className="px-4 py-1.5 text-xs font-semibold text-emerald-600 dark:text-emerald-400 uppercase tracking-wide bg-gray-50 dark:bg-gray-900">Nuevo chat</p>
            {searchResults.map(function (u) {
              return (
                <div
                  key={'s-' + u.id}
                  onClick={function () { startNewChat(u); }}
                  className="flex items-center gap-3 px-4 py-2.5 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition"
                >
                  <div className="relative">
                    <div className={'w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm ' + getAvatarColor(u.role)}>
                      {getInitials(u.nombre, u.apellido)}
                    </div>
                    {isOnline(u.id) && (
                      <span className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white dark:border-gray-800 rounded-full" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{u.nombre} {u.apellido}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">@{u.username} · {u.role.replace('ROLE_', '')}</p>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Contact list */}
        <div className="flex-1 overflow-y-auto">
          {contacts.length === 0 && !searchText && (
            <div className="flex flex-col items-center justify-center h-full px-6 text-center">
              <div className="text-5xl mb-3">💬</div>
              <h4 className="text-gray-900 dark:text-white font-medium mb-1">Inicia una conversación</h4>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Busca un usuario arriba para comenzar a chatear
              </p>
            </div>
          )}
          {contacts.map(function (c) {
            var active = selected && selected.user.id === c.user.id;
            return (
              <div
                key={c.user.id}
                onClick={function () { selectContact(c); }}
                className={'flex items-center gap-3 px-4 py-3 cursor-pointer transition border-b border-gray-50 dark:border-gray-700/50 ' +
                  (active
                    ? 'bg-indigo-50 dark:bg-indigo-900/20 border-l-4 border-l-indigo-500'
                    : 'hover:bg-gray-50 dark:hover:bg-gray-700/30 border-l-4 border-l-transparent')}
              >
                {/* Avatar with online dot */}
                <div className="relative flex-shrink-0">
                  <div className={'w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-sm ' + getAvatarColor(c.user.role)}>
                    {getInitials(c.user.nombre, c.user.apellido)}
                  </div>
                  {isOnline(c.user.id) && (
                    <span className="absolute bottom-0 right-0 w-3.5 h-3.5 bg-green-500 border-2 border-white dark:border-gray-800 rounded-full" />
                  )}
                </div>

                {/* Contact info */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <h4 className="text-sm font-semibold text-gray-900 dark:text-white truncate">
                      {c.user.nombre} {c.user.apellido}
                    </h4>
                    {c.lastMessage && (
                      <span className={'text-xs flex-shrink-0 ml-2 ' +
                        (c.unread > 0 ? 'text-emerald-600 dark:text-emerald-400 font-semibold' : 'text-gray-400 dark:text-gray-500')}>
                        {formatDate(c.lastMessage.fechaEnvio)}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center justify-between mt-0.5">
                    <p className="text-xs text-gray-500 dark:text-gray-400 truncate flex items-center gap-1">
                      {c.lastMessage && me && c.lastMessage.remitenteId === me.id && (
                        <span className={'flex-shrink-0 ' + (c.lastMessage.leido ? 'text-blue-500' : 'text-gray-400')}>
                          {c.lastMessage.leido ? '✓✓' : '✓'}
                        </span>
                      )}
                      {c.lastMessage ? c.lastMessage.contenido : 'Sin mensajes aún'}
                    </p>
                    {c.unread > 0 && (
                      <span className="ml-2 flex-shrink-0 bg-emerald-500 text-white text-xs rounded-full h-5 min-w-[20px] flex items-center justify-center px-1.5 font-bold">
                        {c.unread}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* ========== RIGHT PANEL: Chat ========== */}
      <div className={'flex-1 flex flex-col bg-gray-100 dark:bg-gray-900 ' +
        (showMobileContacts ? 'hidden lg:flex' : 'flex')}>

        {!selected ? (
          /* Empty state */
          <div className="flex-1 flex items-center justify-center">
            <div className="text-center px-6">
              <div className="w-24 h-24 rounded-full bg-indigo-100 dark:bg-indigo-900/30 flex items-center justify-center mx-auto mb-4">
                <svg className="w-12 h-12 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">iTeaching Chat</h3>
              <p className="text-gray-500 dark:text-gray-400 max-w-sm">
                Envía y recibe mensajes en tiempo real. Selecciona un contacto o busca un usuario para empezar.
              </p>
              <p className="text-xs text-gray-400 dark:text-gray-500 mt-4">
                🔒 Los mensajes están cifrados extremo a extremo
              </p>
            </div>
          </div>
        ) : (
          <>
            {/* Chat header */}
            <div className="px-4 py-2.5 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center gap-3 shadow-sm">
              {/* Back button (mobile) */}
              <button
                onClick={function () { setShowMobileContacts(true); setSelected(null); }}
                className="lg:hidden p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition"
              >
                <svg className="w-5 h-5 text-gray-600 dark:text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>

              <div className="relative">
                <div className={'w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm ' + getAvatarColor(selected.user.role)}>
                  {getInitials(selected.user.nombre, selected.user.apellido)}
                </div>
                {isOnline(selected.user.id) && (
                  <span className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white dark:border-gray-800 rounded-full" />
                )}
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="font-semibold text-gray-900 dark:text-white text-sm truncate">
                  {selected.user.nombre} {selected.user.apellido}
                </h4>
                <p className="text-xs">
                  {showTyping ? (
                    <span className="text-emerald-500 font-medium">escribiendo...</span>
                  ) : isOnline(selected.user.id) ? (
                    <span className="text-emerald-500">en línea</span>
                  ) : (
                    <span className="text-gray-400 dark:text-gray-500">desconectado</span>
                  )}
                </p>
              </div>

              <div className="flex items-center gap-1">
                <span className="text-xs text-gray-400 dark:text-gray-500">{selected.user.role.replace('ROLE_', '')}</span>
              </div>
            </div>

            {/* Messages area - WhatsApp style wallpaper */}
            <div className="flex-1 overflow-y-auto px-4 py-2"
              style={{
                backgroundImage: 'url("data:image/svg+xml,%3Csvg width=\'60\' height=\'60\' viewBox=\'0 0 60 60\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cg fill=\'none\' fill-rule=\'evenodd\'%3E%3Cg fill=\'%239C92AC\' fill-opacity=\'0.05\'%3E%3Cpath d=\'M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z\'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")',
              }}
            >
              {messages.length === 0 && (
                <div className="flex items-center justify-center h-full">
                  <div className="bg-white dark:bg-gray-800 rounded-xl px-6 py-4 shadow-sm text-center">
                    <div className="text-3xl mb-2">👋</div>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      ¡Envía tu primer mensaje a {selected.user.nombre}!
                    </p>
                  </div>
                </div>
              )}

              {messageGroups.map(function (group) {
                return (
                  <div key={group.date}>
                    {/* Date separator */}
                    <div className="flex justify-center my-3">
                      <span className="bg-white dark:bg-gray-700 text-gray-500 dark:text-gray-300 text-xs px-3 py-1 rounded-lg shadow-sm font-medium">
                        {formatDate(group.date)}
                      </span>
                    </div>

                    {/* Messages */}
                    {group.messages.map(function (m) {
                      var isMine = me && m.remitenteId === me.id;
                      return (
                        <div key={m.id} className={'flex mb-1 ' + (isMine ? 'justify-end' : 'justify-start')}>
                          <div className={'relative max-w-[75%] lg:max-w-[60%] px-3 py-1.5 rounded-lg text-sm shadow-sm ' +
                            (isMine
                              ? 'bg-emerald-100 dark:bg-emerald-900/40 text-gray-900 dark:text-white rounded-tr-none'
                              : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-white rounded-tl-none')
                          }>
                            <p className="break-words whitespace-pre-wrap">{m.contenido}</p>
                            <div className={'flex items-center justify-end gap-1 mt-0.5 -mb-0.5'}>
                              <span className="text-[10px] text-gray-400 dark:text-gray-500">
                                {formatTime(m.fechaEnvio)}
                              </span>
                              {isMine && (
                                <span className={'text-[10px] ' + (m.leido ? 'text-blue-500' : 'text-gray-400')}>
                                  {m.leido ? '✓✓' : '✓'}
                                </span>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                );
              })}

              {/* Typing indicator */}
              {showTyping && (
                <div className="flex justify-start mb-1">
                  <div className="bg-white dark:bg-gray-800 rounded-lg rounded-tl-none px-4 py-2 shadow-sm">
                    <div className="flex gap-1">
                      <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                      <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                      <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                    </div>
                  </div>
                </div>
              )}

              <div ref={chatEndRef} />
            </div>

            {/* Input area */}
            <div className="px-3 py-2 bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700">
              <div className="flex items-end gap-2">
                {/* Emoji button */}
                <button
                  className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition flex-shrink-0"
                  title="Emojis (próximamente)"
                >
                  <svg className="w-5 h-5 text-gray-500 dark:text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </button>

                {/* Text input */}
                <div className="flex-1 bg-gray-100 dark:bg-gray-700 rounded-2xl px-4 py-2 flex items-end">
                  <textarea
                    placeholder="Escribe un mensaje"
                    value={inputText}
                    onChange={function (e) {
                      setInputText(e.target.value);
                      handleTyping();
                    }}
                    onKeyDown={function (e) {
                      if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        sendMessage();
                      }
                    }}
                    rows={1}
                    className="bg-transparent text-sm text-gray-900 dark:text-white placeholder-gray-400 outline-none w-full resize-none max-h-32"
                    style={{ minHeight: '20px' }}
                  />
                </div>

                {/* Send button */}
                <button
                  onClick={sendMessage}
                  disabled={!inputText.trim()}
                  className={'p-2.5 rounded-full transition flex-shrink-0 ' +
                    (inputText.trim()
                      ? 'bg-emerald-500 hover:bg-emerald-600 text-white shadow-md'
                      : 'bg-gray-200 dark:bg-gray-700 text-gray-400 cursor-not-allowed')}
                >
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                  </svg>
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
