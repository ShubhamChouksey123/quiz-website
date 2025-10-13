#!/usr/bin/env node

/**
 * Automated Question Addition Script for Quiz Website
 *
 * This script automatically adds questions to the quiz website using the /submit-add-question API endpoint.
 * It supports loading questions from JSON files or using predefined sample questions.
 *
 * Usage:
 *   node scripts/05-add-questions-automated.js [options]
 *
 * Options:
 *   --url <url>         Base URL of quiz application (default: QUIZ_APP_URL env var or http://161.118.188.237:8080)
 *   --file <path>       JSON file containing questions to add
 *   --sample            Use predefined sample questions
 *   --dry-run           Show what would be sent without sending requests
 *   --verbose           Enable detailed logging
 *   --delay <ms>        Delay between requests in milliseconds (default: 500)
 *   --help              Show help information
 *
 * Environment-specific Usage:
 *
 * LOCAL DEVELOPMENT:
 *   # Run against local development server (http://localhost:8080)
 *   QUIZ_APP_URL=http://localhost:8080 node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose
 *
 *   # Or load local environment variables
 *   export $(grep -v '^#' local-development/.env.local | grep -v '^$' | xargs) && \
 *   node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose
 *
 * PRODUCTION DEPLOYMENT:
 *   # Run against production server (uses default production URL)
 *   node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose
 *
 *   # Or with explicit production URL
 *   QUIZ_APP_URL=http://161.118.188.237:8080 node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose
 *
 *   # Load production environment variables (if .env file is configured)
 *   export $(grep -v '^#' .env | grep -v '^$' | xargs) && \
 *   node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose
 *
 * JSON Format:
 * [
 *   {
 *     "category": "GENERAL",
 *     "difficulty_level": "MEDIUM",
 *     "question": "What is the capital of France?",
 *     "optionA": "London",
 *     "optionB": "Berlin",
 *     "optionC": "Paris",
 *     "optionD": "Madrid",
 *     "answer": 3
 *   }
 * ]
 *
 * Valid categories: GENERAL, HISTORY, FINANCE, SPORTS, SCIENCE_AND_TECHNOLOGY, ENGINEERING
 * Valid difficulty levels: LOW, MEDIUM, HIGH
 * Answer should be 0-3 (0=A, 1=B, 2=C, 3=D)
 */

const fs = require('fs').promises;
const { URLSearchParams } = require('url');

class QuizQuestionAdder {
    constructor(baseUrl = process.env.QUIZ_APP_URL || 'http://localhost:8080', verbose = false, delay = 500) {
        this.baseUrl = baseUrl.replace(/\/$/, ''); // Remove trailing slash
        this.verbose = verbose;
        this.delay = delay;

        // Valid enum values from Java backend
        this.validCategories = [
            'GENERAL', 'HISTORY', 'FINANCE', 'SPORTS',
            'SCIENCE_AND_TECHNOLOGY', 'ENGINEERING', 'ENTERTAINMENT', 'GEOGRAPHY', 'LITERATURE', 'FOOD_AND_CUISINE',
            'NATURE_AND_WILDLIFE', 'MYTHOLOGY_AND_RELIGION', 'POLITICS', 'MUSIC'
        ];
        this.validDifficultyLevels = ['LOW', 'MEDIUM', 'HIGH'];
    }

    /**
     * Log message if verbose mode is enabled
     */
    log(message) {
        if (this.verbose) {
            console.log(`[INFO] ${message}`);
        }
    }

    /**
     * Validate a single question data structure
     */
    validateQuestion(question) {
        const requiredFields = [
            'category', 'difficulty_level', 'question',
            'optionA', 'optionB', 'optionC', 'optionD', 'answer'
        ];

        // Check required fields
        for (const field of requiredFields) {
            if (!(field in question)) {
                console.error(`[ERROR] Missing required field: ${field}`);
                return false;
            }
        }

        // Validate category
        if (!this.validCategories.includes(question.category)) {
            console.error(`[ERROR] Invalid category: ${question.category}. Must be one of: ${this.validCategories.join(', ')}`);
            return false;
        }

        // Validate difficulty level
        if (!this.validDifficultyLevels.includes(question.difficulty_level)) {
            console.error(`[ERROR] Invalid difficulty_level: ${question.difficulty_level}. Must be one of: ${this.validDifficultyLevels.join(', ')}`);
            return false;
        }

        // Validate answer (must be 0-3)
        if (!Number.isInteger(question.answer) || question.answer < 0 || question.answer > 3) {
            console.error(`[ERROR] Invalid answer: ${question.answer}. Must be integer between 0-3`);
            return false;
        }

        // Check that all text fields are non-empty strings
        const textFields = ['question', 'optionA', 'optionB', 'optionC', 'optionD'];
        for (const field of textFields) {
            if (typeof question[field] !== 'string' || !question[field].trim()) {
                console.error(`[ERROR] Field ${field} must be a non-empty string`);
                return false;
            }
        }

        // Check reasonable question length
        if (question.question.length < 10 || question.question.length > 500) {
            console.error(`[ERROR] Question length must be between 10-500 characters. Current: ${question.question.length}`);
            return false;
        }

        return true;
    }

