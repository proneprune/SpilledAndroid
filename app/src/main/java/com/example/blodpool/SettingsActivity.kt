package com.example.blodpool

import android.annotation.SuppressLint
//import androidx.databinding.DataBindingUtil
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial) // Set the layout file here

        displaySettingsPage()
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
            displaySettingsPage()
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

    fun displaySettingsPage(){
        setContentView(R.layout.settings)
        val aboutUsButton = findViewById<ImageButton>(R.id.aboutus)
        val displayLiquidsButton = findViewById<ImageButton>(R.id.liquid)
        val goBackToLandingPageButton = findViewById<ImageButton>(R.id.backbtn123)
        val goToChooseUnitPageButton = findViewById<ImageButton>(R.id.language)

        goToChooseUnitPageButton.setOnClickListener{
            chooseUnit()
        }

        displayLiquidsButton.setOnClickListener{
            displayCustomLiquids()
        }

        aboutUsButton.setOnClickListener{
            displayAboutUsPage()
        }

        goBackToLandingPageButton.setOnClickListener{
            finish()
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
                                    val surfaceTension = surfaceTensionFloat


                                    // Valid surface tension input
                                    val liquidManager = LiquidManager()
                                    liquidManager.saveLiquid(nameInput, densityInput, surfaceTension, getExternalFilesDir(Environment.DIRECTORY_PICTURES))
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

    fun displayCustomLiquids() {
        setContentView(R.layout.display_liquids)

        val liquidManager = LiquidManager()

        val liquids = liquidManager.loadLiquids(getExternalFilesDir(Environment.DIRECTORY_PICTURES))

        val goBackButton = findViewById<ImageButton>(R.id.buttonback123)

        val text = TextView(this)
        text.text = "Liquids"


        val view = findViewById<LinearLayout>(R.id.vertView)



        //iterates through all the liquids and adds a button for choosing each liquid.
        for (liquid in liquids) {

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.HORIZONTAL


            val btn = ImageButton(this)
            btn.setBackgroundResource(R.drawable.transparent_image)
            when (liquid.name) {
                "Blood" -> btn.setImageResource(R.drawable.blood)
                "Water" -> btn.setImageResource(R.drawable.water)
                "Oil" -> btn.setImageResource(R.drawable.oil)
                else -> btn.setImageResource(R.drawable.custom_add_button)
            }

            btn.setOnClickListener {
                displayIndividualCustom(liquid)
            }



            val btnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            btnParams.gravity = Gravity.START
            btn.x = 40f
            btn.layoutParams = btnParams

            val textView = TextView(this)
            textView.text = liquid.name
            textView.gravity = Gravity.END
            textView.textSize = 18f // Set text size (in sp)
            textView.setTypeface(null, Typeface.BOLD) // Set text style to bold
            textView.setBackgroundResource(R.drawable.transparent_image) // Set text background to transparent image

            val textParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textParams.setMargins(0,0,0,0);
            textView.x = 40f
            textView.y = 80f //Fuck this shit, moves the text below the image in the horizontal scroller
            textParams.gravity = Gravity.END // Center text horizontally
            textView.layoutParams = textParams


            layout.addView(btn)
            layout.addView(textView)

            view.addView(layout)
        }

        goBackButton.setOnClickListener{
            displaySettingsPage()
        }

        //add custom liquid button

        val addCustomLiquidButton = ImageButton(this)
        addCustomLiquidButton.setImageResource(R.drawable.custom)
        view.addView(addCustomLiquidButton)

        addCustomLiquidButton.setOnClickListener{
            customliquids()
        }
    }

    fun displayIndividualCustom(liquid: LiquidManager.Liquid){
        setContentView(R.layout.display_liquid_info)

        val confirmliquidbutton = findViewById<Button>(R.id.conliq)
        val deletliquidbutton = findViewById<Button>(R.id.delustomC1)
        val backbutton = findViewById<ImageButton>(R.id.goback)

        val nametext = findViewById<TextView>(R.id.textViewname)
        val dentext = findViewById<TextView>(R.id.textViewdensity)
        val surftext = findViewById<TextView>(R.id.textViewsurfacetension)

        nametext.text = liquid.name
        dentext.text = liquid.density.toString()
        surftext.text = liquid.surfaceTension.toString()

        confirmliquidbutton.setOnClickListener{
            currentLiquid = liquid
            finish() // go back to landing page
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

    fun displayAboutUsPage(){

        setContentView(R.layout.aboutus)
        val go_back = findViewById<ImageButton>(R.id.imageButton)
        go_back.setOnClickListener{
            displaySettingsPage()
        }

        val website = findViewById<ImageButton>(R.id.imageButton3)
        website.setOnClickListener{
            val url = "https://spilledowner.wixsite.com/spilled"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

}