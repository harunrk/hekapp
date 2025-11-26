package com.harun.hek

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.harun.hek.databinding.ActivityAnamenuBinding
import com.harun.hek.view.yeniSera
import com.harun.hek.kontrolEt

class anamenu : AppCompatActivity() {
    private lateinit var binding: ActivityAnamenuBinding
    private lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        binding = ActivityAnamenuBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun yeniSeraKur(view: View){
        var intent = Intent(this@anamenu, yeniSera::class.java)
        startActivity(intent)
    }

    fun cikisYap(view: View){
        auth.signOut()
        var intent = Intent(this@anamenu, MainActivity::class.java)
        startActivity(intent)
    }

    fun serayiKontrolEt(view: View){
        var intent = Intent(this@anamenu, kontrolEt::class.java)
        startActivity(intent)
    }
}