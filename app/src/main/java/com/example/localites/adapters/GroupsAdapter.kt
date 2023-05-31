package com.example.localites.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.helpers.TextChanges
import com.example.localites.interfaces.GroupCallback
import com.example.localites.models.CreateGroupModel
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class GroupsAdapter(
    var groupsModelList: List<CreateGroupModel?>,
    var listener: GroupCallback,
    var context: Context
) : RecyclerView.Adapter<GroupsAdapter.MViewHolder>() {

    var groupCallback: GroupCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)

        groupCallback = listener

        return MViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupsModelList.size
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {

        var eachItem = groupsModelList.get(position)
        if (!eachItem?.groupCoverPic.isNullOrEmpty()) {
            holder.tv_groupItem_shortName.visibility = View.GONE
            holder.img_groupItem_coverPic.visibility = View.VISIBLE
            Glide.with(context).load(eachItem?.groupCoverPic).error(R.drawable.ic_sync_problem)
                .into(holder.img_groupItem_coverPic)
        } else {
            holder.tv_groupItem_shortName.visibility = View.VISIBLE
            holder.img_groupItem_coverPic.visibility = View.GONE
            if (eachItem!!.groupName!!.length > 2) {
                holder.tv_groupItem_shortName.setText(
                    eachItem.groupName!!.substring(0, 2).toUpperCase(Locale.ROOT)
                )
            }
        }
        holder.tv_groupItem_title.text = TextChanges().capitalize(eachItem?.groupName.toString())
        holder.tv_groupItem_description.text = eachItem?.grroupDescription.toString()
        holder.tv_groupItem_location.text =
            "${TextChanges().capitalize(eachItem?.groupLocationCity.toString())}, ${TextChanges().capitalize(eachItem?.groupLocationCountry.toString())}"

        holder.itemView.setOnClickListener { groupCallback!!.onitemClicked(position) }
    }

    inner class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var img_groupItem_coverPic: ImageView
        var tv_groupItem_shortName: TextView
        var tv_groupItem_title: TextView
        var tv_groupItem_description: TextView
        var tv_groupItem_location: TextView

        init {
            img_groupItem_coverPic = itemView.findViewById(R.id.img_groupItem_coverPic)
            tv_groupItem_shortName = itemView.findViewById(R.id.tv_groupItem_shortName)
            tv_groupItem_title = itemView.findViewById(R.id.tv_groupItem_title)
            tv_groupItem_description = itemView.findViewById(R.id.tv_groupItem_description)
            tv_groupItem_location = itemView.findViewById(R.id.tv_groupItem_location)
        }
    }


}