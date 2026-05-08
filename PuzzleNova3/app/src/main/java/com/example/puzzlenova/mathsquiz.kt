package com.example.puzzlenova

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random // <-- THIS IS THE FIX

class MathsQuiz : AppCompatActivity() {

    // UI Elements
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var feedbackTextView: TextView
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var gameLayout: RelativeLayout
    private lateinit var resetButton: Button
    private lateinit var newGameButton: Button

    // Game State
    private var score = 0
    private var correctAnswer = 0
    private var timer: CountDownTimer? = null
    private var gameActive = false
    private var currentDifficulty = Difficulty.NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mathsquiz)

        // Find all views
        scoreTextView = findViewById(R.id.score_text_view)
        timerTextView = findViewById(R.id.timer_text_view)
        questionTextView = findViewById(R.id.question_text_view)
        feedbackTextView = findViewById(R.id.feedback_text_view)
        option1Button = findViewById(R.id.option_1_button)
        option2Button = findViewById(R.id.option_2_button)
        option3Button = findViewById(R.id.option_3_button)
        option4Button = findViewById(R.id.option_4_button)
        gameLayout = findViewById(R.id.game_layout)
        resetButton = findViewById(R.id.reset_button)
        newGameButton = findViewById(R.id.new_game_button)

        // Set click listeners
        resetButton.setOnClickListener {
            startGame() // Reset with same difficulty
        }
        newGameButton.setOnClickListener {
            quitGame() // Go back to difficulty selection
        }

        val optionClickListener = View.OnClickListener {
            if (gameActive) {
                checkAnswer(it)
            }
        }
        option1Button.setOnClickListener(optionClickListener)
        option2Button.setOnClickListener(optionClickListener)
        option3Button.setOnClickListener(optionClickListener)
        option4Button.setOnClickListener(optionClickListener)

        // Show difficulty dialog immediately on start
        showDifficultySelectionDialog()
    }

    private fun showDifficultySelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_difficulty_selection, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)

        // Set the dialog's radio button to the last-used difficulty
        when (currentDifficulty) {
            Difficulty.EASY -> radioGroup.check(R.id.radio_easy)
            Difficulty.NORMAL -> radioGroup.check(R.id.radio_normal)
            Difficulty.HARD -> radioGroup.check(R.id.radio_hard)
        }

        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start Game") { dialog, _ ->
                currentDifficulty = when (radioGroup.checkedRadioButtonId) {
                    R.id.radio_easy -> Difficulty.EASY
                    R.id.radio_hard -> Difficulty.HARD
                    else -> Difficulty.NORMAL
                }
                startGame()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                if (gameLayout.visibility == View.GONE) {
                    finish()
                } else {
                    dialog.dismiss()
                }
            }
            .show()
    }

    private fun startGame() {
        score = 0
        gameActive = true
        gameLayout.visibility = View.VISIBLE
        feedbackTextView.text = ""
        updateScoreUI()
        generateQuestion(currentDifficulty)
        startTimer(currentDifficulty)
    }

    private fun quitGame() {
        gameActive = false
        gameLayout.visibility = View.GONE
        showDifficultySelectionDialog()
    }

    private fun generateQuestion(difficulty: Difficulty) {
        val options = mutableListOf<Int>()
        var num1: Int
        var num2: Int
        var question: String

        when (difficulty) {
            Difficulty.EASY -> {
                // Addition and Subtraction (1-30)
                val operator = Random.nextInt(2)
                num1 = Random.nextInt(1, 31)
                num2 = Random.nextInt(1, 31)

                if (operator == 0) { // Addition
                    correctAnswer = num1 + num2
                    question = "$num1 + $num2 = ?"
                } else { // Subtraction
                    if (num1 < num2) { val temp = num1; num1 = num2; num2 = temp }
                    correctAnswer = num1 - num2
                    question = "$num1 - $num2 = ?"
                }
                options.add(correctAnswer)
                while (options.size < 4) {
                    val wrongAnswer = Random.nextInt(1, 61)
                    if (wrongAnswer != correctAnswer && !options.contains(wrongAnswer)) {
                        options.add(wrongAnswer)
                    }
                }
            }
            Difficulty.NORMAL -> {
                // Multiplication and Division (Higher numbers)
                val operator = Random.nextInt(2)

                if (operator == 0) { // Multiplication (2-20)
                    num1 = Random.nextInt(2, 21)
                    num2 = Random.nextInt(2, 21)
                    correctAnswer = num1 * num2
                    question = "$num1 * $num2 = ?"
                } else { // Division (Result 2-25)
                    correctAnswer = Random.nextInt(2, 26)
                    num2 = Random.nextInt(2, 11)
                    num1 = correctAnswer * num2
                    question = "$num1 / $num2 = ?"
                }
                options.add(correctAnswer)
                while (options.size < 4) {
                    val wrongAnswer = Random.nextInt(2, 401)
                    if (wrongAnswer != correctAnswer && !options.contains(wrongAnswer)) {
                        options.add(wrongAnswer)
                    }
                }
            }
            Difficulty.HARD -> {
                // All operators + Modulo (Highest numbers)
                val operator = Random.nextInt(5) // 0:+, 1:-, 2:*, 3:/, 4:%

                when (operator) {
                    0 -> { // Addition (50-200)
                        num1 = Random.nextInt(50, 201)
                        num2 = Random.nextInt(20, 101)
                        correctAnswer = num1 + num2
                        question = "$num1 + $num2 = ?"
                    }
                    1 -> { // Subtraction (20-100) - Can be negative
                        num1 = Random.nextInt(20, 101)
                        num2 = Random.nextInt(20, 101)
                        correctAnswer = num1 - num2
                        question = "$num1 - $num2 = ?"
                    }
                    2 -> { // Multiplication (10-30)
                        num1 = Random.nextInt(10, 31)
                        num2 = Random.nextInt(10, 31)
                        correctAnswer = num1 * num2
                        question = "$num1 * $num2 = ?"
                    }
                    3 -> { // Division (Result 5-30)
                        correctAnswer = Random.nextInt(5, 31)
                        num2 = Random.nextInt(2, 16)
                        num1 = correctAnswer * num2
                        question = "$num1 / $num2 = ?"
                    }
                    else -> { // Modulo (Remainder) (20-100) % (2-15)
                        num1 = Random.nextInt(20, 101)
                        num2 = Random.nextInt(2, 16)
                        correctAnswer = num1 % num2
                        question = "$num1 % $num2 = ?"
                    }
                }
                options.add(correctAnswer)
                while (options.size < 4) {
                    val wrongAnswer = Random.nextInt(-50, 901)
                    if (wrongAnswer != correctAnswer && !options.contains(wrongAnswer)) {
                        options.add(wrongAnswer)
                    }
                }
            }
        }

        questionTextView.text = question
        options.shuffle()

        option1Button.text = options[0].toString()
        option2Button.text = options[1].toString()
        option3Button.text = options[2].toString()
        option4Button.text = options[3].toString()
    }

    private fun checkAnswer(view: View) {
        val selectedButton = view as Button
        val selectedAnswer = selectedButton.text.toString().toIntOrNull()

        if (selectedAnswer == correctAnswer) {
            score += 10
            feedbackTextView.text = "Correct!"
            feedbackTextView.setTextColor(Color.parseColor("#00ff00")) // Green
        } else {
            feedbackTextView.text = "Wrong! Answer: $correctAnswer"
            feedbackTextView.setTextColor(Color.parseColor("#ff0000")) // Red
        }

        updateScoreUI()
        generateQuestion(currentDifficulty)
    }

    private fun startTimer(difficulty: Difficulty) {
        timer?.cancel()

        val timeInMillis = when (difficulty) {
            Difficulty.EASY -> 30000L // 30s
            Difficulty.NORMAL -> 25000L // 25s
            Difficulty.HARD -> 20000L // 20s
        }

        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                gameOver()
            }
        }.start()
    }

    private fun gameOver() {
        gameActive = false
        timer?.cancel()
        feedbackTextView.text = "Time's Up! Final Score: $score"
        feedbackTextView.setTextColor(Color.parseColor("#00ffff")) // Neon Cyan
    }

    private fun updateScoreUI() {
        scoreTextView.text = "$score"
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }
}


