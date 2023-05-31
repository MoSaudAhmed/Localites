package com.example.localites.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.helpers.TextChanges
import com.example.localites.models.PostDiscussionModel

class PostDiscussionAdapter(
    var discussionsModelList: List<PostDiscussionModel?>,
    var context: Context
) : RecyclerView.Adapter<PostDiscussionAdapter.MViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {

        var view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_post_reply_row, parent, false)

        return MViewHolder(view)
    }

    override fun getItemCount(): Int {
        return discussionsModelList.size
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {

        var eachItem = discussionsModelList.get(position)

        holder.tv_postReplyDate.text = TextChanges().convertDateToText(eachItem!!.date.toString())


        if (!TextUtils.isEmpty(eachItem!!.displayName)) {
            holder.tv_postReplyName.text = eachItem.displayName
        }
        if (!TextUtils.isEmpty(eachItem.profilePic)) {
            Glide.with(context).load(eachItem.profilePic).into(holder.img_postReplyPic)
        } else {
            if (eachItem.displayName!!.length > 1) {
                holder.tv_postReplyshortName.text = eachItem.displayName!!.substring(0, 2)
            } else if (eachItem.displayName!!.length == 1) {
                holder.tv_postReplyshortName.text = eachItem.displayName!!.substring(0, 1)
            } else {
                holder.tv_postReplyshortName.text = "SA"
            }

        }

        if (!TextUtils.isEmpty(eachItem.reply)) {
            holder.tv_postReplyMessage.text = eachItem.reply
        }


    }

    inner class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tv_postReplyDate: TextView
        var tv_postReplyName: TextView
        var tv_postReplyMessage: TextView
        var tv_postReplyshortName: TextView
        var img_postReplyPic: ImageView

        init {
            tv_postReplyDate = itemView.findViewById(R.id.tv_postReplyDate)
            tv_postReplyName = itemView.findViewById(R.id.tv_postReplyName)
            tv_postReplyMessage = itemView.findViewById(R.id.tv_postReplyMessage)
            tv_postReplyshortName = itemView.findViewById(R.id.tv_postReplyshortName)
            img_postReplyPic = itemView.findViewById(R.id.img_postReplyPic)

        }
    }


}