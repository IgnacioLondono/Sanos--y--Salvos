/** 
 * User Profile Module
 * Maneja el modal de perfil de usuario y la funcionalidad asociada
 */

window.SANOS_PROFILE = (function () {
  const modal = document.getElementById('userProfileModal');
  const closeBtn = document.getElementById('btnCloseProfile');
  const dashUser = document.querySelector('.dash-user');
  
  // Elementos del perfil
  const profileDisplayName = document.getElementById('profileDisplayName');
  const profileEmail = document.getElementById('profileEmail');
  const profileRole = document.getElementById('profileRole');
  const profileFullName = document.getElementById('profileFullName');
  const profileEmailFull = document.getElementById('profileEmailFull');
  const profilePhone = document.getElementById('profilePhone');
  const profileLocation = document.getElementById('profileLocation');
  const profilePets = document.getElementById('profilePets');
  const profileReports = document.getElementById('profileReports');
  const profileMedia = document.getElementById('profileMedia');
  const profileMatches = document.getElementById('profileMatches');
  
  // Botones del perfil
  const btnEditProfile = document.getElementById('btnEditProfile');
  const btnProfileSettings = document.getElementById('btnProfileSettings');
  const btnProfileLogout = document.getElementById('btnProfileLogout');

  function init() {
    if (!modal) return;
    
    // Event listeners
    closeBtn?.addEventListener('click', closeProfile);
    dashUser?.addEventListener('click', openProfile);
    modal.addEventListener('click', (e) => {
      if (e.target === modal) closeProfile();
    });
    
    btnEditProfile?.addEventListener('click', editProfile);
    btnProfileSettings?.addEventListener('click', openSettings);
    btnProfileLogout?.addEventListener('click', logoutUser);
    
    // Cargar datos del perfil
    loadProfileData();
  }

  function openProfile(e) {
    e.preventDefault();
    if (modal) {
      modal.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
      loadProfileData();
    }
  }

  function closeProfile() {
    if (modal) {
      modal.classList.add('hidden');
      document.body.style.overflow = 'auto';
    }
  }

  function loadProfileData() {
    const core = window.SANOS_CORE;
    const isAdmin = document.body.classList.contains('admin-page');
    const sessionKey = isAdmin ? 'admin' : 'citizen';
    const session = core?.readSession(sessionKey);

    if (session && session.user) {
      const user = session.user;
      
      // Actualizar información básica
      if (profileDisplayName) {
        profileDisplayName.textContent = user.displayName || user.email || 'Usuario';
      }
      if (profileEmail) {
        profileEmail.textContent = user.email || '—';
      }
      if (profileEmailFull) {
        profileEmailFull.textContent = user.email || '—';
      }
      if (profileFullName) {
        profileFullName.textContent = user.displayName || user.fullName || '—';
      }
      if (profilePhone) {
        profilePhone.textContent = user.phone || '—';
      }
      if (profileLocation) {
        profileLocation.textContent = user.location || '—';
      }
      
      // Rol del usuario
      if (profileRole) {
        if (isAdmin) {
          profileRole.textContent = 'Administrador';
          profileRole.className = 'badge badge-admin';
        } else {
          profileRole.textContent = 'Ciudadano';
          profileRole.className = 'badge badge-primary';
        }
      }
      
      // Estadísticas (se actualizarán desde el dashboard)
      updateStatistics();
    }
  }

  function updateStatistics() {
    const core = window.SANOS_CORE;
    const isAdmin = document.body.classList.contains('admin-page');
    const sessionKey = isAdmin ? 'admin' : 'citizen';
    const session = core?.readSession(sessionKey);

    if (session && session.user) {
      // Aquí iría lógica para cargar estadísticas desde el API
      // Por ahora mostraremos valores por defecto
      if (profilePets) profilePets.textContent = '0';
      if (profileReports) profileReports.textContent = '0';
      if (profileMedia) profileMedia.textContent = '0';
      if (profileMatches) profileMatches.textContent = '0';
    }
  }

  function editProfile() {
    closeProfile();
    if (document.body.classList.contains('citizen-page')) {
      window.location.href = (window.SANOS_CORE && SANOS_CORE.navPage('citizen-perfil.html')) || './pages/citizen/citizen-perfil.html';
      return;
    }
    window.location.href = (window.SANOS_CORE && SANOS_CORE.navPage('admin-resumen.html')) || './pages/admin/admin-resumen.html';
  }

  function openSettings() {
    editProfile();
  }

  function logoutUser() {
    const isAdmin = document.body.classList.contains('admin-page');
    const sessionKey = isAdmin ? 'admin' : 'citizen';
    const core = window.SANOS_CORE;
    
    core?.clearSession(sessionKey);
    window.location.href = (window.SANOS_CORE && SANOS_CORE.indexUrl()) + '?logout=1';
  }

  return {
    init,
    openProfile,
    closeProfile,
    loadProfileData,
    updateStatistics
  };
})();

// Inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    SANOS_PROFILE.init();
  });
} else {
  SANOS_PROFILE.init();
}
