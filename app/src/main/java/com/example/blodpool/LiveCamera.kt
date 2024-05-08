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

        setContentView(R.layout.live_camera_page)
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
                        findobjectinfo(tmp.nativeObjAddr, tmp.cols()/2, tmp.rows()/2)

                        getimage(tmp.nativeObjAddr)
                        tmp.copyTo(currentMat)

                        return tmp
                    }
                })
                viewBase.enableView()

        val goToLandingPageButton = findViewById<ImageButton>(R.id.Live_Back_Button)

        goToLandingPageButton.setOnClickListener{
            finish()
        }
        val backbutton = findViewById<Button>(R.id.Captured_Button)

        backbutton.setOnClickListener{
            selectedImageLiquidArea = getarea()
            SelectedImage = currentMat
            val resIntent = Intent()
            setResult(Activity.RESULT_OK, resIntent)
            finish()
        }

    }



    override fun getCameraViewList() : List<CameraBridgeViewBase> {
        return Collections.singletonList(viewBase)

    }
}