    /**
     * Add a single question to the quiz database
     */
    async addQuestion(question, dryRun = false) {
        if (!this.validateQuestion(question)) {
            return false;
        }

        // Prepare form data
        const formData = new URLSearchParams({
            category: question.category,
            difficulty_level: question.difficulty_level,
            question: question.question,
            optionA: question.optionA,
            optionB: question.optionB,
            optionC: question.optionC,
            optionD: question.optionD,
            answer: question.answer.toString()
        });

        if (dryRun) {
            console.log(`[DRY RUN] Would send POST to ${this.baseUrl}/submit-add-question`);
            console.log(`[DRY RUN] Form data:`, Object.fromEntries(formData));
            return true;
        }

        try {
            this.log(`Adding question: ${question.question.substring(0, 50)}...`);
            const url = `${this.baseUrl}/submit-add-question`;

            // Send POST request using fetch
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData,
                redirect: 'follow' // Follow redirects to complete the form submission
            });

            // The endpoint redirects to /home on success (even if final status is 405)
            if (response.url.includes('/home')) {
                this.log(`✅ Successfully added question`);
                return true;
            } else {
                console.error(`[ERROR] Failed to add question. Status: ${response.status}`);
                const responseText = await response.text();
                console.error(`[ERROR] Response: ${responseText}`);
                return false;
            }

        } catch (error) {
            console.error(`[ERROR] Network error while adding question: ${error.message}`);
            return false;
        }
    }

    /**
     * Add multiple questions from a list
     */
    async addQuestionsFromList(questions, dryRun = false) {
        let successCount = 0;
        const totalCount = questions.length;

        console.log(`Adding ${totalCount} questions to quiz database...`);

        for (let i = 0; i < questions.length; i++) {
            const question = questions[i];
            console.log(`\n[${i + 1}/${totalCount}] Processing question...`);

            if (await this.addQuestion(question, dryRun)) {
                successCount++;
                if (!dryRun && this.delay > 0) {
                    // Small delay between requests to be respectful
                    await this.sleep(this.delay);
                }
            } else {
                console.error(`[ERROR] Failed to add question ${i + 1}`);
            }
        }

        console.log(`\n✅ Successfully added ${successCount}/${totalCount} questions`);
        return successCount;
    }

    /**
     * Load questions from a JSON file
     */
    async loadQuestionsFromFile(filepath) {
        try {
            const fileContent = await fs.readFile(filepath, 'utf-8');
            const questions = JSON.parse(fileContent);

            if (!Array.isArray(questions)) {
                throw new Error('JSON file must contain an array of questions');
            }

            return questions;

        } catch (error) {
            if (error.code === 'ENOENT') {
                console.error(`[ERROR] File not found: ${filepath}`);
            } else if (error instanceof SyntaxError) {
                console.error(`[ERROR] Invalid JSON in file ${filepath}: ${error.message}`);
            } else {
                console.error(`[ERROR] Error reading file ${filepath}: ${error.message}`);
            }
            return [];
        }
    }

    /**
     * Sleep for specified milliseconds
     */
    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}

/**
 * Get predefined sample questions for testing
 */
async function getSampleQuestions() {
    const path = require('path');
    const scriptDir = __dirname;
    const dataFilePath = path.join(scriptDir, '..', 'docs', 'data.json');

    try {
        const fileContent = await fs.readFile(dataFilePath, 'utf-8');
        const questions = JSON.parse(fileContent);

        if (!Array.isArray(questions)) {
            throw new Error('data.json must contain an array of questions');
        }

        return questions;
    } catch (error) {
        console.error(`[ERROR] Failed to load sample questions from ${dataFilePath}: ${error.message}`);
        console.log('[INFO] Falling back to hardcoded sample questions...');

        // Fallback to hardcoded questions if file loading fails
        return [
            {
                category: 'GENERAL',
                difficulty_level: 'LOW',
                question: 'What is the capital of France?',
                optionA: 'London',
                optionB: 'Berlin',
                optionC: 'Paris',
                optionD: 'Madrid',
                answer: 3
            },
            {
                category: 'SCIENCE_AND_TECHNOLOGY',
                difficulty_level: 'MEDIUM',
                question: 'What does \'HTTP\' stand for?',
                optionA: 'HyperText Transfer Protocol',
                optionB: 'High Tech Transfer Protocol',
                optionC: 'HyperText Transport Protocol',
                optionD: 'Home Tool Transfer Protocol',
                answer: 0
            }
        ];
    }
}

/**
 * Parse command line arguments
 */
