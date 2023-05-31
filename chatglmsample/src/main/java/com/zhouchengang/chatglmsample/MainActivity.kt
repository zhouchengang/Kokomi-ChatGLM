package com.zhouchengang.chatglmsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.et_input
import kotlinx.android.synthetic.main.activity_main.iv_setting
import kotlinx.android.synthetic.main.activity_main.iv_trash
import kotlinx.android.synthetic.main.activity_main.rv_chat
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity() {
    companion object {
        private const val TAG = "MainActivity_LOG"
        public const val URL = "http://ai.fuyann.cn:7860/"

        fun launch(context: Context, restart: Boolean = true) {
            var intent = Intent(context, MainActivity::class.java)
            intent.putExtra("RESTART", restart)
            context.startActivity(intent)
        }

        fun getTopP(): Float {
            return SharedManager.get(SharedManager.MMKV_TOP_P, 0.7f)
        }

        fun getTemp(): Float {
            return SharedManager.get(SharedManager.MMKV_TEMP, 0.9f)
        }

        fun getMax(): Int {
            return SharedManager.get(SharedManager.MMKV_MAX, 1800)
        }
    }

    var adapter = ChatAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv_chat.layoutManager = LinearLayoutManager(this)
        rv_chat.adapter = adapter

        et_input.imeOptions = EditorInfo.IME_ACTION_SEND
        et_input.inputType = TYPE_CLASS_TEXT

        et_input.setOnEditorActionListener { v, actionId, event ->
            Log.d(TAG, "onCreate: setOnEditorActionListener, actionId = $actionId")
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                chat(et_input.text.toString())
                et_input.setText("")
            }
            false
        }

        iv_setting.setOnClickListener {
            SettingActivity.launch(this)
        }

        iv_trash.setOnClickListener {
            launch(this)
        }

        chat(
            "在之后的对话里面无论使用什么语言你都叫“珊瑚宫心海的锦囊”。请你有礼貌地自我介绍一下",
            true
        )
    }

    private fun chat(str: String, isGreeting: Boolean = false) {
        try {
            if (SharedManager.get(SharedManager.MMKV_USE_STREAM, true)) {
                streamChat(str, isGreeting)
            } else {
                chatApi(str, isGreeting)
            }
        } catch (e: Exception) {
            SettingActivity.launch(this)
        }
    }


    var history: ArrayList<ArrayList<String?>?>? = null

    private fun chatApi(str: String, isGreeting: Boolean = false) {

        var client = OkHttpClient.Builder().connectTimeout(160, TimeUnit.SECONDS)
            .readTimeout(160, TimeUnit.SECONDS).writeTimeout(160, TimeUnit.SECONDS).build()


        val re: Retrofit = Retrofit.Builder().client(client)
            .baseUrl(SharedManager.get(SharedManager.MMKV_URL, URL))
            .addConverterFactory(GsonConverterFactory.create()).build()
        val getService = re.create(GetService::class.java)

        Log.d(TAG, "getRemotePicList: history = $history")
        Log.d(
            TAG, "getRemotePicList: transHistoryRequest = ${BaseBean.transHistoryRequest(history)}"
        )

        var postData: String =
            "{\"prompt\": \"$str" + "\", \"history\":" + BaseBean.transHistoryRequest(history) + ", \"top_p\":" + getTopP() + ", \"temperature\":" + getTemp() + ", \"max_length\":" + getMax() + "}"
        var requestBody = RequestBody.create(MediaType.parse("application/json"), postData)
        addMeMessage(str, isGreeting)
        addLoading()
        getService.chat(requestBody).enqueue(object : Callback<BaseBean> {
            override fun onResponse(
                call: Call<BaseBean>, response: Response<BaseBean>
            ) {
                if (!response.isSuccessful) {
                    onFailure(call, Throwable("response.isNotSuccessful"))
                    return
                }
                removeLastLoading()
                response.body()?.let {
                    Log.d(TAG, "response = ${it.response}")
                    Log.d(TAG, "history  = ${it.history}")
                    Log.d(TAG, "transHistoryRequest  = ${BaseBean.transHistoryRequest(it.history)}")
                    history = it.history
                    adapter.addData(ChatListData(ChatListData.MOD_BOT, "" + it.response))
                }
            }

            override fun onFailure(call: Call<BaseBean>, t: Throwable) {
                Log.d(TAG, "onFailure: " + t)
                removeLastLoading()
                adapter.addData(ChatListData(ChatListData.MOD_BOT, "FAIL"))
            }

        })

    }


    private fun addLoading() {
        runOnUiThread {
            et_input.isEnabled = false
            adapter.addData(ChatListData(ChatListData.MOD_BOT, "...", true))
        }
    }

    private fun addMeMessage(str: String, isGreeting: Boolean) {
        runOnUiThread {
            if (!isGreeting) adapter.addData(ChatListData(ChatListData.MOD_ME, str))
        }
    }

    private fun removeLastLoading() {
        runOnUiThread {
            et_input.isEnabled = true
            adapter.data.last()?.let {
                Log.d(TAG, "removeLastLoading: it=$it")
                if (it.isLoad) {
                    adapter.removeAt(adapter.data.size - 1)
                }
            }
        }
    }


    private fun editLoading(message: String) {
        runOnUiThread {
            if (adapter.data.isNotEmpty()) adapter.data.last().let {
                Log.d(TAG, "removeLastLoading: it=$it")
                if (it.isLoad) {
                    it.message = message
                    adapter.notifyItemChanged(adapter.data.size - 1,
                        ArrayList<Any>().apply { add("STREAM") })
                }
            }
        }

    }

    private fun finishLoading() {
        runOnUiThread {
            if (adapter.data.isNotEmpty()) adapter.data.last().let {
                Log.d(TAG, "removeLastLoading: it=$it")
                if (it.isLoad) {
                    it.isLoad = false
                    adapter.notifyItemChanged(adapter.data.size - 1,
                        ArrayList<Any>().apply { add("STREAM") })
                }
            }
            removeLastLoading()
        }
    }


    private fun streamChat(str: String, isGreeting: Boolean = false) {
        Thread {
            addMeMessage(str, isGreeting)
            addLoading()
            Log.d(TAG, "streamChat Start ")
            val okHttpClient = OkHttpClient.Builder().readTimeout(500, TimeUnit.SECONDS)
                .writeTimeout(500, TimeUnit.SECONDS).connectTimeout(500, TimeUnit.SECONDS).build()
            val requestBuilder = Request.Builder()

            // 构建url
            val urlBuilder = HttpUrl.parse(
                SharedManager.get(
                    SharedManager.MMKV_URL, URL
                )
            )?.newBuilder()?.addEncodedPathSegments("")
            urlBuilder?.build()?.let { requestBuilder.url(it) }

            var postData: String =
                "{\"prompt\": \"$str" + "\", \"history\":" + BaseBean.transHistoryRequest(history) + ", \"top_p\":" + getTopP() + ", \"temperature\":" + getTemp() + ", \"max_length\":" + getMax() + ", \"stream\":1" + "}";
            var requestBody =
                RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), postData)

            var historyStream: ArrayList<ArrayList<String?>?>? = null

            requestBuilder.post(requestBody)
            try {
                val response = okHttpClient.newCall(requestBuilder.build()).execute()
                val responseBody = response.body()
                if (null != responseBody) {
                    try {
                        responseBody.byteStream().use { inputStream ->
                            val buffer = ByteArray(1024)
                            var len = 0
                            var strTotal = ""
                            while (inputStream.read(buffer).also { len = it } != -1) {
                                var str = String(
                                    buffer, 0, len, Charset.defaultCharset()
                                )
                                if (str != null && str.startsWith("data:")) {
                                    str = str.substring(5);
                                    strTotal = ""
                                }
                                strTotal += str
                                Log.d(TAG, "streamChat: strTotal=" + strTotal)
                                try {
                                    var bean =
                                        Gson().fromJson<BaseBean>(strTotal, BaseBean::class.java)
                                    Log.d(TAG, "streamChat: Gson" + bean.response)
                                    historyStream = bean.history
                                    editLoading(bean.response ?: "")
                                } catch (e: Exception) {

                                }
                            }
                        }
                    } catch (ioException: IOException) {
                        Log.i(TAG, "streamChat responseBody 2")
                        ioException.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                Log.i(TAG, "streamChat responseBody 3")
                e.printStackTrace()
            }
            Log.i(TAG, "streamChat End ")
            finishLoading()
            Log.d(TAG, "streamChat: historyStream" + historyStream)
            history = historyStream
        }.start()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        var restart = intent.getBooleanExtra("RESTART", false)
        Log.d(TAG, "onNewIntent: s = $restart")
        if (restart) {
            launch(this)
            finish()
        }
    }
}
