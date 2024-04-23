package com.example.blodpool

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {


    private lateinit var imageUri: Uri

    external fun getTest() : String

    external fun cvTest(mat_addy: Long, mat_addy_res: Long, x_addy: Int, y_addy: Int)

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission has already been granted

            System.loadLibrary("testcpp")




            displayFrontpage()

            OpenCVLoader.initDebug()


            Toast.makeText(applicationContext,getTest(),Toast.LENGTH_LONG).show()
            // You can now use the camera
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, you can now use the camera
            } else {
                // Camera permission denied
                // Close the app
                finish()
            }
        }
        System.loadLibrary("testcpp")




        displayFrontpage()

        OpenCVLoader.initDebug()


        Toast.makeText(applicationContext,getTest(),Toast.LENGTH_LONG).show()

    }







    fun displayFrontpage(){
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.btn)
        val intent = Intent("android.media.action.IMAGE_CAPTURE")

       // var f = createImageFile()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        var filePath = imageFileName

        val mImageUri = FileProvider.getUriForFile(this,
            "com.example.blodpool.fileprovider",
            image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        //startActivityForResult(intent, 0);

        imageUri = mImageUri

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

    fun selectObjectImage(initialUri: Uri, xPos: Int, yPoS: Int): Bitmap{

        var initialImage = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), initialUri));


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
    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK){
           // Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()



            setContentView(R.layout.captured_image_view)

            Toast.makeText(applicationContext,"found res!",Toast.LENGTH_LONG).show()

            val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)

            val mTextViewX = findViewById<TextView>(R.id.text_view_1)
            val mTextViewY = findViewById<TextView>(R.id.text_view_2)
            val image = findViewById<ImageView>(R.id.captured_image)



          //  val bitmap = (data?.extras?.get("data")) as Bitmap

           // image.setImageBitmap(bitmap)
            image.setImageURI(imageUri)

          //  Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()

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

                val resultBitmap = selectObjectImage(imageUri, imageX, imageY)

                image.setImageBitmap(resultBitmap)
                //image.setRotation(90F);

                true
            }



            
           // displayImagePage(resultBitmap)

        }


    }
}



