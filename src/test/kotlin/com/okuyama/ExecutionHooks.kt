import com.github.tomakehurst.wiremock.client.WireMock
import com.thoughtworks.gauge.BeforeSuite
import com.thoughtworks.gauge.BeforeSpec
import com.thoughtworks.gauge.ExecutionContext
import com.okuyama.config
import com.uzabase.Db
import com.uzabase.playtest2.core.config.Configuration.Companion.playtest2
import com.uzabase.playtest2.http.config.http
import com.uzabase.playtest2.wiremock.config.wireMock
import java.nio.file.Paths

class ExectionHooks {

    @BeforeSuite
    fun beforeSuite() {
        println("=== @BeforeSuite が実行されました ===")
        println("テストスイート開始前の初期化処理を実行中...")
        // テストを開始する前に User API と Ads API のサーバーを WireMock を使ってモック化し、テストが外部サービスに依存せず、安定して実行できるように準備している
        playtest2 {
            listOf(
                http(config.rest.baseUrl.toURL()),
                wireMock("User API", config.rest.userApi.baseUrl.toURL()),
                wireMock("Ads API", config.rest.adsApi.baseUrl.toURL())
            )
        }
        println("=== @BeforeSuite 処理完了 ===")
    }

    @BeforeSpec
    fun setup(context: ExecutionContext) {
        println("=== @BeforeSpec が実行されました ===")
        val path = getContextPath(context)
        println("テスト仕様: $path のセットアップを実行中...")

        setupMocks(path)

        Db.reset()

        println("=== @BeforeSpec 処理完了 ===")
    }

    // 現在までのパスを削除して、.specも除去する。
    private fun getContextPath(context: ExecutionContext) =
        context.currentSpecification.fileName
            .replace("${Paths.get(".").toRealPath().toString()}/", "")
            .replace(".spec", "")

//    listOf は Kotlin の標準関数で、複数の要素からリスト（List）を作成します。
//    to はペア（Pair）を作るための中置関数で、A to B と書くと Pair(A, B) になります。
    private fun setupMocks(path: String) {
        listOf(
            UserApi to "user-api",
            AdsApi to "ads-api"
        ).forEach { (mock, apiPath) ->
            mock.resetRequests()
            mock.resetMappings()
            println("mock: $mock")
            println("APIパス: $apiPath")

            Thread.currentThread().contextClassLoader.getResource("${path}/$apiPath")
                ?.let { Paths.get(it.toURI()) }
                ?.run { mock.loadMappingsFrom(this.toFile())}
        }
    }

    // A.let { B(it) } は「AをitとしてBに渡し、Bの結果を返す」という意味です。
    val UserApi =
        config.rest.userApi.baseUrl
            .let { WireMock(it.host, it.port) }

    val AdsApi =
        config.rest.adsApi.baseUrl
            .let { WireMock(it.host, it.port) }

}