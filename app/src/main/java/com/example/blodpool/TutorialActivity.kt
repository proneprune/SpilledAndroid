package com.example.blodpool

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial) // Set the layout file here
        Quittutorial()
    }

    fun Quittutorial(){
        setContentView(R.layout.activity_tutorial)
        val quitbutton = findViewById<Button>(R.id.button8)


        quitbutton.setOnClickListener{
          finish()

        }

    }


}