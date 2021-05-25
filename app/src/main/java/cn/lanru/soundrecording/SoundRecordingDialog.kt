package cn.lanru.soundrecording

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_sound_recording.*


/**
 * @ClassName: PublishJobDialog
 * @Description: 录音对话框
 * @Author: zengqiang
 * @github: https://github.com/zqMyself
 * @Date: 2021-03-25 9:43
 */
class SoundRecordingDialog(context: Context) : Dialog(context, R.style.ActionSheetDialogStyle) {
    var mTotal = 0f
    var mPath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_sound_recording)
        iv_sound.setmMaxProgress(600f)
        iv_sound.setmProgress(0f)

        setCancelable(false)
        //设置Dialog从窗体底部弹出
        window!!.setGravity(Gravity.BOTTOM)
        //获得窗体的属性
        val lp = window!!.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.dimAmount = 0.0f

        //将属性设置给窗体
        window!!.attributes = lp
        tv_close.setOnClickListener {
            dismiss()
        }

        tv_confirm.setOnClickListener {
            dismiss()
        }

        iv_revision.setOnClickListener {
            iv_sound.setmMaxProgress(600f)
            iv_sound.clear()
            iv_sound.setBackgroundResource(R.mipmap.ic_sound_ready)
            iv_revision.visibility = View.GONE
            tv_time.text = "点击录制"
//            tv_time.visibility = View.GONE
            tv_time.setTextColor(context.resources.getColor(android.R.color.black))

            tv_confirm.visibility = View.GONE
        }

        tv_time.setOnClickListener {

        }

        iv_sound.setOnProgressTouchListener(object : HDCircleProgressView.onProgressTouchListener{
            override fun start() {
                tv_time.text= ""
            }

            override fun finish(total: Float,path:String) {
                mTotal = total
                mPath = path
                iv_revision.visibility = View.VISIBLE
                tv_confirm.visibility = View.VISIBLE
                tv_time.text =  String.format("%02d:%02d", (total / 1000 / 60).toInt() , ((total / 1000) % 60).toInt())
                tv_time.setTextColor(context.resources.getColor(android.R.color.white))
            }

            override fun less() {
                iv_revision.visibility = View.GONE

                Toast.makeText(context,"语音时长太短",Toast.LENGTH_LONG).show()
            }

        })
        iv_sound.setmMaxProgress(600f)
        iv_revision.visibility = View.GONE
        tv_confirm.visibility = View.GONE
//        tv_time.visibility = View.GONE
//        tv_time.text = ""
    }



    fun reset(){

    }
    override fun show() {

        super.show()
    }
    override fun dismiss() {
        iv_sound.setBackgroundResource(R.mipmap.ic_sound_ready)
        iv_sound.clear()
        iv_sound.setmMaxProgress(600f)
        iv_revision.visibility = View.GONE
        tv_confirm.visibility = View.GONE
        tv_time.visibility = View.GONE
        tv_time.text = ""
        super.dismiss()
    }



}