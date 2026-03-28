package com.example.silentwatch

val tutorialSteps = listOf(
    TutorialStep(R.string.learning_step_1_title, R.string.learning_step_1_body),
    TutorialStep(R.string.learning_step_2_title, R.string.learning_step_2_body),
    TutorialStep(R.string.learning_step_3_title, R.string.learning_step_3_body),
    TutorialStep(R.string.learning_step_4_title, R.string.learning_step_4_body),
)

val quizQuestions = listOf(
    QuizQuestion(
        titleResId = R.string.quiz_q1_title,
        optionResIds = listOf(
            R.string.quiz_q1_option_1,
            R.string.quiz_q1_option_2,
            R.string.quiz_q1_option_3,
        ),
        correctIndex = 0,
    ),
    QuizQuestion(
        titleResId = R.string.quiz_q2_title,
        optionResIds = listOf(
            R.string.quiz_q2_option_1,
            R.string.quiz_q2_option_2,
            R.string.quiz_q2_option_3,
        ),
        correctIndex = 0,
    ),
    QuizQuestion(
        titleResId = R.string.quiz_q3_title,
        optionResIds = listOf(
            R.string.quiz_q3_option_1,
            R.string.quiz_q3_option_2,
            R.string.quiz_q3_option_3,
        ),
        correctIndex = 0,
    ),
    QuizQuestion(
        titleResId = R.string.quiz_q4_title,
        optionResIds = listOf(
            R.string.quiz_q4_option_1,
            R.string.quiz_q4_option_2,
            R.string.quiz_q4_option_3,
        ),
        correctIndex = 0,
    ),
    QuizQuestion(
        titleResId = R.string.quiz_q5_title,
        optionResIds = listOf(
            R.string.quiz_q5_option_1,
            R.string.quiz_q5_option_2,
            R.string.quiz_q5_option_3,
        ),
        correctIndex = 1,
    ),
)
