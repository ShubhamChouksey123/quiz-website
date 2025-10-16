/**
 * Modern Admin Dashboard JavaScript
 * Handles view switching, question filtering, and form submissions
 */

(function() {
    'use strict';

    // Initialize on page load
    document.addEventListener('DOMContentLoaded', function() {
        initializeNavigation();
        initializeQuestionFilters();
        checkURLParameters();
    });

    /**
     * Initialize sidebar navigation
     */
    function initializeNavigation() {
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => {
            item.addEventListener('click', function(e) {
                // Only handle navigation for non-external links
                const href = this.getAttribute('href');
                if (href === 'javascript:void(0);') {
                    navItems.forEach(nav => nav.classList.remove('active'));
                    this.classList.add('active');
                }
            });
        });
    }

    /**
     * Initialize question filter tabs
     */
    function initializeQuestionFilters() {
        const filterTabs = document.querySelectorAll('.filter-tab');
        filterTabs.forEach(tab => {
            tab.addEventListener('click', function() {
                filterTabs.forEach(t => t.classList.remove('active'));
                this.classList.add('active');
            });
        });
    }

    /**
     * Check URL parameters on load
     */
    function checkURLParameters() {
        const params = new URLSearchParams(window.location.search);
        const approvalLevel = params.get('approvalLevel');
        const view = params.get('view');

        // Show questions view if approvalLevel parameter exists or view=questions
        if (approvalLevel || view === 'questions') {
            showQuestions();
            if (approvalLevel) {
                setActiveFilter(approvalLevel);
                showProperButtons(approvalLevel);
            }
        }
    }

    /**
     * Set active filter tab based on approval level
     */
    function setActiveFilter(approvalLevel) {
        const filterTabs = document.querySelectorAll('.filter-tab');
        filterTabs.forEach(tab => {
            tab.classList.remove('active');
            if (tab.dataset.filter === approvalLevel) {
                tab.classList.add('active');
            }
        });
    }

    /**
     * Show/hide buttons based on approval level
     */
    function showProperButtons(approvalLevel) {
        const approveButtons = document.querySelectorAll('.approve-button');
        const discardButtons = document.querySelectorAll('.discard-button');

        if (approvalLevel === 'APPROVED') {
            // Hide approve buttons, show discard buttons
            approveButtons.forEach(btn => btn.style.display = 'none');
            discardButtons.forEach(btn => btn.style.display = 'inline-flex');
        } else if (approvalLevel === 'DISCARD') {
            // Show approve buttons, hide discard buttons
            approveButtons.forEach(btn => btn.style.display = 'inline-flex');
            discardButtons.forEach(btn => btn.style.display = 'none');
        } else {
            // NEW or default - show both
            approveButtons.forEach(btn => btn.style.display = 'inline-flex');
            discardButtons.forEach(btn => btn.style.display = 'inline-flex');
        }
    }

    /**
     * Show Dashboard view
     */
    window.showDashboard = function() {
        document.getElementById('dashboardView').classList.add('active');
        document.getElementById('questionsView').classList.remove('active');

        // Update navigation
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => item.classList.remove('active'));
        navItems[0].classList.add('active');

        // Update URL without reload
        window.history.pushState({}, '', '/admin');
    };

    /**
     * Show Questions view
     */
    window.showQuestions = function() {
        document.getElementById('dashboardView').classList.remove('active');
        document.getElementById('questionsView').classList.add('active');

        // Update navigation
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => item.classList.remove('active'));
        navItems[1].classList.add('active');

        // Update URL without reload
        window.history.pushState({}, '', '/admin?view=questions');
    };

    /**
     * Filter questions by approval level
     */
    window.filterQuestions = function(approvalLevel) {
        console.log('Filtering questions:', approvalLevel);

        // Update URL with approval level parameter
        const newUrl = updateQueryStringParameter(window.location.href, 'approvalLevel', approvalLevel);
        window.location.href = newUrl;
    };

    /**
     * Approve a question
     */
    window.approveQuestion = function(questionId) {
        console.log('Approving question:', questionId);
        submitQuestionAction(questionId, 'APPROVED');
    };

    /**
     * Discard a question
     */
    window.discardQuestion = function(questionId) {
        console.log('Discarding question:', questionId);
        submitQuestionAction(questionId, 'DISCARD');
    };

    /**
     * Edit a question
     */
    window.editQuestion = function(questionId) {
        console.log('Editing question:', questionId);
        submitQuestionAction(questionId, 'EDIT');
    };

    /**
     * Submit question action form
     */
    function submitQuestionAction(questionId, approvalLevel) {
        const form = document.getElementById('changeCategorySubmitForm');
        document.getElementById('questionId').value = questionId;
        document.getElementById('approvalLevel').value = approvalLevel;
        form.submit();
    }

    /**
     * Update query string parameter in URL
     */
    function updateQueryStringParameter(uri, key, value) {
        const re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
        const separator = uri.indexOf('?') !== -1 ? "&" : "?";

        if (uri.match(re)) {
            return uri.replace(re, '$1' + key + "=" + value + '$2');
        } else {
            return uri + separator + key + "=" + value;
        }
    }

    /**
     * Easy selector helper function
     */
    function select(el, all = false) {
        el = el.trim();
        if (all) {
            return [...document.querySelectorAll(el)];
        } else {
            return document.querySelector(el);
        }
    }

})();
