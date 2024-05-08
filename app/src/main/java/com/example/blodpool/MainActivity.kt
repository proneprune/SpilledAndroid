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
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import kotlin.math.sqrt

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import org.opencv.core.Core


val gravity = 9.82f
var unitcalc = 1f
var unittobedisplayed = "dl"
lateinit var BloodMat: Mat
var BloodPixelArea : Float = 0.0f

var currentLiquid = LiquidManager.Liquid("Blood", 1060f, 0.058f)


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.loadLibrary("testcpp")

        //changes the background theme to the pinkish one
        setTheme(R.style.Theme_Blodpool)

        OpenCVLoader.initDebug()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)

        } else {

            displayFrontpage()
            if (!isTutorialShown()) {
                showTutorial()
                markTutorialAsShown()
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                // If request is cancelled, the grantResults array will be empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your camera-related tasks
                    displayFrontpage()
                    if (!isTutorialShown()) {
                        showTutorial()
                        markTutorialAsShown()
                    }

                } else {
                    // Permission denied, handle accordingly (e.g., display a message or disable camera functionality)
                    setContentView(R.layout.permission_denied)
                }

            }
        }
    }


    private fun goToLiveCamera(){
        val intent = Intent(this, LiveCamera::class.java)
        startActivityForResult(intent, 5)
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


    fun displayFrontpage(){
        setContentView(R.layout.frontpage)

        val liveCameraButton = findViewById<ImageButton>(R.id.btn)
        val settingsButton = findViewById<ImageButton>(R.id.buttonbog)
        val tutorialButton = findViewById<ImageButton>(R.id.button10)

        liveCameraButton.setOnClickListener{
            goToLiveCamera()
        }
        settingsButton.setOnClickListener{
            goToSettings()
        }
        tutorialButton.setOnClickListener{
            goToTutorial()
        }
    }

    fun goToSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun goToTutorial(){
        val intent = Intent(this, TutorialActivity::class.java)
        startActivity(intent)
    }

    fun goToClickReference(){
        val intent = Intent(this, ClickReferenceActivity::class.java)
        startActivity(intent)
    }

    //@Deprecated
    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 5 && resultCode == Activity.RESULT_OK){

            goToClickReference()
        }
    }
}
