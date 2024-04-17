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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        OpenCVLoader.initDebug()
        Toast.makeText(applicationContext,"CORRECT!",Toast.LENGTH_LONG).show()

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivityForResult(intent, 0)



    }
    //@Deprecated
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()

            val image = findViewById<ImageView>(R.id.imageView);
            val bitmap = (data.extras?.get("data")) as Bitmap


            // Create OpenCV mat object and copy content from bitmap
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)


            val hsvMat = Mat()
            Imgproc.cvtColor(mat, hsvMat, COLOR_RGB2HSV)

            val low_red = Scalar(0.0, 50.0, 50.0)
            val high_red = Scalar(10.0, 255.0, 255.0)
            inRange(hsvMat, low_red, high_red, hsvMat)


            val redHighlight = bitmap.copy(bitmap.config, true)
            Utils.matToBitmap(hsvMat, redHighlight)

            image.setImageBitmap(redHighlight)
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

