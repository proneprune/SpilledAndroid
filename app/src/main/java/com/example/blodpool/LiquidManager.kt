package com.example.blodpool

import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.text.style.ResolvedTextDirection
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LiquidManager {

    //lateinit var liquids : List<Liquid>

    @Serializable
    data class Liquid(val name: String, val density: Float, val surfaceTension: Float)
    fun saveLiquid(name: String, density: Float, surfaceTension: Float, storageDir: File){

        val liquidsFileName = "liquids.json"

        val f = File(storageDir, liquidsFileName)

        val newLiquid = Liquid(name, density, surfaceTension)

        val liquids = loadLiquids(storageDir).toMutableList()

        liquids.add(newLiquid)

        val updatedLiquidString = Json.encodeToString<List<Liquid>>(liquids)
        f.writeText(updatedLiquidString)
    }

    fun baseLiquids() : List<Liquid>{
        val blood = Liquid("Blood", 1060f, 0.058f)
        val water = Liquid("Water", 1000f, 0.072f)

        val liquids = listOf(blood, water)
        return liquids
    }

    fun loadLiquids(storageDir: File) : List<Liquid>{
        val liquidsFileName = "liquids.json"

        val f = File(storageDir, liquidsFileName)

        if(f.exists()){
            val json = f.readText()
            val liquids = Json.decodeFromString<List<Liquid>>(json)
            return baseLiquids()
        }
        else{
            f.createNewFile()
            val baseLiquids = baseLiquids()
            val baseLiquidsString = Json.encodeToString<List<Liquid>>(baseLiquids)
            f.writeText(baseLiquidsString)
            return baseLiquids
        }
    }


}