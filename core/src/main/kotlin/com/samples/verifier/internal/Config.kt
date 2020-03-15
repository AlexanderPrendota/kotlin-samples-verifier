package com.samples.verifier.internal

import org.eclipse.jgit.transport.URIish

class Config(var sourceDir: String) {
    var flags: List<String> = emptyList()
    var repositoryURL: URIish? = null
    var targetDir: String = "${sourceDir}_snippets"
    val baseUrl: String = "http://localhost:8080/"

    constructor(repositoryURL: URIish)
            : this(repositoryURL.humanishName) {
        this.repositoryURL = repositoryURL
    }
}

fun setConfiguration(repositoryURL: URIish, block: Config.() -> Unit): Config {
    val config = Config(repositoryURL)
    config.block()
    return config
}

fun setConfiguration(sourceDir: String, block: Config.() -> Unit): Config {
    val config = Config(sourceDir)
    config.block()
    return config
}