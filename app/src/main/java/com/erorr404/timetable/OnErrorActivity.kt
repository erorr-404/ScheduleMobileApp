package com.erorr404.timetable

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnErrorActivity : AppCompatActivity() {
    private lateinit var args: ArrayList<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_on_error)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        args = intent.getStringArrayListExtra("error_info")?: arrayListOf()
        val errorCause = findViewById<TextView>(R.id.errorCause)
        errorCause.text = args[0]
        val errorMessage = findViewById<TextView>(R.id.errorMessage)
        errorMessage.text = args[1]
        val stackTrace = findViewById<TextView>(R.id.stackTrace)
        stackTrace.text = args[3]
    }
}