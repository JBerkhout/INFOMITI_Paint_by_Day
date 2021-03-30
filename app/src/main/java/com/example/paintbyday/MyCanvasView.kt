package com.example.paintbyday

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

private const val STROKE_WIDTH = 12f

class MyCanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private lateinit var scaledOverlay : Bitmap
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.white, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.purple_500, null)
    private var path = Path()
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private var sectionIndex = 0

    private var overlayArray = arrayOf(BitmapFactory.decodeResource(resources, R.drawable.section1overlay),
                                       BitmapFactory.decodeResource(resources, R.drawable.section2overlay),
                                       BitmapFactory.decodeResource(resources, R.drawable.section3overlay),
                                       BitmapFactory.decodeResource(resources, R.drawable.section4overlay),
                                       BitmapFactory.decodeResource(resources, R.drawable.section5overlay),
                                       BitmapFactory.decodeResource(resources, R.drawable.section6overlay)
    )
    private var backgroundArray = arrayOf(BitmapFactory.decodeResource(resources, R.drawable.section1background),
                                          BitmapFactory.decodeResource(resources, R.drawable.section2background),
                                          BitmapFactory.decodeResource(resources, R.drawable.section3background),
                                          BitmapFactory.decodeResource(resources, R.drawable.section4background),
                                          BitmapFactory.decodeResource(resources, R.drawable.section5background),
                                          BitmapFactory.decodeResource(resources, R.drawable.section6background)
    )

    private var mShowText: Boolean
    private var textPos: Int

    // Cached touch location
    private var currentX = 0f
    private var currentY = 0f

    init{
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MyCanvasView,
                0, 0).apply {

            try {
                mShowText = getBoolean(R.styleable.MyCanvasView_showText, false)
                textPos = getInteger(R.styleable.MyCanvasView_labelPosition, 0)
            } finally {
                recycle()
            }
        }
    }

    // Define painting parameters
    // See https://developer.android.com/reference/kotlin/android/graphics/Paint
    private val paint = Paint().apply{
        color       = drawColor
        isAntiAlias = true
        isDither    = true
        style       = Paint.Style.STROKE
        strokeJoin  = Paint.Join.ROUND
        strokeCap   = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    // Detect touches by user
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action){
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    // MARKED FOR DELETE
    public fun drawNewSection(canvas: Canvas){
        canvas.drawBitmap(backgroundArray[sectionIndex], 0f, 0f, null)
        canvas.drawBitmap(overlayArray[sectionIndex], 0f, 0f, null)
    }

    private fun touchStart(){
        // Retrieve the current color before we start drawing
        paint.color = FullscreenActivity.getColor()

        // Clear old path
        path.reset()
        // Set path to new touch coordinates
        path.moveTo(motionTouchEventX, motionTouchEventY)
        // Cache current location
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove(){
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2)
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it
            extraCanvas.drawPath(path, paint)
        }
        // Invalidate forces a redraw of the screen with the new path
        invalidate()
    }

    private fun touchUp(){
        // Reset path once user finishes drawing. Path has already been drawn in touchMove(), so no need for invalidation
        path.reset()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        // Delete previously existing bitmap to avoid memory leaks
        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        // Create bitmap of the new correct size, and convert it to canvas, fill it with background color
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        //extraCanvas.drawColor(backgroundColor)

        // Scale overlays and backgrounds because for loops are stupid in kotlin
        overlayArray[0] = Bitmap.createScaledBitmap(overlayArray[0], extraCanvas.width, extraCanvas.height, false)
        overlayArray[1] = Bitmap.createScaledBitmap(overlayArray[1], extraCanvas.width, extraCanvas.height, false)
        overlayArray[2] = Bitmap.createScaledBitmap(overlayArray[2], extraCanvas.width, extraCanvas.height, false)
        overlayArray[3] = Bitmap.createScaledBitmap(overlayArray[3], extraCanvas.width, extraCanvas.height, false)
        overlayArray[4] = Bitmap.createScaledBitmap(overlayArray[4], extraCanvas.width, extraCanvas.height, false)
        overlayArray[5] = Bitmap.createScaledBitmap(overlayArray[5], extraCanvas.width, extraCanvas.height, false)

        backgroundArray[0] = Bitmap.createScaledBitmap(backgroundArray[0], extraCanvas.width, extraCanvas.height, false)
        backgroundArray[1] = Bitmap.createScaledBitmap(backgroundArray[1], extraCanvas.width, extraCanvas.height, false)
        backgroundArray[2] = Bitmap.createScaledBitmap(backgroundArray[2], extraCanvas.width, extraCanvas.height, false)
        backgroundArray[3] = Bitmap.createScaledBitmap(backgroundArray[3], extraCanvas.width, extraCanvas.height, false)
        backgroundArray[4] = Bitmap.createScaledBitmap(backgroundArray[4], extraCanvas.width, extraCanvas.height, false)
        backgroundArray[5] = Bitmap.createScaledBitmap(backgroundArray[5], extraCanvas.width, extraCanvas.height, false)

        //scaledOverlay = Bitmap.createScaledBitmap(overlayArray[0], extraCanvas.width, extraCanvas.height, false)
    }

    override fun onDraw(canvas: Canvas){
        // Draw all pieces of the appropriate background
        var index1 = 0
        while(index1 <= FullscreenActivity.getIndex()){
            canvas.drawColor(backgroundColor)
            canvas.drawBitmap(backgroundArray[index1], 0f, 0f, null)
            index1 += 1
        }

        // Draw the user's drawing
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)

        // Finally, draw all pieces of the appropriate overlay
        var index2 = 0
        while(index2 <= FullscreenActivity.getIndex()){
            //canvas.drawBitmap(backgroundArray[index], 0f, 0f, null)
            canvas.drawBitmap(overlayArray[index2], 0f, 0f, null)
            index2 += 1
        }
    }
}