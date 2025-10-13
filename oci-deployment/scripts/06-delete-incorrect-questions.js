#!/usr/bin/env node

/**
 * Delete All Questions Script
 *
 * This script deletes ALL questions from the database by:
 * 1. Fetching all question IDs from the /admin endpoint
 * 2. Deleting each question using the delete endpoints
 *
 * Usage:
 *   node oci-deployment/scripts/06-delete-incorrect-questions.js [options]
 *
 * Options:
 *   --url <url>         Base URL of quiz application (default: QUIZ_APP_URL env var or http://161.118.188.237:8080)
 *   --dry-run           Show what would be deleted without making changes
 *   --verbose           Enable detailed logging
 *   --help              Show help information
 */

const { URLSearchParams } = require('url');

class QuestionDeleter {
    constructor(baseUrl = process.env.QUIZ_APP_URL || 'http://localhost:8080', verbose = false) {
        this.baseUrl = baseUrl.replace(/\/$/, '');
        this.verbose = verbose;
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
     * Get all question IDs from the admin page
     */
    async getAllQuestionIds() {
        try {
            this.log('Fetching all question IDs from admin page...');
            const response = await fetch(`${this.baseUrl}/admin`);

            if (!response.ok) {
                console.error(`[ERROR] Failed to fetch admin page. Status: ${response.status}`);
                return [];
            }

            const html = await response.text();

            // Extract question IDs from the admin page HTML
            // Look for patterns like questionId=123, question_id=123, or onclick functions with IDs
            const questionIdMatches = html.match(/(?:questionId|question_id)[="](\d+)|(?:Question|question)\(&#39;(\d+)&#39;\)|(?:Question|question)\('(\d+)'\)/g);

            if (!questionIdMatches) {
                console.warn('[WARNING] No question IDs found in admin page');
                return [];
            }

            const questionIds = questionIdMatches
                .map(match => {
                    // Try different patterns to extract the ID
                    let idMatch = match.match(/(?:questionId|question_id)[="](\d+)/);
                    if (idMatch) return parseInt(idMatch[1]);

                    idMatch = match.match(/(?:Question|question)\(&#39;(\d+)&#39;\)/);
                    if (idMatch) return parseInt(idMatch[1]);

                    idMatch = match.match(/(?:Question|question)\('(\d+)'\)/);
                    if (idMatch) return parseInt(idMatch[1]);

                    return null;
                })
                .filter(id => id !== null && id > 0) // Filter out null and invalid IDs
                .filter((id, index, array) => array.indexOf(id) === index) // Remove duplicates
                .sort((a, b) => a - b); // Sort numerically

            this.log(`Found ${questionIds.length} questions in admin interface: ${questionIds.join(', ')}`);
            return questionIds;

        } catch (error) {
            console.error(`[ERROR] Failed to get question IDs from admin: ${error.message}`);
            return [];
        }
    }

    /**
     * Delete all questions found in the database
     */
    async deleteAllQuestions(dryRun = false) {
        // Get all question IDs from admin page
        const questionIds = await this.getAllQuestionIds();

        if (questionIds.length === 0) {
            console.log('ℹ️  No questions found to delete');
            return true;
        }

        console.log(`📊 Found ${questionIds.length} questions to delete: ${questionIds.join(', ')}`);

        if (dryRun) {
            console.log(`[DRY RUN] Would delete ${questionIds.length} questions`);
            console.log(`[DRY RUN] Question IDs: ${questionIds.join(', ')}`);
            return true;
        }

        let successCount = 0;
        let failedCount = 0;

        // Delete questions one by one for better control and logging
        for (let i = 0; i < questionIds.length; i++) {
            const questionId = questionIds[i];
            console.log(`\n[${i + 1}/${questionIds.length}] Deleting question ID ${questionId}...`);

            try {
                const success = await this.deleteSingleQuestion(questionId, false);
                if (success) {
                    successCount++;
                } else {
                    failedCount++;
                }

                // Small delay between deletions to be respectful to the server
                if (i < questionIds.length - 1) {
                    await this.sleep(500);
                }

            } catch (error) {
                console.error(`[ERROR] Error deleting question ${questionId}: ${error.message}`);
                failedCount++;
            }
        }

        console.log(`\n📊 Deletion Summary:`);
        console.log(`   ✅ Successfully deleted: ${successCount}`);
        console.log(`   ❌ Failed to delete: ${failedCount}`);
        console.log(`   📝 Total processed: ${successCount + failedCount}`);

        return failedCount === 0;
    }

    /**
     * Sleep for specified milliseconds
     */
    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Delete a single question by ID
     */
    async deleteSingleQuestion(questionId, dryRun = false) {
        if (dryRun) {
            console.log(`[DRY RUN] Would delete question ID ${questionId}`);
            console.log(`[DRY RUN] Would send POST to ${this.baseUrl}/delete-question`);
            console.log(`[DRY RUN] Form data: questionId=${questionId}`);
            return true;
        }

        try {
            this.log(`Deleting question ID ${questionId}...`);

            const formData = new URLSearchParams({
                questionId: questionId.toString()
            });

            const response = await fetch(`${this.baseUrl}/delete-question`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData,
                redirect: 'manual'
            });

            if (response.status === 302) {
                this.log(`✅ Successfully deleted question ID ${questionId}`);
                return true;
            } else {
                console.error(`[ERROR] Failed to delete question ID ${questionId}. Status: ${response.status}`);
                return false;
            }

        } catch (error) {
            console.error(`[ERROR] Network error deleting question ID ${questionId}: ${error.message}`);
            return false;
        }
    }
}

/**
 * Parse command line arguments
 */
function parseArguments() {
    const args = process.argv.slice(2);
    const config = {
        url: process.env.QUIZ_APP_URL || 'http://161.118.188.237:8080',
        dryRun: false,
        verbose: false,
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
            case '--dry-run':
                config.dryRun = true;
                break;
            case '--verbose':
                config.verbose = true;
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
Delete All Questions Script

Usage:
  node oci-deployment/scripts/06-delete-incorrect-questions.js [options]

Options:
  --url <url>         Base URL of quiz application (default: QUIZ_APP_URL env var or http://161.118.188.237:8080)
  --dry-run           Show what would be deleted without making changes
  --verbose           Enable detailed logging
  --help              Show this help information

Description:
  This script deletes ALL questions from the database by:
  1. Fetching all question IDs from the /admin endpoint
  2. Deleting each question individually using the /delete-question endpoint

  This is useful for completely clearing the question table to start fresh
  with corrected data.

Examples:
  # Test what would be deleted (dry run) - RECOMMENDED FIRST
  node oci-deployment/scripts/06-delete-incorrect-questions.js --dry-run --verbose

  # Delete ALL questions from the database
  node oci-deployment/scripts/06-delete-incorrect-questions.js --verbose

WARNING:
  This operation will DELETE ALL QUESTIONS from the database and CANNOT BE UNDONE!
  Make sure you have the corrected data.json file ready before running this script.

  After deletion, you can re-add questions with:
  node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose
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

    console.log('🗑️  Delete ALL Questions Script');
    console.log(`📋 Target: ${config.url}`);
    console.log(`🧪 Mode: ${config.dryRun ? 'DRY RUN' : 'LIVE DELETION'}`);

    if (!config.dryRun) {
        console.log('⚠️  WARNING: This will permanently delete ALL questions from the database!');
        console.log('⚠️  This operation CANNOT BE UNDONE!');
        console.log('⚠️  Make sure you have the corrected data.json ready for re-adding questions.');
    }

    console.log('');

    // Initialize the deleter
    const deleter = new QuestionDeleter(config.url, config.verbose);

    // Delete all questions
    const success = await deleter.deleteAllQuestions(config.dryRun);

    if (success) {
        if (config.dryRun) {
            console.log(`\n✅ Dry run completed successfully!`);
            console.log(`📝 Ready to delete ALL questions when you remove --dry-run`);
        } else {
            console.log(`\n🎉 All questions deleted successfully!`);
            console.log(`📝 Next steps:`);
            console.log(`   1. Verify deletion at: ${config.url}/admin`);
            console.log(`   2. Re-add questions with corrected answers:`);
            console.log(`      node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose`);
        }
        process.exit(0);
    } else {
        console.log(`\n❌ Some deletions failed. Check the logs for errors.`);
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

module.exports = { QuestionDeleter };