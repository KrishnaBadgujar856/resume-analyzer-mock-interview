// ==========================================================================
// Resume Analyzer & Mock Interview - Resumes CRUD & Server Search
// ==========================================================================

let resumesCache = [];

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireAuth();

  loadResumes();

  // Search listeners
  const searchInput = document.getElementById('search-input');
  const searchBtn = document.getElementById('search-btn');
  const clearBtn = document.getElementById('clear-search-btn');

  searchBtn.addEventListener('click', () => {
    const query = searchInput.value.trim();
    loadResumes(query);
  });

  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      const query = searchInput.value.trim();
      loadResumes(query);
    }
  });

  clearBtn.addEventListener('click', () => {
    searchInput.value = '';
    clearBtn.style.display = 'none';
    loadResumes();
  });

  // Modal setup
  setupUploadModal();
  setupEditModal();
  setupDeleteModal();

  // URL query parameter check
  const params = new URLSearchParams(window.location.search);
  if (params.get('upload') === 'true') {
    openUploadModal();
  }
});

async function loadResumes(query = '') {
  const tbody = document.getElementById('resumes-table-body');
  const emptyContainer = document.getElementById('resumes-empty-container');
  const clearBtn = document.getElementById('clear-search-btn');

  if (query) {
    clearBtn.style.display = 'inline-flex';
  } else {
    clearBtn.style.display = 'none';
  }

  try {
    const endpoint = query ? `/api/resumes?search=${encodeURIComponent(query)}` : '/api/resumes';
    const resumes = await API.get(endpoint);
    resumesCache = resumes;

    if (resumes.length === 0) {
      tbody.innerHTML = '';
      emptyContainer.style.display = 'block';
      if (query) {
        emptyContainer.innerHTML = `
          <div class="empty-state" style="border: none;">
            <div class="empty-state-icon">🔍</div>
            <div class="empty-state-title">No Matching Resumes Found</div>
            <div class="empty-state-desc">No resumes matched your search term "<strong>${escapeHtml(query)}</strong>". Try searching for different skills or clear the search.</div>
            <button onclick="document.getElementById('clear-search-btn').click()" class="btn btn-secondary btn-sm">Clear Search</button>
          </div>
        `;
      } else {
        emptyContainer.innerHTML = `
          <div class="empty-state" style="border: none;">
            <div class="empty-state-icon">📄</div>
            <div class="empty-state-title">Your Resume Library is Empty</div>
            <div class="empty-state-desc">Upload your first resume in PDF or DOCX format to receive automated quality scoring, section diagnostics, and mock interviews.</div>
            <button onclick="openUploadModal()" class="btn btn-primary btn-sm">+ Upload Your Resume</button>
          </div>
        `;
      }
      return;
    }

    emptyContainer.style.display = 'none';
    let html = '';
    resumes.forEach(r => {
      const scoreBadge = r.overallScore != null
        ? `<span class="badge ${r.overallScore >= 70 ? 'badge-success' : 'badge-warning'}">${r.overallScore}/100</span>`
        : `<span class="badge badge-neutral">${r.status}</span>`;

      const formattedDate = new Date(r.createdAt).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });

      html += `
        <tr>
          <td>
            <div style="font-weight: 600;">
              <a href="/resume-detail.html?id=${r.id}">${escapeHtml(r.title)}</a>
            </div>
            <div style="font-size: 0.8rem; color: var(--text-muted);">
              ${escapeHtml(r.originalFilename)} &bull; ${formatBytes(r.fileSize)}
            </div>
          </td>
          <td><span class="badge badge-neutral">${r.fileType}</span></td>
          <td>${escapeHtml(r.targetRole || 'Not specified')}</td>
          <td>${scoreBadge}</td>
          <td><span class="badge badge-primary">${r.skillCount || 0} skills</span></td>
          <td><span class="badge badge-neutral">v${r.versionCount || 1}</span></td>
          <td style="font-size: 0.85rem; color: var(--text-muted);">${formattedDate}</td>
          <td style="text-align: right;">
            <div style="display: inline-flex; gap: 0.4rem;">
              <a href="/resume-detail.html?id=${r.id}" class="btn btn-secondary btn-sm" title="View Quality Report">Report</a>
              <a href="/resume-editor.html?id=${r.id}" class="btn btn-secondary btn-sm" title="Review Suggestions & Edit">Edit</a>
              <a href="/interview-setup.html?resumeId=${r.id}" class="btn btn-primary btn-sm" title="Practice Interview">Practice</a>
              <button onclick="openEditModal(${r.id})" class="btn btn-secondary btn-sm" title="Edit Metadata">✏️</button>
              <button onclick="openDeleteModal(${r.id}, '${escapeForJs(r.title)}')" class="btn btn-danger btn-sm" title="Delete Resume">🗑️</button>
            </div>
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  } catch (err) {
    showAlert('alert-container', 'Failed to load resumes: ' + err.message);
  }
}

// Upload Modal Logic
function setupUploadModal() {
  const modal = document.getElementById('upload-modal');
  const openBtn = document.getElementById('open-upload-modal-btn');
  const closeBtn = document.getElementById('close-upload-modal');
  const cancelBtn = document.getElementById('cancel-upload-btn');
  const form = document.getElementById('upload-form');
  const submitBtn = document.getElementById('upload-submit-btn');

  openBtn.addEventListener('click', openUploadModal);
  closeBtn.addEventListener('click', closeUploadModal);
  cancelBtn.addEventListener('click', closeUploadModal);

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert('upload-alert');

    const fileInput = document.getElementById('upload-file');
    const titleInput = document.getElementById('upload-title');
    const roleInput = document.getElementById('upload-role');

    if (!fileInput.files || fileInput.files.length === 0) {
      showAlert('upload-alert', 'Please select a resume file (.pdf or .docx).');
      return;
    }

    const file = fileInput.files[0];
    if (file.size > 10 * 1024 * 1024) {
      showAlert('upload-alert', 'File size exceeds maximum limit of 10MB.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    if (titleInput.value.trim()) formData.append('title', titleInput.value.trim());
    if (roleInput.value.trim()) formData.append('targetRole', roleInput.value.trim());

    submitBtn.disabled = true;
    submitBtn.textContent = 'Extracting & Analyzing...';

    try {
      const res = await API.upload('/api/resumes', formData);
      closeUploadModal();
      form.reset();
      // Redirect to the detail page for immediate feedback
      window.location.href = `/resume-detail.html?id=${res.id}`;
    } catch (err) {
      showAlert('upload-alert', err.message || 'Upload failed.');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Upload & Analyze';
    }
  });
}

function openUploadModal() {
  document.getElementById('upload-modal').classList.add('show');
}

function closeUploadModal() {
  document.getElementById('upload-modal').classList.remove('show');
  clearAlert('upload-alert');
}

// Edit Modal Logic
function setupEditModal() {
  const modal = document.getElementById('edit-modal');
  const closeBtn = document.getElementById('close-edit-modal');
  const cancelBtn = document.getElementById('cancel-edit-btn');
  const form = document.getElementById('edit-form');
  const submitBtn = document.getElementById('edit-submit-btn');

  closeBtn.addEventListener('click', () => modal.classList.remove('show'));
  cancelBtn.addEventListener('click', () => modal.classList.remove('show'));

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert('edit-alert');

    const id = document.getElementById('edit-resume-id').value;
    const title = document.getElementById('edit-title').value.trim();
    const targetRole = document.getElementById('edit-role').value.trim();

    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    try {
      await API.put(`/api/resumes/${id}`, { title, targetRole });
      modal.classList.remove('show');
      loadResumes();
    } catch (err) {
      showAlert('edit-alert', err.message || 'Failed to update resume.');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Save Changes';
    }
  });
}

function openEditModal(resumeId) {
  const resume = resumesCache.find(r => r.id === resumeId);
  if (!resume) return;

  document.getElementById('edit-resume-id').value = resume.id;
  document.getElementById('edit-title').value = resume.title;
  document.getElementById('edit-role').value = resume.targetRole || '';
  clearAlert('edit-alert');
  document.getElementById('edit-modal').classList.add('show');
}

// Delete Modal Logic
function setupDeleteModal() {
  const modal = document.getElementById('delete-modal');
  const closeBtn = document.getElementById('close-delete-modal');
  const cancelBtn = document.getElementById('cancel-delete-btn');
  const confirmBtn = document.getElementById('confirm-delete-btn');

  closeBtn.addEventListener('click', () => modal.classList.remove('show'));
  cancelBtn.addEventListener('click', () => modal.classList.remove('show'));

  confirmBtn.addEventListener('click', async () => {
    const id = document.getElementById('delete-resume-id').value;
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Deleting...';

    try {
      await API.delete(`/api/resumes/${id}`);
      modal.classList.remove('show');
      showAlert('alert-container', 'Resume deleted successfully.', 'success');
      loadResumes();
    } catch (err) {
      showAlert('alert-container', 'Failed to delete resume: ' + err.message);
    } finally {
      confirmBtn.disabled = false;
      confirmBtn.textContent = 'Yes, Delete Resume';
    }
  });
}

function openDeleteModal(id, title) {
  document.getElementById('delete-resume-id').value = id;
  document.getElementById('delete-resume-title').textContent = title;
  document.getElementById('delete-modal').classList.add('show');
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

function escapeForJs(str) {
  if (!str) return '';
  return str.replace(/'/g, "\\'").replace(/"/g, '\\"');
}

function formatBytes(bytes) {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}
