package Rabia.examples.surfaceviewexample3_kt

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null
) :
    SurfaceView(mContext, attrs), Runnable {
    private var mRunning = false
    private var mGameThread: Thread? = null
    private val mPath: Path
    private var mFlashlightCone: FlashlightCone? = null
    private val mPaint: Paint
    private var mBitmap: Bitmap? = null
    private var mWinnerRect: RectF? = null
    private var mBitmapX = 0
    private var mBitmapY = 0
    private var mViewWidth = 0
    private var mViewHeight = 0
    private val mSurfaceHolder: SurfaceHolder
    private var circleColor = Color.GREEN
    var mediaPlayer: MediaPlayer? = null

    init {
        Log.i("Nitesh", "init")
        mSurfaceHolder = holder //from getHolder()
        mPaint = Paint()
        mPaint.color = Color.DKGRAY
        mPath = Path()
    }

    /**
     * We cannot get the correct dimensions of views in onCreate because
     * they have not been inflated yet. This method is called every time the
     * size of a view changes, including the first time after it has been
     * inflated.
     *
     * @param w Current width of view.
     * @param h Current height of view.
     * @param oldw Previous width of view.
     * @param oldh Previous height of view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.i("Nitesh", "onSizeChanged")
        mViewWidth = w
        mViewHeight = h
        mFlashlightCone = FlashlightCone(mViewWidth, mViewHeight)

        // Set font size proportional to view size.
        mPaint.textSize = (mViewHeight / 5).toFloat()
        mBitmap = BitmapFactory.decodeResource(
            mContext.resources, R.drawable.treasure
        )
        setUpBitmap()
    }

    /**
     * Runs in a separate thread.
     * All drawing happens here.
     */
    override fun run() {
        Log.i("Nitesh", "run")
        var canvas: Canvas
        while (mRunning) {
            Log.i("Nitesh", "mRunning")
            // If we can obtain a valid drawing surface...
            if (mSurfaceHolder.surface.isValid) {

                // Helper variables for performance.
                val x: Int? = mFlashlightCone?.getX()
                val y: Int? = mFlashlightCone?.getY()
                val radius: Int? = mFlashlightCone?.getRadius()

                canvas = mSurfaceHolder.lockCanvas()
                // Fill the canvas with white and draw the bitmap.
                canvas.drawColor(circleColor) // added circle color green
                canvas.save()
                canvas.drawBitmap(mBitmap!!, mBitmapX.toFloat(), mBitmapY.toFloat(), mPaint)
                // Add clipping region and fill rest of the canvas with black.
                mPath.addCircle(
                    x!!.toFloat(),
                    y!!.toFloat(),
                    radius!!.toFloat(),
                    Path.Direction.CCW
                )
                // The method clipPath(path, Region.Op.DIFFERENCE) was
                // deprecated in API level 26. The recommended alternative
                // method is clipOutPath(Path), which is currently available
                // in API level 26 and higher.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                    canvas.clipPath(mPath, Region.Op.DIFFERENCE)

                } else {
                    canvas.clipOutPath(mPath)
                }
                // add image on background

                var backgroundImage = BitmapFactory.decodeResource(
                    mContext.resources, R.drawable.map
                )
                canvas.drawBitmap(backgroundImage, 0.0f, 0.0f, mPaint)
                // If the x, y coordinates of the user touch are within a
                //  bounding rectangle, display the winning message.
                if (x > mWinnerRect!!.left && x < mWinnerRect!!.right && y > mWinnerRect!!.top && y < mWinnerRect!!.bottom) {
                    canvas.drawColor(Color.WHITE)

                    playMusic()
                    canvas.drawBitmap(mBitmap!!, mBitmapX.toFloat(), mBitmapY.toFloat(), mPaint)
                    canvas.drawText(
                        "GOT IT!", (mViewWidth / 3).toFloat(), (mViewHeight / 2).toFloat(), mPaint
                    )
                    circleColor = Color.WHITE
                } else {
                    circleColor = Color.GREEN
                    stopMusic()
                }
                // Clear the path data structure.

                mPath.rewind()
                // Restore the previously saved (default) clip and matrix state.
                canvas.restore()
                // Release the lock on the canvas and show the surface's
                // contents on the screen.
                mSurfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
    }

    /**
     * Updates the game data.
     * Sets new coordinates for the flashlight cone.
     *
     * @param newX New x position of touch event.
     * @param newY New y position of touch event.
     */
    private fun updateFrame(newX: Int, newY: Int) {
        Log.i("Nitesh", "updateFrame")
        mFlashlightCone!!.update(newX, newY)
    }

    /**
     * Calculates a randomized location for the bitmap
     * and the winning bounding rectangle.
     */
    private fun setUpBitmap() {
        Log.i("Nitesh", "setUpBitmap")
        mBitmapX = Math.floor(
            Math.random() * (mViewWidth - mBitmap!!.width)
        ).toInt()
        mBitmapY = Math.floor(
            Math.random() * (mViewHeight - mBitmap!!.height)
        ).toInt()
        mWinnerRect = RectF(
            mBitmapX.toFloat(), mBitmapY.toFloat(),
            (mBitmapX + mBitmap!!.width).toFloat(),
            (mBitmapY + mBitmap!!.height).toFloat()
        )
    }

    /**
     * Called by MainActivity.onPause() to stop the thread.
     */
    fun pause() {
        mRunning = false
        Log.i("Nitesh", "pause")
        stopMusic()
        try {
            // Stop the thread == rejoin the main thread.
            mGameThread!!.join()
        } catch (e: InterruptedException) {
        }
    }

    /**
     * Called by MainActivity.onResume() to start a thread.
     */
    fun resume() {
        mRunning = true
        Log.i("Nitesh", "resume")
        mGameThread = Thread(this)
        mGameThread!!.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.i("Nitesh", "onTouchEvent")
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                setUpBitmap()
                // Set coordinates of flashlight cone.
                updateFrame(x.toInt(), y.toInt())
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // Updated coordinates for flashlight cone.
                updateFrame(x.toInt(), y.toInt())
                invalidate()
            }
            else -> {}
        }
        return true
    }

    private fun playMusic() {
        Log.i("Nitesh", "playMusic")
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer.create(mContext, R.raw.you_got_it)
        if (mediaPlayer?.isPlaying == false)
            mediaPlayer?.start()
    }

    private fun stopMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer = null
        }
    }


}