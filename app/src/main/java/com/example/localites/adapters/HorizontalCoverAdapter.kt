package com.example.localites.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.localites.R
import com.example.localites.activities.UpdateProfileActivity
import com.example.localites.interfaces.CoverCallback

class HorizontalCoverAdapter(val coverImagesList: Array<Int>, val context: Context) :
    RecyclerView.Adapter<HorizontalCoverAdapter.ViewHolder>() {

    var coverCallback: CoverCallback? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HorizontalCoverAdapter.ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.cover_chooser_item, parent, false)

        coverCallback = context as UpdateProfileActivity
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return coverImagesList.size
    }

    override fun onBindViewHolder(holder: HorizontalCoverAdapter.ViewHolder, position: Int) {
        holder.img_cover_chooser_item.setImageResource(coverImagesList.get(position))
        //holder.img_cover_chooser_item.alpha = 0.0f
        holder.img_cover_chooser_item.setOnClickListener { coverCallback?.coverSelected(position) }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img_cover_chooser_item: ImageView = itemView.findViewById(R.id.img_cover_chooser_item)
    }
}