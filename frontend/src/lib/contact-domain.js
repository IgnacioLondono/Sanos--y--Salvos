/** Dominio de contacto y chat en mapa. */

export function contactStatusLabel(status) {
  const s = String(status || "").toUpperCase();
  if (s === "PENDING") return "Pendiente";
  if (s === "ACCEPTED") return "Aceptada";
  if (s === "REJECTED") return "Rechazada";
  if (s === "OPEN") return "Activo";
  if (s === "CLOSED") return "Cerrado";
  return s || "—";
}

export function contactBadgeClass(status) {
  const s = String(status || "").toUpperCase();
  if (s === "PENDING") return "badge-gold";
  if (s === "REJECTED") return "badge-danger";
  if (s === "ACCEPTED" || s === "OPEN") return "badge-mint";
  return "badge-primary";
}

export function otherUserId(conv, currentUserId) {
  if (!conv) return null;
  return Number(conv.fromUserId) === Number(currentUserId) ? conv.toUserId : conv.fromUserId;
}

export function userLabel(userId, userNames) {
  const id = Number(userId);
  const map = userNames || {};
  const u = map[id];
  if (!u) return `Usuario #${id}`;
  return (u.displayName || u.fullName || u.email || `Usuario #${id}`).trim();
}

export function computeTabBadges(state) {
  const inbox = state.inbox || [];
  const sent = state.sent || [];
  const chatsOpen = state.chatsOpen || [];
  const chatsClosed = state.chatsClosed || [];
  return {
    inbox: inbox.length,
    sent: sent.filter((s) => String(s.status).toUpperCase() === "PENDING").length,
    chats: chatsOpen.length,
    history: chatsClosed.length
  };
}

export function filterPendingInbox(inboxRaw) {
  return (inboxRaw || []).filter((i) => String(i.status).toUpperCase() === "PENDING");
}

export function shouldShowCloseChat(activeTab, conv) {
  if (!conv) return false;
  const isClosed = String(conv.status).toUpperCase() === "CLOSED";
  const fromHistory = activeTab === "history";
  const isReceiver = Number(conv.toUserId) === Number(conv.viewerUserId);
  const isOpen = String(conv.status).toUpperCase() === "OPEN";
  return isReceiver && isOpen && !fromHistory && !isClosed;
}

export function validateContactMessage(message) {
  const text = String(message || "").trim();
  if (text.length < 10) {
    return { ok: false, error: "El mensaje debe tener al menos 10 caracteres." };
  }
  return { ok: true, value: text };
}
