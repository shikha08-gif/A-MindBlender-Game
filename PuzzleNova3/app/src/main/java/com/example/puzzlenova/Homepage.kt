package com.example.puzzlenova

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import kotlin.random.Random

// NOTE: Ensure your target activity (GameHubActivity) exists and is registered in AndroidManifest.xml
class Homepage : AppCompatActivity() {

    // Custom colors for the gradient effect
    private val colorStart = Color.parseColor("#1E1B4B") // Dark Purple
    private val colorEnd = Color.parseColor("#4C1D95") // Mid Purple

    // View references
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var enterButton: Button
    private lateinit var mainTitle: TextView // Not explicitly used but kept for context

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize Views
        // Assuming R.id.main_layout is the ID of your ConstraintLayout in activity_main.xml
        mainLayout = findViewById(R.id.main_layout)
        enterButton = findViewById(R.id.enter_button)

        // 2. Apply Custom Gradient Background Animation
        animateBackgroundGradient()

        // 3. Apply Animations to Main Elements
        animateFloatingPieces()
        animateButtonPulse()

        // 4. Set Click Listener to navigate to the game hub
        enterButton.setOnClickListener {
            enterHub()
        }
    }

    /**
     * Simulates the CSS `gradientShift` animation by smoothly transitioning the background color.
     * Uses ValueAnimator to cycle between two colors infinitely.
     */
    private fun animateBackgroundGradient() {
        val colorAnimator = ValueAnimator.ofArgb(colorStart, colorEnd, colorStart)
        colorAnimator.duration = 8000 // 8s duration, matching the original CSS
        colorAnimator.repeatMode = ValueAnimator.REVERSE
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.addUpdateListener { animator ->
            mainLayout.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimator.start()
    }

    /**
     * Applies the 'float' animation to the decorative puzzle pieces in the background.
     * The `repeatCount` and `repeatMode` must be set on the individual animators.
     */
    private fun animateFloatingPieces() {
        // NOTE: The IDs below are placeholders and assume they exist in activity_main.xml
        val pieceIds = listOf(R.id.piece_1, R.id.piece_2, R.id.piece_3, R.id.piece_4, R.id.piece_5, R.id.piece_6)

        for (id in pieceIds) {
            val pieceView = findViewById<TextView>(id)
            if (pieceView != null) {
                val delay = Random.nextLong(0, 3000)

                // 1. Floating (Y-translation)
                val floatUp = ObjectAnimator.ofFloat(pieceView, "translationY", 0f, -60f).apply {
                    duration = 3000 // Half the total cycle time
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                }

                // 2. Subtle rotation
                val rotate = ObjectAnimator.ofFloat(pieceView, "rotation", 0f, 5f, -5f, 0f).apply {
                    duration = 6000 // Full cycle time
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                }

                val animatorSet = AnimatorSet()
                animatorSet.playTogether(floatUp, rotate)
                animatorSet.startDelay = delay
                animatorSet.interpolator = AccelerateDecelerateInterpolator()
                animatorSet.start()
            }
        }
    }

    /**
     * Applies the 'pulse' animation to the "Enter the Hub" button using ValueAnimator properties
     * to ensure infinite repetition.
     */
    private fun animateButtonPulse() {
        val scaleUpX = ObjectAnimator.ofFloat(enterButton, "scaleX", 1f, 1.05f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val scaleUpY = ObjectAnimator.ofFloat(enterButton, "scaleY", 1f, 1.05f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        // Run both X and Y scaling simultaneously
        val pulseSet = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            interpolator = LinearInterpolator()
            startDelay = 500 // Initial delay
        }
        pulseSet.start()
    }

    /**
     * Handles the 'Enter the Hub' action and navigates to GameHubActivity.
     */
    private fun enterHub() {
        // 1. Simulate Entrance Animation (Screen shrink/fade)
        val transition = AnimatorSet()
        val scaleX = ObjectAnimator.ofFloat(mainLayout, "scaleX", 1f, 0.95f)
        val scaleY = ObjectAnimator.ofFloat(mainLayout, "scaleY", 1f, 0.95f)
        val alpha = ObjectAnimator.ofFloat(mainLayout, "alpha", 1f, 0.8f)

        transition.playTogether(scaleX, scaleY, alpha)
        transition.duration = 500
        transition.interpolator = AccelerateDecelerateInterpolator()
        transition.start()

        Toast.makeText(this, "🚀 Loading PuzzleNova Hub...", Toast.LENGTH_SHORT).show()

        // 2. Launch new activity on animation end
        transition.doOnEnd {
            // Use Intent to navigate to GameHubActivity
            val intent = Intent(this, GameHubActivity::class.java)
            startActivity(intent)

            // Optional: Finish MainActivity so the user doesn't return with the back button
            finish()
        }
    }
}
