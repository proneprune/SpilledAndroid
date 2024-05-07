package com.example.blodpool

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import kotlin.math.sqrt

import android.content.Context
import org.opencv.core.Core


val gravity = 9.82f
var densityBlood = 1060f
var liquidname = "Blood"
var surfaceTensionBlood = 0.058f
var unitcalc = 1f
var unittobedisplayed = "dl"
lateinit var BloodMat: Mat
lateinit var BloodMatOriginal: Mat
var BloodPixelArea : Float = 0.0f



class MainActivity : ComponentActivity() {

    private lateinit var imageUri: Uri
    private val GALLERY_REQUEST_CODE = 100
    private val PREFS_NAME = "MyPrefs"
    private val PREF_TUTORIAL_SHOWN = "tutorialShown"


    external fun Undo(mat_addy: Long)
    external fun removeAllContours()

    external fun findArea(mat_addy: Long, x_addy: Int, y_addy: Int) : Int

    external fun findAreaTwo() : Int

    external fun cvTest(mat_addy: Long, mat_addy_res: Long, x_addy: Int, y_addy: Int)

    external fun rotateMat(mat_addy: Long, mat_addy_res: Long)

    external fun findobjectinfo(mat_addy: Long, x_addy: Int, y_addy: Int)

    external fun centerobjectinfo(mat_addy: Long)

    external fun getimage(mat_addy: Long)

    external fun getarea() : Float

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.loadLibrary("testcpp")

