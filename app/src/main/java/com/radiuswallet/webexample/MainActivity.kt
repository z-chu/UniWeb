package com.radiuswallet.webexample

import android.os.Bundle
import android.view.Menu
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.radiuswallet.uniweb.UniWebActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnTest2).setOnClickListener {
            UniWebActivity.start(this, "file:///android_asset/jsbridge/btnActionDemo.html", "默认标题")
        }

        findViewById<Button>(R.id.btnTest3).setOnClickListener {
            UniWebActivity.start(
                this,
                "https://www.youku.com/",
                "测试视频全屏播放"
            )
        }

        findViewById<Button>(R.id.btnTest4).setOnClickListener {
            UniWebActivity.start(
                this,
                "https://www.runoob.com/try/try.php?filename=tryjs_alert"
            )
        }
        findViewById<Button>(R.id.btnTest5).setOnClickListener {
            UniWebActivity.start(
                this,
                "https://www.runoob.com/try/try.php?filename=tryjs_confirm"
            )
        }
        findViewById<Button>(R.id.btnTest6).setOnClickListener {
            UniWebActivity.start(
                this,
                "https://www.runoob.com/try/try.php?filename=tryjs_prompt"
            )
        }
        findViewById<Button>(R.id.btnTest7).setOnClickListener {
            UniWebActivity.start(
                this,
                "https://github.com/z-chu/RxCache"
            )
        }
        findViewById<Button>(R.id.btnTest8).setOnClickListener {
            UniWebActivity.start(
                this,
                "https://github.com/z-chu/",
                "传入标题：柱子哥你好"
            )
        }
        findViewById<Button>(R.id.btnTest9).setOnClickListener {
            UniWebActivity.start(
                this,
                "file:///android_asset/jsbridge/btnActionDemo.html",
                "测试单独 webModule",
                "separate"
            )
        }
        findViewById<Button>(R.id.btnTest10).setOnClickListener {
            UniWebActivity.start(
                this,
                "file:///android_asset/jsbridge/btnActionDemo.html",
                isShowToolbar = false
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }


}