package com.harun.hek.view

import android.Manifest
import android.app.DirectAction
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.harun.hek.databinding.FragmentBitkiBinding
import com.harun.hek.model.Bitki
import com.harun.hek.roomdb.BitkiDAO
import com.harun.hek.roomdb.BitkiDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.util.UUID

class bitkiFragment : Fragment() {
    private var _binding: FragmentBitkiBinding? = null
    private val binding get() = _binding!!


    // Galeriye gitmek için yazıldılar.
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel : Uri?= null // Yer belirtir. (Url gibi- //data/images...)
    private var secilenBitmap : Bitmap? = null
    private val mDisposable = CompositeDisposable()
    private var secilenBitki : Bitki? = null

    private lateinit var  db : BitkiDatabase
    private lateinit var bitkiDao : BitkiDAO

    // Firebase
    private lateinit var auth : FirebaseAuth
    private lateinit var Fdb : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        //Firebase
        auth = Firebase.auth

        db = Room.databaseBuilder(requireContext(), BitkiDatabase::class.java, "Bitkiler").build()
        bitkiDao = db.bitkiDao()

        Fdb = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBitkiBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // onClick ismi ile çağrıldı
        // Fragmentlarda bunları yapmak zorunlu
        binding.imageView3.setOnClickListener{gorselSec(it)}
        binding.bitkiKaydet.setOnClickListener{bitkiKaydet(it)}
        binding.bitkiSil.setOnClickListener{bitkiSil(it)}
        binding.onaylaButon.setOnClickListener{onayla(it)}

