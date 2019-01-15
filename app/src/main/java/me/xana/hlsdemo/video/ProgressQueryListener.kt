package me.xana.hlsdemo.video

/**
 * @author Xana/cuixianming
 * @version 1.0
 * @since 2019-01-15 10:37
 */
interface ProgressQueryListener {
    /**
     * 播放进度查询回调接口
     * @param currentPositionMs 当前播放的位置，单位毫秒 = 1e-6 秒
     * @param totalLengthMs 视频总长度，单位毫秒
     * @param bufferedPercentage 已经缓存的位置百分比, 范围 0 - 1
     */
    fun onPositionChange(currentPositionMs: Long, totalLengthMs: Long, bufferedPercentage: Float)
}