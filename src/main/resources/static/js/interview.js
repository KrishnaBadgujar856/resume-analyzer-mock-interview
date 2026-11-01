// ==========================================================================
// Resume Analyzer & Mock Interview - Interactive Interview Session Logic
// ==========================================================================

let sessionId = null;
let sessionData = null;
let currentIndex = 0;
let answersMap = {}; // questionId -> current in-memory draft
let savedAnswersMap = {}; // questionId -> confirmed saved text in MySQL
let isSaving = false;

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireAuth();

  const params = new URLSearchParams(window.location.search);
  sessionId = params.get('id');

  if (!sessionId) {
    showAlert('alert-container', 'No interview session ID provided.');
    return;
  }

  loadSession();

  // Navigation listeners with async save-before-advance
  document.getElementById('prev-question-btn').addEventListener('click', async () => {
    if (isSaving) return;
    const ok = await saveCurrentAnswer(false);
    if (!ok) return; // Halt on error so user does not lose input
    if (currentIndex > 0) {
      currentIndex--;
      renderCurrentQuestion();
    }
  });

  document.getElementById('next-question-btn').addEventListener('click', async () => {
    if (isSaving) return;
    const ok = await saveCurrentAnswer(false);
    if (!ok) return; // Halt on error so user does not lose input
    if (currentIndex < sessionData.questions.length - 1) {
      currentIndex++;
      renderCurrentQuestion();
    } else {
      openSubmitModal();
    }
  });

  document.getElementById('save-answer-btn').addEventListener('click', async () => {
    if (isSaving) return;
    await saveCurrentAnswer(true);
  });

  // Textarea input
  const textarea = document.getElementById('answer-input');
  textarea.addEventListener('input', () => {
    updateCounts();
    if (!sessionData || !sessionData.questions) return;
    const q = sessionData.questions[currentIndex];
    answersMap[q.id] = textarea.value;

    const trimmed = textarea.value.trim();
    if (savedAnswersMap[q.id] !== trimmed) {
      document.getElementById('auto-save-status').textContent = 'Unsaved changes';
      document.getElementById('auto-save-status').style.color = 'var(--accent-amber)';
    } else {
      document.getElementById('auto-save-status').textContent = 'Saved to MySQL';
      document.getElementById('auto-save-status').style.color = 'var(--success)';
    }
    renderPills();
  });

  // Finish button
  document.getElementById('finish-btn').addEventListener('click', async () => {
    if (isSaving) return;
    const ok = await saveCurrentAnswer(false);
    if (!ok) return;
    openSubmitModal();
  });

  // Modal setup
  setupSubmitModal();
});

async function loadSession() {
  try {
    sessionData = await API.get(`/api/interviews/${sessionId}`);

    if (sessionData.status === 'COMPLETED') {
      window.location.href = `/interview-results.html?id=${sessionId}`;
      return;
    }

    document.getElementById('session-title').textContent = `${sessionData.targetRole} Mock Interview`;
    document.getElementById('session-meta-role').textContent =
      `${sessionData.interviewType} • ${sessionData.difficulty} • Based on "${sessionData.resumeTitle}"`;

    // Map existing answers from MySQL persistence
    answersMap = {};
    savedAnswersMap = {};
    if (sessionData.answers) {
      sessionData.answers.forEach(a => {
        const text = a.answerText || '';
        answersMap[a.questionId] = text;
        savedAnswersMap[a.questionId] = text;
      });
    }

    renderPills();
    renderCurrentQuestion();
  } catch (err) {
    showAlert('alert-container', 'Failed to load interview session: ' + err.message);
  }
}

