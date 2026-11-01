// ==========================================================================
// Resume Analyzer & Mock Interview - Resume Editor & Suggestion Review
// ==========================================================================

let resumeId = null;
let currentResume = null;
let originalLoadedText = '';

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireAuth();

  const params = new URLSearchParams(window.location.search);
  resumeId = params.get('id');

  if (!resumeId) {
    showAlert('alert-container', 'No resume ID provided.');
    return;
  }

  loadEditorData();

  // Word/Character counter
  const textarea = document.getElementById('resume-text-editor');
  textarea.addEventListener('input', updateCounts);

  // Save buttons
  document.getElementById('save-version-btn').addEventListener('click', handleSaveVersion);
  document.getElementById('save-bottom-btn').addEventListener('click', handleSaveVersion);

  // Revert button
  document.getElementById('revert-btn').addEventListener('click', () => {
    if (confirm('Revert all unsaved text changes back to the currently loaded version?')) {
      textarea.value = originalLoadedText;
      updateCounts();
    }
  });

  // Bullet helper
  document.getElementById('format-bullets-btn').addEventListener('click', () => {
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const text = textarea.value;
    const bulletTemplate = "\n- Accomplished [X], as measured by [Y], by implementing [Z] using [Technology].\n";
    textarea.value = text.substring(0, start) + bulletTemplate + text.substring(end);
    textarea.focus();
    updateCounts();
  });

  // Version selector
  document.getElementById('version-select').addEventListener('change', (e) => {
    const selectedVersionId = parseInt(e.target.value);
    const ver = currentResume.versions.find(v => v.id === selectedVersionId);
    if (ver) {
      textarea.value = ver.contentText || '';
      originalLoadedText = textarea.value;
      updateCounts();
      document.getElementById('version-meta-text').textContent =
        `Viewing Version ${ver.versionNumber}: "${ver.versionNote || 'No notes'}" (${new Date(ver.createdAt).toLocaleDateString()})`;
    }
  });
});

async function loadEditorData() {
  try {
    const resume = await API.get(`/api/resumes/${resumeId}`);
    currentResume = resume;

    document.getElementById('editor-title').textContent = `Editing: ${resume.title}`;
    document.getElementById('back-to-report-link').href = `/resume-detail.html?id=${resume.id}`;

    const textarea = document.getElementById('resume-text-editor');
    textarea.value = resume.extractedText || '';
    originalLoadedText = textarea.value;
    updateCounts();

    renderVersionHistory(resume.versions || []);
    renderSuggestionsSidebar(resume.latestAnalysis ? resume.latestAnalysis.suggestions : []);
  } catch (err) {
    showAlert('alert-container', 'Failed to load resume editor data: ' + err.message);
  }
}

function updateCounts() {
  const text = document.getElementById('resume-text-editor').value;
  const charCount = text.length;
  const wordCount = text.trim() ? text.trim().split(/\s+/).length : 0;
  document.getElementById('word-char-count').textContent = `${charCount} characters • ${wordCount} words`;
}

function renderVersionHistory(versions) {
  const select = document.getElementById('version-select');
  const badge = document.getElementById('version-count-badge');

  badge.textContent = `${versions.length} Version${versions.length > 1 ? 's' : ''}`;

  if (versions.length === 0) {
    select.innerHTML = '<option value="">Version 1 (Initial)</option>';
    return;
  }

  select.innerHTML = versions.map(v => `
    <option value="${v.id}">Version ${v.versionNumber} — ${v.versionNote ? escapeHtml(v.versionNote) : 'Saved revision'}</option>
  `).join('');
}

