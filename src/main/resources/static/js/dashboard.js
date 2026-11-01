// ==========================================================================
// Resume Analyzer & Mock Interview - Dashboard Logic
// ==========================================================================

document.addEventListener('DOMContentLoaded', async () => {
  Auth.requireAuth();

  const user = API.getUser();
  if (user && user.name) {
    document.getElementById('welcome-heading').textContent = `Welcome Back, ${user.name}`;
  }

  try {
    const stats = await API.get('/api/dashboard/stats');
    renderStats(stats);
    renderRecentResumes(stats.recentResumes || []);
    renderRecentInterviews(stats.recentInterviews || []);
  } catch (err) {
    showAlert('alert-container', 'Failed to load dashboard data: ' + err.message);
  }
});

function renderStats(stats) {
  document.getElementById('stat-total-resumes').textContent = stats.totalResumes || 0;
  document.getElementById('stat-total-interviews').textContent = stats.totalInterviews || 0;
  document.getElementById('stat-completed-interviews').textContent = stats.completedInterviews || 0;
  document.getElementById('stat-avg-score').textContent = stats.averageScore != null ? `${stats.averageScore}%` : '—';
}

function renderRecentResumes(resumes) {
  const container = document.getElementById('recent-resumes-container');
  if (!container) return;

  if (resumes.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 1.5rem;">
        <div class="empty-state-icon">📄</div>
        <div class="empty-state-title">No Resumes Yet</div>
        <div class="empty-state-desc">Upload your PDF or DOCX resume to get instant quality analysis and suggestions.</div>
        <a href="/resumes.html?upload=true" class="btn btn-primary btn-sm">Upload Resume</a>
      </div>
    `;
    return;
  }

  let html = '<div style="display: flex; flex-direction: column; gap: 0.75rem;">';
  resumes.forEach(r => {
    const scoreBadge = r.overallScore != null
      ? `<span class="badge ${r.overallScore >= 70 ? 'badge-success' : 'badge-warning'}">${r.overallScore}/100</span>`
      : `<span class="badge badge-neutral">${r.status}</span>`;

    html += `
      <div style="border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 0.85rem; display: flex; justify-content: space-between; align-items: center; gap: 0.5rem;">
        <div>
          <div style="font-weight: 600; font-size: 0.95rem;">
            <a href="/resume-detail.html?id=${r.id}">${escapeHtml(r.title)}</a>
          </div>
          <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.15rem;">
            Role: ${escapeHtml(r.targetRole || 'Not specified')} &bull; ${r.fileType} (${formatBytes(r.fileSize)})
          </div>
        </div>
        <div style="display: flex; align-items: center; gap: 0.6rem;">
          ${scoreBadge}
          <a href="/resume-detail.html?id=${r.id}" class="btn btn-secondary btn-sm" title="View Quality Report">Report</a>
        </div>
      </div>
    `;
  });
  html += '</div>';
  container.innerHTML = html;
}

function renderRecentInterviews(interviews) {
  const container = document.getElementById('recent-interviews-container');
  if (!container) return;

  if (interviews.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 1.5rem;">
        <div class="empty-state-icon">🎯</div>
        <div class="empty-state-title">No Practice Interviews</div>
        <div class="empty-state-desc">Take a mock interview tailored to your resume skills to evaluate your readiness.</div>
        <a href="/interview-setup.html" class="btn btn-primary btn-sm">Start Interview</a>
      </div>
    `;
    return;
  }

  let html = '<div style="display: flex; flex-direction: column; gap: 0.75rem;">';
  interviews.forEach(i => {
    const isCompleted = i.status === 'COMPLETED';
    const statusBadge = isCompleted
      ? `<span class="badge badge-success">${i.overallScore}% Score</span>`
      : `<span class="badge badge-warning">${i.status.replace('_', ' ')}</span>`;

    const actionUrl = isCompleted
      ? `/interview-results.html?id=${i.id}`
      : `/interview.html?id=${i.id}`;

    const actionText = isCompleted ? 'View Results' : 'Resume';

    html += `
      <div style="border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 0.85rem; display: flex; justify-content: space-between; align-items: center; gap: 0.5rem;">
        <div>
          <div style="font-weight: 600; font-size: 0.95rem;">
            <a href="${actionUrl}">${escapeHtml(i.targetRole)}</a>
          </div>
          <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.15rem;">
            ${i.interviewType} &bull; ${i.difficulty} &bull; ${i.questionCount} Questions
          </div>
        </div>
        <div style="display: flex; align-items: center; gap: 0.6rem;">
          ${statusBadge}
          <a href="${actionUrl}" class="btn btn-secondary btn-sm">${actionText}</a>
        </div>
      </div>
    `;
  });
  html += '</div>';
  container.innerHTML = html;
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

function formatBytes(bytes) {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}
