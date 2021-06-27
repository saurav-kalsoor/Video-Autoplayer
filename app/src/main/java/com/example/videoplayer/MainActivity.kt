package com.example.videoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.videoplayer.models.MediaObject
import com.example.videoplayer.util.VerticalSpacingItemDecorator
import kotlin.collections.ArrayList
import com.example.videoplayer.util.Resources


class MainActivity : AppCompatActivity() {

    lateinit var recyclerView: VideoPlayerRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)

        initRecyclerView()

    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val itemDecorator = VerticalSpacingItemDecorator(10)
        recyclerView.addItemDecoration(itemDecorator)

        val mediaObjects = Resources.MEDIA_OBJECTS

        recyclerView.setMediaObjects(mediaObjects)

        val adapter = VideoPlayerRecyclerAdapter(mediaObjects, initGlide())
        recyclerView.adapter = adapter
    }

    private fun initGlide(): RequestManager {
        val options = RequestOptions()
            .placeholder(R.drawable.white_background)
            .error(R.drawable.white_background)

        return Glide.with(this).setDefaultRequestOptions(options)
    }

    override fun onStop() {
        recyclerView.releasePlayer()
        super.onStop()

    }

}