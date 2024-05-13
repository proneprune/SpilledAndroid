package com.example.blodpool

import android.annotation.SuppressLint
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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout.LayoutParams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SettingsActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial) // Set the layout file here


        displaySettingsPage()
    }



    //all the buttons on the settings page
    fun displaySettingsPage(){
        setContentView(R.layout.settings)
        val aboutUsButton = findViewById<ImageButton>(R.id.aboutus)
        val displayLiquidsButton = findViewById<ImageButton>(R.id.liquid)
        val goBackToLandingPageButton = findViewById<ImageButton>(R.id.backbtn123)
        val goToChooseUnitPageButton = findViewById<ImageButton>(R.id.language)

        goToChooseUnitPageButton.setOnClickListener {
            // Create and show the bottom sheet menu
            val bottomSheetFragment = MyBottomSheetDialogFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
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
        //when creating custom liquids input a name
        //it cannot be null
        val nameInputDialog = AlertDialog.Builder(this)
        val nameInputEditText = EditText(this)
        nameInputDialog.setTitle("Insert Liquid Name")
        nameInputDialog.setView(nameInputEditText)
        nameInputDialog.setPositiveButton("Confirm") { dialog, _ ->
            val nameInput = nameInputEditText.text.toString()
            if(nameInput != null){

                //entering density when inputting the liquid
                //it can only handle numbers
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

                            //enter surface tensions, this can also
                            //handle only numbers
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

    //displays the custom liquid layout and view
    fun displayCustomLiquids() {
        setContentView(R.layout.display_liquids)

        val liquidManager = LiquidManager()

        val liquids = liquidManager.loadLiquids(getExternalFilesDir(Environment.DIRECTORY_PICTURES))

        val goBackButton = findViewById<ImageButton>(R.id.buttonback123)
        val addNewLiquidButton = findViewById<ImageButton>(R.id.buttonhej)

        addNewLiquidButton.setOnClickListener{
            customliquids()
        }

        val view = findViewById<LinearLayout>(R.id.linearlayout1)

        //iterates through all the liquids and adds a button for choosing each liquid.
        for (liquid in liquids) {
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL

            val btn = ImageButton(this)
            btn.setBackgroundResource(R.drawable.transparent_image)     // Set button background
            when (liquid.name) {                                        //gives blood, water and oil
                "Blood" -> btn.setImageResource(R.drawable.blood_1_)    //special images otherwise
                "Water" -> btn.setImageResource(R.drawable.water120h)   //it is a regular image
                "Oil" -> btn.setImageResource(R.drawable.oil_1_)
                else -> btn.setImageResource(R.drawable.custom_add_button)
            }
            btn.setOnClickListener {
                displayIndividualCustom(liquid)
            }

            val btnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            btnParams.gravity = Gravity.CENTER
            btn.layoutParams = btnParams

            val textView = TextView(this)
            textView.text = liquid.name
            textView.gravity = Gravity.CENTER
            textView.textSize = 18f // Set text size (in sp)
            textView.setTypeface(null, Typeface.BOLD) // Set text style to bold
            textView.setBackgroundResource(R.drawable.transparent_image) // Set text background to transparent image

            val textParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textParams.setMargins(0,0,0,0);
            textView.y = 320f //Fuck this shit, moves the text below the image in the horizontal scroller
            textParams.gravity = Gravity.CENTER_HORIZONTAL // Center text horizontally
            textView.layoutParams = textParams

            layout.addView(textView)
            layout.addView(btn)
            view.addView(layout)
        }
        goBackButton.setOnClickListener{
            displaySettingsPage()
        }
    }
    //function to display the information of the custom
    //added liquid, it allows the liquid to be chosen
    //and to be deleted if necessary
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
        //this makes sure that stupid users cannot delete water or blood
        //from the json
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
    //display the about us page, which gives some general information
    //and also has a button that links to the expo website :)
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

//this is the new class for creating the popup menu when selecting
//units, instead of moving us to an entire new view, allows you to
//either use metric or imperial units to be displayed and
//calculated in
class MyBottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.bottom_sheet_unit_select, container, false)

        // Find your buttons and set click listeners
        val button1 = view.findViewById<Button>(R.id.button1)
        val button2 = view.findViewById<Button>(R.id.button2)

        button1.setOnClickListener {
            unitcalc = 1f
            unittobedisplayed = "dl"
            dismiss()
        }
        button2.setOnClickListener {
            // Handle button2 click
            unitcalc = 3.38140227f
            unittobedisplayed = "fl.oz"
            dismiss()
        }
        return view
    }
}