        //changes the background theme to the pinkish one
        setTheme(R.style.Theme_Blodpool)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)


        } else {
            if (!isTutorialShown()) {
                showTutorial()
                markTutorialAsShown()
            }
            displayFrontpage()
            // Permission is already granted, proceed with your camera-related tasks
        }

        OpenCVLoader.initDebug()

        /*var currentMat = Mat()

        setContentView(R.layout.livecameratest)
        viewBase = findViewById(R.id.javaCameraView)

        viewBase.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {
                // Your implementation for camera view started
            }

            override fun onCameraViewStopped() {
                // Your implementation for camera view stopped
            }

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                // Your implementation for camera frame processing

                var tmp = inputFrame.rgba()
                tmp.copyTo(currentMat)
                findobjectinfo(tmp.nativeObjAddr, tmp.cols()/2, tmp.rows()/2)

                //centerobjectinfo(tmp.nativeObjAddr)
                getimage(tmp.nativeObjAddr)

                return tmp
            }
        })
        viewBase.enableFpsMeter()
        viewBase.enableView()*/
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



    @SuppressLint("SetTextI18n")
    fun chooseUnit(){
        setContentView(R.layout.choose_unit)
        val dlbutton = findViewById<Button>(R.id.button4)
        val flozbutton = findViewById<Button>(R.id.button8)
        val backbutton = findViewById<ImageButton>(R.id.buttonbaackk)
        val unitused = findViewById<TextView>(R.id.textView3)

        unitused.text = "current unit: $unittobedisplayed"


        backbutton.setOnClickListener{
        settings()

        }

        dlbutton.setOnClickListener{
            unitcalc = 1f
            unittobedisplayed = "dl"
            chooseUnit()
        }

        flozbutton.setOnClickListener{
            unitcalc = 3.38140227f
            unittobedisplayed = "fl.oz"
            chooseUnit()
        }







    }

    fun settings(){
        setContentView(R.layout.settings)
        val abtusbutton = findViewById<ImageButton>(R.id.aboutus)
        val liquidbutton = findViewById<ImageButton>(R.id.liquid)
        val backbtn = findViewById<ImageButton>(R.id.backbtn123)
        val unitbutton = findViewById<ImageButton>(R.id.language)

        unitbutton.setOnClickListener{
        chooseUnit()

        }

        liquidbutton.setOnClickListener{
            displayCustomLiquids()

        }

        abtusbutton.setOnClickListener{
            aboutus()

        }

        backbtn.setOnClickListener{
        displayFrontpage()

        }




    }
    fun aboutus(){

        setContentView(R.layout.aboutus)
        val go_back = findViewById<ImageButton>(R.id.imageButton)
        go_back.setOnClickListener{
            settings()
        }

        val website = findViewById<ImageButton>(R.id.imageButton3)

        website.setOnClickListener{
            val url = "https://spilledowner.wixsite.com/spilled"
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
            densityBlood = 1060f
            surfaceTensionBlood = 0.058f
            liquidname = "Blood"
            displayFrontpage()


        }
        waterbutton.setOnClickListener{
            densityBlood = 1000f
            surfaceTensionBlood = 0.072f
            liquidname = "Water"
            displayFrontpage()

        }
        custombutton.setOnClickListener{

            //val editTextDensity = findViewById<EditText>(R.id.editTextDensity)
            //val buttonConfirmDensity = findViewById<Button>(R.id.buttonConfirmDensity)
            //val editTextSurfaceTension = findViewById<EditText>(R.id.editTextSurfaceTension)
            //val buttonConfirmSurfaceTension = findViewById<Button>(R.id.buttonConfirmSurfaceTension)

            displayCustomLiquids()




        }





    }

    fun displayIndividualCustom(liquid: LiquidManager.Liquid){
        setContentView(R.layout.display_individual_custom)

        val confirmliquidbutton = findViewById<Button>(R.id.conliq)
        val deletliquidbutton = findViewById<Button>(R.id.delustomC1)
        val backbutton = findViewById<Button>(R.id.goback)

        val nametext = findViewById<TextView>(R.id.textViewname)
        val dentext = findViewById<TextView>(R.id.textViewdensity)
        val surftext = findViewById<TextView>(R.id.textViewsurfacetension)

        nametext.text = liquid.name
        dentext.text = liquid.density.toString()
        surftext.text = liquid.surfaceTension.toString()

        confirmliquidbutton.setOnClickListener{
            densityBlood = liquid.density
            surfaceTensionBlood = liquid.surfaceTension
            liquidname = liquid.name
            displayFrontpage()
        }

        deletliquidbutton.setOnClickListener{
            if(liquid.name != "Blood" && liquid.name != "Water") {
                val liquidManager = LiquidManager()
                liquidManager.removeLiquid(
                    liquid,
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                )
                displayCustomLiquids()
            }


        }

        backbutton.setOnClickListener{

            displayCustomLiquids()

        }


    }

    fun displayCustomLiquids()
    {
        setContentView(R.layout.horizontal_scroll_liquids)

        val liquidManager = LiquidManager()
        //liquidManager.saveLiquid("blaba",100f,200f,getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        val liquids = liquidManager.loadLiquids(getExternalFilesDir(Environment.DIRECTORY_PICTURES))

        val backbtn = findViewById<ImageButton>(R.id.buttonback123)
        val newliquidbutton = findViewById<ImageButton>(R.id.buttonhej)

        newliquidbutton.setOnClickListener{
            customliquids()
        }
        // Get reference to the layout container inside the ScrollView

        val view = findViewById<LinearLayout>(R.id.linearlayout1)

        for (liquid in liquids) {
            val btn = ImageButton(this)
            when (liquid.name) {
                "Blood" -> btn.setImageResource(R.drawable.blood)
                "Water" -> btn.setImageResource(R.drawable.water)
                "Oil" -> btn.setImageResource(R.drawable.oil)
                else -> btn.setImageResource(R.drawable.custom_add_button)
            }
            btn.setOnClickListener {
                // Assuming displayIndividualCustom() function takes ImageButton as input
                displayIndividualCustom(liquid)
            }
            view.addView(btn)
        }


        backbtn.setOnClickListener{
            settings()
        }


    }
    private fun customliquids() {

        val nameInputDialog = AlertDialog.Builder(this)
        val nameInputEditText = EditText(this)
        nameInputDialog.setTitle("Insert Liquid Name")
        nameInputDialog.setView(nameInputEditText)
        nameInputDialog.setPositiveButton("Confirm") { dialog, _ ->
            val nameInput = nameInputEditText.text.toString()
             if(nameInput != null){


                 val densityInputDialog = AlertDialog.Builder(this)
                 val densityInputEditText = EditText(this)
                 densityInputDialog.setTitle("Insert Density in kg/m^3")
                 densityInputDialog.setView(densityInputEditText)
                 densityInputDialog.setPositiveButton("Confirm") { dialog, _ ->
                     val densityInputString = densityInputEditText.text.toString()
                     try {
                         val densityInput = densityInputString.toFloat()
                         if (densityInput != null) {
                             // Density input is valid, proceed to surface tension input
                             dialog.dismiss()

                             // Show a dialog to input surface tension
                             val surfaceTensionInputDialog = AlertDialog.Builder(this)
                             val surfaceTensionInputEditText = EditText(this)
                             surfaceTensionInputDialog.setTitle("Insert Surface Tension in N/m")
                             surfaceTensionInputDialog.setView(surfaceTensionInputEditText)
                             surfaceTensionInputDialog.setPositiveButton("Confirm") { _, _ ->
                                 val surfaceTensionInput = surfaceTensionInputEditText.text.toString()
                                 try {
                                     val surfaceTensionFloat = surfaceTensionInput.toFloat()
                                     surfaceTensionBlood = surfaceTensionFloat


                                     // Valid surface tension input
                                     val liquidManager = LiquidManager()
                                     liquidManager.saveLiquid(nameInput, densityInput, surfaceTensionBlood, getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                                     displayCustomLiquids()
                                 } catch (e: NumberFormatException) {
                                     // Invalid surface tension input
                                     Toast.makeText(this, "Invalid surface tension input", Toast.LENGTH_SHORT).show()
                                 }
                             }
                             surfaceTensionInputDialog.show()
                         }
                     } catch (e: NumberFormatException) {
                         // Invalid density input
                         Toast.makeText(this, "Invalid density input", Toast.LENGTH_SHORT).show()
                     }
                 }
                 densityInputDialog.show()

             }
            else{
                 Toast.makeText(this, "Invalid name input", Toast.LENGTH_SHORT).show()

            }

        }
        nameInputDialog.show()

    }
    private fun LiveCamera(){
        val intent = Intent(this, LiveCamera::class.java)
        startActivityForResult(intent, 5)
    }

    fun deletePreviousPhotos(){
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        var filesInsidePath = storageDir?.listFiles()

        filesInsidePath?.forEach {
            it.delete()
        }
    }

    private fun isTutorialShown(): Boolean {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(PREF_TUTORIAL_SHOWN, false)
    }

    private fun markTutorialAsShown() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean(PREF_TUTORIAL_SHOWN, true)
        editor.apply()
    }

    private fun showTutorial() {
        val intent = Intent(this, TutorialActivity::class.java)
        startActivity(intent)
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


        val settingsbutton = findViewById<ImageButton>(R.id.buttonbog)
        val turtorialbutton = findViewById<ImageButton>(R.id.button10)



        //deletePreviousPhotos()

        button.setOnClickListener{
            LiveCamera()
        }
        settingsbutton.setOnClickListener{
            settings()
        }

        turtorialbutton.setOnClickListener{
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
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

    fun findObjectInfo(initialImage: Bitmap, xPos: Int, yPoS: Int){


        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)

        //  Toast.makeText(applicationContext,mat.toString(),Toast.LENGTH_LONG).show()

        val resMat = Mat()

        findobjectinfo(mat.nativeObjAddr, xPos, yPoS)

    }

    fun getImageBitmap() : Bitmap{



        var resMat = Mat()
        getimage(resMat.nativeObjAddr)

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



            // Display the coordinates relative to the original image
            //mTextViewX.text = "X: $imageX"
            //mTextViewY.text = "Y: $imageY"



            val resultBitmap = selectObjectImage(imageUri, imageX, imageY)


            image.setImageBitmap(resultBitmap)

            buttonToUndo.setOnClickListener(){
                var resultBitmap = undoBridge(imageUri)
                image.setImageBitmap(resultBitmap)
            }

            buttontoconfirm.setOnClickListener(){

                var pixels = findAreaTwo()

                var bloodpoolarea = areaperpixel*pixels



                //val volume = ((2 * surfaceTensionBlood * 10000) / (densityBlood * gravity * 3.14159 * bloodpoolarea * 0.0001))* unitcalc
                bloodpoolarea = bloodpoolarea * 0.0001f
                var innerArg = surfaceTensionBlood / (densityBlood * gravity)
                var depth = 2 * sqrt(innerArg)
                var volume = depth * bloodpoolarea* 10000f

                val formattedVolume = String.format("%.2f",volume)



                setContentView(R.layout.area_of_blood)
                val Textviewarea = findViewById<TextView>(R.id.textViewb)

                Textviewarea.text = "The volume of the blood is $formattedVolume $unittobedisplayed"


                //    Toast.makeText(applicationContext, "bloodpool area is: " + bloodpoolarea ,Toast.LENGTH_LONG).show()

                //functionality for button to go back to start when an area has been found
                val go_back_2 = findViewById<ImageButton>(R.id.go_back_2)
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

        if(requestCode == 5 && resultCode == Activity.RESULT_OK){

            //sätter upp att hitta referens
            setContentView(R.layout.captured_image_view)
            val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)
            val image = findViewById<ImageView>(R.id.captured_image)

            //BloodMat = BloodMatOriginal

            Core.transpose(BloodMat, BloodMat);
            Core.flip(BloodMat, BloodMat, 1);

            val bitmap  = Bitmap.createBitmap(BloodMat.cols(), BloodMat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(BloodMat, bitmap)


            image.setImageBitmap(bitmap)
            //image.rotation = 90f


            //button to take new picutre
            val newpicture = findViewById<ImageButton>(R.id.New_Picture)

            mRelativeLayout.layoutParams.height = image.height
            mRelativeLayout.layoutParams.width = image.width
            mRelativeLayout.requestLayout()
            newpicture.setOnClickListener(){
                displayFrontpage()
            }

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



                // Display the coordinates relative to the original image
                //mTextViewX.text = "X: ${imageX}"
                //mTextViewY.text = "Y: ${imageY}"


                findObjectInfo(bitmap, imageX, imageY)
                var resultBitmap = getImageBitmap()

                image.setImageBitmap(resultBitmap)


                buttontoconfirm.setOnClickListener(){

                    //var pixels = findObjectArea(imageUri, imageX, imageY)
                    var pixels = getarea()
                    val areaperpixel = 46.75f/pixels
                    println(BloodPixelArea)

                    var bloodpoolarea = areaperpixel * BloodPixelArea

                    bloodpoolarea = bloodpoolarea * 0.0001f
                    var innerArg = surfaceTensionBlood / (densityBlood * gravity)
                    var depth = 2 * sqrt(innerArg)
                    var volume = depth * bloodpoolarea * 10000f

                    val formattedVolume = String.format("%.2f", volume)

                    setContentView(R.layout.area_of_blood)
                    val Textviewarea = findViewById<TextView>(R.id.textViewb)

                    val liquidtext = findViewById<TextView>(R.id.liquidtext)

                    liquidtext.text = "$liquidname"

                    Textviewarea.text = "Volume \n $formattedVolume $unittobedisplayed"

                    val go_back_2 = findViewById<ImageButton>(R.id.go_back_2)

                    go_back_2.setOnClickListener(){
                        displayFrontpage()
                    }

                }
                true
            }




        }

        if((requestCode== 0 || requestCode==GALLERY_REQUEST_CODE) && resultCode == Activity.RESULT_OK && data != null ){
            // Toast.makeText(applicationContext,"took photo!",Toast.LENGTH_LONG).show()



            setContentView(R.layout.captured_image_view)

            Toast.makeText(applicationContext,"found res!",Toast.LENGTH_LONG).show()

            val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)


            val image = findViewById<ImageView>(R.id.captured_image)
            val selectedImageUri: Uri? = data.data


            if (selectedImageUri != null) {
                imageUri = selectedImageUri
            }

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



                // Display the coordinates relative to the original image
                //mTextViewX.text = "X: ${imageX}"
                //mTextViewY.text = "Y: ${imageY}"

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