function renderPills() {
  const container = document.getElementById('question-pills-container');
  if (!container || !sessionData || !sessionData.questions) return;

  container.innerHTML = sessionData.questions.map((q, idx) => {
    const hasAnswer = (answersMap[q.id] && answersMap[q.id].trim().length > 0) ||
                      (savedAnswersMap[q.id] && savedAnswersMap[q.id].trim().length > 0);
    const isActive = idx === currentIndex;
    let classes = 'question-nav-btn';
    if (isActive) classes += ' active';
    else if (hasAnswer) classes += ' answered';

    return `
      <button class="${classes}" onclick="jumpToQuestion(${idx})" title="Question ${q.questionOrder}" type="button">
        ${q.questionOrder}
      </button>
    `;
  }).join('');
}

function renderCurrentQuestion() {
  if (!sessionData || !sessionData.questions || sessionData.questions.length === 0) return;

  const q = sessionData.questions[currentIndex];
  const total = sessionData.questions.length;

  // Header progress
  document.getElementById('progress-text').textContent = `Question ${q.questionOrder} of ${total}`;
  const pct = Math.round(((currentIndex + 1) / total) * 100);
  document.getElementById('session-progress-bar').style.width = `${pct}%`;

  const answeredCount = Object.values(savedAnswersMap).filter(v => v && v.trim().length > 0).length;
  document.getElementById('answered-count-text').textContent = `${answeredCount} / ${total} Saved`;

  // Question details
  document.getElementById('question-category-badge').textContent = q.questionCategory || 'Technical';
  document.getElementById('question-difficulty-badge').textContent = q.difficulty || sessionData.difficulty;
  document.getElementById('question-text').textContent = q.questionText;
  document.getElementById('question-hint').textContent = q.contextHint || 'Elaborate clearly and provide real-world context.';

  // Answer text
  const textarea = document.getElementById('answer-input');
  textarea.value = answersMap[q.id] !== undefined ? answersMap[q.id] : (savedAnswersMap[q.id] || '');
  updateCounts();

  // Save status badge
  const trimmed = textarea.value.trim();
  if (savedAnswersMap[q.id] !== undefined && savedAnswersMap[q.id] === trimmed) {
    if (trimmed.length > 0) {
      document.getElementById('auto-save-status').textContent = 'Saved to MySQL';
      document.getElementById('auto-save-status').style.color = 'var(--success)';
    } else {
      document.getElementById('auto-save-status').textContent = 'Not answered yet';
      document.getElementById('auto-save-status').style.color = 'var(--text-muted)';
    }
  } else if (trimmed.length > 0) {
    document.getElementById('auto-save-status').textContent = 'Unsaved changes';
    document.getElementById('auto-save-status').style.color = 'var(--accent-amber)';
  } else {
    document.getElementById('auto-save-status').textContent = 'Not answered yet';
    document.getElementById('auto-save-status').style.color = 'var(--text-muted)';
  }

  // Prev / Next button labels
  document.getElementById('prev-question-btn').disabled = currentIndex === 0;
  const nextBtn = document.getElementById('next-question-btn');
  if (currentIndex === total - 1) {
    nextBtn.textContent = 'Review & Finish →';
  } else {
    nextBtn.textContent = 'Next Question →';
  }

  renderPills();
}

async function jumpToQuestion(idx) {
  if (isSaving || idx === currentIndex) return;
  const ok = await saveCurrentAnswer(false);
  if (!ok) return;
  currentIndex = idx;
  renderCurrentQuestion();
}

function updateCounts() {
  const text = document.getElementById('answer-input').value;
  const chars = text.length;
  const words = text.trim() ? text.trim().split(/\s+/).length : 0;
  document.getElementById('char-word-count').textContent = `${chars} characters • ${words} words`;
}

