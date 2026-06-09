/** Normalización de perfil de usuario. */

export function normalizeProfile(raw) {
  if (!raw || typeof raw !== "object") return null;
  return {
    id: raw.id,
    email: raw.email || "",
    displayName: raw.displayName || "",
    fullName: raw.fullName || raw.full_name || "",
    rut: raw.rut || raw.rutDocument || raw.rut_document || "",
    commune: raw.commune || raw.comuna || "",
    address: raw.address || raw.direccion || "",
    phone: raw.phone || raw.telefonoPrincipal || raw.telefono_principal || "",
    emergencyContactName:
      raw.emergencyContactName || raw.emergency_contact_name || raw.contactoEmergenciaNombre || "",
    emergencyContactPhone:
      raw.emergencyContactPhone || raw.emergency_contact_phone || raw.contactoEmergenciaTelefono || "",
    role: raw.role,
    createdAt: raw.createdAt || raw.created_at
  };
}

export function profileLocationLabel(profile) {
  if (!profile) return "—";
  const parts = [profile.commune, profile.address].filter(Boolean);
  return parts.length ? parts.join(" · ") : "—";
}
