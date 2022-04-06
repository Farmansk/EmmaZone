package com.live.emmazone.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.live.emmazone.R
import com.live.emmazone.response_model.socket_response.ChatListResponse
import com.live.emmazone.utils.AppConstants
import com.live.emmazone.utils.AppUtils
import com.schunts.extensionfuncton.loadImage
import com.tubb.smrv.SwipeHorizontalMenuLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_message.view.*

class MessageAdapter(val listMsg: ArrayList<ChatListResponse.ChatListResponseItem>) :
    RecyclerView.Adapter<MessageAdapter.MessageListViewHolder>() {

    var onItemCLick: ((pos: Int, clickOn: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageListViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return listMsg.size
    }

    inner class MessageListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val civProfile: CircleImageView = itemView.civProfile
        private val tvName: TextView = itemView.tvName
        private val tvMessage: TextView = itemView.tvMessage
        private val tvMessageTime: TextView = itemView.tvTime
        private val tvMessageCount: TextView = itemView.tvMessageCount

        fun bind(pos: Int) {
            val model = listMsg[pos]

            itemView.setOnClickListener {
                onItemCLick?.invoke(pos, "itemClick")

            }

            civProfile.loadImage(AppConstants.IMAGE_USER_URL + model.image)
            tvName.text = model.userName
            tvMessage.text = model.lastMessage
            tvMessageTime.text =
                AppUtils.secondsToTime(model.created.toLong(), AppConstants.DATE_FORMAT)

        }
    }
}