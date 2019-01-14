package me.xana.hlsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.xana.hlsdemo.video.VideoPlayer

class MainActivity : AppCompatActivity() {

    private val player by lazy { VideoPlayer(this) }
    private val uri = "http://7xlv47.com1.z0.glb.clouddn.com/xxx004.m3u8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        video_view.setOnClickListener {
            player.play(uri, video_view)
        }
    }
}