        arguments?.let {
            val bilgi = bitkiFragmentArgs.fromBundle(it).bilgi
            val id = bitkiFragmentArgs.fromBundle(it).id

            if (bilgi == "yeni"){
                // Yeni bitki eklenecek
                secilenBitki = null
                binding.bitkiSil.isEnabled = false
                binding.onaylaButon.isEnabled = false
                binding.bitkiKaydet.isEnabled = true
            } else{
                // Varsayılan bitki gösterilecek
                binding.bitkiSil.isEnabled = true
                binding.bitkiKaydet.isEnabled = false
                binding.onaylaButon.isEnabled = true
                val id = bitkiFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    bitkiDao.findById(id).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )

            }
        }
    }



    private fun handleResponse(bitki : Bitki){
        val bitmap = BitmapFactory.decodeByteArray(bitki.gorsel, 0, bitki.gorsel.size)
        binding.imageView3.setImageBitmap(bitmap)
        binding.isim.setText(bitki.isim)
        binding.pH.setText(bitki.pH)
        binding.EC.setText(bitki.EC)
        binding.Light.setText(bitki.sure)
        binding.Nem.setText(bitki.nem)

        secilenBitki = bitki
    }

    fun onayla(it: View?) {
        if (secilenBitki == null) {
            Toast.makeText(requireContext(), "Güncellenecek bitki seçilmedi!", Toast.LENGTH_SHORT).show()
            return
        }

        val isim = binding.isim.text.toString()
        val pH = binding.pH.text.toString()
        val EC = binding.EC.text.toString()
        val sure = binding.Light.text.toString()
        val nem = binding.Nem.text.toString()

        if (isim.isEmpty() || pH.isEmpty() || EC.isEmpty() || sure.isEmpty() || nem.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen tüm alanları doldurunuz!", Toast.LENGTH_SHORT).show()
            return
        }

        val guncellenmisBitki = secilenBitki!!.copy(
            isim = isim,
            pH = pH,
            EC = EC,
            sure = sure,
            nem = nem,
            gorsel = if (secilenBitmap != null) {
                val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, 300)
                val outputStream = ByteArrayOutputStream()
                kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                outputStream.toByteArray()
            } else {
                secilenBitki!!.gorsel
            }
        )

        mDisposable.add(
            bitkiDao.update(guncellenmisBitki)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(requireContext(), "Bitki başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                    kaydedildi(isim, pH, EC, sure, nem)
                   //requireActivity().supportFragmentManager.popBackStack() // Önceki ekrana dön
                }, {
                    Toast.makeText(requireContext(), "Güncelleme hatası: ${it.message}", Toast.LENGTH_SHORT).show()
                })
        )
    }

    fun bitkiKaydet(view: View){
        val isim = binding.isim.text.toString()
        val pH = binding.pH.text.toString()
        val EC = binding.EC.text.toString()
        val sure = binding.Light.text.toString()
        val nem = binding.Nem.text.toString()


        if(secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, maximumBoyut = 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            val bitki = Bitki(isim, pH, EC, sure, nem, byteDizisi)

            if (isim.isEmpty() || pH.isEmpty() || EC.isEmpty() || sure.isEmpty() || nem.isEmpty()) {
                Toast.makeText(view.context, "Lütfen tüm alanları doldurunuz!", Toast.LENGTH_SHORT).show()
                return // Boş bir değer varsa işlemi burada sonlandırıyoruz
            }
            // RxJava

            mDisposable.add(
                bitkiDao.insert(bitki)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        kaydedildi(isim, pH, EC, sure, nem) // Parametreleri doğru şekilde geçiriyoruz
                    }, {
                        // Hata durumu burada ele alınabilir
                        Toast.makeText(requireContext(), "Bir hata oluştu: ${it.message}", Toast.LENGTH_SHORT).show()
                    })
            )

        }
    }

    private fun handleResponseForInsert(){
        // Bir önceki fragment'a dön.
        val action = bitkiFragmentDirections.actionBitkiFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun kaydedildi(isim: String, pH: String, EC: String, sure: String, nem: String) {
        // Firebase Kodları
        val istenenMap = hashMapOf<String, Any>()
        istenenMap["Bitki Adı"] = isim
        istenenMap["pH Değeri"] = pH
        istenenMap["EC Değeri"] = EC
        istenenMap["Işık Süresi"] = sure
        istenenMap["Nem Oranı"] = nem
        istenenMap["Son Güncellenme Tarihi"] = Timestamp.now()

        Fdb.collection("İstenen Değerler").add(istenenMap).addOnSuccessListener { documentReference ->
            // Veriler veri tabanına yüklendi.
            view?.let { // null değilse işlemi yap
                requireActivity().supportFragmentManager.popBackStack()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
        }

        Toast.makeText(requireContext(), "Veri kaydedildi!", Toast.LENGTH_SHORT).show()
    }


    fun bitkiSil(view: View){

        if (secilenBitki != null) {
            mDisposable.add(
                bitkiDao.delete(bitki = secilenBitki!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
        // Bu kod sıkıntı ileride yaşatabilir.
        // Kaydet veya Sil butonuna tıkladığında, mevcut fragment'ın yaşam döngüsünü sonlandırır.
        // Bu işlemden sonra otomatik olarak bir önceki ekrana (veya belirlediğin bir yere) dönmesini sağlar.
        requireActivity().supportFragmentManager.popBackStack()

    }

    // ------------ Farklı izinler için READ_MEDIA_IMAGES vb kodları değiştirip izin istenebilir.
    fun gorselSec(view: View){
        // Daha önce bu izin alındı mı? Kontrolü yapılır.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                // İzin verilmediyse izin istemek.

                // Aşağıdaki kod kullanıcı izin isteğini reddeddiyse izini tekrar Snackbar ile otomatik olarak (Android kendi karar verir) ister
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    // Snackbar gösterilir, kullanıcıya neden bir daha izin istediğimizi söyleyerek izin istenir.
                    // İzin istenir
                    Snackbar.make(view, "Galeriye gidilsin mi?", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver",
                        View.OnClickListener {
                            // İzin istenecek alan
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                } else{
                    // İzin istenecek
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }
            } else {
                // İzin verilmemişse galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        } else{
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                // İzin verilmediyse izin istemek.

                // Aşağıdaki kod kullanıcı izin isteğini reddeddiyse izini tekrar Snackbar ile otomatik olarak (Android kendi karar verir) ister
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    // Snackbar gösterilir, kullanıcıya neden bir daha izin istediğimizi söyleyerek izin istenir.
                    // İzin istenir
                    Snackbar.make(view, "Galeriye gidilsin mi?", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver",
                        View.OnClickListener {
                            // İzin istenecek alan
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                } else{
                    // İzin istenecek
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }
            } else {
                // İzin verilmemişse galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {
        // İzim iste
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    secilenGorsel = intentFromResult.data

                    try {
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView3.setImageBitmap(secilenBitmap)
                        } else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, secilenGorsel)
                            binding.imageView3.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: Exception){
                        println(e.localizedMessage)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result->
            if(result){
                // İzin verildi, galeriye gidilebilir.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            } else {
                // İzin verilmedi
                Toast.makeText(requireContext(), "İzin verilmedi!", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap, maximumBoyut : Int) : Bitmap {
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if(bitmapOrani > 1){
            // Görsel yatay.
            width = maximumBoyut
            val kisaYukseklik = width/bitmapOrani
            height = kisaYukseklik.toInt()

        } else{
            // Görsel dikey.
            height = maximumBoyut
            val kisaGenislik = height * bitmapOrani
            width = kisaGenislik.toInt()

        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width,height, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}