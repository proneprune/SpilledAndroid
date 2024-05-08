package com.example.blodpool

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import kotlin.math.sqrt

class ClickReferenceActivity : AppCompatActivity() {

    external fun findobjectinfo(mat_addy: Long, x_addy: Int, y_addy: Int)

    external fun centerobjectinfo(mat_addy: Long)

    external fun getimage(mat_addy: Long)

    external fun getarea() : Float
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clickReferencePage()
    }

    fun clickReferencePage(){
        setContentView(R.layout.captured_image_view)
        val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)
        val image = findViewById<ImageView>(R.id.captured_image)

        Core.transpose(BloodMat, BloodMat);
        Core.flip(BloodMat, BloodMat, 1);

        val bitmap  = Bitmap.createBitmap(BloodMat.cols(), BloodMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(BloodMat, bitmap)


        image.setImageBitmap(bitmap)

        //button to take new picutre
        val newpicture = findViewById<ImageButton>(R.id.New_Picture)

        mRelativeLayout.layoutParams.height = image.height
        mRelativeLayout.layoutParams.width = image.width
        mRelativeLayout.requestLayout()

        newpicture.setOnClickListener(){
            finish()
        }

        val buttontoconfirm = findViewById<Button>(R.id.button2)

        mRelativeLayout.setOnTouchListener { _, motionEvent ->

            val imageWidth = image.drawable.intrinsicWidth
            val imageHeight = image.drawable.intrinsicHeight

            // X and Y values are fetched relative to the view (mRelativeLayout)
            val mX = motionEvent.x
            val mY = motionEvent.y

            // Calculate the corresponding coordinates relative to the original image
            val imageX = (mX * (imageWidth.toFloat() / image.width.toFloat())).toInt()
            val imageY = (mY * (imageHeight.toFloat() / image.height.toFloat())).toInt()



            findObjectInfo(bitmap, imageX, imageY)
            var resultBitmap = getImageBitmap()

            image.setImageBitmap(resultBitmap)

            buttontoconfirm.setOnClickListener(){
                var pixels = getarea()
                val areaperpixel = 46.75f/pixels
                println(BloodPixelArea)

                var bloodpoolarea = areaperpixel * BloodPixelArea

                bloodpoolarea = bloodpoolarea * 0.0001f
                var innerArg = currentLiquid.surfaceTension / (currentLiquid.density * gravity)
                var depth = 2 * sqrt(innerArg)
                var volume = depth * bloodpoolarea * 10000f

                val formattedVolume = String.format("%.2f", volume)

                setContentView(R.layout.display_liquid_volume)
                val Textviewarea = findViewById<TextView>(R.id.textViewb)

                val liquidtext = findViewById<TextView>(R.id.liquidtext)

                liquidtext.text = currentLiquid.name

                Textviewarea.text = "Volume \n $formattedVolume $unittobedisplayed"

                val go_back_2 = findViewById<ImageButton>(R.id.go_back_2)

                go_back_2.setOnClickListener(){
                    finish()
                }

            }
            true
        }
    }

    fun findObjectInfo(initialImage: Bitmap, xPos: Int, yPoS: Int){

        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        findobjectinfo(mat.nativeObjAddr, xPos, yPoS)

    }

    fun getImageBitmap() : Bitmap{

        var resMat = Mat()
        getimage(resMat.nativeObjAddr)

        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }
}