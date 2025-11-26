package com.harun.hek

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.harun.hek.databinding.FragmentKullaniciBinding


class KullaniciFragment : Fragment() {
    private var _binding: FragmentKullaniciBinding? = null

    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKullaniciBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.girisButton.setOnClickListener{girisYap(it)}
        // Kayıt Ol butonu eklendiğinde kullanlıbilir
        // binding.kayitButton.setOnClickListener{kayitOl(it)}


        val guncelKullanici = auth.currentUser
        if(guncelKullanici != null) {
            // Kullanıcı daha önceden giriş yapmış.
            val action = KullaniciFragmentDirections.actionKullaniciFragmentToAnamenu()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun kayitOl(view: View){
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextNumberPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    // Kullanici oluşturuldu, giriş yap.
                    val action = KullaniciFragmentDirections.actionKullaniciFragmentToAnamenu()
                    Navigation.findNavController(view).navigate(action)
                }
            }.addOnFailureListener{exception ->
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun girisYap(view: View){
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextNumberPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                val action = KullaniciFragmentDirections.actionKullaniciFragmentToAnamenu()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener{exeption ->
                Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}