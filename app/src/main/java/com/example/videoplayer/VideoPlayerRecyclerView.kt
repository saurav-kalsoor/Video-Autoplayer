package com.example.videoplayer

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.videoplayer.models.MediaObject
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.upstream.DataSource


class VideoPlayerRecyclerView : RecyclerView {
    enum class VolumeState { ON, OFF}

    //UI
     var thumbnail : ImageView? = null
     lateinit var volumeControl : ImageView
     var progressBar : ProgressBar? = null
     var frameLayout : FrameLayout? = null
     var viewHolderParent : View? = null
     lateinit var videoSurfaceView : PlayerView
     var videoPlayer : SimpleExoPlayer? = null

    //vars
    lateinit var mediaObjects : List<MediaObject>
    var playPosition = -1
    var isVideoViewAdded = false
    lateinit var requestManager: RequestManager

    // controlling playback state
    var volumeState: VolumeState = VolumeState.ON

    constructor(context : Context) : super(context) {
        initialize(context)
    }


    constructor(context: Context, attrs : AttributeSet) : super(context, attrs){
        initialize(context)
    }

    fun initialize(context: Context){
        videoSurfaceView = PlayerView(context.applicationContext)
        videoSurfaceView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        //Create The player
        val trackSelector = DefaultTrackSelector()
        videoPlayer = ExoPlayerFactory.newSimpleInstance(context.applicationContext, trackSelector)

        //Bind Player to the view
        videoSurfaceView.useController = false
        videoSurfaceView.player = videoPlayer
        setVolumeControl(VolumeState.ON)


        //On Scrolling
        addOnScrollListener(object  : OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)



                if(newState == SCROLL_STATE_IDLE){
                    thumbnail?.let {
                        it.visibility = VISIBLE
                    }

                    progressBar?.let {
                        it.visibility = INVISIBLE
                    }

                    if(recyclerView.canScrollVertically(1)){
                        playVideo(false)
                    }else{
                        playVideo(true)
                    }
                }
            }
        })


        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}

            override fun onChildViewDetachedFromWindow(view: View) {
                viewHolderParent?.let {
                    if(it == view){
                        resetVideoView()
                    }
                }
            }
        })

        with(videoPlayer){
            this?.addListener(object : Player.EventListener{
                override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}

                override fun onTracksChanged(
                    trackGroups: TrackGroupArray?,
                    trackSelections: TrackSelectionArray?
                ) {}

                override fun onLoadingChanged(isLoading: Boolean) {}

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when(playbackState){
                        Player.STATE_BUFFERING -> {
                            progressBar?.visibility = VISIBLE
                        }

                        Player.STATE_ENDED -> {
                            videoPlayer?.seekTo(0)
                        }

                        Player.STATE_READY -> {
                            progressBar?.visibility = INVISIBLE

                            if(!isVideoViewAdded){
                                addVideoView()
                            }
                        }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {}

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

                override fun onPlayerError(error: ExoPlaybackException?) {}

                override fun onPositionDiscontinuity(reason: Int) {}

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

                override fun onSeekProcessed() {}

            })
        }


    }

    fun playVideo(isEndOfList: Boolean) {

        val targetPosition = if(isEndOfList){
            mediaObjects.size - 1
        }else{
            (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        if(targetPosition == playPosition)
            return



        playPosition = targetPosition

        // remove any old surface views from previously playing videos
        videoSurfaceView.visibility = INVISIBLE
        removeVideoView(videoSurfaceView)


        val currentPosition = targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        val holder : VideoPlayerViewHolder = child.tag as VideoPlayerViewHolder ?: return

        thumbnail?.visibility = VISIBLE

        thumbnail = holder.thumbnail
        progressBar = holder.progressBar
        volumeControl = holder.volumeControl
        viewHolderParent = holder.itemView
        requestManager = holder.requestManager
        frameLayout = holder.mediaContainer

        progressBar?.visibility = VISIBLE

        videoSurfaceView.player = videoPlayer

        viewHolderParent?.setOnClickListener {
            toggleVolume()
        }

        val dataSourceFactory : DataSource.Factory = DefaultDataSourceFactory(
            context, Util.getUserAgent(context, "RecyclerView VideoPlayer")
        )

        val mediaUrl = mediaObjects[targetPosition].mediaUrl
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediaUrl))
        videoPlayer?.prepare(videoSource)
        videoPlayer?.playWhenReady = true
    }

    private fun toggleVolume() {
        videoPlayer?.let {
            if(volumeState == VolumeState.ON){
                setVolumeControl(VolumeState.OFF)
            }else{
                setVolumeControl(VolumeState.ON)
            }
        }
    }

    private fun removeVideoView(videoView : PlayerView) {

        videoView.parent?.let {
            val parent : ViewGroup = it as ViewGroup
            val index = parent.indexOfChild(videoSurfaceView)

            if(index >= 0){
                parent.removeViewAt(index)
                isVideoViewAdded = false
                viewHolderParent?.setOnClickListener(null)
            }
        }


    }

    private fun addVideoView() {
        frameLayout?.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView.requestFocus()
        videoSurfaceView.visibility = VISIBLE
        videoSurfaceView.alpha = 1F
        thumbnail?.visibility = INVISIBLE

    }

    private fun resetVideoView() {
        if(isVideoViewAdded){
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView.visibility = INVISIBLE
            thumbnail?.visibility = VISIBLE
        }
    }

    private fun setVolumeControl(state : VolumeState) {
        volumeState = state
        if(state == VolumeState.ON)
            videoPlayer?.volume = 1F
        else
            videoPlayer?.volume = 0F
    }

    fun releasePlayer(){
        videoPlayer?.release()
        videoPlayer = null
        viewHolderParent = null
    }

    fun setMediaObjects(list : ArrayList<MediaObject>){
        mediaObjects = list
    }

}

