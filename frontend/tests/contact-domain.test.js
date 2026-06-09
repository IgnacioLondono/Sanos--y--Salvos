import { describe, expect, it } from "vitest";
import {
  computeTabBadges,
  contactBadgeClass,
  contactStatusLabel,
  filterPendingInbox,
  otherUserId,
  shouldShowCloseChat,
  userLabel,
  validateContactMessage
} from "../src/lib/contact-domain.js";

describe("contact-domain", () => {
  it("etiquetas y badges de estado de contacto", () => {
    expect(contactStatusLabel("PENDING")).toBe("Pendiente");
    expect(contactBadgeClass("ACCEPTED")).toBe("badge-mint");
    expect(contactBadgeClass("REJECTED")).toBe("badge-danger");
  });

  it("identifica al otro participante", () => {
    const conv = { fromUserId: 2, toUserId: 9 };
    expect(otherUserId(conv, 2)).toBe(9);
    expect(otherUserId(conv, 9)).toBe(2);
  });

  it("resuelve nombre de usuario desde mapa IAM", () => {
    const names = { 9: { displayName: "Ana Perez", email: "a@t.cl" } };
    expect(userLabel(9, names)).toBe("Ana Perez");
    expect(userLabel(1, names)).toBe("Usuario #1");
  });

  it("cuenta badges de pestañas", () => {
    const badges = computeTabBadges({
      inbox: [{ status: "PENDING" }],
      sent: [{ status: "PENDING" }, { status: "ACCEPTED" }],
      chatsOpen: [{ id: 1 }],
      chatsClosed: []
    });
    expect(badges.inbox).toBe(1);
    expect(badges.sent).toBe(1);
    expect(badges.chats).toBe(1);
    expect(badges.history).toBe(0);
  });

  it("filtra solo solicitudes pendientes en recibidas", () => {
    const list = filterPendingInbox([
      { status: "PENDING" },
      { status: "ACCEPTED" }
    ]);
    expect(list).toHaveLength(1);
  });

  it("oculta cerrar chat en historial o conversación cerrada", () => {
    const open = { status: "OPEN", toUserId: 9, viewerUserId: 9 };
    expect(shouldShowCloseChat("chats", open)).toBe(true);
    expect(shouldShowCloseChat("history", open)).toBe(false);
    expect(shouldShowCloseChat("chats", { ...open, status: "CLOSED" })).toBe(false);
  });

  it("valida longitud mínima del mensaje de contacto", () => {
    expect(validateContactMessage("hola").ok).toBe(false);
    expect(validateContactMessage("mensaje largo ok").ok).toBe(true);
  });
});
