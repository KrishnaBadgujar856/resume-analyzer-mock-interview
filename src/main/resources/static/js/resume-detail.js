// ==========================================================================
// Resume Analyzer & Mock Interview - Resume Quality Report Logic
// ==========================================================================

let resumeId = null;

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireAuth();

  const params = new URLSearchParams(window.location.search);
  resumeId = params.get('id');

  if (!resumeId) {
    showAlert('alert-container', 'No resume ID provided.');
    return;
  }

  loadResumeDetail();

  // Reanalyze button
  document.getElementById('reanalyze-btn').addEventListener('click', handleReanalyze);

  // Extracted text preview toggle
  const toggleBtn = document.getElementById('toggle-extracted-text');
  const textBody = document.getElementById('extracted-text-body');
  const arrow = document.getElementById('text-toggle-arrow');
  toggleBtn.addEventListener('click', () => {
    const isHidden = textBody.style.display === 'none';
    textBody.style.display = isHidden ? 'block' : 'none';
    arrow.innerHTML = isHidden ? 'Hide &uarr;' : 'Show &darr;';
  });
});

async function loadResumeDetail() {
  try {
    const resume = await API.get(`/api/resumes/${resumeId}`);
    renderHeader(resume);
    renderScores(resume.latestAnalysis, resume.targetRole);
    renderStrengthsAndWeaknesses(resume.latestAnalysis);
    renderSkills(resume.skills || []);
    renderSuggestions(resume.latestAnalysis ? resume.latestAnalysis.suggestions : []);
    renderExtractedText(resume.extractedText);
  } catch (err) {
    showAlert('alert-container', 'Failed to load resume details: ' + err.message);
  }
}

function renderHeader(resume) {
  document.getElementById('resume-title').textContent = resume.title;
  document.getElementById('resume-subtitle').textContent =
    `Target Role: ${resume.targetRole || 'Software Engineer'} • File: ${resume.originalFilename} • Format: ${resume.fileType}`;

  document.getElementById('edit-resume-link').href = `/resume-editor.html?id=${resume.id}`;
  document.getElementById('practice-interview-link').href = `/interview-setup.html?resumeId=${resume.id}`;
  document.getElementById('editor-deep-link').href = `/resume-editor.html?id=${resume.id}`;
}

function renderScores(analysis, targetRole) {
  if (!analysis) {
    document.getElementById('overall-score-val').textContent = '—';
    document.getElementById('analysis-summary-box').textContent = 'No analysis completed yet. Click "Re-Analyze" above.';
    return;
  }

  const overall = analysis.overallScore || 0;
  const completeness = analysis.sectionCompletenessScore || 0;
  const readability = analysis.readabilityScore || 0;
  const relevance = analysis.relevanceScore || 0;

  document.getElementById('overall-score-val').textContent = overall;

  const gauge = document.getElementById('overall-gauge');
  if (overall >= 75) {
    gauge.style.borderColor = 'var(--success)';
    document.getElementById('overall-score-val').style.color = 'var(--success)';
  } else if (overall >= 50) {
    gauge.style.borderColor = 'var(--accent-amber)';
    document.getElementById('overall-score-val').style.color = 'var(--accent-amber)';
  } else {
    gauge.style.borderColor = 'var(--danger)';
    document.getElementById('overall-score-val').style.color = 'var(--danger)';
  }

  document.getElementById('bar-completeness').style.width = `${completeness}%`;
  document.getElementById('val-completeness').textContent = `${completeness}%`;

  document.getElementById('bar-readability').style.width = `${readability}%`;
  document.getElementById('val-readability').textContent = `${readability}%`;

  document.getElementById('bar-relevance').style.width = `${relevance}%`;
  document.getElementById('val-relevance').textContent = `${relevance}%`;
  document.getElementById('role-relevance-label').textContent = `Relevance for ${targetRole || 'Target Role'}`;

  document.getElementById('analysis-summary-box').textContent = analysis.summary || 'Summary unavailable.';
}

function renderStrengthsAndWeaknesses(analysis) {
  const strengthsUl = document.getElementById('strengths-list');
  const weaknessesUl = document.getElementById('weaknesses-list');

  if (!analysis) {
    strengthsUl.innerHTML = '<li>No data.</li>';
    weaknessesUl.innerHTML = '<li>No data.</li>';
    return;
  }

  if (analysis.strengths && analysis.strengths.length > 0) {
    strengthsUl.innerHTML = analysis.strengths.map(s => `<li>${escapeHtml(s)}</li>`).join('');
  } else {
    strengthsUl.innerHTML = '<li>No specific standout strengths detected yet.</li>';
  }

  if (analysis.weaknesses && analysis.weaknesses.length > 0) {
    weaknessesUl.innerHTML = analysis.weaknesses.map(w => `<li>${escapeHtml(w)}</li>`).join('');
  } else {
    weaknessesUl.innerHTML = '<li>No major structural weaknesses detected.</li>';
  }
}

