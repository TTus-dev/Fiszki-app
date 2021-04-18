package com.example.fiszki

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.common.util.Hex
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class guessing : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    var g_mode = 0
    var vis_first_time = true
    private val max_words = 1355
    private var drawed_lang = 0
    private var correctcount = 0
    private var allcount = 0
    private var correctBtnId = 0
    private var correctAnsId = 0
    private var retCorrStr = ""
    private var prevAnsType = 0
    private var prevAnsCount = 0
    private var prevQuestionType = 0
    private var prevQuestCount = 0

    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        g_mode = intent.getIntExtra("gm_val", 0)
        setContentView(R.layout.activity_guessing)
        findViewById<TextView>(R.id.score).text = "0/0"
        findViewById<TextView>(R.id.scorepercentage).text = "0%"
        try {
            drawingQuestions()
        }
        catch (e: Exception){
            Log.e("mylog", "Błąd: " + e.message, e)
        }
        val editxt = findViewById<TextInputEditText>(R.id.userInpt)
        editxt.setOnClickListener {
            editxt.setOnKeyListener(View.OnKeyListener{_, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){
                    main_fun(4)
                }
                false
            })
        }
        findViewById<Button>(R.id.btnAnswer1).setOnClickListener {
            main_fun(0)
        }
        findViewById<Button>(R.id.btnAnswer2).setOnClickListener {
            main_fun(1)
        }
        findViewById<Button>(R.id.btnAnswer3).setOnClickListener {
            main_fun(2)
        }
        findViewById<Button>(R.id.btnAnswer4).setOnClickListener {
            main_fun(3)
        }
    }

    override fun onBackPressed(){
        var ADbuilder = AlertDialog.Builder(this)
        ADbuilder.setTitle("Czy chcesz zakończyć naukę ?")
        ADbuilder.setNegativeButton("Tak") { _: DialogInterface, _: Int ->
            var result_intent = intent
            result_intent.putExtra("correctc", correctcount)
            result_intent.putExtra("allc", allcount)
            setResult(RESULT_OK, result_intent)
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        ADbuilder.setPositiveButton("Nie", { _: DialogInterface, _: Int -> })
        ADbuilder.show()
    }

    override fun onPause() {
        Log.d("OnPause","Pauza")
        super.onPause()
    }

    fun main_fun(x :Int) {
        if (x < 4) {
            checkAnswerbtn(x, correctBtnId)
            toggleBtns(false)
            Handler(Looper.getMainLooper()).postDelayed({
                drawingQuestions()
            }, 1500)
            Handler(Looper.getMainLooper()).postDelayed({
                toggleBtns(true)
            }, 1700)
        }
        else if(x == 4){
            hideKeyboard()
            checkAnswerInpt()
            Handler(Looper.getMainLooper()).postDelayed({
                drawingQuestions()
            }, 1500)
        }
        findViewById<TextView>(R.id.score).text = "${correctcount}/${allcount}"
        if (allcount > 0) {
            findViewById<TextView>(R.id.scorepercentage).text = "${correctcount * 100 / allcount}%"
        }
    }

    fun toggleBtns(enbl :Boolean){
        var state = enbl
        findViewById<Button>(R.id.btnAnswer1).isEnabled = state
        findViewById<Button>(R.id.btnAnswer2).isEnabled = state
        findViewById<Button>(R.id.btnAnswer3).isEnabled = state
        findViewById<Button>(R.id.btnAnswer4).isEnabled = state
    }

    fun changeBtnColor(id:Int, color: Long){
        var intcolor = color.toInt()
        when (id){
            0 -> findViewById<Button>(R.id.btnAnswer1).setBackgroundColor(intcolor)
            1 -> findViewById<Button>(R.id.btnAnswer2).setBackgroundColor(intcolor)
            2 -> findViewById<Button>(R.id.btnAnswer3).setBackgroundColor(intcolor)
            3 -> findViewById<Button>(R.id.btnAnswer4).setBackgroundColor(intcolor)
        }
    }

    fun checkAnswerInpt(){
        val userInpView = findViewById<TextInputEditText>(R.id.userInpt)
        var prev_c = userInpView.textColors
        if (userInpView.text.toString() == retCorrStr){
            userInpView.setTextColor(0xFF00FF00.toInt())
            correctcount += 1
        }
        else{
            findViewById<TextView>(R.id.correctquestionText)
                .setText("Poprawna odpowiedź: ${retCorrStr}")
            userInpView.setTextColor(0xFFFF0000.toInt())
        }
        userInpView.isEnabled = false
        allcount += 1
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            userInpView.setTextColor(prev_c)
            userInpView.isEnabled = true
        }, 1500)
    }

    fun checkAnswerbtn(selected :Int, correct :Int){
        changeBtnColor(correct, 0xFF00FF00)
        if(selected != correct){
            changeBtnColor(selected, 0xFFFF0000)
        }
        else{
            correctcount += 1
        }
        allcount += 1
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            changeBtnColor(selected, 0xFF6200EE)
            changeBtnColor(correct, 0xFF6200EE)
        }, 1650)
    }

    fun hideKeyboard(){
        val view = this.currentFocus
        if (view != null){
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        else{
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
    }

    fun drawingQuestions() {
        var a_mode = (0..1).random()
        if (a_mode == prevAnsType){
            if (prevAnsCount == 3) {
                prevAnsCount = 1
                if (prevAnsType == 0){
                    a_mode = 1
                }
                else{
                    a_mode = 0
                }
            }
            else {
                prevAnsCount += 1
            }
        }
        else{
            prevAnsCount = 1
        }
        if (g_mode == 2) {
            drawed_lang = (0..1).random()
            if (drawed_lang == prevQuestionType){
                if (prevQuestCount == 3) {
                    prevQuestCount = 1
                    if (prevQuestionType == 0){
                        drawed_lang = 1
                    }
                    else{
                        drawed_lang = 0
                    }
                }
                else {
                    prevQuestCount += 1
                }
            }
            else{
                prevQuestCount = 1
            }
        }
        var temp_int: Int
        var correctAnswer = (0..max_words).random()
        correctAnsId = correctAnswer
        firestore_set_str(correctAnswer, 5)
        firestore_set_str(correctAnswer, 4)
        if (a_mode == 0) {
            val drawedAnswers = arrayOfNulls<Int>(4)
            correctBtnId = (0..3).random()
            firestore_set_str(correctAnswer, correctBtnId)
            for (i in 0..3) {
                do {
                    temp_int = (0..max_words).random()
                } while (temp_int in drawedAnswers || temp_int == correctAnswer)
                if (i != correctBtnId) {
                    drawedAnswers[i] = temp_int
                    firestore_set_str(temp_int, i)
                }
            }
            findViewById<ConstraintLayout>(R.id.inner_layout_btn).visibility = View.VISIBLE
            findViewById<ConstraintLayout>(R.id.inner_layout_inpt).visibility = View.GONE
        }
        else {
            findViewById<ConstraintLayout>(R.id.inner_layout_btn).visibility = View.GONE
            findViewById<ConstraintLayout>(R.id.inner_layout_inpt).visibility = View.VISIBLE
            findViewById<TextInputEditText>(R.id.userInpt).setText("")
            findViewById<TextView>(R.id.correctquestionText).setText("")
        }
        prevAnsType = a_mode
        if (g_mode == 2){
            prevQuestionType = drawed_lang
        }
    }

    fun firestore_set_str(cAns: Int, view_id: Int){
        db.collection("slowka").document(cAns.toString())
                .get()
                .addOnSuccessListener {
                    if (vis_first_time) {
                        findViewById<ConstraintLayout>(R.id.inner_layout).visibility = View.VISIBLE
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    }
                    var new_text_pl = it.data?.getValue("pl").toString().trim()
                    var new_text_en = it.data?.getValue("en").toString().trim()
                    if (view_id == 0) {
                        if(g_mode == 0 || (g_mode == 2 && drawed_lang == 0)) {
                            findViewById<Button>(R.id.btnAnswer1).setText(new_text_pl)
                        }
                        else if(g_mode == 1 || (g_mode == 2 && drawed_lang == 1)){
                            findViewById<Button>(R.id.btnAnswer1).setText(new_text_en)
                        }
                    }
                    else if (view_id == 1) {
                        if(g_mode == 0  || (g_mode == 2 && drawed_lang == 0)) {
                            findViewById<Button>(R.id.btnAnswer2).setText(new_text_pl)
                        }
                        else if(g_mode == 1 || (g_mode == 2 && drawed_lang == 1)){
                            findViewById<Button>(R.id.btnAnswer2).setText(new_text_en)
                        }
                    }
                    else if (view_id == 2) {
                        if(g_mode == 0  || (g_mode == 2 && drawed_lang == 0)) {
                            findViewById<Button>(R.id.btnAnswer3).setText(new_text_pl)
                        }
                        else if(g_mode == 1 || (g_mode == 2 && drawed_lang == 1)){
                            findViewById<Button>(R.id.btnAnswer3).setText(new_text_en)
                        }
                    }
                    else if (view_id == 3) {
                        if(g_mode == 0 || (g_mode == 2 && drawed_lang == 0)) {
                            findViewById<Button>(R.id.btnAnswer4).setText(new_text_pl)
                        }
                        if(g_mode == 1 || (g_mode == 2 && drawed_lang == 1)) {
                            findViewById<Button>(R.id.btnAnswer4).setText(new_text_en)
                        }
                    }
                    else if (view_id == 4){
                        if(g_mode == 0 || (g_mode == 2 && drawed_lang == 0)) {
                            findViewById<TextView>(R.id.questionText).text = new_text_en
                        }
                        else if (g_mode == 1 || (g_mode == 2 && drawed_lang == 1)) {
                            findViewById<TextView>(R.id.questionText).text = new_text_pl
                        }
                    }
                    else{
                        if(g_mode == 0 || (g_mode == 2 && drawed_lang == 0)) {
                            retCorrStr = new_text_pl
                        }
                        else if (g_mode == 1 || (g_mode == 2 && drawed_lang == 1)) {
                            retCorrStr = new_text_en
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception.message ==
                            "Failed to get document because the client is offline."){
                        var t1 = Toast.makeText(this, "Błąd połączenia",
                                Toast.LENGTH_LONG)
                        t1.show()
                        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                            super.onBackPressed()
                            t1.cancel()
                        }, 1000)
                    }
                    else {
                        Toast.makeText(this, "Wystąpił błąd: " + exception.message,
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}