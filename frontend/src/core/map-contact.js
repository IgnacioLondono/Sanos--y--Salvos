/**
 * Solicitudes de contacto, chats e historial (mapa ciudadano).
 */
(function () {
  const core = window.SANOS_CORE;

  const CHAT_POLL_MS = 3000;
  const HUB_POLL_MS = 6000;

  const state = {
    userId: null,
    token: null,
    activeTab: "inbox",
    activeConversationId: null,
    inbox: [],
    sent: [],
    chatsOpen: [],
    chatsClosed: [],
    lastMessagesKey: "",
    chatPollTimer: null,
    hubPollTimer: null
  };

  function statusLabel(status) {
    const s = String(status || "").toUpperCase();
    if (s === "PENDING") return "Pendiente";
    if (s === "ACCEPTED") return "Aceptada";
    if (s === "REJECTED") return "Rechazada";
    if (s === "OPEN") return "Activo";
    if (s === "CLOSED") return "Cerrado";
    return s || "—";
  }

  function badgeClass(status) {
    const s = String(status || "").toUpperCase();
    if (s === "PENDING") return "badge-gold";
    if (s === "REJECTED") return "badge-danger";
    if (s === "ACCEPTED" || s === "OPEN") return "badge-mint";
    return "badge-primary";
  }

  const TAB_PANEL_IDS = {
    inbox: "mapContactTabInbox",
    sent: "mapContactTabSent",
    chats: "mapContactTabChats",
    history: "mapContactTabHistory"
  };

  function session() {
    return core.readSession("citizen");
  }

  function reapplyTabVisibility() {
    if (state.activeConversationId) return;
    Object.keys(TAB_PANEL_IDS).forEach((key) => {
      const el = document.getElementById(TAB_PANEL_IDS[key]);
      if (!el) return;
      const active = key === state.activeTab;
      el.classList.toggle("is-active", active);
      if (active) el.removeAttribute("hidden");
      else el.setAttribute("hidden", "");
    });
  }

  function buildPopupActions(report, currentUserId) {
    const reportId = Number(report.id);
    const ownerId = Number(report.createdBy);
    const me = Number(currentUserId);
    if (!reportId || !me || !ownerId || me === ownerId) return "";

    return `<div class="map-report-popup__contact">
      <p class="map-report-popup__contact-hint">¿Quieres contactar al dueño de este reporte?</p>
      <textarea class="map-contact-msg" id="mapContactMsg-${reportId}" rows="2" maxlength="500" placeholder="Escribe un mensaje breve (mín. 10 caracteres)…"></textarea>
      <button type="button" class="btn btn-primary btn-sm map-contact-send" onclick="window.SANOS_MAP_CONTACT.send(${reportId}, ${me})">
        Enviar solicitud
      </button>
    </div>`;
  }

  async function send(reportId, fromUserId) {
    const ta = document.getElementById(`mapContactMsg-${reportId}`);
    const message = (ta && ta.value ? ta.value : "").trim();
    if (message.length < 10) {
      alert("El mensaje debe tener al menos 10 caracteres.");
      return;
    }
    const s = session();
    try {
      await core.api("/api/reports/contact-requests", {
        method: "POST",
        token: s.token,
        body: { reportId, fromUserId, message }
      });
      alert("Solicitud enviada. Si la aceptan, se abrirá un chat entre ambos.");
      if (ta) ta.value = "";
      await refreshAll();
    } catch (err) {
      alert(err.message || "No se pudo enviar la solicitud.");
    }
  }

  async function respond(requestId, responderUserId, status) {
    const s = session();
    try {
      await core.api(`/api/reports/contact-requests/${requestId}`, {
        method: "PATCH",
        token: s.token,
        body: { responderUserId, status }
      });
      if (status === "ACCEPTED") {
        alert("Solicitud aceptada. Ya pueden conversar en Chats activos.");
      }
      await refreshAll();
      if (status === "ACCEPTED") switchTab("chats");
    } catch (err) {
      alert(err.message || "No se pudo actualizar la solicitud.");
    }
  }

  async function loadInbox(userId, token) {
    return core.api(`/api/reports/contact-requests/inbox?userId=${userId}`, { token });
  }

  async function loadSent(userId, token) {
    return core.api(`/api/reports/contact-requests/sent?userId=${userId}`, { token });
  }

  async function loadConversations(userId, token, status) {
    return core.api(`/api/reports/contact-conversations?userId=${userId}&status=${status}`, { token });
  }

  function renderRequestList(container, list, currentUserId, mode) {
    if (!container) return;
    const items = Array.isArray(list) ? list : [];
    if (!items.length) {
      const empty =
        mode === "sent"
          ? "No has enviado solicitudes."
          : "No tienes solicitudes recibidas.";
      container.innerHTML = `<p class="map-contact-empty">${empty}</p>`;
      return;
    }

    container.innerHTML = items
      .map((item) => {
        const st = String(item.status || "").toUpperCase();
        const isPending = st === "PENDING";
        const isReceiver = mode === "inbox";
        const isSender = mode === "sent";
        const convId = item.conversationId;

        let actions = "";
        if (isPending && isReceiver) {
          actions = `<div class="map-contact-inbox__actions">
              <button type="button" class="btn btn-primary btn-sm" data-map-contact-action="accept" data-request-id="${item.id}">Aceptar y abrir chat</button>
              <button type="button" class="btn btn-secondary btn-sm" data-map-contact-action="reject" data-request-id="${item.id}">Rechazar</button>
            </div>`;
        } else if (st === "ACCEPTED") {
          const chatBtn = convId
            ? `data-map-contact-action="open-chat" data-conversation-id="${convId}"`
            : `data-map-contact-action="open-request" data-request-id="${item.id}"`;
          actions = `<div class="map-contact-inbox__actions">
              <button type="button" class="btn btn-primary btn-sm" ${chatBtn}>Abrir chat</button>
            </div>`;
        } else if (isSender && st === "PENDING") {
          actions = `<p class="map-contact-inbox__meta">Esperando que el dueño del reporte acepte (él debe entrar en <strong>Recibidas</strong>).</p>`;
        }

        const meta = isReceiver
          ? `De usuario #${item.fromUserId}`
          : `Para el dueño del reporte #${item.toUserId}`;

        return `<article class="map-contact-inbox__item ${isPending ? "is-pending" : ""}">
          <div class="map-contact-inbox__head">
            <strong>Reporte #${core.escapeHtml(String(item.reportId))}</strong>
            <span class="badge ${badgeClass(st)}">${core.escapeHtml(statusLabel(st))}</span>
          </div>
          <p class="map-contact-inbox__msg">${core.escapeHtml(item.message || "")}</p>
          <p class="map-contact-inbox__meta">${core.escapeHtml(meta)}</p>
          ${actions}
        </article>`;
      })
      .join("");

    if (mode === "inbox") {
      const pending = items.filter((i) => String(i.status).toUpperCase() === "PENDING").length;
      const badge = document.getElementById("mapContactBadge");
      if (badge) {
        badge.textContent = pending ? String(pending) : "";
        badge.hidden = !pending;
      }
      const inboxTab = document.querySelector('[data-contact-tab="inbox"]');
      if (inboxTab) inboxTab.classList.toggle("has-pending", pending > 0);
    }
  }

  function renderConversationList(container, list, currentUserId) {
    if (!container) return;
    const items = Array.isArray(list) ? list : [];
    if (!items.length) {
      const hint =
        container.id === "mapContactTabChats"
          ? "No hay chats activos. Si una solicitud está <strong>Aceptada</strong>, abre el chat desde <strong>Enviadas</strong> o <strong>Recibidas</strong>."
          : "No hay conversaciones en esta sección.";
      container.innerHTML = `<p class="map-contact-empty">${hint}</p>`;
      return;
    }
    container.innerHTML = items
      .map((c) => {
        const other =
          Number(c.fromUserId) === Number(currentUserId) ? c.toUserId : c.fromUserId;
        const isReceiver = Number(c.toUserId) === Number(currentUserId);
        return `<article class="map-contact-inbox__item">
          <div class="map-contact-inbox__head">
            <strong>Reporte #${core.escapeHtml(String(c.reportId))} · Usuario #${core.escapeHtml(String(other))}</strong>
            <span class="badge ${badgeClass(c.status)}">${core.escapeHtml(statusLabel(c.status))}</span>
          </div>
          <p class="map-contact-inbox__msg">${core.escapeHtml(c.lastMessagePreview || "Sin mensajes")}</p>
          <p class="map-contact-inbox__meta">${c.messageCount} mensaje(s)</p>
          <div class="map-contact-inbox__actions">
            <button type="button" class="btn btn-primary btn-sm" data-map-contact-action="open-chat" data-conversation-id="${c.id}">Ver chat</button>
          </div>
        </article>`;
      })
      .join("");
  }

  function switchTab(tab) {
    state.activeTab = tab;
    document.querySelectorAll("[data-contact-tab]").forEach((btn) => {
      const on = btn.getAttribute("data-contact-tab") === tab;
      btn.classList.toggle("is-active", on);
      btn.setAttribute("aria-selected", on ? "true" : "false");
    });
    hideChatPanel(false);
    reapplyTabVisibility();
  }

  function stopChatPolling() {
    if (state.chatPollTimer) {
      clearInterval(state.chatPollTimer);
      state.chatPollTimer = null;
    }
  }

  function startChatPolling() {
    stopChatPolling();
    if (!state.activeConversationId) return;
    state.chatPollTimer = setInterval(() => {
      if (!state.activeConversationId || document.visibilityState === "hidden") return;
      renderChatMessages({ silent: true });
    }, CHAT_POLL_MS);
  }

  function stopHubPolling() {
    if (state.hubPollTimer) {
      clearInterval(state.hubPollTimer);
      state.hubPollTimer = null;
    }
  }

  function startHubPolling() {
    stopHubPolling();
    state.hubPollTimer = setInterval(() => {
      if (document.visibilityState === "hidden") return;
      refreshAll({ quiet: true });
    }, HUB_POLL_MS);
  }

  function messagesRenderKey(messages) {
    if (!messages || !messages.length) return "0";
    const last = messages[messages.length - 1];
    return `${messages.length}:${last.id}:${last.createdAt}`;
  }

  function hideChatPanel(resetTab) {
    const doReset = resetTab !== false;
    stopChatPolling();
    state.activeConversationId = null;
    state.lastMessagesKey = "";
    const panel = document.getElementById("mapContactChatPanel");
    const tabs = document.querySelector(".map-contact-tabs");
    const hubBody = document.getElementById("mapContactHub");
    const input = document.getElementById("mapContactChatInput");
    if (panel) panel.classList.add("hidden");
    if (tabs) tabs.hidden = false;
    if (hubBody) hubBody.classList.remove("is-chat-open");
    if (input) {
      input.disabled = false;
      input.value = "";
    }
    if (doReset) reapplyTabVisibility();
  }

  function showChatPanel() {
    document.querySelectorAll(".map-contact-tab-panel").forEach((p) => {
      p.classList.remove("is-active");
      p.setAttribute("hidden", "");
    });
    const tabs = document.querySelector(".map-contact-tabs");
    const hubBody = document.getElementById("mapContactHub");
    if (tabs) tabs.hidden = true;
    if (hubBody) hubBody.classList.add("is-chat-open");
    const panel = document.getElementById("mapContactChatPanel");
    if (panel) panel.classList.remove("hidden");
  }

  async function openChatFromRequest(requestId) {
    const s = session();
    const uid = state.userId;
    if (!uid || !s.token) return;
    try {
      const conv = await core.api(
        `/api/reports/contact-conversations/by-request/${requestId}?userId=${uid}`,
        { token: s.token }
      );
      const exists = state.chatsOpen.some((c) => Number(c.id) === Number(conv.id));
      if (!exists) state.chatsOpen = [conv].concat(state.chatsOpen);
      await openChat(conv.id);
    } catch (err) {
      alert(err.message || "No se pudo abrir el chat.");
    }
  }

  async function openChat(conversationId) {
    const s = session();
    const uid = state.userId;
    if (!uid || !s.token) return;

    state.activeConversationId = conversationId;
    showChatPanel();

    const conv = [...state.chatsOpen, ...state.chatsClosed].find(
      (c) => Number(c.id) === Number(conversationId)
    );
    const title = document.getElementById("mapContactChatTitle");
    const meta = document.getElementById("mapContactChatMeta");
    const closeBtn = document.getElementById("mapContactChatClose");
    const form = document.getElementById("mapContactChatForm");
    const input = document.getElementById("mapContactChatInput");

    if (title && conv) {
      title.textContent = `Chat · Reporte #${conv.reportId}`;
    }
    if (meta && conv) {
      meta.textContent =
        String(conv.status).toUpperCase() === "CLOSED"
          ? "Conversación cerrada (historial)"
          : "Conversación activa";
    }

    const isReceiver = conv && Number(conv.toUserId) === Number(uid);
    const isOpen = conv && String(conv.status).toUpperCase() === "OPEN";
    if (closeBtn) {
      closeBtn.classList.toggle("hidden", !(isReceiver && isOpen));
    }
    if (form) form.classList.toggle("hidden", !isOpen);
    if (input && !isOpen) input.disabled = true;

    await renderChatMessages();
    startChatPolling();
    core.refreshIcons();
  }

  function paintChatMessages(box, messages, uid) {
    if (!messages.length) {
      box.innerHTML = `<p class="map-contact-empty">Sin mensajes aún.</p>`;
      return;
    }
    const atBottom = box.scrollHeight - box.scrollTop - box.clientHeight < 48;
    box.innerHTML = messages
      .map((m) => {
        const mine = Number(m.authorUserId) === Number(uid);
        return `<div class="map-contact-chat__bubble ${mine ? "is-mine" : "is-theirs"}">
            <p>${core.escapeHtml(m.content || "")}</p>
            <span>${core.escapeHtml(core.formatMapDate(m.createdAt) || "")}</span>
          </div>`;
      })
      .join("");
    if (atBottom) box.scrollTop = box.scrollHeight;
  }

  async function renderChatMessages(opts) {
    const silent = opts && opts.silent;
    const box = document.getElementById("mapContactChatMessages");
    const id = state.activeConversationId;
    const uid = state.userId;
    const s = session();
    if (!box || !id || !uid) return;

    if (!silent) box.innerHTML = `<p class="map-contact-empty">Cargando mensajes…</p>`;
    try {
      const messages = await core.api(
        `/api/reports/contact-conversations/${id}/messages?userId=${uid}`,
        { token: s.token }
      );
      const key = messagesRenderKey(messages);
      if (silent && key === state.lastMessagesKey) return;
      state.lastMessagesKey = key;
      paintChatMessages(box, messages, uid);
    } catch (err) {
      if (!silent) {
        box.innerHTML = `<p class="map-contact-empty">${core.escapeHtml(err.message)}</p>`;
      }
    }
  }

  async function sendChatMessage(event) {
    if (event) event.preventDefault();
    const id = state.activeConversationId;
    const uid = state.userId;
    const input = document.getElementById("mapContactChatInput");
    const s = session();
    if (!id || !uid || !input) return;
    const content = input.value.trim();
    if (!content) return;

    try {
      await core.api(`/api/reports/contact-conversations/${id}/messages`, {
        method: "POST",
        token: s.token,
        body: { authorUserId: uid, content }
      });
      input.value = "";
      await renderChatMessages();
      await refreshAll();
    } catch (err) {
      alert(err.message || "No se pudo enviar el mensaje.");
    }
  }

  async function closeChat() {
    const id = state.activeConversationId;
    const uid = state.userId;
    const s = session();
    if (!id || !uid) return;
    if (!confirm("¿Cerrar este chat? Quedará en historial y no se podrá enviar más mensajes.")) return;

    try {
      await core.api(`/api/reports/contact-conversations/${id}/close`, {
        method: "PATCH",
        token: s.token,
        body: { userId: uid }
      });
      hideChatPanel();
      await refreshAll();
      switchTab("history");
      alert("Chat cerrado. Lo encuentras en Historial.");
    } catch (err) {
      alert(err.message || "No se pudo cerrar el chat.");
    }
  }

  async function refreshAll(opts) {
    const quiet = opts && opts.quiet;
    const s = session();
    const uid = Number(s.user && s.user.id);
    if (!uid || !s.token) return;
    state.userId = uid;
    state.token = s.token;

    try {
      const [inbox, sent, open, closed] = await Promise.all([
        loadInbox(uid, s.token),
        loadSent(uid, s.token),
        loadConversations(uid, s.token, "OPEN"),
        loadConversations(uid, s.token, "CLOSED")
      ]);
      state.inbox = inbox;
      state.sent = sent;
      state.chatsOpen = open;
      state.chatsClosed = closed;

      renderRequestList(document.getElementById("mapContactTabInbox"), inbox, uid, "inbox");
      renderRequestList(document.getElementById("mapContactTabSent"), sent, uid, "sent");
      renderConversationList(document.getElementById("mapContactTabChats"), open, uid);
      renderConversationList(document.getElementById("mapContactTabHistory"), closed, uid);

      if (state.activeConversationId) {
        await renderChatMessages({ silent: quiet });
      }
      reapplyTabVisibility();
    } catch (err) {
      if (!quiet) console.error(err);
    }
  }

  function bindHubActions() {
    const hub = document.getElementById("mapContactHub");
    if (!hub || hub.dataset.bound === "1") return;
    hub.dataset.bound = "1";
    hub.addEventListener("click", (event) => {
      const btn = event.target.closest("[data-map-contact-action]");
      if (!btn) return;
      event.preventDefault();
      const action = btn.getAttribute("data-map-contact-action");
      const requestId = Number(btn.getAttribute("data-request-id"));
      const conversationId = Number(btn.getAttribute("data-conversation-id"));
      const uid = state.userId;
      if (action === "accept" && requestId) respond(requestId, uid, "ACCEPTED");
      else if (action === "reject" && requestId) respond(requestId, uid, "REJECTED");
      else if (action === "open-chat" && conversationId) openChat(conversationId);
      else if (action === "open-request" && requestId) openChatFromRequest(requestId);
    });
  }

  function initHub() {
    const s = session();
    const uid = Number(s.user && s.user.id);
    if (!uid) return;
    state.userId = uid;
    state.token = s.token;

    document.querySelectorAll("[data-contact-tab]").forEach((btn) => {
      btn.addEventListener("click", () => switchTab(btn.getAttribute("data-contact-tab")));
    });
    bindHubActions();

    const back = document.getElementById("mapContactChatBack");
    if (back) back.addEventListener("click", hideChatPanel);

    const closeBtn = document.getElementById("mapContactChatClose");
    if (closeBtn) closeBtn.addEventListener("click", closeChat);

    const form = document.getElementById("mapContactChatForm");
    if (form) form.addEventListener("submit", sendChatMessage);

    switchTab("inbox");
    refreshAll();

    startHubPolling();
    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "visible") {
        refreshAll({ quiet: true });
        if (state.activeConversationId) renderChatMessages({ silent: true });
      }
    });
    window.addEventListener("beforeunload", () => {
      stopChatPolling();
      stopHubPolling();
    });
  }

  /* Compat con citizen-dashboard */
  async function refreshMapContactInbox() {
    await refreshAll();
  }

  function renderInbox(container, items, currentUserId) {
    renderRequestList(container, items, currentUserId, "inbox");
  }

  window.SANOS_MAP_CONTACT = {
    buildPopupActions,
    send,
    respond,
    renderInbox,
    loadInbox,
    statusLabel,
    initHub,
    refreshAll,
    openChat,
    switchTab
  };

  window.SANOS_MAP_CONTACT_ON_UPDATE = refreshAll;
})();
