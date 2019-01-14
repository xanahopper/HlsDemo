package me.xana.hlsdemo.video

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.TextureView
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

/**
 * HlsDemo
 * @author Xana Hopper(xanahopper@163.com)
 * @since 2019-01-14 22:52
 */
class VideoPlayer(context: Context) : BandwidthMeter.EventListener {
    private val rendererFactory = DefaultRenderersFactory(context.applicationContext)
    private val eventHandler = Handler(Looper.getMainLooper())
    private val bandwidthMeter = DefaultBandwidthMeter.Builder().setEventListener(eventHandler, this).build()
    private val trackSelector = DefaultTrackSelector(bandwidthMeter)
    private val dataSourceFactory = DefaultDataSourceFactory(context.applicationContext, "Xana", bandwidthMeter)
    private val mediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)

    private val player = ExoPlayerFactory.newSimpleInstance(rendererFactory, trackSelector, DefaultLoadControl())

    fun play(uri: String, videoView: TextureView) {
        player.setVideoTextureView(videoView)
        val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(uri))
        player.prepare(mediaSource)
        player.playWhenReady = true
    }

    override fun onBandwidthSample(elapsedMs: Int, bytes: Long, bitrate: Long) {
        Log.d("HlsDemo", "onBandwidthSample(elapsedMs: $elapsedMs, bytes: $bytes, bitrate: $bitrate)")
    }
}