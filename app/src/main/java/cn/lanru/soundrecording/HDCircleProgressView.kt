package cn.lanru.soundrecording

import android.app.Service
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View


/**
 * @ClassName: HDCircleProgressView
 * @Description: java类作用描述
 * @Author: zengqiang
 * @github: https://github.com/zqMyself
 * @Date: 2021-05-19 15:01
 */
class HDCircleProgressView : View ,View.OnClickListener, AudioManger.AudioStateListener {
    private var mMaxProgress = 90 * 10f //90个100ms
    private var mProgress = 0f
    private val mCircleLineStrokeWidth = 3.toFloat()
    private val mRectF: RectF
    private val mPaint: Paint
    private val mPaintHas: Paint
    private var hasMax = 0f
    private val colorBg = "#00000000"
    private val colorHas = "#ffffff"
    private val colorCurtProgress = "#00000000"
    val dir: String = context.cacheDir.toString() + "/audios"
    var isFinish = false
    var isPlayer = false
    var total =  90 * 10f
    var mOnProgressTouchListener : onProgressTouchListener?=null
    var audioManger: AudioManger?=null
    var state = 0
    interface onProgressTouchListener{
        fun start()
        fun finish(total:Float,path:String)
        fun less() //小于三秒钟，提示
    }

    fun setOnProgressTouchListener(mOnProgressTouchListener : onProgressTouchListener){
        this.mOnProgressTouchListener = mOnProgressTouchListener
    }
    constructor(context: Context?) : super(context, null) {
        mRectF = RectF()
        mPaint = Paint()
        mPaintHas = Paint()
        setOnClickListener(this)
        audioManger = AudioManger.getInstance(dir)
        audioManger!!.setOnAudioStateListener(this)

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs, 0) {
        mRectF = RectF()
        mPaint = Paint()
        mPaintHas = Paint()
        setOnClickListener(this)
        audioManger = AudioManger.getInstance(dir)
        audioManger!!.setOnAudioStateListener(this)

    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mRectF = RectF()
        mPaint = Paint()
        mPaintHas = Paint()
        setOnClickListener(this)
        audioManger = AudioManger.getInstance(dir)
        audioManger!!.setOnAudioStateListener(this)


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (state ==1) {
            var width = this.width
            var height = this.height
            if (width != height) {
                val min = Math.min(width, height)
                width = min
                height = min
            }
            canvas.drawColor(Color.TRANSPARENT)

            //设置  当前进度画笔
            mPaint.setAntiAlias(true)
            mPaint.setColor(Color.parseColor(colorBg))
            mPaint.setStrokeWidth(mCircleLineStrokeWidth)
            mPaint.setStyle(Paint.Style.STROKE)
            mPaintHas.setAntiAlias(true)
            mPaintHas.setColor(Color.parseColor(colorBg))
            mPaintHas.setStrokeWidth(mCircleLineStrokeWidth)
            mPaintHas.setStyle(Paint.Style.STROKE)

            //位置
            mRectF.left = mCircleLineStrokeWidth / 2.toFloat()
            mRectF.top = mCircleLineStrokeWidth / 2.toFloat()
            mRectF.right = width - mCircleLineStrokeWidth / 2.toFloat()
            mRectF.bottom = height - mCircleLineStrokeWidth / 2.toFloat()

            //绘制圆圈，进度条背景
            //todo 绘制整个圆圈灰色背景
//        canvas.drawArc(mRectF, (-90).toFloat(), 360.toFloat(), false, mPaint)

            //todo 绘制进度背景
            mPaint.color = Color.parseColor(colorCurtProgress)
            mPaintHas.color = Color.parseColor(colorHas)

            //绘制当前进度1
            if (hasMax <= mProgress) {
                canvas.drawArc(mRectF, (-90).toFloat(), mProgress.toFloat() / mMaxProgress * 360, false, mPaintHas)
                hasMax = mProgress
            } else {
                canvas.drawArc(mRectF, (-90).toFloat(), hasMax.toFloat() / mMaxProgress * 360, false, mPaintHas)
            }

            //绘制当前进度2
//        canvas.drawArc(mRectF, (-90).toFloat(), mProgress.toFloat() / mMaxProgress * 360, false, mPaint)
            canvas.save()

            //画布移动到中心点
            canvas.translate(width / 2.toFloat(), height / 2.toFloat())
            mPaint.color = Color.parseColor(colorCurtProgress)
            //计算圆的位置
            val radius = width / 2 - mCircleLineStrokeWidth / 2.toFloat() //绘制圆的半径等于矩形的宽度 - 线的一半宽度
            val angle = 1.0f * mProgress / mMaxProgress * Math.PI * 2
            //当前的进度/总进度*360=当前的角度   当前的进度/总进度*2PI=当前的弧度
            val circleX = (radius * Math.cos(angle - Math.PI / 2)).toFloat()
            val circleY = (radius * Math.sin(angle - Math.PI / 2)).toFloat() //从-90°开始计算角度
            //计算圆的半径
            val circleRadius = mCircleLineStrokeWidth / 2.toFloat()
            val angle2 = 1.0f * hasMax / mMaxProgress * Math.PI * 2
            //当前的进度/总进度*360=当前的角度   当前的进度/总进度*2PI=当前的弧度
            val circleY2 = (radius * Math.sin(angle2 - Math.PI / 2)).toFloat() //从-90°开始计算角度
            val circleX2 = (radius * Math.cos(angle2 - Math.PI / 2)).toFloat()

            //绘制末点的圆
            mPaintHas.style = Paint.Style.FILL
            canvas.drawCircle(circleX2, circleY2, circleRadius, mPaintHas)
            mPaint.style = Paint.Style.FILL
            //绘制末点的圆
            canvas.drawCircle(circleX, circleY, circleRadius, mPaint)
            //绘制初始点的圆
            canvas.drawCircle(0.toFloat(), -radius, circleRadius, mPaint)
            canvas.restore()
        }
    }

