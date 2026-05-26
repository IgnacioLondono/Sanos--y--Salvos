(function () {
  const core = window.SANOS_CORE;

  const state = {
    session: core.readSession("citizen"),
    threads: [],
    detail: null,
    view: "list",
    selectedThreadId: null,
    categoryFilter: ""
  };

  const els = {
    citizenIdentity: document.getElementById("citizenIdentity"),
    citizenStatus: document.getElementById("citizenStatus"),
    forumThreadList: document.getElementById("forumThreadList"),
    forumEmpty: document.getElementById("forumEmpty"),
    forumCategoryFilter: document.getElementById("forumCategoryFilter"),
    forumViewList: document.getElementById("forumViewList"),
    forumViewThread: document.getElementById("forumViewThread"),
    forumViewNew: document.getElementById("forumViewNew"),
    forumThreadHeader: document.getElementById("forumThreadHeader"),
    forumPostsList: document.getElementById("forumPostsList"),
    forumReplyForm: document.getElementById("forumReplyForm"),
    forumReplyContent: document.getElementById("forumReplyContent"),
    forumNewThreadForm: document.getElementById("forumNewThreadForm"),
    forumNewTitle: document.getElementById("forumNewTitle"),
    forumNewCategory: document.getElementById("forumNewCategory"),
    forumNewContent: document.getElementById("forumNewContent"),
    btnNewThread: document.getElementById("btnNewThread"),
    btnBackToList: document.getElementById("btnBackToList"),
    btnCancelNewThread: document.getElementById("btnCancelNewThread"),
    btnRefresh: document.getElementById("btnRefresh"),
    btnCitizenLogout: document.getElementById("btnCitizenLogout")
  };

  init();

  function init() {
    if (!state.session.token || !state.session.user) {
      window.location.href = core.indexUrl();
      return;
    }

    if (els.citizenIdentity) {
      els.citizenIdentity.textContent =
        state.session.user.displayName || state.session.user.email || "Ciudadano";
    }

    wireActions();

    if (window.SANOS_DASH_LAYOUT && typeof window.SANOS_DASH_LAYOUT.applyLayout === "function") {
      window.SANOS_DASH_LAYOUT.applyLayout();
    }

    wireCitizenNav();
    ensureValidSession().then((ok) => {
      if (ok) loadThreads();
    });
  }

  async function ensureValidSession() {
    const token = core.normalizeToken(state.session.token);
    if (!token) {
      core.clearSession("citizen");
      core.redirectToLogin("session");
      return false;
    }
    state.session.token = token;
    try {
      await core.api("/api/iam/profile", { token });
      return true;
    } catch (error) {
      if (core.isUnauthorizedError(error)) {
        core.clearSession("citizen");
        core.redirectToLogin("session");
        return false;
      }
      setStatus(
        "No se pudo validar la sesión. Comprueba que el gateway esté activo en " +
          core.getApiBaseUrl() +
          ".",
        true
      );
      return true;
    }
  }

  function handleAuthError(error) {
    if (!core.isUnauthorizedError(error)) return false;
    core.clearSession("citizen");
    core.redirectToLogin("session");
    return true;
  }

  function wireActions() {
    document.addEventListener("click", (event) => {
      if (event.target.closest("#btnCitizenLogout")) {
        event.preventDefault();
        core.clearSession("citizen");
        window.location.href = core.indexUrl() + "?logout=1";
        return;
      }
      if (event.target.closest("#btnRefresh")) {
        event.preventDefault();
        if (state.view === "thread" && state.selectedThreadId) {
          openThread(state.selectedThreadId);
        } else {
          loadThreads();
        }
      }
    });

    if (els.btnNewThread) {
      els.btnNewThread.addEventListener("click", () => showView("new"));
    }
    if (els.btnBackToList) {
      els.btnBackToList.addEventListener("click", () => showView("list"));
    }
    if (els.btnCancelNewThread) {
      els.btnCancelNewThread.addEventListener("click", () => showView("list"));
    }
    if (els.forumCategoryFilter) {
      els.forumCategoryFilter.addEventListener("change", () => {
        state.categoryFilter = els.forumCategoryFilter.value;
        loadThreads();
      });
    }
    if (els.forumReplyForm) {
      els.forumReplyForm.addEventListener("submit", onReply);
    }
    if (els.forumNewThreadForm) {
      els.forumNewThreadForm.addEventListener("submit", onCreateThread);
    }
  }

  function wireCitizenNav() {
    const cur = (window.location.pathname.split("/").pop() || "").toLowerCase();
    document.querySelectorAll(".dash-nav__link[href], .dash-sidebar__link[href]").forEach((a) => {
      const href = (a.getAttribute("href") || "").toLowerCase();
      const match = href === cur;
      a.classList.toggle("is-active", match);
      if (match) a.setAttribute("aria-current", "page");
      else a.removeAttribute("aria-current");
    });
  }

  function userId() {
    const id = state.session.user && state.session.user.id;
    const n = Number(id);
    return Number.isFinite(n) && n > 0 ? n : null;
  }

  function authorName() {
    const u = state.session.user || {};
    return u.displayName || u.fullName || u.email || "Ciudadano";
  }

  function showView(view) {
    state.view = view;
    const isList = view === "list";
    const isThread = view === "thread";
    const isNew = view === "new";

    if (els.forumViewList) els.forumViewList.classList.toggle("hidden", !isList);
    if (els.forumViewThread) els.forumViewThread.classList.toggle("hidden", !isThread);
    if (els.forumViewNew) els.forumViewNew.classList.toggle("hidden", !isNew);
    if (els.btnBackToList) els.btnBackToList.hidden = isList;
    if (els.btnNewThread) els.btnNewThread.hidden = !isList;

    core.refreshIcons();
  }

  async function loadThreads() {
    try {
      setStatus("Cargando foro…", false, true);
      const q = state.categoryFilter ? `?category=${encodeURIComponent(state.categoryFilter)}` : "";
      state.threads = await core.api(`/api/forum/threads${q}`, { auth: false });
      showView("list");
      renderThreadList();
      setStatus("Foro actualizado.");
    } catch (error) {
      if (!handleAuthError(error)) setStatus(error.message, true);
    }
  }

  function renderThreadList() {
    if (!els.forumThreadList) return;
    const list = state.threads || [];

    if (!list.length) {
      els.forumThreadList.innerHTML = "";
      if (els.forumEmpty) els.forumEmpty.classList.remove("hidden");
      return;
    }

    if (els.forumEmpty) els.forumEmpty.classList.add("hidden");

    els.forumThreadList.innerHTML = list
      .map(
        (t) => `
        <button type="button" class="forum-thread-card" data-thread-id="${core.escapeHtml(String(t.id))}">
          <div class="forum-thread-card__top">
            <span class="forum-cat forum-cat--${categoryClass(t.category)}">${categoryLabel(t.category)}</span>
            <span class="forum-thread-card__meta">${core.escapeHtml(core.formatDate(t.updatedAt || t.createdAt))}</span>
          </div>
          <h3 class="forum-thread-card__title">${core.escapeHtml(t.title || "Sin título")}</h3>
          <p class="forum-thread-card__preview">${core.escapeHtml(t.preview || "")}</p>
          <div class="forum-thread-card__foot">
            <span><i data-lucide="user"></i>${core.escapeHtml(t.authorName || "Ciudadano")}</span>
            <span><i data-lucide="message-circle"></i>${Number(t.replyCount || 0)} respuestas</span>
          </div>
        </button>
      `
      )
      .join("");

    els.forumThreadList.querySelectorAll("[data-thread-id]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = Number(btn.getAttribute("data-thread-id"));
        if (Number.isFinite(id)) openThread(id);
      });
    });

    core.refreshIcons();
  }

  async function openThread(threadId) {
    try {
      setStatus("Cargando hilo…", false, true);
      state.selectedThreadId = threadId;
      state.detail = await core.api(`/api/forum/threads/${threadId}`, { auth: false });
      renderThreadDetail();
      showView("thread");
      setStatus("Hilo cargado.");
    } catch (error) {
      if (!handleAuthError(error)) setStatus(error.message, true);
    }
  }

  function renderThreadDetail() {
    const detail = state.detail;
    if (!detail || !detail.thread) return;
    const t = detail.thread;
    const posts = detail.posts || [];

    if (els.forumThreadHeader) {
      els.forumThreadHeader.innerHTML = `
        <span class="forum-cat forum-cat--${categoryClass(t.category)}">${categoryLabel(t.category)}</span>
        <h3>${core.escapeHtml(t.title || "")}</h3>
        <p class="forum-thread-detail__meta">
          Por <strong>${core.escapeHtml(t.authorName || "Ciudadano")}</strong>
          · ${core.escapeHtml(core.formatDate(t.createdAt))}
          · ${posts.length} mensaje${posts.length === 1 ? "" : "s"}
        </p>
      `;
    }

    if (els.forumPostsList) {
      els.forumPostsList.innerHTML = posts
        .map(
          (p, idx) => `
          <article class="forum-post${idx === 0 ? " forum-post--op" : ""}">
            <header class="forum-post__head">
              <strong>${core.escapeHtml(p.authorName || "Ciudadano")}</strong>
              <time>${core.escapeHtml(core.formatDate(p.createdAt))}</time>
              ${idx === 0 ? '<span class="forum-post__badge">Autor</span>' : ""}
            </header>
            <p class="forum-post__body">${formatBody(p.content)}</p>
          </article>
        `
        )
        .join("");
    }

    if (els.forumReplyContent) els.forumReplyContent.value = "";
    core.refreshIcons();
  }

  async function onReply(event) {
    event.preventDefault();
    if (!state.selectedThreadId || !els.forumReplyContent) return;

    const content = els.forumReplyContent.value.trim();
    if (content.length < 5) {
      return setStatus("La respuesta debe tener al menos 5 caracteres.", true);
    }

    try {
      setStatus("Publicando…", false, true);
      await core.api(`/api/forum/threads/${state.selectedThreadId}/posts`, {
        method: "POST",
        token: state.session.token,
        body: {
          content,
          authorId: userId(),
          authorName: authorName()
        }
      });
      await openThread(state.selectedThreadId);
      setStatus("Respuesta publicada.");
    } catch (error) {
      if (!handleAuthError(error)) setStatus(error.message, true);
    }
  }

  async function onCreateThread(event) {
    event.preventDefault();
    if (!els.forumNewTitle || !els.forumNewContent) return;

    const title = els.forumNewTitle.value.trim();
    const content = els.forumNewContent.value.trim();
    const category = els.forumNewCategory ? els.forumNewCategory.value : "AYUDA";

    if (title.length < 5 || content.length < 10) {
      return setStatus("Título (5+) y mensaje (10+) son obligatorios.", true);
    }

    try {
      setStatus("Creando hilo…", false, true);
      const created = await core.api("/api/forum/threads", {
        method: "POST",
        token: state.session.token,
        body: {
          title,
          content,
          category,
          authorId: userId(),
          authorName: authorName()
        }
      });
      els.forumNewThreadForm.reset();
      if (els.forumNewCategory) els.forumNewCategory.value = "AYUDA";
      state.detail = created;
      state.selectedThreadId = created.thread && created.thread.id;
      renderThreadDetail();
      showView("thread");
      setStatus("Hilo creado.");
    } catch (error) {
      if (!handleAuthError(error)) setStatus(error.message, true);
    }
  }

  function categoryLabel(cat) {
    const c = String(cat || "").toUpperCase();
    if (c === "AYUDA") return "Ayuda";
    if (c === "CONSEJOS") return "Consejos";
    if (c === "GENERAL") return "General";
    return cat || "General";
  }

  function categoryClass(cat) {
    const c = String(cat || "").toUpperCase();
    if (c === "AYUDA") return "ayuda";
    if (c === "CONSEJOS") return "consejos";
    return "general";
  }

  function formatBody(text) {
    return core.escapeHtml(String(text || "")).replace(/\n/g, "<br/>");
  }

  function statusNode() {
    return document.querySelector(".dash-topbar #citizenStatus") || document.getElementById("citizenStatus");
  }

  function setStatus(message, isError, loading) {
    const node = statusNode();
    if (!node) return;
    const esc = core.escapeHtml(message);
    const left = loading
      ? `<span class="lucide-spin"><i data-lucide="loader"></i></span>`
      : `<i data-lucide="${isError ? "alert-circle" : "check"}"></i>`;
    node.className = `sync-pill${isError ? " is-error" : ""}`;
    node.innerHTML = `${left}<span>${esc}</span>`;
    core.refreshIcons();
  }
})();
