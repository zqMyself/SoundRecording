package cn.lanru.soundrecording

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.util.Log
import java.io.IOException


/**
 * @ClassName: MediaManager
 * @Description: 播放录音工具类
 * @Author: zengqiang
 * @github: https://github.com/zqMyself
 * @Date: 2021-05-19 10:57
 */
class MediaManager : MediaPlayer.OnSeekCompleteListener {
    private var isPause = false
     var isFinish = false
    private var mMediaPlayer: MediaPlayer? = null
    protected var mOnPlayerListener : onPlayerListener?=null
    companion object{
        private var mediaManager: MediaManager? = null

        fun getInstance(): MediaManager {
            if (mediaManager == null) {
                synchronized(MediaManager::class.java) {
                    if (mediaManager == null) {
                        mediaManager = MediaManager()
                    }
                }
            }
            return mediaManager!!

        }
    }

    interface onPlayerListener : MediaPlayer.OnCompletionListener{
        /**
         * @param duration 总时间长
         * @param currentPosition 当前市场
         */
        fun updateSeekBar(duration:Int,currentPosition:Int)
    }
    fun playSound(soundPath: String?, onCompletionListener: onPlayerListener?) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setOnErrorListener { _, what, _ ->
                mMediaPlayer!!.reset()
                Log.e("TAG","what = $what")

                false

            }
        } else {
            mMediaPlayer!!.reset()
        }
        this.mOnPlayerListener = onCompletionListener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setLegacyStreamType(AudioManager.STREAM_ALARM)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
        }else {
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        mMediaPlayer!!.setOnCompletionListener(onCompletionListener)
        mMediaPlayer!!.setOnSeekCompleteListener(this);

        try {
            mMediaPlayer!!.setDataSource(soundPath)
            mMediaPlayer!!.prepare()
            mMediaPlayer!!.start()

            updateSeekBar()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TAG","${e.message}")
        }
    }

    fun pause() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
            isPause = true
        }
    }

    fun resume() {
        if (mMediaPlayer != null && isPause) {
            mMediaPlayer!!.start()
            isPause = false
        }
    }

    fun release() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    fun stop() {
        isFinish =false
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer = null
        }
    }
    fun isPlaying(): Boolean {
        return mMediaPlayer != null && mMediaPlayer!!.isPlaying
    }


    /**
     * 更新SeekBar
     */
    fun updateSeekBar() {

        //获取总时长
        val duration = mMediaPlayer!!.duration
        var handle = Handler()
        //开启线程发送数据
        object : Thread() {
            override fun run() {

                while (!isFinish) {
                    try {
                        sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    var currentPosition = 0

                    if (mMediaPlayer == null) {
                        currentPosition = duration
                    }else {
                        currentPosition = mMediaPlayer!!.currentPosition
                    }

                    handle.post(Runnable {
                        mOnPlayerListener?.updateSeekBar(duration,currentPosition)
                    })
                }
            }
        }.start()
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        Log.e("TAG","${mp!!.currentPosition}")
        Log.e("TAG","${mp!!.duration}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("TAG","${mp!!.timestamp}")
        }

    }


}