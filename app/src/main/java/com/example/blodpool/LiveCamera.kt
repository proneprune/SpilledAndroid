package com.example.blodpool

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.Collections


class LiveCamera : CameraActivity() {
    //initalizes the camera and external c++ functions
    private lateinit var viewBase : CameraBridgeViewBase
    external fun findobjectinfo(mat_addy: Long, x_addy: Int, y_addy: Int)

    external fun centerobjectinfo(mat_addy: Long)

    external fun getimage(mat_addy: Long)

    external fun getarea() : Float

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("testcpp")
        OpenCVLoader.initDebug()
        var currentMat = Mat()

        //initalizes the camera view
        setContentView(R.layout.live_camera_page)
        viewBase = findViewById(R.id.javaCameraView)

        //starts the camera listener (it is the live camera)
        viewBase.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
                    //does some shit that we dont need
                    override fun onCameraViewStarted(width: Int, height: Int) {
                        // Your implementation for camera view started
                    }
                    //does some shit that we dont need
                    override fun onCameraViewStopped() {
                        // Your implementation for camera view stopped
                    }
                    //every frame we do the object detection on the
                    //frame that the live camera finds.
                    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                        // Your implementation for camera frame processing

                        var tmp = inputFrame.rgba()
                        findobjectinfo(tmp.nativeObjAddr, tmp.cols()/2, tmp.rows()/2)

                        getimage(tmp.nativeObjAddr)
                        tmp.copyTo(currentMat)

                        return tmp
                    }
                })
                viewBase.enableView()


        //go back button
        val goToLandingPageButton = findViewById<ImageButton>(R.id.Live_Back_Button)

        goToLandingPageButton.setOnClickListener{
            finish()
        }

        //backbutton is badly named, it is a big invisible button
        //behind the camera "captures" an image when pressed
        //it also marks the activity as complete
        val backbutton = findViewById<Button>(R.id.Captured_Button)

        backbutton.setOnClickListener{
            selectedImageLiquidArea = getarea()
            SelectedImage = currentMat
            val resIntent = Intent()
            setResult(Activity.RESULT_OK, resIntent)
            finish()
        }
    }


    //unclear wtf this does but necessary
    override fun getCameraViewList() : List<CameraBridgeViewBase> {
        return Collections.singletonList(viewBase)

    }
}