function parseArguments() {
    const args = process.argv.slice(2);
    const config = {
        url: process.env.QUIZ_APP_URL || 'http://161.118.188.237:8080',
        file: null,
        sample: false,
        dryRun: false,
        verbose: false,
        delay: 500,
        help: false
    };

    for (let i = 0; i < args.length; i++) {
        const arg = args[i];

        switch (arg) {
            case '--url':
                if (i + 1 < args.length) {
                    config.url = args[++i];
                } else {
                    console.error('[ERROR] --url requires a value');
                    process.exit(1);
                }
                break;
            case '--file':
                if (i + 1 < args.length) {
                    config.file = args[++i];
                } else {
                    console.error('[ERROR] --file requires a path');
                    process.exit(1);
                }
                break;
            case '--sample':
                config.sample = true;
                break;
            case '--dry-run':
                config.dryRun = true;
                break;
            case '--verbose':
                config.verbose = true;
                break;
            case '--delay':
                if (i + 1 < args.length) {
                    const delay = parseInt(args[++i]);
                    if (isNaN(delay) || delay < 0) {
                        console.error('[ERROR] --delay must be a non-negative number');
                        process.exit(1);
                    }
                    config.delay = delay;
                } else {
                    console.error('[ERROR] --delay requires a number in milliseconds');
                    process.exit(1);
                }
                break;
            case '--help':
                config.help = true;
                break;
            default:
                console.error(`[ERROR] Unknown option: ${arg}`);
                process.exit(1);
        }
    }

    return config;
}

/**
 * Show help information
 */
function showHelp() {
    console.log(`
Automated Question Addition Script for Quiz Website

Usage:
  node scripts/05-add-questions-automated.js [options]

Options:
  --url <url>         Base URL of quiz application (default: QUIZ_APP_URL env var or http://161.118.188.237:8080)
  --file <path>       JSON file containing questions to add
  --sample            Use predefined sample questions
  --dry-run           Show what would be sent without sending requests
  --verbose           Enable detailed logging
  --delay <ms>        Delay between requests in milliseconds (default: 500)
  --help              Show this help information

Examples:

LOCAL DEVELOPMENT:
  # Run against local server (localhost:8080)
  QUIZ_APP_URL=http://localhost:8080 node scripts/05-add-questions-automated.js --sample --verbose

  # Load local environment variables
  export $(grep -v '^#' local-development/.env.local | grep -v '^$' | xargs) && \\
  node scripts/05-add-questions-automated.js --sample --verbose

PRODUCTION:
  # Run against production server (default)
  node scripts/05-add-questions-automated.js --sample --verbose

  # Load production environment variables
  export $(grep -v '^#' .env | grep -v '^$' | xargs) && \\
  node scripts/05-add-questions-automated.js --sample --verbose

GENERAL:
  # Add questions from custom file
  node scripts/05-add-questions-automated.js --file questions.json --delay 1000

  # Test without making changes (dry run)
  node scripts/05-add-questions-automated.js --file questions.json --dry-run

  # Use custom application URL
  node scripts/05-add-questions-automated.js --url http://localhost:8080 --sample

JSON Format:
[
  {
    "category": "GENERAL",
    "difficulty_level": "MEDIUM",
    "question": "What is the capital of France?",
    "optionA": "London",
    "optionB": "Berlin",
    "optionC": "Paris",
    "optionD": "Madrid",
    "answer": 3
  }
]

Valid categories: GENERAL, HISTORY, FINANCE, SPORTS, SCIENCE_AND_TECHNOLOGY, ENGINEERING
Valid difficulty levels: LOW, MEDIUM, HIGH
Answer should be 0-3 (0=A, 1=B, 2=C, 3=D)
`);
}

/**
 * Main function
 */
async function main() {
    const config = parseArguments();

    if (config.help) {
        showHelp();
        process.exit(0);
    }

    // Validate arguments
    if (!config.file && !config.sample) {
        console.error('[ERROR] Must specify either --file or --sample');
        console.error('Use --help for usage information');
        process.exit(1);
    }

    if (config.file && config.sample) {
        console.error('[ERROR] Cannot use both --file and --sample options');
        process.exit(1);
    }

    // Initialize the question adder
    const adder = new QuizQuestionAdder(config.url, config.verbose, config.delay);

    let questions = [];

    // Load questions
    if (config.sample) {
        console.log('Using predefined sample questions...');
        questions = await getSampleQuestions();
    } else {
        console.log(`Loading questions from file: ${config.file}`);
        questions = await adder.loadQuestionsFromFile(config.file);
    }

    if (questions.length === 0) {
        console.error('[ERROR] No questions to add');
        process.exit(1);
    }

    // Add questions
    const successCount = await adder.addQuestionsFromList(questions, config.dryRun);

    if (successCount === questions.length) {
        console.log(`\n🎉 All ${successCount} questions added successfully!`);
        process.exit(0);
    } else {
        console.log(`\n⚠️  Added ${successCount}/${questions.length} questions. Some failed.`);
        process.exit(1);
    }
}

// Run the script
if (require.main === module) {
    main().catch(error => {
        console.error('[ERROR] Unhandled error:', error.message);
        process.exit(1);
    });
}

module.exports = { QuizQuestionAdder, getSampleQuestions };