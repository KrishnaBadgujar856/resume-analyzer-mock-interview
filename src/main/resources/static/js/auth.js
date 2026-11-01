// ==========================================================================
// Resume Analyzer & Mock Interview - Auth State & Navbar Management
// ==========================================================================

const Auth = {
  checkAuth() {
    return !!API.getToken();
  },

  requireAuth() {
    if (!this.checkAuth()) {
      window.location.href = '/login.html';
    }
  },

  redirectIfAuthenticated() {
    if (this.checkAuth()) {
      window.location.href = '/dashboard.html';
    }
  },

  logout() {
    API.removeToken();
    API.removeUser();
    window.location.href = '/index.html';
  },

  renderNavbar() {
    const navEl = document.getElementById('main-navbar');
    if (!navEl) return;

    const user = API.getUser();
    const isLoggedIn = !!user && !!API.getToken();
    const currentPath = window.location.pathname;

    let linksHtml = '';
    let userMenuHtml = '';

    if (isLoggedIn) {
      linksHtml = `
        <ul class="nav-links">
          <li><a href="/dashboard.html" class="${currentPath.includes('dashboard') ? 'active' : ''}">Dashboard</a></li>
          <li><a href="/resumes.html" class="${currentPath.includes('resume') ? 'active' : ''}">Resumes</a></li>
          <li><a href="/interview-setup.html" class="${currentPath.includes('interview-setup') ? 'active' : ''}">Practice Interview</a></li>
          <li><a href="/interview-history.html" class="${currentPath.includes('interview-history') ? 'active' : ''}">History</a></li>
        </ul>
      `;

      userMenuHtml = `
        <div class="user-nav-menu">
          <a href="/profile.html" class="user-badge" title="Account Profile">👤 ${user.name || 'User'}</a>
          <button onclick="Auth.logout()" class="btn btn-secondary btn-sm">Log Out</button>
        </div>
      `;
    } else {
      linksHtml = `
        <ul class="nav-links">
          <li><a href="/index.html#features">Features</a></li>
          <li><a href="/index.html#how-it-works">How It Works</a></li>
        </ul>
      `;

      userMenuHtml = `
        <div class="user-nav-menu">
          <a href="/login.html" class="btn btn-secondary btn-sm">Log In</a>
          <a href="/register.html" class="btn btn-primary btn-sm">Get Started</a>
        </div>
      `;
    }

    navEl.innerHTML = `
      <div class="nav-container">
        <a href="${isLoggedIn ? '/dashboard.html' : '/index.html'}" class="brand" title="Resume Analyzer & Mock Interview Home">
          <img src="/favicon.svg" alt="Logo" class="brand-logo" width="30" height="30">
          <span>Resume Analyzer & Mock Interview</span>
        </a>
        ${linksHtml}
        ${userMenuHtml}
      </div>
    `;
  },

  renderFooter() {
    const footerEl = document.getElementById('main-footer');
    if (!footerEl) return;

    footerEl.innerHTML = `
      <div class="footer-container">
        <div style="display: flex; align-items: center; justify-content: center; gap: 0.6rem; margin-bottom: 0.5rem;">
          <img src="/favicon.svg" alt="Logo" width="22" height="22" style="border-radius: 4px; vertical-align: middle;">
          <strong style="color: var(--text-primary); font-size: 1rem;">Resume Analyzer & Mock Interview</strong>
        </div>
        <div style="font-size: 0.9rem; color: var(--text-secondary); margin-bottom: 0.4rem; text-align: center;">
          Analyze your resume. Practice with confidence. Prepare for your next opportunity.
        </div>
        <div style="font-size: 0.82rem; color: var(--text-muted); text-align: center;">
          &copy; ${new Date().getFullYear()} Resume Analyzer & Mock Interview &bull; Professional Career Preparation Platform
        </div>
      </div>
    `;
  }
};

document.addEventListener('DOMContentLoaded', () => {
  Auth.renderNavbar();
  Auth.renderFooter();
});
