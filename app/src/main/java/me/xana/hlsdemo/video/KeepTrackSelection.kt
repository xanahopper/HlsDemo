package me.xana.hlsdemo.video

import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.chunk.MediaChunk
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.upstream.BandwidthMeter

/**
 * @author Xana/cuixianming
 * @version 1.0
 * @since 2019-01-15 15:46
 */
class KeepTrackSelection(group: TrackGroup?, tracks: IntArray, bandwidthMeter: BandwidthMeter) : AdaptiveTrackSelection(group, tracks, bandwidthMeter) {
    var forceSelectedIndex = -1

    init {
        Log.d("Hls_Selection", "KeepTrackSelection Created")
    }
    override fun updateSelectedTrack(playbackPositionUs: Long, bufferedDurationUs: Long, availableDurationUs: Long) {
        super.updateSelectedTrack(playbackPositionUs, bufferedDurationUs, availableDurationUs)
        Log.d("HlsDemo_Selection", "after updateSelectedTrack, super.selectedIndex = ${super.getSelectedIndex()}, selectedIndex = $selectedIndex, selectedIndexInTrackGroup = $forceSelectedIndex")
    }

    override fun evaluateQueueSize(playbackPositionUs: Long, queue: MutableList<out MediaChunk>?): Int {
        Log.d("HlsDemo", "evaluateQueueSize(playPos: $playbackPositionUs, ")
        return super.evaluateQueueSize(playbackPositionUs, queue)
    }

    override fun getSelectedIndex(): Int {
        val index = if (forceSelectedIndex >= 0) forceSelectedIndex else super.getSelectedIndex()
        Log.d("HlsDemo", "getSelectedIndex(): $index, ")
        return index
    }

    override fun getSelectionReason(): Int {
        return if (forceSelectedIndex >= 0) C.SELECTION_REASON_MANUAL else super.getSelectionReason()
    }

    class Factory(private val bandwidthMeter: BandwidthMeter) : TrackSelection.Factory {
        override fun createTrackSelection(group: TrackGroup?, vararg tracks: Int): TrackSelection {
            return KeepTrackSelection(group, tracks, bandwidthMeter)
        }
    }
}