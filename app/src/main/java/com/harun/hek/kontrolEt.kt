package com.harun.hek

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.harun.hek.databinding.ActivityKontrolEtBinding

class kontrolEt : AppCompatActivity() {
    private lateinit var binding: ActivityKontrolEtBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKontrolEtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Firebase Kodları
        db = Firebase.firestore

        enableEdgeToEdge() 

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        verileriAl()
    }

    private fun verileriAl(){
        db.collection("Mevcut Değerler").addSnapshotListener{ value, error ->
            if (error != null){
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                if(value != null) {
                    if(!value.isEmpty) {
                        // Boş değilse

                        val documents = value.documents
                        for (document in documents) {
                            val BitkiAdi = document.get("Bitki Adı") as String
                            val pHdeger = document.get("pH Değeri") as String
                            val ECdeger = document.get("EC Değeri") as String
                            val isikDeger = document.get("Işık Süresi") as String
                            val nemDeger = document.get("Nem Oranı") as String
                            val sicaklikDeger = document.get("Sıcaklık") as String

                            binding.bitkiAdi.text = BitkiAdi
                            binding.pHdeger.text = pHdeger
                            binding.ECdeger.text = ECdeger
                            binding.sureDeger.text = isikDeger
                            binding.nemDeger.text = nemDeger
                            binding.sicaklikDeger.text = sicaklikDeger

                        }
                    }
                }
            }
        }
    }

}