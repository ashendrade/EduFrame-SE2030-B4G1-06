function toggleQuestionFields() {
  var typeSelect = document.getElementById('questionType');
  if (!typeSelect) return;
  var type = typeSelect.value;

  var mcqOptions = document.getElementById('mcqOptions');
  var correctAnswerInput = document.getElementById('correctAnswer');
  var trueFalseHelp = document.getElementById('trueFalseHelp');
  var shortAnswerHelp = document.getElementById('shortAnswerHelp');

  if (mcqOptions) mcqOptions.style.display = (type === 'MCQ') ? 'block' : 'none';
  if (trueFalseHelp) trueFalseHelp.style.display = (type === 'TRUE_FALSE') ? 'block' : 'none';
  if (shortAnswerHelp) shortAnswerHelp.style.display = (type === 'SHORT_ANSWER') ? 'block' : 'none';

  if (correctAnswerInput) {
    if (type === 'MCQ') {
      correctAnswerInput.placeholder = 'Must exactly match one of the options above';
    } else if (type === 'TRUE_FALSE') {
      correctAnswerInput.placeholder = 'TRUE or FALSE';
    } else {
      correctAnswerInput.placeholder = 'Reference answer for auto-grading';
    }
  }
}

document.addEventListener('DOMContentLoaded', toggleQuestionFields);
