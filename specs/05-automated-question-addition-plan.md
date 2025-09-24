# 05-automated-question-addition-plan.md

**JavaScript Script for Automated Question Addition to Quiz Website**

## Overview

Create a Node.js JavaScript script that automates the process of adding questions to the quiz website using the existing `/submit-add-question` API endpoint. This script will support batch question addition from JSON files and include sample questions for testing.

## Current API Analysis

Based on `QuizController.java:145-164`, the `/submit-add-question` POST endpoint accepts:

### Form Parameters
- `question_id` (optional): Long - For editing existing questions
- `category`: QuestionCategory enum - GENERAL, HISTORY, FINANCE, SPORTS, SCIENCE_AND_TECHNOLOGY, ENGINEERING
- `difficulty_level`: DifficultyLevel enum - LOW, MEDIUM, HIGH
- `question`: String - The question text
- `optionA`, `optionB`, `optionC`, `optionD`: String - Multiple choice options
- `answer`: Integer - Correct answer (1-4, where 1=A, 2=B, 3=C, 4=D)

### Response Behavior
- Success: Returns 302 redirect to `/home` with flash message "Added new question."
- The endpoint uses form-encoded data submission

## Script Requirements

### Core Functionality
1. **Question Validation**: Validate question data structure before submission
2. **Batch Processing**: Support adding multiple questions from JSON files
3. **Sample Data**: Include predefined sample questions for testing
4. **Error Handling**: Robust error handling and reporting
5. **Progress Tracking**: Show progress during batch operations
6. **Flexible Input**: Support both file-based and programmatic question input

### Technical Specifications

#### Script Name and Location
- **File**: `oci-deployment/scripts/05-add-questions-automated.js`
- **Executable**: Pure JavaScript script (no external dependencies)

#### Dependencies
- **Zero Dependencies**: Pure JavaScript using only built-in APIs
- `fetch`: Native fetch API for HTTP requests (available in Node.js 18+)
- `fs`: Native file system module for JSON loading
- `process.argv`: Native command-line argument parsing
- `URLSearchParams`: Native form data encoding

#### Command Line Interface
```bash
node oci-deployment/scripts/05-add-questions-automated.js [options]

Options:
  --url <url>         Base URL of quiz application (default: http://161.118.188.237:8080)
  --file <path>       JSON file containing questions to add
  --sample            Use predefined sample questions
  --dry-run           Show what would be sent without sending requests
  --verbose           Enable detailed logging
  --delay <ms>        Delay between requests in milliseconds (default: 500)
  --help              Show help information
```

#### JSON Input Format
```json
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
```

#### Validation Rules
1. **Category**: Must be one of the valid QuestionCategory enum values
2. **Difficulty Level**: Must be LOW, MEDIUM, or HIGH
3. **Question Text**: Non-empty string, reasonable length (10-500 characters)
4. **Options**: All four options must be non-empty strings
5. **Answer**: Integer between 1-4 inclusive
6. **Data Types**: Strict type checking for all fields

### Sample Questions Dataset

Include 6-10 sample questions covering:
- All difficulty levels (LOW, MEDIUM, HIGH)
- All question categories
- Variety of topics to test the system
- Well-formed questions with clear correct answers

### Error Handling Strategy

#### Network Errors
- Connection timeouts
- HTTP errors (4xx, 5xx)
- Redirect handling verification

#### Data Errors
- Invalid JSON format in input files
- Missing required fields
- Invalid enum values
- Malformed question data

#### Operational Errors
- File not found
- Permission errors
- Rate limiting considerations

### Logging and Output

#### Verbose Mode
- Request details (URL, form data)
- Response status and headers
- Timing information
- Progress indicators

#### Standard Mode
- Success/failure summary
- Error messages for failures
- Overall statistics

#### Dry Run Mode
- Show all requests that would be made
- Display form data without sending
- Validate all input without side effects

## Implementation Strategy

### Phase 1: Core Structure
1. Set up Node.js project structure
2. Implement command-line argument parsing
3. Create question validation functions
4. Set up HTTP client configuration

### Phase 2: API Integration
1. Implement form data submission to `/submit-add-question`
2. Handle redirect responses correctly
3. Add proper error handling for HTTP requests
4. Test with single question submission

### Phase 3: Batch Processing
1. JSON file loading and parsing
2. Batch question processing with progress tracking
3. Rate limiting between requests
4. Comprehensive error reporting

### Phase 4: Testing and Samples
1. Create sample questions dataset
2. Add dry-run functionality
3. Test with live application
4. Documentation and usage examples

## Success Criteria

### Functional Requirements
- ✅ Successfully add individual questions via API
- ✅ Process JSON files with multiple questions
- ✅ Validate all input data before submission
- ✅ Handle errors gracefully without crashing
- ✅ Provide clear feedback on success/failure

### Quality Requirements
- ✅ Clean, readable, well-documented code
- ✅ Proper error handling and user feedback
- ✅ Command-line interface follows Unix conventions
- ✅ Comprehensive input validation
- ✅ Safe default behaviors (e.g., dry-run option)

### Integration Requirements
- ✅ Works with current quiz application deployment
- ✅ Compatible with existing API endpoint
- ✅ Handles the form-based submission correctly
- ✅ Respects server rate limits and stability

## Risk Mitigation

### Technical Risks
- **API Changes**: Script depends on current endpoint format
- **Rate Limiting**: Batch operations might overload server
- **Data Corruption**: Invalid data could affect database

### Mitigation Strategies
- **Validation**: Strict input validation before any API calls
- **Rate Limiting**: Built-in delays between requests
- **Dry Run**: Always test with dry-run before actual submission
- **Rollback**: No rollback needed as questions are just additions

## Future Enhancements

### Potential Features
- Question editing/updating support (using question_id parameter)
- CSV file input support
- Interactive question creation mode
- Integration with external question databases
- Automatic question generation using AI APIs

### Maintenance Considerations
- Keep enum values synchronized with Java backend
- Monitor API endpoint changes
- Update validation rules as needed
- Add new categories/difficulty levels as they're added

## Usage Examples

### Add Sample Questions
```bash
node oci-deployment/scripts/05-add-questions-automated.js --sample --verbose
```

### Add Questions from File
```bash
node oci-deployment/scripts/05-add-questions-automated.js --file questions.json --delay 1000
```

### Test Without Making Changes
```bash
node oci-deployment/scripts/05-add-questions-automated.js --file questions.json --dry-run
```

### Use Custom Application URL
```bash
node oci-deployment/scripts/05-add-questions-automated.js --url http://localhost:8080 --sample
```

---

**Implementation Reference**: This plan corresponds to the implementation script `oci-deployment/scripts/05-add-questions-automated.js`

**Status**: 📋 Planning Phase - Ready for review and implementation