package com.example.localites.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.interfaces.MarketCallbacks

class MarketAdapter(
    var picturesList: MutableList<String>,
    var listener: MarketCallbacks,
    var context: Context
) : RecyclerView.Adapter<MarketAdapter.MViewHolder>() {

    var marketCallbacks: MarketCallbacks? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {

        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.single_image, parent, false)

        marketCallbacks = listener

        return MViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (picturesList.size < 3) {
            return picturesList.size + 1
        } else {
            return picturesList.size
        }
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {


        if (picturesList.size > 0) {
            Glide.with(context).load(picturesList[position])
                .into(holder.img_singleImage)
        } else {
            Glide.with(context).load(R.drawable.ic_person).into(holder.img_singleImage)
        }

        holder.itemView.setOnClickListener {
            /*if (position >= picturesList.size) {
                Log.e("ItemClicked", "${picturesList.size} , $position")
            } else {
                Log.e("ItemClickedElse", "${picturesList.size} , $position")
            }*/
            marketCallbacks!!.onPictureAddClicked(position)
        }

        holder.img_singleImage_close.setOnClickListener {
            marketCallbacks!!.onPictureRemoveClicked(position)
        }
    }

    inner class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var img_singleImage: ImageView
        var img_singleImage_close: ImageView

        init {
            img_singleImage = itemView.findViewById(R.id.img_singleImage)
            img_singleImage_close = itemView.findViewById(R.id.img_singleImage_close)
        }
    }

}