function renderSuggestionsSidebar(suggestions) {
  const container = document.getElementById('suggestions-sidebar');
  if (!suggestions || suggestions.length === 0) {
    container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.85rem;">No active suggestions for this resume.</div>';
    return;
  }

  let html = '';
  suggestions.forEach(s => {
    const isAccepted = s.status === 'ACCEPTED';
    const isRejected = s.status === 'REJECTED';

    html += `
      <div class="suggestion-card" style="border-left: 3px solid ${isAccepted ? 'var(--success)' : 'var(--primary)'};">
        <div style="font-weight: 700; font-size: 0.85rem; color: var(--primary-dark); margin-bottom: 0.25rem;">
          ${escapeHtml(s.category)}
        </div>
        <div style="color: var(--text-secondary); margin-bottom: 0.4rem;">
          ${escapeHtml(s.suggestedAction)}
        </div>
        ${s.suggestedSnippet ? `
          <div style="background: var(--bg-subtle); padding: 0.35rem 0.5rem; border-radius: var(--radius-sm); font-size: 0.8rem; margin-bottom: 0.5rem; font-family: monospace;">
            ${escapeHtml(s.suggestedSnippet)}
          </div>
          <div style="display: flex; gap: 0.4rem; justify-content: flex-end;">
            <button onclick="applySnippet('${escapeForJs(s.originalSnippet || '')}', '${escapeForJs(s.suggestedSnippet)}', ${s.id})" class="btn btn-primary btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">
              Apply to Text
            </button>
            ${!isAccepted ? `<button onclick="setSuggestionStatus(${s.id}, 'ACCEPTED')" class="btn btn-secondary btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Accept</button>` : '<span class="badge badge-success" style="font-size: 0.7rem;">Applied</span>'}
          </div>
        ` : ''}
      </div>
    `;
  });

  container.innerHTML = html;
}

function applySnippet(original, replacement, suggestionId) {
  const textarea = document.getElementById('resume-text-editor');
  let text = textarea.value;

  if (original && text.includes(original)) {
    text = text.replace(original, replacement);
  } else {
    // Append at current selection or end
    const start = textarea.selectionStart;
    text = text.substring(0, start) + "\n" + replacement + "\n" + text.substring(start);
  }

  textarea.value = text;
  updateCounts();
  showAlert('alert-container', 'Suggestion applied to the editor text! Remember to save as a new version.', 'info');
  setSuggestionStatus(suggestionId, 'ACCEPTED');
}

async function setSuggestionStatus(id, status) {
  try {
    await API.patch(`/api/resumes/suggestions/${id}`, { status });
    const resume = await API.get(`/api/resumes/${resumeId}`);
    currentResume = resume;
    renderSuggestionsSidebar(resume.latestAnalysis ? resume.latestAnalysis.suggestions : []);
  } catch (err) {
    // Silently log or display error
  }
}

async function handleSaveVersion() {
  clearAlert('alert-container');
  const text = document.getElementById('resume-text-editor').value.trim();
  const note = document.getElementById('version-note-input').value.trim();

  if (!text) {
    showAlert('alert-container', 'Resume content cannot be empty.');
    return;
  }

  const saveBtn = document.getElementById('save-version-btn');
  const bottomBtn = document.getElementById('save-bottom-btn');
  saveBtn.disabled = true;
  bottomBtn.disabled = true;
  saveBtn.textContent = 'Saving & Analyzing...';
  bottomBtn.textContent = 'Saving & Analyzing...';

  try {
    const newVersion = await API.post(`/api/resumes/${resumeId}/versions`, {
      contentText: text,
      versionNote: note || `Revised content on ${new Date().toLocaleDateString()}`
    });

    showAlert('alert-container', `Successfully saved Version ${newVersion.versionNumber} and updated database analysis!`, 'success');
    document.getElementById('version-note-input').value = '';
    loadEditorData();
  } catch (err) {
    showAlert('alert-container', 'Failed to save revised version: ' + err.message);
  } finally {
    saveBtn.disabled = false;
    bottomBtn.disabled = false;
    saveBtn.textContent = '💾 Save Revised Version';
    bottomBtn.textContent = 'Save as New Version & Re-Analyze';
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

function escapeForJs(str) {
  if (!str) return '';
  return str.replace(/'/g, "\\'").replace(/"/g, '\\"').replace(/\n/g, '\\n').replace(/\r/g, '');
}
