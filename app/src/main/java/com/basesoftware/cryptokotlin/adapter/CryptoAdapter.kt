package com.basesoftware.cryptokotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.basesoftware.cryptokotlin.R
import com.basesoftware.cryptokotlin.databinding.RowCryptoBinding
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel

class CryptoAdapter : RecyclerView.Adapter<CryptoAdapter.RowHolder>() {

    private var mDiffer : AsyncListDiffer<CryptoRecyclerModel>

    init {

        val diffCallBack = object : DiffUtil.ItemCallback<CryptoRecyclerModel>() {
            override fun areItemsTheSame( oldItem: CryptoRecyclerModel, newItem: CryptoRecyclerModel): Boolean {
                return oldItem.currency.matches(Regex(newItem.currency)) // Currency aynı ise item aynıdır (ekleme/çıkarma yok)
            }

            override fun areContentsTheSame(oldItem: CryptoRecyclerModel, newItem: CryptoRecyclerModel): Boolean {
                // Eğer verilerden herhangi birisi farklı ise güncelleme gerekiyor [false dönecek]
                return oldItem.currency.matches(Regex(newItem.currency)) &&
                        oldItem.price.matches(Regex(newItem.price)) &&
                        oldItem.isApiData == newItem.isApiData
            }

        }

        mDiffer = AsyncListDiffer(this, diffCallBack) // AsyncListDiffer'ı oluştur

    }

    class RowHolder(val binding : RowCryptoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val binding = RowCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowHolder(binding)
    }


    override fun onBindViewHolder(holder: RowHolder, position: Int) {

        holder.apply {

            binding.apply {

                crypto = mDiffer.currentList[holder.bindingAdapterPosition] // XML'e DataBinding verisini gönder

                txtDataStatus.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        when(mDiffer.currentList[bindingAdapterPosition].isApiData) {
                            true -> R.color.green
                            else -> R.color.red }
                    )
                ) // Duruma göre yeşil veya kırmızı renk

            }

        }

    }

    fun updateData(arrayCrypto : ArrayList<CryptoRecyclerModel>) = mDiffer.submitList(arrayCrypto) // Listeyi güncelle

    fun getItem(index : Int) : CryptoRecyclerModel = mDiffer.currentList[index] // Item ara

    override fun getItemCount(): Int = mDiffer.currentList.size // RecyclerView item sayısı
}