package me.xana.hlsdemo.video

import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.upstream.BandwidthMeter

/**
 * @author Xana/cuixianming
 * @version 1.0
 * @since 2019-01-15 16:08
 */
class KeepTrackSelector(bandwidthMeter: BandwidthMeter) : DefaultTrackSelector(bandwidthMeter) {
    override fun selectVideoTrack(
        groups: TrackGroupArray?,
        formatSupports: Array<out IntArray>?,
        mixedMimeTypeAdaptationSupports: Int,
        params: Parameters?,
        adaptiveTrackSelectionFactory: TrackSelection.Factory?
    ): TrackSelection? {
        var selection: TrackSelection? = null
        if (selection == null) {
            selection = super.selectVideoTrack(
                groups, formatSupports, mixedMimeTypeAdaptationSupports, params,
                adaptiveTrackSelectionFactory
            )
        }
        return selection
    }
}