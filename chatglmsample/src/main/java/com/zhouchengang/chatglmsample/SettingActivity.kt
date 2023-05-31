package com.zhouchengang.chatglmsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.android.material.slider.Slider
import com.zhouchengang.chatglmsample.WechatClickButton.OnMClickListener
import kotlinx.android.synthetic.main.activity_setting.click_button
import kotlinx.android.synthetic.main.activity_setting.et_domain
import kotlinx.android.synthetic.main.activity_setting.iv_return
import kotlinx.android.synthetic.main.activity_setting.slider_max
import kotlinx.android.synthetic.main.activity_setting.slider_temp
import kotlinx.android.synthetic.main.activity_setting.slider_top_p
import kotlinx.android.synthetic.main.activity_setting.tv_max_value
import kotlinx.android.synthetic.main.activity_setting.tv_save
import kotlinx.android.synthetic.main.activity_setting.tv_temp_value
import kotlinx.android.synthetic.main.activity_setting.tv_top_p_value


class SettingActivity : BaseActivity() {
    companion object {
        private const val TAG = "SettingActivity"
        fun launch(context: Context) {
            context.startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        iv_return.setOnClickListener {
            finish()
        }
        tv_save.setOnClickListener {
            var url = et_domain.text.toString()
            if (!url.isEmpty())
                SharedManager.save(SharedManager.MMKV_URL, et_domain.text.toString())

            SharedManager.save(
                SharedManager.MMKV_TOP_P,
                String.format("%.2f", slider_top_p.value).toFloat()
            )
            SharedManager.save(
                SharedManager.MMKV_TEMP,
                String.format("%.2f", slider_temp.value).toFloat()
            )
            SharedManager.save(
                SharedManager.MMKV_MAX,
                String.format("%.0f", slider_max.value).toFloat().toInt()
            )

            val intent_finish_all = Intent()
            intent_finish_all.setClass(this, MainActivity::class.java).flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent_finish_all.putExtra("RESTART", true)
            startActivity(intent_finish_all)
            finish()

        }

        click_button.setOnMbClickListener(OnMClickListener { isRight ->
            Log.d(TAG, "onCreate: setOnMbClickListener, isRight = $isRight")
            SharedManager.save(SharedManager.MMKV_USE_STREAM, isRight)
        })


        var isStreamEnable = SharedManager.get(SharedManager.MMKV_USE_STREAM, true)
        Log.d(TAG, "onCreate: isStreamEnable=$isStreamEnable")
        click_button.setDirection(isStreamEnable)


        et_domain.hint = SharedManager.get(SharedManager.MMKV_URL, MainActivity.URL)
        et_domain.setOnFocusChangeListener { v, hasFocus ->
            if (et_domain.text.isEmpty()) {
                et_domain.setText(
                    SharedManager.get(
                        SharedManager.MMKV_URL,
                        MainActivity.URL
                    )
                )
            }
        }

        et_domain.imeOptions = EditorInfo.IME_ACTION_DONE
        et_domain.inputType = InputType.TYPE_CLASS_TEXT
        et_domain.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Log.d(TAG, "IME_ACTION_DONE: " + et_domain.text)
                var imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0)
            }
            false
        }


        var top_p = MainActivity.getTopP()
        tv_top_p_value.text = String.format("%.2f", top_p)
        slider_top_p.addOnChangeListener(Slider.OnChangeListener { slider1: Slider?, value: Float, fromUser: Boolean ->
            tv_top_p_value.text = String.format("%.2f", value)
        })
        slider_top_p.value = String.format("%.2f", top_p).toFloat()


        var temperature = MainActivity.getTemp()
        tv_temp_value.text = String.format("%.2f", temperature)
        slider_temp.addOnChangeListener(Slider.OnChangeListener { slider1: Slider?, value: Float, fromUser: Boolean ->
            tv_temp_value.text = String.format("%.2f", value)
        })
        slider_temp.value = String.format("%.2f", temperature).toFloat()


        var max_length = MainActivity.getMax().toFloat()
        tv_max_value.text = String.format("%.0f", max_length)
        slider_max.addOnChangeListener(Slider.OnChangeListener { slider1: Slider?, value: Float, fromUser: Boolean ->
            tv_max_value.text = String.format("%.0f", value)
        })
        slider_max.value = String.format("%.0f", max_length).toFloat()

    }

}
