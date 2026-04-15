const API = "http://localhost:8080";
let token = "";

const byId = (id) => document.getElementById(id);
const logEl = byId("log");

const log = (msg, obj) => {
  const line = [] ;
  logEl.textContent = ${line}\n\n\n;
};

async function api(path, method = "GET", body) {
  const headers = { "Content-Type": "application/json" };
  if (token) {
    headers.Authorization = Bearer ;
  }

  const res = await fetch(${API}, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new Error(${res.status}  );
  }

  return data;
}

byId("btnRegister").onclick = async () => {
  try {
    const payload = {
      email: byId("email").value,
      password: byId("password").value,
      displayName: byId("email").value.split("@")[0],
      role: "CITIZEN"
    };
    const data = await api("/api/iam/register", "POST", payload);
    byId("authStatus").textContent = "Usuario registrado";
    log("Registro IAM exitoso", data);
  } catch (e) {
    log("Error al registrar", { error: String(e) });
  }
};

byId("btnLogin").onclick = async () => {
  try {
    const payload = {
      email: byId("email").value,
      password: byId("password").value
    };
    const data = await api("/api/iam/login", "POST", payload);
    token = data.token;
    byId("authStatus").textContent = Sesion activa como ;
    log("Login exitoso", data);
  } catch (e) {
    log("Error de login", { error: String(e) });
  }
};

byId("btnLoadDashboard").onclick = async () => {
  try {
    const data = await api("/api/bff/dashboard");
    byId("dashboardOut").textContent = JSON.stringify(data, null, 2);
    byId("kpis").innerHTML = 
      <div class="kpi"><small>Mascotas</small><b></b></div>
      <div class="kpi"><small>Reportes</small><b></b></div>
      <div class="kpi"><small>Capacity</small><b></b></div>
      <div class="kpi"><small>Fotos</small><b></b></div>
    ;
    log("Dashboard actualizado", data);
  } catch (e) {
    log("Error al cargar dashboard", { error: String(e) });
  }
};

byId("btnCreatePet").onclick = async () => {
  try {
    const payload = {
      chipNumber: byId("petChip").value,
      name: byId("petName").value,
      species: "DOG",
      breed: byId("petBreed").value,
      color: byId("petColor").value,
      size: byId("petSize").value,
      ownerId: byId("petOwner").value
    };
    const data = await api("/api/pets", "POST", payload);
    log("Mascota creada", data);
  } catch (e) {
    log("Error al crear mascota", { error: String(e) });
  }
};

byId("btnCreateReport").onclick = async () => {
  try {
    const payload = {
      petId: byId("reportPetId").value,
      type: byId("reportType").value,
      status: "OPEN",
      healthStatus: byId("reportHealth").value,
      commune: byId("reportCommune").value,
      latitude: Number(byId("reportLat").value),
      longitude: Number(byId("reportLng").value)
    };
    const data = await api("/api/reports", "POST", payload);
    log("Reporte creado", data);
  } catch (e) {
    log("Error al crear reporte", { error: String(e) });
  }
};

byId("btnCreateCapacity").onclick = async () => {
  try {
    const payload = {
      organization: byId("capOrg").value,
      volunteers: Number(byId("capVolunteers").value),
      hoursAvailable: Number(byId("capHours").value),
      zone: "Santiago"
    };
    const data = await api("/api/capacity", "POST", payload);
    log("Capacity guardado", data);
  } catch (e) {
    log("Error capacity", { error: String(e) });
  }
};

byId("btnCreateMedia").onclick = async () => {
  try {
    const payload = {
      petId: byId("mediaPetId").value,
      url: byId("mediaUrl").value,
      takenAt: new Date().toISOString(),
      tags: ["frontend"]
    };
    const data = await api("/api/media", "POST", payload);
    log("Foto registrada", data);
  } catch (e) {
    log("Error media", { error: String(e) });
  }
};
