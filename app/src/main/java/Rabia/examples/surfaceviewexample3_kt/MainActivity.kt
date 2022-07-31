package Rabia.examples.surfaceviewexample3_kt

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var mGameView: GameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock orientation into landscape.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Create a GameView and bind it to this activity.
        // You don't need a ViewGroup to fill the screen, because the system
        // has a FrameLayout to which this will be added.
        mGameView = GameView(this)
        setContentView(mGameView)
    }

    /**
     * Pauses game when activity is paused.
     */
    override fun onPause() {
        super.onPause()
        mGameView?.pause()
    }

    /**
     * Resumes game when activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        mGameView?.resume()
    }
}