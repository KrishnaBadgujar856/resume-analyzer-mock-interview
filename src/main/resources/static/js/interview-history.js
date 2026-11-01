// ==========================================================================
// Resume Analyzer & Mock Interview - Interview History Logic
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireAuth();

  loadHistory();

  const filterBtn = document.getElementById('filter-btn');
  const resetBtn = document.getElementById('reset-filter-btn');
  const roleInput = document.getElementById('role-filter');
  const statusSelect = document.getElementById('status-filter');

  filterBtn.addEventListener('click', () => {
    loadHistory(roleInput.value.trim(), statusSelect.value);
  });

  resetBtn.addEventListener('click', () => {
    roleInput.value = '';
    statusSelect.value = '';
    resetBtn.style.display = 'none';
    loadHistory();
  });
});

async function loadHistory(role = '', status = '') {
  const tbody = document.getElementById('history-table-body');
  const emptyContainer = document.getElementById('history-empty-container');
  const resetBtn = document.getElementById('reset-filter-btn');

  if (role || status) {
    resetBtn.style.display = 'inline-flex';
  } else {
    resetBtn.style.display = 'none';
  }

  try {
    let url = '/api/interviews?';
    if (role) url += `role=${encodeURIComponent(role)}&`;
    if (status) url += `status=${encodeURIComponent(status)}&`;

    const sessions = await API.get(url);

    if (sessions.length === 0) {
      tbody.innerHTML = '';
      emptyContainer.style.display = 'block';
      emptyContainer.innerHTML = `
        <div class="empty-state" style="border: none;">
          <div class="empty-state-icon">🎯</div>
          <div class="empty-state-title">No Interview Sessions Found</div>
          <div class="empty-state-desc">You haven't conducted any mock interviews matching these filters yet. Start a session now!</div>
          <a href="/interview-setup.html" class="btn btn-primary btn-sm">+ Start Mock Interview</a>
        </div>
      `;
      return;
    }

    emptyContainer.style.display = 'none';
    let html = '';
    sessions.forEach(s => {
      const isCompleted = s.status === 'COMPLETED';
      const statusBadge = isCompleted
        ? '<span class="badge badge-success">Completed</span>'
        : '<span class="badge badge-warning">In Progress</span>';

      const scoreBadge = s.overallScore != null
        ? `<span class="badge ${s.overallScore >= 70 ? 'badge-success' : 'badge-warning'}">${s.overallScore}%</span>`
        : '<span class="badge badge-neutral">—</span>';

      const dateStr = new Date(s.createdAt).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });

      const actionLink = isCompleted ? `/interview-results.html?id=${s.id}` : `/interview.html?id=${s.id}`;
      const actionText = isCompleted ? 'View Results' : 'Resume';
      const actionClass = isCompleted ? 'btn-secondary' : 'btn-primary';

      html += `
        <tr>
          <td>
            <div style="font-weight: 600;">
              <a href="${actionLink}">${escapeHtml(s.targetRole)}</a>
            </div>
            ${s.focusArea ? `<div style="font-size: 0.8rem; color: var(--text-muted);">Focus: ${escapeHtml(s.focusArea)}</div>` : ''}
          </td>
          <td>
            <a href="/resume-detail.html?id=${s.resumeId}" style="font-size: 0.9rem;">${escapeHtml(s.resumeTitle)}</a>
          </td>
          <td>
            <div style="font-size: 0.85rem;">${s.interviewType}</div>
            <div style="font-size: 0.75rem; color: var(--text-muted);">${s.difficulty}</div>
          </td>
          <td>${s.answeredCount || 0} / ${s.questionCount}</td>
          <td>${statusBadge}</td>
          <td>${scoreBadge}</td>
          <td style="font-size: 0.85rem; color: var(--text-muted);">${dateStr}</td>
          <td style="text-align: right;">
            <a href="${actionLink}" class="btn ${actionClass} btn-sm">${actionText}</a>
          </td>
        </tr>
      `;
    });

    tbody.innerHTML = html;
  } catch (err) {
    showAlert('alert-container', 'Failed to load interview history: ' + err.message);
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
