package com.example.blodpool

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageButton
import com.example.blodpool.MainActivity
import com.example.blodpool.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("testcpp")
        OpenCVLoader.initDebug()

        var currentMat = Mat()

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
                viewBase.enableView()

        val button = findViewById<ImageButton>(R.id.Live_Back_Button)

        button.setOnClickListener{
            finish()
        }
        
    }



    override fun getCameraViewList() : List<CameraBridgeViewBase> {
        return Collections.singletonList(viewBase)

    }
}