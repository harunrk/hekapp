package com.harun.hek.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.harun.hek.adater.BitkiAdapter
import com.harun.hek.databinding.FragmentListeBinding
import com.harun.hek.model.Bitki
import com.harun.hek.roomdb.BitkiDAO
import com.harun.hek.roomdb.BitkiDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class listeFragment : Fragment() {

    private var _binding: FragmentListeBinding? = null
    private val binding get() = _binding!!
    private lateinit var  db : BitkiDatabase
    private lateinit var bitkiDao : BitkiDAO
    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(), BitkiDatabase::class.java, "Bitkiler").build()
        bitkiDao = db.bitkiDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener{yeniEkle(it)}
        binding.bitkiRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        verileriAl()
    }

    private fun verileriAl(){
        mDisposable.add(
            bitkiDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(bitkiler : List<Bitki>){
        val adapter = BitkiAdapter(bitkiler)
        binding.bitkiRecyclerView.adapter = adapter
    }

    fun  yeniEkle(view: View){
        val action = listeFragmentDirections.actionListeFragmentToBitkiFragment(bilgi="yeni", id=0)
        Navigation.findNavController(view).navigate(action)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}