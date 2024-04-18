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
import org.opencv.core.Core
import org.opencv.core.Core.bitwise_and
import org.opencv.core.CvType.CV_8UC1


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayFrontpage()

        OpenCVLoader.initDebug()
       // Toast.makeText(applicationContext,"CORRECT!",Toast.LENGTH_LONG).show()

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


    //@Deprecated
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
           // Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()

            val bitmap = (data.extras?.get("data")) as Bitmap

            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)

            val hsvMat = Mat()
            Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV)

            val low_red = Scalar(0.0, 100.0, 100.0)
            val high_red = Scalar(10.0, 255.0, 255.0)
            val redMask = Mat()
            Core.inRange(hsvMat, low_red, high_red, redMask)

            // Invert the mask
            val invertedMask = Mat()
            Core.bitwise_not(redMask, invertedMask)

            // Set non-red areas to black
            val resultMat = Mat()
            mat.copyTo(resultMat)
            resultMat.setTo(Scalar(0.0, 0.0, 0.0), invertedMask)

            val resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(resultMat, resultBitmap)
            
            displayImagePage(resultBitmap)

        }


    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BlodpoolTheme {
        Greeting("Android")
    }
}

