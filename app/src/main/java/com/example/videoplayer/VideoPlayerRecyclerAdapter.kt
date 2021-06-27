package com.example.videoplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.videoplayer.models.MediaObject

class VideoPlayerRecyclerAdapter(val mediaObjects : List<MediaObject>, val requestManager: RequestManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_video_list_item, parent, false)
        return VideoPlayerViewHolder(view)
    }


    override fun getItemCount() = mediaObjects.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as VideoPlayerViewHolder
        myHolder.onBind(mediaObjects[position], requestManager)
    }

}