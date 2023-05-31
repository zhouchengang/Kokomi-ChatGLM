package com.zhouchengang.chatglmsample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar


open class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        ImmersionBar.with(this).transparentStatusBar()
            .statusBarDarkFont(true)
            .fitsSystemWindows(false).init()
    }
}
