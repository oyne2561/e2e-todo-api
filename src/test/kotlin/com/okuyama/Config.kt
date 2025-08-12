package com.okuyama

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import java.net.URI

val config = ConfigLoaderBuilder.default()
    .addResourceSource("/uat.yaml")
    .build()
    .loadConfigOrThrow<Config>()

data class Config(
    val rest: Rest
)

data class Rest(
    val companyApi: Endpoint,
    val baseUrl: URI
)

data class Endpoint(
    val baseUrl: URI
)