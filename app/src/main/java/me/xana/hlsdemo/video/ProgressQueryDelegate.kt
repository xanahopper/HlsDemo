package me.xana.hlsdemo.video

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import me.xana.hlsdemo.Weak
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 进度查询行为委托类
 *
 * @author Xana/cuixianming
 * @version 1.0
 * @since 2018-11-02 11:42
 * @see ProgressQueryListener
 *
 * @param lifecycleOwner LifeOwner 对象，用以注册监听生命周期
 * @param view 代理的 view 对象
 * @param listener 回调方法，内部为弱引用，<b>不要</b>使用临时对象
 */
class ProgressQueryDelegate(
    private val player: VideoPlayer,
    lifecycleOwner: LifecycleOwner,
    view: View,
    listener: ProgressQueryListener
) : Runnable, LifecycleObserver {

    private val listener by Weak { listener }
    private val view by Weak { view }
    private var running = AtomicBoolean(false)
    private var available = false

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun start() {
        running.set(true)
        startQuery()
    }

    fun stop() {
        running.set(false)
        stopQuery()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        available = true
        startQuery()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        available = false
        stopQuery()
    }

    private fun startQuery() {
        if (running.get() && available) {
            view?.postOnAnimation(this)
        }
    }

    private fun stopQuery() {
        view?.removeCallbacks(this)
    }

    override fun run() {
        if (running.get()) {
            listener?.onPositionChange(player.currentPosition, player.totalLength,
                player.currentBufferedPercentage)
            startQuery()
        }
    }
}