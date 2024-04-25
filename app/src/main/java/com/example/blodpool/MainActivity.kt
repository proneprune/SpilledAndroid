package com.example.blodpool

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
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


class MainActivity : ComponentActivity() {

    //init of OpenCV functions
    private lateinit var imageUri: Uri
    external fun Undo(mat_addy: Long)
    external fun removeAllContours()
    external fun findArea(mat_addy: Long, x_addy: Int, y_addy: Int) : Int
    external fun findAreaTwo() : Int
    external fun cvTest(mat_addy: Long, mat_addy_res: Long, x_addy: Int, y_addy: Int)
    external fun rotateMat(mat_addy: Long, mat_addy_res: Long)

    //on startup loads OpenCV and displays frontpage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.loadLibrary("testcpp")

        displayFrontpage()

        OpenCVLoader.initDebug()
    }

    //function to display frontpage
    fun displayFrontpage(){
        //sets the correct view, init button and starts camera
        setContentView(R.layout.activity_main)
        val button = findViewById<ImageButton>(R.id.btn)
        val intent = Intent("android.media.action.IMAGE_CAPTURE")

        //time format for picture to store it
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

        imageUri = mImageUri

        button.setOnClickListener{
            startActivityForResult(intent, 0)
        }
    }

    fun displayImagePage(displayImage: Bitmap){

        //function to display the image page
        setContentView(R.layout.display_image)
        val button = findViewById<Button>(R.id.button)

        val image = findViewById<ImageView>(R.id.imageView2);

        image.setImageBitmap(displayImage)

        button.setOnClickListener{
            displayFrontpage()
        }
    }

    //function that can undo a selected object and also redraws the image to
    //display the correct objects. Removes the pixels from total count
    fun undoBridge(initialUri: Uri) : Bitmap{

        var initialImage = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), initialUri));

        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)


        Undo(mat.nativeObjAddr) //calls the undo c++ function


        val resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, resultBitmap)

        return resultBitmap
    }


    fun selectObjectImage(initialUri: Uri, xPos: Int, yPoS: Int): Bitmap{

        //selects object and displays correct image of the objects
        var initialImage = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), initialUri))
        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        val resMat = Mat()
        cvTest(mat.nativeObjAddr, resMat.nativeObjAddr, xPos, yPoS)



        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)
        return resultBitmap
    }

    fun rotateMatKotlin(initialImage: Bitmap) : Bitmap{
        //rotate image
        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        val resMat = Mat()
        rotateMat(mat.nativeObjAddr, resMat.nativeObjAddr)


        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }

    fun selectObjectImage(initialImage: Bitmap, xPos: Int, yPoS: Int): Bitmap{

        //Function to select objects in images
        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        val resMat = Mat()

        cvTest(mat.nativeObjAddr, resMat.nativeObjAddr, xPos, yPoS)

        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }

    fun findObjectArea(initialUri: Uri, xPos: Int, yPoS: Int): Int{
        //find the area of an object
        var initialImage = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), initialUri));


        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        return findArea(mat.nativeObjAddr, xPos, yPoS)
    }

    //function to display the chooseblood xml layout
    fun displaychooseblood(areaperpixel: Float ){
        setContentView(R.layout.choose_blood)


        val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)

        val mTextViewX = findViewById<TextView>(R.id.text_view_1)
        val mTextViewY = findViewById<TextView>(R.id.text_view_2)
        val image = findViewById<ImageView>(R.id.captured_image)

        image.setImageURI(imageUri)


        // When relative layout is touched
        val buttontoconfirm = findViewById<Button>(R.id.button2)
        val buttonToUndo = findViewById<Button>(R.id.button3)

        //listener to find if the screen is being pressed to find
        //coordinates of an object
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

            buttonToUndo.setOnClickListener(){
                var resultBitmap = undoBridge(imageUri)
                image.setImageBitmap(resultBitmap)
            }

            buttontoconfirm.setOnClickListener(){

                var pixels = findAreaTwo()

                val bloodpoolarea = areaperpixel*pixels

                setContentView(R.layout.area_of_blood)
                val Textviewarea = findViewById<TextView>(R.id.textViewb)
                Textviewarea.text = "The area of the bloodpool is $bloodpoolarea cm²"


                //functionality for button to go back to start when an area has been found
                val go_back_2 = findViewById<Button>(R.id.go_back_2)
                go_back_2.setOnClickListener(){
                    displayFrontpage()
                }
            }
            true
        }
    }

    //@Deprecated
    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK){
            setContentView(R.layout.captured_image_view)

            Toast.makeText(applicationContext,"found res!",Toast.LENGTH_LONG).show()

            val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)

            val mTextViewX = findViewById<TextView>(R.id.text_view_1)
            val mTextViewY = findViewById<TextView>(R.id.text_view_2)
            val image = findViewById<ImageView>(R.id.captured_image)


            image.setImageURI(imageUri)

            // When relative layout is touched
            val buttontoconfirm = findViewById<Button>(R.id.button2)


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

                buttontoconfirm.setOnClickListener(){
                    //var pixels = findObjectArea(imageUri, imageX, imageY)
                    var pixels = findAreaTwo()
                    val areaperpixel = 46.75f/pixels

                    displaychooseblood(areaperpixel)
                }
                true
            }
        }
    }
}