    fun setProgressNotInUiThread(progress: Float) {
        mProgress = progress
        this.postInvalidate()
    }

    fun getmMaxProgress(): Float {
        return mMaxProgress
    }

    fun setmMaxProgress(mMaxProgress: Float) {
        this.mMaxProgress = mMaxProgress
    }

    fun getmProgress(): Float {
        return mProgress
    }

    fun setmProgress(mProgress: Float) {
        this.mProgress = mProgress


    }
    fun sendMessageDelayed(){
        if (handlers !=null) {
            handlers.sendEmptyMessageDelayed(1, 100)
        }
    }

    fun clear() {
        MediaManager.getInstance().pause()
        MediaManager.getInstance().isFinish =false
        isFinish =false
        hasMax = 0f
        state = 0
        mProgress = 0f
        this.invalidate()
    }
    var startTime = 0L
    var endTime = 0L

    var handlers = Handler(object :Handler.Callback{


        override fun handleMessage(p0: Message): Boolean {
            when(p0!!.what){
                1->{
                    if (getmProgress() + 1 >= 600) {
                        if (mOnProgressTouchListener !=null){
                            if (audioManger!=null){
                                audioManger!!.release()
                            }
                            setBackgroundResource(R.mipmap.ic_sound_end)
                            isFinish = true
                            total= getmProgress() * 100

                            mOnProgressTouchListener!!.finish(total,audioManger!!.getCurrentFilePath()!!)
                            play(audioManger!!.getCurrentFilePath()!!)

                        }

                    } else if (state == 1) {
                        setProgressNotInUiThread(getmProgress() + 1)
                        sendMessageDelayed()
                    }
                }
            }
            return true
        }

    })

