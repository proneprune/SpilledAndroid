package com.example.blodpool

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.blodpool.ui.theme.BlodpoolTheme

import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core.inRange
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2HSV
import org.opencv.imgproc.Imgproc.COLOR_RGB2HSV

import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import org.opencv.core.Core
import org.opencv.core.Core.bitwise_and
import org.opencv.core.CvType.CV_8UC1


class MainActivity : ComponentActivity() {

    external fun getTest() : String

    external fun cvTest(mat_addy: Long, mat_addy_res: Long, x_addy: Int, y_addy: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.loadLibrary("testcpp")


        displayFrontpage()

        OpenCVLoader.initDebug()


        Toast.makeText(applicationContext,getTest(),Toast.LENGTH_LONG).show()

    }

    fun displayFrontpage(){
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.btn)
        val intent = Intent("android.media.action.IMAGE_CAPTURE")

        button.setOnClickListener{
            startActivityForResult(intent, 0)
        }
    }

    fun displayImagePage(displayImage: Bitmap){

        setContentView(R.layout.display_image)
        val button = findViewById<Button>(R.id.button)

        val image = findViewById<ImageView>(R.id.imageView2);

        image.setImageBitmap(displayImage)

        button.setOnClickListener{
            displayFrontpage()
        }

    }

    fun selectObjectImage(initialImage: Bitmap, xPos: Int, yPoS: Int): Bitmap{

        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)


        Toast.makeText(applicationContext,mat.toString(),Toast.LENGTH_LONG).show()

        val resMat = Mat()

        cvTest(mat.nativeObjAddr, resMat.nativeObjAddr, xPos, yPoS)


        Toast.makeText(applicationContext,resMat.toString(),Toast.LENGTH_LONG).show()

        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }



    //@Deprecated
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
           // Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()

            setContentView(R.layout.captured_image_view)

            val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)

            val mTextViewX = findViewById<TextView>(R.id.text_view_1)
            val mTextViewY = findViewById<TextView>(R.id.text_view_2)
            val image = findViewById<ImageView>(R.id.captured_image)



            val bitmap = (data.extras?.get("data")) as Bitmap

            image.setImageBitmap(bitmap)

            // When relative layout is touched
            mRelativeLayout.setOnTouchListener { _, motionEvent ->
                val imageWidth = image.drawable.intrinsicWidth
                val imageHeight = image.drawable.intrinsicHeight

                // X and Y values are fetched relative to the view (mRelativeLayout)
                val mX = motionEvent.x
                val mY = motionEvent.y

                // X and Y values are
                // displayed in the TextView
                // mTextViewX.text = "X: $mX"
                // mTextViewY.text = "Y: $mY"

                // Calculate the corresponding coordinates relative to the original image
                val imageX = (mX / image.width.toFloat() * imageWidth).toInt()
                val imageY = (mY / image.height.toFloat() * imageHeight).toInt()

                println("X: $imageX")
                println("Y: $imageY")

                // Display the coordinates relative to the original image
                mTextViewX.text = "X: $imageX"
                mTextViewY.text = "Y: $imageY"

                val resultBitmap = selectObjectImage(bitmap, imageX, imageY)

                image.setImageBitmap(resultBitmap)

                true
            }

            
           // displayImagePage(resultBitmap)

        }


    }
}



