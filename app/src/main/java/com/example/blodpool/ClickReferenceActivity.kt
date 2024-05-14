package com.example.blodpool

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import kotlin.math.sqrt

class ClickReferenceActivity : AppCompatActivity() {
    //external c++ functions that finds area etc
    external fun findobjectinfo(mat_addy: Long, x_addy: Int, y_addy: Int)

    external fun centerobjectinfo(mat_addy: Long)

    external fun getimage(mat_addy: Long)

    external fun getarea() : Float
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clickReferencePage()
    }

    fun clickReferencePage(){

        //init views and buttons and layout
        setContentView(R.layout.click_reference_page)
        val mRelativeLayout = findViewById<RelativeLayout>(R.id.relative_layout_1)
        val image = findViewById<ImageView>(R.id.captured_image)

        //some black magic flipping that must be done
        Core.transpose(SelectedImage, SelectedImage);
        Core.flip(SelectedImage, SelectedImage, 1);

        //some conversion from mat to bitmap
        val bitmap  = Bitmap.createBitmap(SelectedImage.cols(), SelectedImage.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(SelectedImage, bitmap)

        //sets the captured image to the image shown
        //on screen
        image.setImageBitmap(bitmap)

        //button to take new picture (go back to main page)
        val newpicture = findViewById<ImageButton>(R.id.New_Picture)

        //set the relative layout to be the same size as the image
        mRelativeLayout.layoutParams.height = image.height
        mRelativeLayout.layoutParams.width = image.width
        mRelativeLayout.requestLayout()

        // cancel and go back to the main page if clicked
        newpicture.setOnClickListener(){
            finish()
        }

        val buttontoconfirm = findViewById<Button>(R.id.button2)

        //this is some crazy shit that finds the x,y
        //coordinates of your button press on the screen
        mRelativeLayout.setOnTouchListener { _, motionEvent ->

            // get the actual pixel size of the image
            val imageWidth = image.drawable.intrinsicWidth
            val imageHeight = image.drawable.intrinsicHeight

            // get screen x and y values of the click
            val mX = motionEvent.x
            val mY = motionEvent.y

            // Transform the screen x and y values from the click to the equivalent pixel postion for the image
            val imageX = (mX * (imageWidth.toFloat() / image.width.toFloat())).toInt()
            val imageY = (mY * (imageHeight.toFloat() / image.height.toFloat())).toInt()


            //using the found x,y coordinates the c++
            //function to outline an object is called
            //the object outlined is the one were
            //the coordinates reside in
            findObjectInfo(bitmap, imageX, imageY)
            var resultBitmap = getImageBitmap()

            //displays the new image with the chosen object outlined
            image.setImageBitmap(resultBitmap)

            //button to confirm, calculations of volume
            //and pixles are done here
            buttontoconfirm.setOnClickListener(){

                //finding the pixels of the reference object
                //it is hard coded in to be an id-1 card
                //which has an area of roughly 46,75 cm^2
                var pixels = getarea()
                val areaperpixel = 46.75f/pixels
                //println(selectedImageLiquidArea)

                //this takes the amount of pixels in the liquid
                //times the actual size of every pixel to find
                //the area of the bloodpool
                var bloodpoolarea = areaperpixel * selectedImageLiquidArea




                //this is the calculation of volume following the formula
                //of finding volume of a liquid pool on a flat surface with no/low absorption
                bloodpoolarea = bloodpoolarea * 0.0001f //bloodpoolarea is initally in cm^2, this line transforms it to m^2
                var innerArg = (currentLiquid.surfaceTension / (currentLiquid.density * gravity)) * (1 - Math.cos(75 * Math.PI / 180))
                var depth = sqrt(2 * innerArg)


                var volume = (depth * bloodpoolarea * 10000f * unitcalc) //the formula gives the volume in m^3, therefore it is multiplied with 10000 to transform it to dl. Then multiplied with unitcalc which is 1 or 3.38 for dl or ounces respectively.

                //converts the volume to string that can be displayed
                val formattedVolume = String.format("%.2f", volume)

                setContentView(R.layout.display_liquid_volume)
                val textViewb = findViewById<TextView>(R.id.textViewb)

                // Set initial visibility to invisible
                textViewb.visibility = View.INVISIBLE

                //Animation for the volume to slide up the screen
                // Load animation
                val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)

                // Set animation listener to make textViewb visible after animation ends
                slideUpAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        textViewb.visibility = View.VISIBLE
                    }
                })
                // Start animation on textViewb
                textViewb.startAnimation(slideUpAnimation)

                val liquidtext = findViewById<TextView>(R.id.liquidtext)

                liquidtext.text = currentLiquid.name

                textViewb.text = "Volume \n $formattedVolume $unittobedisplayed"

                val go_back_2 = findViewById<ImageButton>(R.id.go_back_2)

                //goes back to the main page
                go_back_2.setOnClickListener(){
                    finish()
                }
            }
            true
        }
    }
    //these kotlin functions call the c++ functions
    fun findObjectInfo(initialImage: Bitmap, xPos: Int, yPoS: Int){

        //transforms the bitmap to a mat which can be used by opencv in c++
        val mat = Mat()
        Utils.bitmapToMat(initialImage, mat)


        //calls c++ function to select the object in the image found at (xPos, yPos)
        findobjectinfo(mat.nativeObjAddr, xPos, yPoS)
    }

    fun getImageBitmap() : Bitmap{

        //gets the image with the object, found by findobjectinfo, outlined
        var resMat = Mat()
        getimage(resMat.nativeObjAddr)

        //transform the mat to a bitmap in order to be displayed as an image
        val resultBitmap = Bitmap.createBitmap(resMat.cols(), resMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resMat, resultBitmap)

        return resultBitmap
    }
}