package com.okuyama

import com.thoughtworks.gauge.BeforeSuite
import com.okuyama.config
import com.uzabase.playtest2.core.config.Configuration.Companion.playtest2
import com.uzabase.playtest2.http.config.http

class ExectionHooks {

    @BeforeSuite
    fun beforeSuite() {
        println("=== @BeforeSuite が実行されました ===")
        println("テストスイート開始前の初期化処理を実行中...")
        // ここに何か書く
        playtest2 {
            listOf(
                http(config.rest.baseUrl.toURL())
            )
        }
        println("=== @BeforeSuite 処理完了 ===")
    }
}