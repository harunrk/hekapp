package com.harun.hek.adater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.harun.hek.databinding.RecyclerRowBinding
import com.harun.hek.model.Bitki
import com.harun.hek.view.listeFragmentDirections

class BitkiAdapter (val bitkiListesi : List<Bitki>) : RecyclerView.Adapter<BitkiAdapter.BitkiHolder> () {

    class BitkiHolder (val binding : RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitkiHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BitkiHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return bitkiListesi.size
    }

    override fun onBindViewHolder(holder: BitkiHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = bitkiListesi[position].isim
        holder.itemView.setOnClickListener{
            val action = listeFragmentDirections.actionListeFragmentToBitkiFragment(bilgi = "eski", id = bitkiListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}