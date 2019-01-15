package me.xana.hlsdemo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.video.VideoListener
import kotlinx.android.synthetic.main.activity_main.*
import me.drakeet.multitype.ItemViewBinder
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.xana.hlsdemo.video.*
import java.util.Comparator

class MainActivity : AppCompatActivity(), VideoListener, Player.EventListener, SeekBar.OnSeekBarChangeListener {

    private val player by lazy { VideoPlayer(this) }
//    private val uri = "http://7xlv47.com1.z0.glb.clouddn.com/xxx004.m3u8"
    private val uri = "https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8"
    private val progressListener by lazy { ProgressListener() }
    private val progressQueryDelegate by lazy { ProgressQueryDelegate(player, this, seekBar, progressListener) }

    private val items = Items()
    private val adapter = MultiTypeAdapter(items)
    private var width = -1
    private var height = -1
    private var state = Player.STATE_IDLE
    private var trackSelection: KeepTrackSelection? = null

    private val stateMap = mapOf(
        Player.STATE_IDLE to "IDLE",
        Player.STATE_BUFFERING to "BUFFERING",
        Player.STATE_READY to "READY",
        Player.STATE_ENDED to "ENDED"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()
        player.player.addVideoListener(this)
        player.player.addListener(this)
        seekBar.setOnSeekBarChangeListener(this)
        video_view.setOnClickListener {
            player.play(uri, video_view)
            progressQueryDelegate.start()
        }
    }

    override fun onPause() {
        super.onPause()
        player.player.stop()
    }

    override fun onVideoSizeChanged(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        this.width = width
        this.height = height
        updateVideoInfo()
    }

    private fun updateVideoInfo() {
        txtInfo.text = "$width x $height, ${stateMap[state]}"
    }

    override fun onRenderedFirstFrame() {
        updateTrackInfo()
    }

    private fun initRecyclerView() {
        adapter.register(TrackSelectionData::class.java, FormatItemViewBinder(DefaultTrackNameProvider(resources), ::updateSelectedTrack))

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun updateSelectedTrack(index: Int) {
        trackSelection?.forceSelectedIndex = index
    }

     class DecreasingBandwidthComparator : Comparator<Format> {
        override fun compare(a: Format, b: Format): Int {
            return b.bitrate - a.bitrate
        }

    }

    private fun updateTrackInfo() {
        items.clear()
        items.add(TrackSelectionData(-1, null))
        val mappedTrackInfo = player.trackSelector.currentMappedTrackInfo
        mappedTrackInfo?.let {
            for (i in 0 until it.rendererCount) {
                if (mappedTrackInfo.getRendererType(i) != C.TRACK_TYPE_VIDEO) {
                    continue
                }
                val groupArray = it.getTrackGroups(i)
                for (j in 0 until groupArray.length) {
                    val group = groupArray[j]
                    val formats = Array(group.length) { index -> group.getFormat(index) }
                    formats.sortWith(DecreasingBandwidthComparator())
                    items.addAll(formats.mapIndexed { index, format -> TrackSelectionData(index, format) })
                }
                val selection = player.player.currentTrackSelections[i]
                if (selection is KeepTrackSelection) {
                    trackSelection = selection
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            player.player.seekTo(progress * 100L)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        progressQueryDelegate.stop()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        progressQueryDelegate.start()
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onSeekProcessed() {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        trackSelections?.all?.forEachIndexed { index, selection ->
            if (index == C.TRACK_TYPE_VIDEO && selection is KeepTrackSelection) {
                this.trackSelection = selection
            }
            Log.d("HlsDemo_Selection", "$index: ${selection?.selectedIndexInTrackGroup}")
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        this.state = playbackState
        updateVideoInfo()
    }

    private data class TrackSelectionData(
        val index: Int,
        val format: Format?
    )

    private class FormatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
    }

    private class FormatItemViewBinder(val provider: TrackNameProvider, val switchAction: (Int) -> Unit) : ItemViewBinder<TrackSelectionData, FormatViewHolder>() {
        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): FormatViewHolder {
            val view = inflater.inflate(R.layout.item_bitrate, parent, false)
            return FormatViewHolder(view)
        }

        override fun onBindViewHolder(holder: FormatViewHolder, item: TrackSelectionData) {
            if (item.format != null) {
                holder.title.text = provider.getTrackName(item.format)
                holder.itemView.setOnClickListener { switchAction(item.index) }
            } else {
                holder.title.text = "自动"
                holder.itemView.setOnClickListener { switchAction(-1) }
            }
        }
    }

    private inner class ProgressListener : ProgressQueryListener {
        override fun onPositionChange(currentPositionMs: Long, totalLengthMs: Long, bufferedPercentage: Float) {
            seekBar.max = (totalLengthMs / 100).toInt()
            seekBar.progress = (currentPositionMs / 100).toInt()
            seekBar.secondaryProgress = (totalLengthMs / 100 * bufferedPercentage).toInt()

            txtPosition.text = "${currentPositionMs / 1000 / 60}:${currentPositionMs / 1000 % 60 }"
            txtDuration.text = "${totalLengthMs / 1000 / 60}:${totalLengthMs / 1000 % 60 }"
        }

    }
}
