package com.example.localites.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.activities.ProfileDetails
import com.example.localites.helpers.Constants
import com.example.localites.helpers.TextChanges
import com.example.localites.interfaces.GroupDetailsCallback
import com.example.localites.models.CreateGroupPostModel

class GroupDetailPostsAdapter(
    var groupsModelList: List<CreateGroupPostModel?>,
    var listener: GroupDetailsCallback,
    var context: Context
) : RecyclerView.Adapter<GroupDetailPostsAdapter.MViewHolder>() {

    var groupDetailsCallback: GroupDetailsCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {

        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.group_post_row, parent, false)

        groupDetailsCallback = listener

        return MViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupsModelList.size
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {

        var eachItem = groupsModelList.get(position)
        if (!eachItem?.groupsCover.isNullOrEmpty()) {
            holder.img_groupPostRow_picture.visibility = View.VISIBLE
            Glide.with(context).load(eachItem?.groupsCover).error(R.drawable.ic_sync_problem)
                .into(holder.img_groupPostRow_picture)
        } else {
            holder.img_groupPostRow_picture.visibility = View.GONE
        }
        holder.tv_groupPostRow_message.text = eachItem?.message.toString()

        holder.tv_groupPostRow_date.text =
            TextChanges().convertDateToText(eachItem!!.createdDate.toString())

        holder.img_groupPostRow_more.setOnClickListener {
            var popupMenu = PopupMenu(context, holder.img_groupPostRow_more)
            popupMenu.menuInflater.inflate(R.menu.menu_group_post_row, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {

                    when (item?.itemId) {
                        R.id.menu_GroupPostRow_PostedBy -> {
                            var intent = Intent(context, ProfileDetails::class.java)
                            intent.putExtra(Constants.uid, eachItem!!.createdBy)
                            Log.e("GroupDetailsPostsAdapter", eachItem!!.createdBy+"_uid")
                            context.startActivity(intent)
                            return true
                        }
                        R.id.menu_GroupPostRow_report -> {
                            groupDetailsCallback!!.onReportClicked(position)
                            return true
                        }
                    }
                    return true
                }
            })
            popupMenu.show()
        }

        holder.itemView.setOnClickListener { groupDetailsCallback!!.onPostClicked(position) }
    }

    inner class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var img_groupPostRow_more: ImageView
        var tv_groupPostRow_date: TextView
        var tv_groupPostRow_message: TextView
        var img_groupPostRow_picture: ImageView
        var lay_groupPostRow_discussion: LinearLayout

        init {
            img_groupPostRow_more = itemView.findViewById(R.id.img_groupPostRow_more)
            tv_groupPostRow_date = itemView.findViewById(R.id.tv_groupPostRow_date)
            tv_groupPostRow_message = itemView.findViewById(R.id.tv_groupPostRow_message)
            img_groupPostRow_picture = itemView.findViewById(R.id.img_groupPostRow_picture)
            lay_groupPostRow_discussion = itemView.findViewById(R.id.lay_groupPostRow_discussion)

        }
    }


}