    override fun onClick(v: View?) {

        if (isPlayer) //正在播放
            return

        if (isFinish){ //播放完成
            play(audioManger!!.getCurrentFilePath()!!)
            return
        }
        when(state){
            MotionEvent.ACTION_DOWN->{

                val vb = context. getSystemService(Service.VIBRATOR_SERVICE) as Vibrator?
                vb!!.vibrate(longArrayOf(50, 100), -1)
                setmMaxProgress(600f)
                state = MotionEvent.ACTION_MOVE
                if (audioManger!=null ){
                    audioManger!!.prepareAudio()
                }

            }
            MotionEvent.ACTION_UP->{
                handlers.removeMessages(1)
                handlers.sendEmptyMessage(2)
                setBackgroundResource(R.mipmap.ic_sound_end)

                if (mOnProgressTouchListener !=null){
                    if (getmProgress() * 100 < 3000) {
                        clear()
                        if (audioManger!=null){
                            audioManger!!.cancel()
                        }
                        state = 0
                        mOnProgressTouchListener!!.less()
                        setBackgroundResource(R.mipmap.ic_sound_ready)
                    }else if (!isFinish){
                        if (audioManger!=null){
                            audioManger!!.release()
                        }
                        total= getmProgress() * 100
                        isFinish = true
                        mOnProgressTouchListener!!.finish(total ,audioManger!!.getCurrentFilePath()!!)
                        play(audioManger!!.getCurrentFilePath()!!)
                    }
                }

            }
            MotionEvent.ACTION_MOVE->{
                if (mOnProgressTouchListener !=null){
                    if (getmProgress() * 100 < 3000) {
                        clear()
                        if (audioManger!=null){
                            audioManger!!.cancel()
                        }
                        state =  MotionEvent.ACTION_DOWN
                        mOnProgressTouchListener!!.less()
                        setBackgroundResource(R.mipmap.ic_sound_ready)
                    }else if (!isFinish){

                        handlers.removeMessages(1)
                        handlers.sendEmptyMessage(2)
                        handlers.sendEmptyMessage(3)

                        setBackgroundResource(R.mipmap.ic_sound_end)

                        total= getmProgress() * 100
                        isFinish = true
                        mOnProgressTouchListener!!.finish(total ,audioManger!!.getCurrentFilePath()!!)
                        play(audioManger!!.getCurrentFilePath()!!)

                        if (audioManger!=null){
                            audioManger!!.release()
                        }
                    }
                }
//                setProgressNotInUiThread((System.currentTimeMillis() - startTime / 100).toFloat() )
//                handlers.removeMessages(1)
//                handlers.sendEmptyMessage(2)
//                setBackgroundResource(R.mipmap.ic_sound_end)
            }

        }

    }
//    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//
//
//
//        return true
//    }

    override fun wellPrepared() {

        if (audioManger!=null){
            audioManger!!.start()
            startTime = System.currentTimeMillis()
            setBackgroundResource(R.mipmap.ic_sound_start)
            handlers.sendEmptyMessageDelayed(1,100)
            state =  MotionEvent.ACTION_UP
            if(mOnProgressTouchListener  != null){
                mOnProgressTouchListener!!.start()
            }
        }
    }



    fun play(path:String){
        setmMaxProgress(total)
        hasMax = 0f
        isPlayer = true
        setProgressNotInUiThread(0f)
        MediaManager.getInstance().isFinish = false
        MediaManager.getInstance().playSound(path,object : MediaManager.onPlayerListener {

            override fun updateSeekBar(duration: Int, currentPosition: Int) {
                setProgressNotInUiThread(currentPosition.toFloat())
                if (currentPosition >= duration){
                    MediaManager.getInstance().release()
                    isPlayer =false
                    MediaManager.getInstance().isFinish = true
                    setProgressNotInUiThread(total)
                }
            }

            override fun onCompletion(mp: MediaPlayer?) {
                if (!mp!!.isPlaying) {
                    MediaManager.getInstance().isFinish = true
                    isPlayer =false
                    setProgressNotInUiThread(total)
                }
                Log.e("TAG","isPlaying  ="+mp!!.isPlaying)
            }

        })

    }


}