function setSavingState(saving) {
  isSaving = saving;
  const saveBtn = document.getElementById('save-answer-btn');
  const nextBtn = document.getElementById('next-question-btn');
  const prevBtn = document.getElementById('prev-question-btn');
  const finishBtn = document.getElementById('finish-btn');

  if (saving) {
    if (saveBtn) {
      saveBtn.disabled = true;
      saveBtn.textContent = 'Saving...';
    }
    if (nextBtn) nextBtn.disabled = true;
    if (prevBtn) prevBtn.disabled = true;
    if (finishBtn) finishBtn.disabled = true;
  } else {
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.textContent = 'Save Answer';
    }
    if (nextBtn) nextBtn.disabled = false;
    if (prevBtn) prevBtn.disabled = currentIndex === 0;
    if (finishBtn) finishBtn.disabled = false;
  }
}

async function saveCurrentAnswer(showBanner = false) {
  if (!sessionData || !sessionData.questions) return true;
  const q = sessionData.questions[currentIndex];
  const textarea = document.getElementById('answer-input');
  const answerText = textarea.value.trim();

  // Sync draft map
  answersMap[q.id] = textarea.value;

  // If already identical to what is persisted in MySQL
  if (savedAnswersMap[q.id] !== undefined && savedAnswersMap[q.id] === answerText) {
    if (showBanner) {
      showAlert('alert-container', 'Answer is already saved.', 'info');
    }
    return true;
  }

  // If both saved and current are blank/empty, nothing to submit
  if ((savedAnswersMap[q.id] === undefined || savedAnswersMap[q.id] === '') && answerText === '') {
    return true;
  }

  setSavingState(true);
  document.getElementById('auto-save-status').textContent = 'Saving to MySQL...';
  document.getElementById('auto-save-status').style.color = 'var(--accent-amber)';

  try {
    await API.post(`/api/interviews/${sessionId}/answers`, {
      questionId: q.id,
      answerText: answerText
    });

    savedAnswersMap[q.id] = answerText;
    document.getElementById('auto-save-status').textContent = 'Saved to MySQL';
    document.getElementById('auto-save-status').style.color = 'var(--success)';

    if (showBanner) {
      showAlert('alert-container', 'Answer saved successfully to MySQL!', 'success');
    } else {
      clearAlert('alert-container');
    }

    renderPills();
    const answeredCount = Object.values(savedAnswersMap).filter(v => v && v.trim().length > 0).length;
    document.getElementById('answered-count-text').textContent = `${answeredCount} / ${sessionData.questions.length} Saved`;

    return true;
  } catch (err) {
    document.getElementById('auto-save-status').textContent = 'Save failed';
    document.getElementById('auto-save-status').style.color = 'var(--danger)';
    showAlert('alert-container', `Failed to save answer: ${err.message || 'Server error'}. Please try again.`, 'danger');
    return false;
  } finally {
    setSavingState(false);
  }
}

// Submit Modal Setup
function setupSubmitModal() {
  const modal = document.getElementById('submit-confirm-modal');
  const closeBtn = document.getElementById('close-confirm-modal');
  const cancelBtn = document.getElementById('cancel-submit-modal-btn');
  const confirmBtn = document.getElementById('confirm-submit-eval-btn');

  closeBtn.addEventListener('click', () => modal.classList.remove('show'));
  cancelBtn.addEventListener('click', () => modal.classList.remove('show'));

  confirmBtn.addEventListener('click', async () => {
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Evaluating Answers...';

    try {
      await API.post(`/api/interviews/${sessionId}/submit`);
      // Redirect to results
      window.location.href = `/interview-results.html?id=${sessionId}`;
    } catch (err) {
      showAlert('alert-container', 'Evaluation failed: ' + err.message);
      confirmBtn.disabled = false;
      confirmBtn.textContent = 'Submit & View Results';
      modal.classList.remove('show');
    }
  });
}

function openSubmitModal() {
  const modal = document.getElementById('submit-confirm-modal');
  const total = sessionData.questions.length;
  const answered = Object.values(savedAnswersMap).filter(v => v && v.trim().length > 0).length;

  document.getElementById('modal-answered-count').textContent = answered;
  document.getElementById('modal-total-count').textContent = total;
  modal.classList.add('show');
}
