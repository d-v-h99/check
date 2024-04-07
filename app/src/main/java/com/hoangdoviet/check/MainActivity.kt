package com.hoangdoviet.check

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.hoangdoviet.check.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    lateinit var binding: ActivityMainBinding
    lateinit var player: SimpleExoPlayer
    var duartion: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = MyWebViewClient()
        webView.loadUrl("https://www.baogiaothong.vn/cuu-binh-gia-ke-ve-chien-dich-dien-bien-phu-192240406113556003.htm")
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)
        // player
        player = SimpleExoPlayer.Builder(this@MainActivity).build()
        player.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && player.playWhenReady) {
                    binding.playPauseButton.setImageDrawable(resources.getDrawable(R.drawable.pause))
                } else {
                    binding.playPauseButton.setImageDrawable(resources.getDrawable(R.drawable.play))
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                duartion = player.duration.toInt() / 1000
                binding.seekbar.max = duartion
                binding.time.text = "0:00 / " + gettimeString(duartion)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                var currentposition = player.currentPosition.toInt() / 1000
                binding.seekbar.progress = currentposition
                binding.time.setText(gettimeString(currentposition) + "/" + gettimeString(player.duration.toInt() / 1000))
            }

        })
        var mediaItem =
            MediaItem.fromUri("https://tts.mediacdn.vn/2024/04/06/baogiaothong-nu-192240406113556003.m4a")
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        binding.playPauseButton.setOnClickListener {
            player.playWhenReady = !player.playWhenReady
        }
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress.toLong() * 1000)
                    binding.time.text = gettimeString(progress) + "/" + gettimeString(duartion)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        var handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                var currentposition = player.currentPosition.toInt() / 1000
                binding.seekbar.progress = currentposition
                binding.time.setText(gettimeString(currentposition) + "/" + gettimeString(duartion))
                handler.postDelayed(this, 1000)
            }

        })

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (Player.STATE_BUFFERING == playbackState) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        })


    }

    fun extractNumber(str: String): String {
        var num = ""
        var i = str.length - 1
        while (i >= 0) {
            val char = str[i]
            if (char.isDigit()) {
                num = char + num
            } else if (char == '-') {
                break
            }
            i--
        }
        return num
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    fun gettimeString(duration: Int): String {
        var min = duration / 60
        var sec = duration % 60
        var time = String.format("%02d:%02d", min, sec)
        return time
    }

    private fun playAudio(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val num1 = url?.let { extractNumber(it) }
            Log.d("url", num1.toString())
            if (num1 != null) {
                if (num1.isNotEmpty())
                    webView.evaluateJavascript("(function() { return document.getElementById('streamid_${num1}_html5_api').src; })();") { result ->
                        Log.d("checkAudio", result)
                        if (result != null && result.isNotEmpty()) {
                            val url = result.replace("?", "")
                            Log.d("check", url)
                            //playAudio(url)
                        }
                    }
            }
        }
    }

}