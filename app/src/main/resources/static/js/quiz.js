// Quiz Application
(function() {
    'use strict';

    // State Management
    let currentQuestionIndex = 0;
    let userAnswers = [];
    let startTime = Date.now();
    let timerInterval;
    let isSubmitting = false;
    let isLoadingQuestion = false;

    // DOM Elements
    const elements = {
        questionDescription: document.getElementById('questionDescription'),
        optionADesc: document.getElementById('optionADescription'),
        optionBDesc: document.getElementById('optionBDescription'),
        optionCDesc: document.getElementById('optionCDescription'),
        optionDDesc: document.getElementById('optionDDescription'),
        previousBtn: document.getElementById('previousBtn'),
        nextBtn: document.getElementById('nextBtn'),
        questionDots: document.getElementById('questionDots'),
        progressBar: document.getElementById('progressBar'),
        progressPercent: document.getElementById('progressPercent'),
        currentQuestionNum: document.getElementById('currentQuestionNum'),
        timeElapsed: document.getElementById('timeElapsed'),
        optionInputs: document.querySelectorAll('input[name="answer"]'),
        submitSection: document.getElementById('submitSection'),
        questionCard: document.querySelector('.question-card'),
        backToQuizBtn: document.getElementById('backToQuizBtn'),
        quizForm: document.getElementById('quizForm'),
        answeredCount: document.getElementById('answeredCount'),
        unansweredCount: document.getElementById('unansweredCount'),
        unansweredWarning: document.getElementById('unansweredWarning'),
        userOptedAnswers: document.getElementById('userOptedAnswers'),
        questionIds: document.getElementById('questionIds')
    };

    // Get data from server
    const totalQuestions = parseInt(document.getElementById('totalQuestions').value);
    const questions = questionsData; // From inline script in HTML

    // Initialize
    function init() {
        // Initialize user answers array
        userAnswers = new Array(totalQuestions).fill(-1);

        // Generate question dots
        generateQuestionDots();

        // Load first question
        loadQuestion(0);

        // Setup event listeners
        setupEventListeners();

        // Start timer
        startTimer();

        // Prepare question IDs
        prepareQuestionIds();
    }

    // Generate Question Dots
    function generateQuestionDots() {
        elements.questionDots.innerHTML = '';
        for (let i = 0; i < totalQuestions; i++) {
            const dot = document.createElement('div');
            dot.className = 'dot';
            if (i === 0) dot.classList.add('active');
            dot.setAttribute('data-question', i);
            dot.addEventListener('click', () => loadQuestion(i));
            dot.title = `Question ${i + 1}`;
            elements.questionDots.appendChild(dot);
        }
    }

    // Load Question
    function loadQuestion(index) {
        if (index < 0 || index >= totalQuestions) return;

        // Set flag to prevent change events during loading
        isLoadingQuestion = true;

        currentQuestionIndex = index;

        // Update question content
        const question = questions[index];
        elements.questionDescription.textContent = `${index + 1}. ${question.statement}`;
        elements.optionADesc.textContent = question.optionA;
        elements.optionBDesc.textContent = question.optionB;
        elements.optionCDesc.textContent = question.optionC;
        elements.optionDDesc.textContent = question.optionD;

        // Restore user's previous answer if exists
        elements.optionInputs.forEach(input => {
            input.checked = false;
        });
        if (userAnswers[index] !== -1) {
            elements.optionInputs[userAnswers[index]].checked = true;
        }

        // Update navigation buttons
        elements.previousBtn.disabled = (index === 0);
        elements.nextBtn.textContent = (index === totalQuestions - 1) ? 'Finish' : 'Next';
        elements.nextBtn.innerHTML = (index === totalQuestions - 1)
            ? '<i class="fas fa-check"></i> Finish'
            : 'Next <i class="fas fa-arrow-right"></i>';

        // Update progress
        updateProgress();

        // Update dots
        updateDots();

        // Update current question number
        elements.currentQuestionNum.textContent = index + 1;

        // Clear flag after loading is complete
        isLoadingQuestion = false;
    }

    // Update Progress
    function updateProgress() {
        const progress = ((currentQuestionIndex + 1) / totalQuestions) * 100;
        elements.progressBar.style.width = `${progress}%`;
        elements.progressPercent.textContent = `${Math.round(progress)}%`;
    }

    // Update Dots
    function updateDots() {
        const dots = elements.questionDots.querySelectorAll('.dot');
        dots.forEach((dot, index) => {
            dot.classList.remove('active');
            if (index === currentQuestionIndex) {
                dot.classList.add('active');
            }
            if (userAnswers[index] !== -1) {
                dot.classList.add('answered');
            } else {
                dot.classList.remove('answered');
            }
        });
    }

    // Setup Event Listeners
    function setupEventListeners() {
        // Option selection
        elements.optionInputs.forEach((input, index) => {
            input.addEventListener('change', function() {
                // Ignore change events while loading a question
                if (isLoadingQuestion) return;

                if (this.checked) {
                    userAnswers[currentQuestionIndex] = parseInt(this.value);
                    updateDots();
                }
            });
        });

        // Navigation buttons
        elements.previousBtn.addEventListener('click', () => {
            loadQuestion(currentQuestionIndex - 1);
        });

        elements.nextBtn.addEventListener('click', () => {
            if (currentQuestionIndex === totalQuestions - 1) {
                showSubmitSection();
            } else {
                loadQuestion(currentQuestionIndex + 1);
            }
        });

        // Back to quiz button
        elements.backToQuizBtn.addEventListener('click', () => {
            elements.submitSection.style.display = 'none';
            elements.questionCard.style.display = 'block';
        });

        // Form submission
        elements.quizForm.addEventListener('submit', handleSubmit);
    }

    // Show Submit Section
    function showSubmitSection() {
        // Calculate answered questions
        const answeredQuestions = userAnswers.filter(ans => ans !== -1).length;
        const unansweredQuestions = totalQuestions - answeredQuestions;

        elements.answeredCount.textContent = answeredQuestions;

        // Show/hide unanswered warning
        if (unansweredQuestions > 0) {
            elements.unansweredCount.textContent = unansweredQuestions;
            elements.unansweredWarning.style.display = 'block';
        } else {
            elements.unansweredWarning.style.display = 'none';
        }

        // Hide question card and show submit section
        elements.questionCard.style.display = 'none';
        elements.submitSection.style.display = 'block';

        // Scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    // Handle Submit
    function handleSubmit(e) {
        const userName = document.getElementById('userName').value.trim();
        const userEmail = document.getElementById('userEmail').value.trim();

        if (!userName || !userEmail) {
            e.preventDefault();
            alert('Please fill in your name and email');
            return false;
        }

        // Prepare answers JSON
        const answersJSON = JSON.stringify(userAnswers);
        elements.userOptedAnswers.value = answersJSON;

        // Mark as submitting to prevent beforeunload warning
        isSubmitting = true;

        // Form will submit naturally
        return true;
    }

    // Prepare Question IDs
    function prepareQuestionIds() {
        const questionIds = questions.map(q => q.questionId);
        const questionIdsJSON = JSON.stringify(questionIds);
        elements.questionIds.value = questionIdsJSON;
    }

    // Start Timer
    function startTimer() {
        timerInterval = setInterval(() => {
            const elapsed = Math.floor((Date.now() - startTime) / 1000);
            const minutes = Math.floor(elapsed / 60);
            const seconds = elapsed % 60;
            elements.timeElapsed.textContent =
                `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        }, 1000);
    }

    // Stop Timer
    function stopTimer() {
        if (timerInterval) {
            clearInterval(timerInterval);
        }
    }

    // Keyboard Navigation
    document.addEventListener('keydown', (e) => {
        // Arrow keys for navigation
        if (e.key === 'ArrowLeft' && currentQuestionIndex > 0) {
            loadQuestion(currentQuestionIndex - 1);
        } else if (e.key === 'ArrowRight' && currentQuestionIndex < totalQuestions - 1) {
            loadQuestion(currentQuestionIndex + 1);
        }
        // Number keys for option selection (1-4 for A-D)
        else if (e.key >= '1' && e.key <= '4') {
            const optionIndex = parseInt(e.key) - 1;
            elements.optionInputs[optionIndex].checked = true;
            elements.optionInputs[optionIndex].dispatchEvent(new Event('change'));
        }
    });

    // Prevent accidental page close
    window.addEventListener('beforeunload', (e) => {
        // Don't show warning if user is submitting the form
        if (isSubmitting) {
            return;
        }

        const answeredQuestions = userAnswers.filter(ans => ans !== -1).length;
        if (answeredQuestions > 0 && answeredQuestions < totalQuestions) {
            e.preventDefault();
            e.returnValue = '';
        }
    });

    // Initialize on page load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
