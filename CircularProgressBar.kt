package com.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.Keep
import kotlin.math.min
import kotlin.math.roundToInt


class CircularProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val defaultDuration = 500
    private val rectF: RectF = RectF()
    private val backgroundPaint: Paint
    private val foregroundPaint: Paint
    private var objectAnimator: ObjectAnimator? = null

    /**Don't change the progress name because it is used by ObjectAnimator*/
    private var progress = 0f
    private var strokeWidth = 3f
    private var strokeCap: Paint.Cap = Paint.Cap.ROUND
    private var minimum = 0
    private var maximum = 100
    private var duration: Int = defaultDuration
    private var color = Color.DKGRAY
    private var bgColor = Color.LTGRAY

    init {
        val typedArray: TypedArray = context.theme.obtainStyledAttributes(attrs,
                R.styleable.CircularProgressBar, 0, 0)

        try {
            strokeWidth = typedArray.getDimension(R.styleable.CircularProgressBar_strokeWidth, strokeWidth)
            progress = typedArray.getFloat(R.styleable.CircularProgressBar_progress, progress)
            color = typedArray.getInt(R.styleable.CircularProgressBar_progressbarColor, color)
            bgColor = typedArray.getInt(R.styleable.CircularProgressBar_backgroundColor, bgColor)
            minimum = typedArray.getInt(R.styleable.CircularProgressBar_min, minimum)
            maximum = typedArray.getInt(R.styleable.CircularProgressBar_max, maximum)
            duration = typedArray.getInt(R.styleable.CircularProgressBar_progressDuration, defaultDuration)
            if (duration < defaultDuration)
                duration = defaultDuration

            strokeCap = when (typedArray.getInt(R.styleable.CircularProgressBar_strokeCap, 1)) {
                0 -> Paint.Cap.BUTT
                1 -> Paint.Cap.ROUND
                2 -> Paint.Cap.SQUARE
                else -> Paint.Cap.ROUND
            }
        } finally {
            typedArray.recycle()
        }

        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.color = bgColor
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = strokeWidth

        foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        foregroundPaint.color = color
        foregroundPaint.style = Paint.Style.STROKE
        foregroundPaint.strokeCap = strokeCap
        foregroundPaint.strokeWidth = strokeWidth
    }

    val minValue get():Int = minimum
    val maxValue get():Int = maximum
    val progressColor get():Int = color
    val progressBgColor get():Int = bgColor
    val progressStrokeWidth get():Float = strokeWidth
    val progressDuration get():Int = duration

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        backgroundPaint.strokeWidth = strokeWidth
        foregroundPaint.strokeWidth = strokeWidth
        invalidate()
        requestLayout()//Because it should recalculate its bounds
    }

    fun setStrokeCap(strokeCap: Paint.Cap) {
        this.strokeCap = strokeCap
    }

    fun setProgressDuration(duration: Int) {
        if (duration < defaultDuration)
            this.duration = defaultDuration
        else
            this.duration = duration
    }

    fun setMin(min: Int) {
        this.minimum = min
        invalidate()
    }

    fun setMax(max: Int) {
        this.maximum = max
        invalidate()
    }

    fun setColor(color: Int) {
        this.color = color
        this.bgColor = Color.LTGRAY
        backgroundPaint.color = bgColor
        foregroundPaint.color = color
        invalidate()
        requestLayout()
    }

    fun setColor(color: Int, bgColor: Int) {
        this.color = color
        this.bgColor = bgColor
        backgroundPaint.color = bgColor
        foregroundPaint.color = color
        invalidate()
        requestLayout()
    }

    /**Don't change the setter name because it is used by ObjectAnimator*/
    @Keep
    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    /**Don't change the getter name because it is used by ObjectAnimator*/
    @Keep
    fun getProgress(): Float {
        return progress
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawOval(rectF, backgroundPaint)
        val angle = 360 * progress / maximum
        val startAngle = -90
        canvas.drawArc(rectF, startAngle.toFloat(), angle, false, foregroundPaint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = min(width, height)
        setMeasuredDimension(min, min)
        rectF.set(0 + strokeWidth / 2, 0 + strokeWidth / 2, min - strokeWidth / 2, min - strokeWidth / 2)
    }

    /**
     * Lighten the given color by the factor
     *
     * @param color  The color to lighten
     * @param factor 0 to 4
     * @return A brighter color
     */
    fun lightenColor(color: Int, factor: Float): Int {
        val r = Color.red(color) * factor
        val g = Color.green(color) * factor
        val b = Color.blue(color) * factor
        val ir = min(255, r.toInt())
        val ig = min(255, g.toInt())
        val ib = min(255, b.toInt())
        val ia = Color.alpha(color)
        return Color.argb(ia, ir, ig, ib)
    }

    /**
     * Transparent the given color by the factor
     * The more the factor closer to zero the more the color gets transparent
     *
     * @param color  The color to transparent
     * @param factor 1.0f to 0.0f
     * @return int - A transplanted color
     */
    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    /**
     * Set the progress with an animation.
     * Note that the [android.animation.ObjectAnimator] Class automatically set the progress
     * so don't call the [CircularProgressBar.setProgress] directly within this method.
     *
     * @param progress The progress it should animate to it.
     */
    fun setProgressWithAnimation(progress: Float, listener: ProgressBarListener? = null) {

        objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress)
                .also {
                    it.duration = this.duration.toLong()
                    it.interpolator = DecelerateInterpolator()
                    it.start()
                }

        listener?.let {
            objectAnimator?.addListener(object : Animator.AnimatorListener {

                override fun onAnimationEnd(animation: Animator?) {
                    it.onProgressComplete()
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    it.onProgressStart()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    it.onProgressCancel()
                }

            })
        }

    }

    fun resetProgress() {
        setProgress(0f)
        objectAnimator?.cancel()
    }
}



















