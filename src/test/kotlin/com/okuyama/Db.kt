package com.okuyama

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Paths
import java.sql.DriverManager
import java.util.*

object User : Table("myappDb.users") {
    val user_id: Column<UUID> = uuid("user_id")
    val display_name: Column<String> = varchar("display_name", 255)
    val username: Column<String> = varchar("username", 255)
    var email: Column<String> = varchar("email", 255)

}

object Todo : Table("myappDb.todos") {
    val todo_id: Column<UUID> = uuid("todo_id")
    val user_id: Column<UUID>  = uuid("user_id")
    val title: Column<String> = varchar("title", 255)
    val description: Column<String> = varchar("description", 255)
}

object Db {
    init {
        config.database.myappDb.jdbcUrl.toString()
            .let {
                Database.connect(it, "org.postgresql.Driver", "postgres", "postgres")
            }
    }

    private fun getFile(path: String): File? =
        Thread.currentThread().contextClassLoader.getResource(path)
            ?.let { Paths.get(it.toURI()).toFile() }

    fun setup(path: String) {
        getFile("$path/db/user.csv")
            ?.let { file ->
                transaction {
                    csvReader().readAllWithHeader(file).forEach { row ->
                        User.insert {
                            it[user_id] = UUID.fromString(row.get("user_id"))
                            it[display_name] = row.get("display_name").toString()
                            it[username] = row.get("username").toString()
                            it[email] = row.get("email").toString()
                        }
                    }
                }
            }
        getFile("$path/db/todo.csv")
            ?.let { file ->
                transaction {
                    csvReader().readAllWithHeader(file).forEach { row ->
                        Todo.insert {
                            it[todo_id] = UUID.fromString(row.get("chat_id"))
                            it[user_id] = UUID.fromString(row.get("user_id"))
                            it[title] = row.get("title").toString()
                            it[description] = row.get("description").toString()

                            println("row.get(\"description\").toString()")
                            println(row.get("description").toString())
                        }
                    }
                }
            }
    }

    fun reset() {
        config.database.myappDb.jdbcUrl.toString()
            .let { DriverManager.getConnection(it, "postgres", "postgres") }
            .createStatement()
            .run {
                this.addBatch("delete from myappDb.todos;")
                this.addBatch("delete from myappDb.users;")
                this.addBatch("delete from myappDb.ads;")
                this.executeBatch()

                // executeBatch は、Java の java.sql.Statement クラスにあるメソッドで、addBatch で追加した SQL コマンドのグループを一度に実行するために使用されます。
                // 複数の削除、更新、挿入処理をまとめて実行して、データベースへの負荷を軽減できるため、パフォーマンス向上に役立ちます。
            }
    }
}

//　Kotlinの object は「シングルトンオブジェクト」を定義するためのキーワードです。クラスのインスタンスを1つだけ作りたい場合に使います。objectで定義したものは、どこからでも同じインスタンスにアクセスできます。
//　init は「初期化ブロック」です。objectやclassが初めて使われるときに一度だけ実行されます。この例では、initブロック内でDatabase.connect(...)を呼び出して、データベース接続の初期化をしています。