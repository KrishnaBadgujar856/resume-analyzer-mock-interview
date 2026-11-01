// ==========================================================================
// Resume Analyzer & Mock Interview - Results & Evaluation Feedback Logic
// ==========================================================================

let sessionId = null;

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireAuth();

  const params = new URLSearchParams(window.location.search);
  sessionId = params.get('id');

  if (!sessionId) {
    showAlert('alert-container', 'No session ID provided.');
    return;
  }

  loadResults();
});

async function loadResults() {
  try {
    const session = await API.get(`/api/interviews/${sessionId}`);

    // If still in progress, redirect back
    if (session.status !== 'COMPLETED') {
      window.location.href = `/interview.html?id=${sessionId}`;
      return;
    }

    renderHeader(session);
    renderOverallScore(session);
    renderFeedbackSummary(session);
    renderQuestionsEvaluation(session);
  } catch (err) {
    showAlert('alert-container', 'Failed to load evaluation results: ' + err.message);
  }
}

function renderHeader(session) {
  document.getElementById('results-title').textContent = `${session.targetRole} Results`;
  document.getElementById('results-subtitle').textContent =
    `${session.interviewType} • ${session.difficulty} • Grounded in "${session.resumeTitle}" • Completed on ${new Date(session.updatedAt || session.createdAt).toLocaleDateString()}`;

  document.getElementById('review-resume-btn').href = `/resume-detail.html?id=${session.resumeId}`;

  if (session.demoMode) {
    document.getElementById('demo-mode-banner').style.display = 'block';
  }
}

function renderOverallScore(session) {
  const score = session.overallScore || 0;
  document.getElementById('overall-score-val').textContent = `${score}%`;

  const gauge = document.getElementById('overall-score-gauge');
  const ratingEl = document.getElementById('performance-rating-text');

  if (score >= 80) {
    gauge.style.borderColor = 'var(--success)';
    document.getElementById('overall-score-val').style.color = 'var(--success)';
    ratingEl.textContent = '🌟 Strong Candidate Performance';
    ratingEl.style.color = 'var(--success)';
  } else if (score >= 60) {
    gauge.style.borderColor = 'var(--accent-amber)';
    document.getElementById('overall-score-val').style.color = 'var(--accent-amber)';
    ratingEl.textContent = '👍 Proficient — Ready with Minor Polish';
    ratingEl.style.color = 'var(--accent-amber)';
  } else {
    gauge.style.borderColor = 'var(--danger)';
    document.getElementById('overall-score-val').style.color = 'var(--danger)';
    ratingEl.textContent = '📈 Developing Foundation — Needs Targeted Practice';
    ratingEl.style.color = 'var(--danger)';
  }
}

function renderFeedbackSummary(session) {
  document.getElementById('summary-feedback-text').textContent =
    `Your overall performance across ${session.questionCount} questions scored ${session.overallScore}%. ` +
    `Evaluations are calculated mathematically using our four-criterion rubric: Correctness (40%), Understanding (25%), Relevance (20%), and Clarity (15%).`;

  document.getElementById('strengths-text').textContent = session.strengths || 'No specific strengths recorded.';
  document.getElementById('improvements-text').textContent = session.improvements || 'No specific areas recorded.';
  document.getElementById('topics-text').textContent = session.topicsToPrepare || 'Review core technical fundamentals and architecture.';
}

