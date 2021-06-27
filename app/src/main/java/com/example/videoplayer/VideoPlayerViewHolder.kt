package com.example.videoplayer

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.videoplayer.models.MediaObject

class VideoPlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val parent = itemView
    val mediaContainer : FrameLayout = itemView.findViewById(R.id.media_container)
    private val title : TextView = itemView.findViewById(R.id.title)
    val thumbnail : ImageView = itemView.findViewById(R.id.thumbnail)
    val volumeControl : ImageView = itemView.findViewById(R.id.volume_control)
    val progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)
    lateinit var requestManager : RequestManager

    fun onBind(mediaObject: MediaObject, requestManager: RequestManager){
        this.requestManager = requestManager
        title.text = mediaObject.title
        parent.tag = this
        this.requestManager
            .load(mediaObject.thumbnail)
            .into(thumbnail)

    }

}