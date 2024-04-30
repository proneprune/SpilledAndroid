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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import kotlin.time.Duration.Companion.milliseconds



val gravity = 9.82f
var densityBlood = 1060f
var surfaceTensionBlood = 0.058f
class MainActivity : ComponentActivity() {

    private lateinit var imageUri: Uri

    external fun getTest() : String

    external fun Undo(mat_addy: Long)
    external fun removeAllContours()

    external fun findArea(mat_addy: Long, x_addy: Int, y_addy: Int) : Int

    external fun findAreaTwo() : Int

    external fun cvTest(mat_addy: Long, mat_addy_res: Long, x_addy: Int, y_addy: Int)

    external fun rotateMat(mat_addy: Long, mat_addy_res: Long)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.loadLibrary("testcpp")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)


        } else {
            displayFrontpage()
            // Permission is already granted, proceed with your camera-related tasks
        }



        OpenCVLoader.initDebug()

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                // If request is cancelled, the grantResults array will be empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your camera-related tasks
                    displayFrontpage()
                } else {
                    // Permission denied, handle accordingly (e.g., display a message or disable camera functionality)
                    setContentView(R.layout.permission_denied)
                }

            }
        }
    }

    fun settings(){
        setContentView(R.layout.settings)
        val abtusbutton = findViewById<Button>(R.id.aboutus)
        val liquidbutton = findViewById<Button>(R.id.liquid)


        liquidbutton.setOnClickListener{
            chooseLiquid()
        }

        abtusbutton.setOnClickListener{
            val url = "https://www.udio.com/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

        }




    }

    fun chooseLiquid(){
        setContentView(R.layout.choose_liquid)
        val bloodbutton = findViewById<Button>(R.id.button5)
        val waterbutton = findViewById<Button>(R.id.button6)
        val custombutton = findViewById<Button>(R.id.button7)


        bloodbutton.setOnClickListener{
            displayFrontpage()


        }
        waterbutton.setOnClickListener{
            densityBlood = 1000f
            surfaceTensionBlood = 0.072f
            displayFrontpage()

        }
        custombutton.setOnClickListener{

            //val editTextDensity = findViewById<EditText>(R.id.editTextDensity)
            //val buttonConfirmDensity = findViewById<Button>(R.id.buttonConfirmDensity)
            //val editTextSurfaceTension = findViewById<EditText>(R.id.editTextSurfaceTension)
            //val buttonConfirmSurfaceTension = findViewById<Button>(R.id.buttonConfirmSurfaceTension)
            val densityInputDialog = AlertDialog.Builder(this)
            val densityInputEditText = EditText(this)
            densityInputDialog.setTitle("Insert Density in kg/m^3")
            densityInputDialog.setView(densityInputEditText)
            densityInputDialog.setPositiveButton("Confirm") { dialog, _ ->
                val densityInput = densityInputEditText.text.toString().toFloat()
                densityBlood = densityInput

                if (densityInput != null) {
                    // Density input is valid, proceed to surface tension input
                    dialog.dismiss()

                    // Show a dialog to input surface tension
                    val surfaceTensionInputDialog = AlertDialog.Builder(this)
                    val surfaceTensionInputEditText = EditText(this)
                    surfaceTensionInputDialog.setTitle("Insert Surface Tension in N/m")
                    surfaceTensionInputDialog.setView(surfaceTensionInputEditText)
                    surfaceTensionInputDialog.setPositiveButton("Confirm") { _, _ ->
                        val surfaceTensionInput = surfaceTensionInputEditText.text.toString().toFloat()
                        surfaceTensionBlood = surfaceTensionInput
                        if (surfaceTensionInput != null) {
                            displayFrontpage()

                        } else {
                            // Invalid surface tension input
                            Toast.makeText(this, "Invalid surface tension input", Toast.LENGTH_SHORT).show()
                        }
                    }
                    surfaceTensionInputDialog.show()
                    displayFrontpage()

                } else {
                    // Invalid density input
                    Toast.makeText(this, "Invalid density input", Toast.LENGTH_SHORT).show()
                }
            }
            densityInputDialog.show()









        }





    }


    fun deletePreviousPhotos(){
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        var filesInsidePath = storageDir?.listFiles()

        filesInsidePath?.forEach {
            it.delete()
        }
    }


    fun startCameraCapture(){

        val intent = Intent("android.media.action.IMAGE_CAPTURE")

        val prefix = "tempPicture"
        val suffix = ".jpg"
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val image = File.createTempFile(
            prefix,
            suffix,
            directory
        )

        imageUri = FileProvider.getUriForFile(this,
            "com.example.blodpool.fileprovider",
            image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent, 0)

    }

    fun displayFrontpage(){
        setContentView(R.layout.activity_main)
        val button = findViewById<ImageButton>(R.id.btn)
        val settingsbutton = findViewById<Button>(R.id.buttonbog)
        val turtorialbutton = findViewById<Button>(R.id.button10)
        val button = findViewById<ImageButton>(R.id.btn)

        //deletePreviousPhotos()

        button.setOnClickListener{
            startCameraCapture()
        }
        settingsbutton.setOnClickListener{
            settings()
        }

        turtorialbutton.setOnClickListener{

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

    fun undoBridge(initialUri: Uri) : Bitmap{

        var initialImage = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), initialUri));

        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)


        Undo(mat.nativeObjAddr)


        val resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, resultBitmap)

        return resultBitmap
    }


    fun selectObjectImage(initialUri: Uri, xPos: Int, yPoS: Int): Bitmap{

        var initialImage = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), initialUri));

        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        //  Toast.makeText(applicationContext,mat.toString(),Toast.LENGTH_LONG).show()

        val resMat = Mat()



        cvTest(mat.nativeObjAddr, resMat.nativeObjAddr, xPos, yPoS)


        // Toast.makeText(applicationContext,resMat.toString(),Toast.LENGTH_LONG).show()

        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }

    fun rotateMatKotlin(initialImage: Bitmap) : Bitmap{

        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        val resMat = Mat()

        rotateMat(mat.nativeObjAddr, resMat.nativeObjAddr)


        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }

    fun selectObjectImage(initialImage: Bitmap, xPos: Int, yPoS: Int): Bitmap{


        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        //  Toast.makeText(applicationContext,mat.toString(),Toast.LENGTH_LONG).show()

        val resMat = Mat()

        cvTest(mat.nativeObjAddr, resMat.nativeObjAddr, xPos, yPoS)


        // Toast.makeText(applicationContext,resMat.toString(),Toast.LENGTH_LONG).show()

        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }

    fun findObjectArea(initialUri: Uri, xPos: Int, yPoS: Int): Int{

        var initialImage = Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), initialUri));


        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        return findArea(mat.nativeObjAddr, xPos, yPoS)

    }



    fun displaychooseblood(areaperpixel: Float ){
        setContentView(R.layout.choose_blood)

        val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)

        val mTextViewX = findViewById<TextView>(R.id.text_view_1)
        val mTextViewY = findViewById<TextView>(R.id.text_view_2)
        val image = findViewById<ImageView>(R.id.captured_image)


        //  val bitmap = (data?.extras?.get("data")) as Bitmap

        // image.setImageBitmap(bitmap)
        image.setImageURI(imageUri)

        mRelativeLayout.layoutParams.height = image.height
        mRelativeLayout.layoutParams.width = image.width

        mRelativeLayout.requestLayout()

       // mRelativeLayout.layoutParams.height = image.height

        //  Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()

        // When relative layout is touched
        val buttontoconfirm = findViewById<Button>(R.id.button2)
        val buttonToUndo = findViewById<Button>(R.id.button3)


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
            val imageX = (mX * (imageWidth.toFloat() / image.width.toFloat())).toInt()
            val imageY = (mY * (imageHeight.toFloat() / image.height.toFloat())).toInt()

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



                val volume = (2 * surfaceTensionBlood * 10000) / (densityBlood * gravity * 3.14159 * bloodpoolarea * 0.0001)
                val formattedVolume = String.format("%.2f",volume)



                setContentView(R.layout.area_of_blood)
                val Textviewarea = findViewById<TextView>(R.id.textViewb)

                Textviewarea.text = "The volume of the blood is $formattedVolume cm³"


                //    Toast.makeText(applicationContext, "bloodpool area is: " + bloodpoolarea ,Toast.LENGTH_LONG).show()

                //functionality for button to go back to start when an area has been found
                val go_back_2 = findViewById<Button>(R.id.go_back_2)
                //deletePreviousPhotos()


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


            //image.layoutParams.width = 500
            //image.layoutParams.height = 500

            mRelativeLayout.layoutParams.height = image.height
            mRelativeLayout.layoutParams.width = image.width

            mRelativeLayout.requestLayout()


            //  Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()

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
                val imageX = (mX * (imageWidth.toFloat() / image.width.toFloat())).toInt()
                val imageY = (mY * (imageHeight.toFloat() / image.height.toFloat())).toInt()

                println("X: $imageX")
                println("Y: $imageY")

                // Display the coordinates relative to the original image
                mTextViewX.text = "X: ${imageX}"
                mTextViewY.text = "Y: ${imageY}"

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






            // displayImagePage(resultBitmap)

        }


    }
}
