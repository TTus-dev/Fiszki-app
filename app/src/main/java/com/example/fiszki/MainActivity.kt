package com.example.fiszki

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {

    var guessing_mode = 0
    val SHARED_PREFS = "sharedPrefs"

    fun start_guessing(){
        var i = Intent(this, guessing::class.java)
        i.putExtra("gm_val", guessing_mode)
        startActivityForResult(i, 1)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        var saved_correct = sharedPreferences.getInt("correct", 0)
        var saved_all = sharedPreferences.getInt("all", 0)
        var last_correct = data?.getIntExtra("correctc",0)
        var last_all = data?.getIntExtra("allc",0)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            if (resultCode == RESULT_OK) {
                if (saved_all!! != 0 && last_all!! != 0) {
                    var score_diff = last_correct!! * 100 / last_all -
                            saved_correct * 100 / saved_all
                    var CustomADBuilder = AlertDialog.Builder(this)
                    var DgView = getLayoutInflater().inflate(R.layout.dialog_layout, null)
                    CustomADBuilder.setView(DgView)
                    var cDialog = CustomADBuilder.create()
                    DgView.findViewById<TextView>(R.id.saved_score)
                            .text = "$saved_correct/$saved_all"
                    DgView.findViewById<TextView>(R.id.recent_score)
                            .text = "$last_correct/$last_all"
                    var scoreperdiff = DgView.findViewById<TextView>(R.id.scorediff)
                    var prev_c = scoreperdiff.textColors
                    if (last_all == saved_all) {
                        if (score_diff > 0) {
                            scoreperdiff.text = "+$score_diff%"
                            scoreperdiff.setTextColor(0xFF00FF00.toInt())
                        } else if (score_diff < 0) {
                            scoreperdiff.text = "$score_diff%"
                            scoreperdiff.setTextColor(0xFFFF0000.toInt())
                        } else {
                            scoreperdiff.text = "~$score_diff%"
                        }
                    }
                    else{
                        scoreperdiff.visibility = View.GONE
                    }
                    DgView.findViewById<Button>(R.id.dBtn).setOnClickListener {
                        cDialog.dismiss()
                        scoreperdiff.setTextColor(prev_c)
                    }
                    cDialog.show()
                }
                if(last_all != 0) {
                    editor.putInt("correct", last_correct!!)
                    editor.putInt("all", last_all!!)
                    editor.apply()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startGuessingbtnen).setOnClickListener {
            guessing_mode = 0
            start_guessing()
        }

        findViewById<Button>(R.id.startGuessingbtnpl).setOnClickListener {
            guessing_mode = 1
            start_guessing()
        }

        findViewById<Button>(R.id.startGuessingbtnmix).setOnClickListener {
            guessing_mode = 2
            start_guessing()
        }
    }
}