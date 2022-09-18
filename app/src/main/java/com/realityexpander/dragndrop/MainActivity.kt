package com.realityexpander.dragndrop

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the "dropped on" locations (views) to accept drops
        val llTop = findViewById<LinearLayout>(R.id.llTop)
        val llBottom = findViewById<LinearLayout>(R.id.llBottom)
        llTop.setOnDragListener(dragListener)
        llBottom.setOnDragListener(dragListener)

        // Set the "drag from" locations (views) to be draggable
        val dragView = findViewById<View>(R.id.dragView)
        dragView.setOnLongClickListener { view ->
            val clipText = "This is our clipData text"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            view.rotation = 10f

            val rotationRad = Math.toRadians(view.rotation.toDouble())
            val w = (view.width * view.scaleX).toInt()
            val h = (view.height * view.scaleY).toInt()
            val s = Math.abs(Math.sin(rotationRad))
            val c = Math.abs(Math.cos(rotationRad))
            val width = (w * c + h * s).toInt()
            val height = (w * s + h * c).toInt()

            // view is the item that is being dragged
            val dragShadowBuilder = object: View.DragShadowBuilder(view) {
                override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
//                    super.onProvideShadowMetrics(size, touch)
//                    size.set(view.width, view.height)
//                    touch.set(view.width / 2, view.height / 2)

                    shadowSize.set(width, height)
                    shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2)
                }

                override fun onDrawShadow(canvas: Canvas) {
//                    super.onDrawShadow(canvas)
//                    canvas.drawColor(Color.RED)

                    println("onDrawShadow")

                    canvas.scale(
                        view.scaleX,
                        view.scaleY,
                        (width / 2).toFloat(),
                        (height / 2).toFloat()
                    );
                    canvas.rotate(view.rotation + rotationAnim, (width / 2).toFloat(), (height / 2).toFloat());
                    canvas.translate(
                        ((width - view.width) / 2).toFloat(),
                        ((height - view.height) / 2).toFloat()
                    );
                    super.onDrawShadow(canvas);
                }
            }

            view.startDragAndDrop(data, dragShadowBuilder, view, 0)

            view.visibility = View.INVISIBLE
            true
        }

    }

    var rotationAnim = 0f
    var oldColor: Int = Color.WHITE

    // view is the item that is being "dropped" on
    private val dragListener = View.OnDragListener { view, dragEvent ->

        when (dragEvent.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                oldColor = (view.background as ColorDrawable).color

                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                view.setBackgroundColor(Color.WHITE)

                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.setBackgroundColor(oldColor)

                view.invalidate()
                true
            }
            DragEvent.ACTION_DROP -> {
                val item = dragEvent.clipData.getItemAt(0)
                val dragData = item.text

                // Reset the color of the view to original
                view.setBackgroundColor(oldColor)
                view.invalidate()

                Toast.makeText(this, "Dragged data is $dragData", Toast.LENGTH_SHORT).show()

                val v = dragEvent.localState as View
                v.rotation = 0f

                // Remove the dragged view from the parent viewGroup source
                val owner = v.parent as ViewGroup
                owner.removeView(v)

                // Add the dragged view to the dropped viewGroup destination
                val destination = view as LinearLayout
                destination.addView(v)

                v.visibility = View.VISIBLE
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                view.invalidate()
                true
            }
            else -> false
        }
    }
}