function renderQuestionsEvaluation(session) {
  const container = document.getElementById('questions-eval-container');
  if (!session.questions || session.questions.length === 0) {
    container.innerHTML = '<div style="color: var(--text-muted);">No question details found.</div>';
    return;
  }

  let html = '';
  session.questions.forEach((q, idx) => {
    // Find answer
    const answer = session.answers ? session.answers.find(a => a.questionId === q.id) : null;
    const evalData = answer ? answer.evaluation : null;

    const answerText = (answer && answer.answerText && answer.answerText.trim().length > 0) ? answer.answerText : '(No response submitted)';
    const totalScore = evalData ? evalData.totalScore : 0;
    const correctness = evalData ? evalData.correctnessScore : 0;
    const understanding = evalData ? evalData.understandingScore : 0;
    const relevance = evalData ? evalData.relevanceScore : 0;
    const clarity = evalData ? evalData.clarityScore : 0;

    const scoreClass = totalScore >= 75 ? 'badge-success' : (totalScore >= 55 ? 'badge-warning' : 'badge-danger');

    html += `
      <div class="eval-card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; flex-wrap: wrap; gap: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-weight: 800; color: var(--primary); font-size: 1.1rem;">Q${q.questionOrder}.</span>
            <span class="badge badge-primary">${escapeHtml(q.questionCategory || 'Technical')}</span>
          </div>
          <span class="badge ${scoreClass}" style="font-size: 0.9rem; padding: 0.35rem 0.75rem;">
            Score: ${totalScore}/100
          </span>
        </div>

        <h4 style="font-size: 1.1rem; font-weight: 700; color: var(--text-primary); margin-bottom: 1rem; line-height: 1.4;">
          ${escapeHtml(q.questionText)}
        </h4>

        <!-- Rubric Grid -->
        <div class="rubric-grid">
          <div class="rubric-box">
            <div class="rubric-val">${correctness}%</div>
            <div class="rubric-title">Correctness (40%)</div>
          </div>
          <div class="rubric-box">
            <div class="rubric-val">${understanding}%</div>
            <div class="rubric-title">Understanding (25%)</div>
          </div>
          <div class="rubric-box">
            <div class="rubric-val">${relevance}%</div>
            <div class="rubric-title">Relevance (20%)</div>
          </div>
          <div class="rubric-box">
            <div class="rubric-val">${clarity}%</div>
            <div class="rubric-title">Clarity (15%)</div>
          </div>
        </div>

        <!-- Candidate's Submitted Response -->
        <div style="margin-bottom: 1rem;">
          <div style="font-weight: 700; font-size: 0.9rem; margin-bottom: 0.35rem;">Your Submitted Answer:</div>
          <div style="background: var(--bg-subtle); border-radius: var(--radius-sm); padding: 0.85rem; font-size: 0.9rem; color: var(--text-secondary); line-height: 1.6; white-space: pre-wrap;">
            ${escapeHtml(answerText)}
          </div>
        </div>

        <!-- Evaluator Feedback -->
        ${evalData && evalData.feedback ? `
          <div style="background: #f0fdf4; border-left: 3px solid var(--success); padding: 0.75rem 1rem; border-radius: 0 var(--radius-sm) var(--radius-sm) 0; margin-bottom: 0.75rem; font-size: 0.9rem;">
            <strong style="color: #14532d;">Evaluator Feedback:</strong>
            <div style="color: #166534; margin-top: 0.2rem;">${escapeHtml(evalData.feedback)}</div>
          </div>
        ` : ''}

        <!-- Missing Concepts -->
        ${evalData && evalData.missingPoints ? `
          <div style="background: #fffbeb; border-left: 3px solid var(--accent-amber); padding: 0.75rem 1rem; border-radius: 0 var(--radius-sm) var(--radius-sm) 0; margin-bottom: 0.75rem; font-size: 0.9rem;">
            <strong style="color: #78350f;">Important Points / Missing Concepts:</strong>
            <div style="color: #92400e; margin-top: 0.2rem;">${escapeHtml(evalData.missingPoints)}</div>
          </div>
        ` : ''}

        <!-- Sample Improved Answer -->
        ${evalData && evalData.sampleImprovedAnswer ? `
          <div style="background: #eff6ff; border-left: 3px solid var(--primary); padding: 0.75rem 1rem; border-radius: 0 var(--radius-sm) var(--radius-sm) 0; font-size: 0.9rem;">
            <strong style="color: var(--primary-dark);">Sample Benchmark Answer for Learning:</strong>
            <div style="color: #1e40af; margin-top: 0.2rem; line-height: 1.5;">${escapeHtml(evalData.sampleImprovedAnswer)}</div>
          </div>
        ` : ''}
      </div>
    `;
  });

  container.innerHTML = html;
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
