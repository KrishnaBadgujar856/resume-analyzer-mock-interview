// ==========================================================================
// Resume Analyzer & Mock Interview - Interview Setup Logic
// ==========================================================================

let resumesList = [];

document.addEventListener('DOMContentLoaded', async () => {
  Auth.requireAuth();

  const params = new URLSearchParams(window.location.search);
  const preselectedResumeId = params.get('resumeId');

  const resumeSelect = document.getElementById('resume-select');
  const targetRoleInput = document.getElementById('target-role-input');
  const form = document.getElementById('interview-setup-form');
  const startBtn = document.getElementById('start-btn');

  try {
    resumesList = await API.get('/api/resumes');

    if (resumesList.length === 0) {
      resumeSelect.innerHTML = '<option value="">No resumes found. Please upload one first.</option>';
      showAlert('alert-container', 'You need to upload at least one resume before starting an interview. <a href="/resumes.html?upload=true" style="font-weight:700;">Upload Now &rarr;</a>', 'warning');
      startBtn.disabled = true;
      return;
    }

    resumeSelect.innerHTML = '<option value="">-- Choose a resume --</option>' +
      resumesList.map(r => `
        <option value="${r.id}" ${preselectedResumeId && r.id == preselectedResumeId ? 'selected' : ''}>
          ${escapeHtml(r.title)} (${r.targetRole || 'General'} • ${r.fileType})
        </option>
      `).join('');

    // Pre-fill target role
    if (preselectedResumeId) {
      const match = resumesList.find(r => r.id == preselectedResumeId);
      if (match && match.targetRole) {
        targetRoleInput.value = match.targetRole;
      }
    } else if (resumesList.length > 0 && resumesList[0].targetRole) {
      targetRoleInput.value = resumesList[0].targetRole;
    }

    resumeSelect.addEventListener('change', (e) => {
      const selectedId = e.target.value;
      const match = resumesList.find(r => r.id == selectedId);
      if (match && match.targetRole && !targetRoleInput.value) {
        targetRoleInput.value = match.targetRole;
      }
    });

  } catch (err) {
    showAlert('alert-container', 'Failed to load resumes: ' + err.message);
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert('alert-container');

    const resumeId = parseInt(resumeSelect.value);
    const targetRole = targetRoleInput.value.trim();
    const interviewType = document.getElementById('interview-type-select').value;
    const difficulty = document.getElementById('difficulty-select').value;
    const questionCount = parseInt(document.getElementById('question-count-select').value);
    const focusArea = document.getElementById('focus-area-input').value.trim();

    if (!resumeId) {
      showAlert('alert-container', 'Please select a resume.');
      return;
    }

    if (!targetRole) {
      showAlert('alert-container', 'Please enter a target job role.');
      return;
    }

    startBtn.disabled = true;
    startBtn.textContent = 'Generating Grounded Questions...';

    try {
      const session = await API.post('/api/interviews', {
        resumeId,
        targetRole,
        interviewType,
        difficulty,
        questionCount,
        focusArea: focusArea || null
      });

      // Redirect to interactive interview session
      window.location.href = `/interview.html?id=${session.id}`;
    } catch (err) {
      showAlert('alert-container', err.message || 'Failed to start interview.');
      startBtn.disabled = false;
      startBtn.textContent = 'Generate Questions & Begin Interview →';
    }
  });
});

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