function renderSkills(skills) {
  const container = document.getElementById('skills-container');
  const countBadge = document.getElementById('skills-count-badge');

  countBadge.textContent = `${skills.length} Skills`;

  if (skills.length === 0) {
    container.innerHTML = '<span style="color: var(--text-muted);">No technical skills detected. Ensure technologies and libraries are spelled standardly in your document.</span>';
    return;
  }

  let html = '';
  skills.forEach(s => {
    let badgeClass = 'badge-primary';
    if (s.category === 'FRAMEWORK') badgeClass = 'badge-success';
    else if (s.category === 'DATABASE') badgeClass = 'badge-warning';
    else if (s.category === 'SOFT_SKILL') badgeClass = 'badge-neutral';

    html += `<span class="badge ${badgeClass}" style="font-size: 0.85rem; padding: 0.35rem 0.65rem;">${escapeHtml(s.name)}</span>`;
  });

  container.innerHTML = html;
}

function renderSuggestions(suggestions) {
  const container = document.getElementById('suggestions-container');
  if (!suggestions || suggestions.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 1.5rem; color: var(--text-muted);">
        No targeted suggestions generated at this time. Your sections and formatting look solid!
      </div>
    `;
    return;
  }

  let html = '<div style="display: flex; flex-direction: column; gap: 1rem;">';
  suggestions.forEach(s => {
    const isAccepted = s.status === 'ACCEPTED';
    const isRejected = s.status === 'REJECTED';

    let statusTag = '';
    if (isAccepted) statusTag = '<span class="badge badge-success">Accepted</span>';
    else if (isRejected) statusTag = '<span class="badge badge-neutral">Dismissed</span>';

    html += `
      <div style="border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 1rem; background: ${isRejected ? 'var(--bg-subtle)' : 'white'};">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span class="badge badge-primary">${escapeHtml(s.category)}</span>
            <span style="font-weight: 700; font-size: 0.95rem;">${escapeHtml(s.issueDescription)}</span>
          </div>
          <div>${statusTag}</div>
        </div>
        <p style="font-size: 0.9rem; color: var(--text-secondary); margin-bottom: 0.75rem;">
          <strong>Suggested Action:</strong> ${escapeHtml(s.suggestedAction)}
        </p>
        ${s.originalSnippet ? `
          <div style="font-size: 0.85rem; color: var(--danger); background: var(--danger-light); padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); margin-bottom: 0.4rem;">
            <strong>Current:</strong> ${escapeHtml(s.originalSnippet)}
          </div>
        ` : ''}
        ${s.suggestedSnippet ? `
          <div style="font-size: 0.85rem; color: var(--success); background: var(--success-light); padding: 0.4rem 0.6rem; border-radius: var(--radius-sm); margin-bottom: 0.75rem;">
            <strong>Recommended Improvement:</strong> ${escapeHtml(s.suggestedSnippet)}
          </div>
        ` : ''}
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
          ${!isAccepted ? `<button onclick="updateSuggestion(${s.id}, 'ACCEPTED')" class="btn btn-secondary btn-sm" style="color: var(--success); border-color: var(--success-border);">Accept Suggestion</button>` : ''}
          ${!isRejected ? `<button onclick="updateSuggestion(${s.id}, 'REJECTED')" class="btn btn-secondary btn-sm">Dismiss</button>` : ''}
        </div>
      </div>
    `;
  });
  html += '</div>';

  container.innerHTML = html;
}

function renderExtractedText(text) {
  document.getElementById('extracted-text-content').textContent = text || 'No extracted text found.';
}

async function updateSuggestion(id, status) {
  try {
    await API.patch(`/api/resumes/suggestions/${id}`, { status });
    loadResumeDetail();
  } catch (err) {
    showAlert('alert-container', 'Failed to update suggestion: ' + err.message);
  }
}

async function handleReanalyze() {
  const btn = document.getElementById('reanalyze-btn');
  btn.disabled = true;
  btn.textContent = 'Analyzing...';
  try {
    await API.post(`/api/resumes/${resumeId}/analyze`);
    showAlert('alert-container', 'Resume re-analysis complete!', 'success');
    loadResumeDetail();
  } catch (err) {
    showAlert('alert-container', 'Re-analysis failed: ' + err.message);
  } finally {
    btn.disabled = false;
    btn.textContent = '🔄 Re-Analyze';
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
