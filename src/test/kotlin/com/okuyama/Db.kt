package com.okuyama

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Paths

object Todo : Table("myappDb.todos") {
    val id: Column<Int> = integer("id").autoIncrement()
    val user_id: Column<Int> = integer("user_id")
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
        getFile("$path/db/todo.csv")
            ?.let { file ->
                transaction {
                    csvReader().readAllWithHeader(file).forEach { row ->
                        Todo.insert {
                            it[id] = row.get("id")?.toInt() ?: 1
                            it[user_id] = row.get("user_id")?.toInt() ?: 1
                            it[title] = row.get("title") ?: ""
                            it[description] = row.get("description") ?: ""
                        }
                    }
                }
            }
    }
}

//　Kotlinの object は「シングルトンオブジェクト」を定義するためのキーワードです。クラスのインスタンスを1つだけ作りたい場合に使います。objectで定義したものは、どこからでも同じインスタンスにアクセスできます。
//　init は「初期化ブロック」です。objectやclassが初めて使われるときに一度だけ実行されます。この例では、initブロック内でDatabase.connect(...)を呼び出して、データベース接続の初期化をしています。