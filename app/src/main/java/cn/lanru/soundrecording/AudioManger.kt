package cn.lanru.soundrecording

import android.media.MediaRecorder
import java.io.File
import java.io.IOException

/**
 * @ClassName: AudioManger
 * @Description: 录音工具
 * @Author: zengqiang
 * @github: https://github.com/zqMyself
 * @Date: 2021-05-19 11:04
 */
open class AudioManger {

    private var mMediaRecorder: MediaRecorder? = null
    private var mDir: String? = null
    private var mCurrentFilePath: String? = null


    private var isPrepared = false

    //Recorder准备好录音后的回调
    interface AudioStateListener {
        fun wellPrepared()
    }


    var mListener: AudioStateListener? = null

    fun setOnAudioStateListener(Listener: AudioStateListener?) {
        mListener = Listener
    }

    //构造器 参数:  录音文件夹
    constructor(dir: String) {

        mDir = dir
    }

    companion object{
        private var mInstance: AudioManger? = null
        //单例模式
        fun getInstance(dir: String): AudioManger? {
            if (mInstance == null) {
                synchronized(AudioManger::class.java) {
                    if (mInstance == null) {
                        mInstance = AudioManger(dir)
                    }
                }

            }
            return mInstance
        }

    }



    //准备录音
    fun prepareAudio() {
        try {
            isPrepared = false
            //在录音目录下建立录音文件
            val dir = File(mDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = generateFileName()
            val file = File(dir, fileName)
            mCurrentFilePath = file.absolutePath
            mMediaRecorder = MediaRecorder()
            //设置输出文件
            mMediaRecorder!!.setOutputFile(mCurrentFilePath)
            //设置音频源为麦克风
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            //设置音频格式api>=10使用amr_nb 小于10使用raw_amr
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            //设置音频的编码为amr
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            //准备录制
            mMediaRecorder!!.prepare()

            //准备好录制
            isPrepared = true
            if (mListener != null) {
                mListener!!.wellPrepared()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun start() {
        if (isPrepared) {
            //开始录制
            mMediaRecorder!!.start()
        }
    }

    //随机生成文件名称
    private fun generateFileName(): String {
        return System.currentTimeMillis().toString() + ".amr"
    }

    //获取音量大小  maxLevel为最大等级
    fun getVoiceLevel(maxLevel: Int): Int {
        if (isPrepared) {
            try {
                //mMediaRecorder.getMaxAmplitude() 1-32767
                return maxLevel * mMediaRecorder!!.maxAmplitude / 32768 + 1
            } catch (e: Exception) {
            }
        }
        return 1
    }

    fun release() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder!!.stop()
                mMediaRecorder!!.release()
                mMediaRecorder = null
                isPrepared = false
            }
        }catch (e:java.lang.Exception){

        }

    }

    fun cancel() {
        release()
        //如果取消就删除生成的录音文件
        if (mCurrentFilePath != null) {
            val file = File(mCurrentFilePath)
            file.delete()
            mCurrentFilePath = null
        }
    }

    fun getCurrentFilePath(): String? {
        return mCurrentFilePath
    }
}