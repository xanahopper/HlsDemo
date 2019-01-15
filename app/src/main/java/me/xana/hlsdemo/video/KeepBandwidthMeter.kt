/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xana.hlsdemo.video

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.SlidingPercentile
import kotlin.math.sqrt

/**
 * 通过监听数据传输来估计带宽。
 */
class KeepBandwidthMeter(
    private val eventHandler: Handler? = Handler(Looper.getMainLooper()),
    private val eventListener: BandwidthMeter.EventListener? = LISTENER
) : BandwidthMeter, TransferListener<Any> {

    private val slidingPercentile = SlidingPercentile(2000)
    private val clock = Clock.DEFAULT
    private var streamCount: Int = 0
    private var sampleStartTimeMs: Long = 0
    private var sampleLastEstimateMs = 0L
    private var sampleBytesTransferred: Long = 0
    private var totalBytesTransferred: Long = 0
    private var bitrateEstimate = 0L
    override fun getBitrateEstimate(): Long = bitrateEstimate
    override fun onTransferStart(source: Any, dataSpec: DataSpec) {
        Log.i("KVP_Bandwidth", "onTransferStart($source, $dataSpec)")
        if (streamCount == 0) {
            sampleStartTimeMs = clock.elapsedRealtime()
            sampleLastEstimateMs = sampleStartTimeMs
        }
        streamCount++
    }

    @Synchronized
    override fun onBytesTransferred(source: Any, bytes: Int) {
        val nowMs = clock.elapsedRealtime()
        val sampleElapsedTimeMs = nowMs - sampleStartTimeMs
        val sampleEstimateTimeMs = nowMs - sampleLastEstimateMs
        sampleBytesTransferred += bytes
        totalBytesTransferred += bytes
        if (sampleElapsedTimeMs > 0) {
            val bitsPerSample = ((sampleBytesTransferred * 8000) / sampleElapsedTimeMs).toFloat()
            val weight = sqrt(sampleBytesTransferred.toDouble()).toInt()
            slidingPercentile.addSample(weight, bitsPerSample)
            Log.i("KVP_Bandwidth", "onBytesTransferred($source, $bytes) in $sampleElapsedTimeMs ms, add sample (weight: $weight, value: $bitsPerSample)")
            if (sampleEstimateTimeMs >= ELAPSED_MILLIS_FOR_ESTIMATE ||
                totalBytesTransferred >= BYTES_TRANSFERRED_FOR_ESTIMATE
            ) {
                bitrateEstimate = slidingPercentile.getPercentile(0.5f).toLong()
                notifyBandwidthSample(sampleEstimateTimeMs.toInt(), totalBytesTransferred, bitrateEstimate)
                totalBytesTransferred = 0
                sampleLastEstimateMs = clock.elapsedRealtime()
            }
            sampleBytesTransferred = 0
            sampleStartTimeMs = clock.elapsedRealtime()
        }
    }

    @Synchronized
    override fun onTransferEnd(source: Any) {
        if (--streamCount > 0) {
            sampleStartTimeMs = clock.elapsedRealtime()
        }
    }

    private fun notifyBandwidthSample(elapsedMs: Int, bytes: Long, bitrate: Long) {
        eventHandler?.post { eventListener?.onBandwidthSample(elapsedMs, bytes, bitrate) }
    }

    private class BandwidthEventListener : BandwidthMeter.EventListener {
        override fun onBandwidthSample(elapsedMs: Int, bytes: Long, bitrate: Long) {
            Log.i("KVP_Bandwidth", "BandwidthSample elapsedMs: $elapsedMs, bytes: $bytes, bitrate: $bitrate")
        }
    }

    companion object {
        private val LISTENER = BandwidthEventListener()
        private const val ELAPSED_MILLIS_FOR_ESTIMATE = 2000
        private const val BYTES_TRANSFERRED_FOR_ESTIMATE = 512 * 1024
    }
}
