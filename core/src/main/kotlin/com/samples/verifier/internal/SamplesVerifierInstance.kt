package com.samples.verifier.internal

import com.samples.verifier.*
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.cloneRepository
import com.samples.verifier.internal.utils.processFile
import com.samples.verifier.model.ExecutionResult
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class SamplesVerifierInstance(compilerUrl: String, kotlinEnv: KotlinEnv) : SamplesVerifier {
    private val logger = LoggerFactory.getLogger("Samples Verifier")
    private val executionHelper = ExecutionHelper(compilerUrl, kotlinEnv)

    override fun collect(
        url: String,
        branch: String,
        attributes: List<String>,
        type: FileType
    ): Map<Code, ExecutionResult> = processRepository(url, branch, attributes, type)
        .flatten()
        .associateWith { executionHelper.executeCode(it) }

    override fun check(url: String, branch: String, attributes: List<String>, type: FileType) {
        val snippets = processRepository(url, branch, attributes, type)
        for (code in snippets.flatten()) {
            val result = executionHelper.executeCode(code)
            val errors = result.errors
            logger.info("Code: \n${code}")
            if (errors.isNotEmpty()) {
                logger.info("Errors: \n${errors.joinToString("\n")}")
            }
            result.exception?.let { logger.info("Exception: \n${it.message}") }
                ?: logger.info("Output: \n${result.text}")
        }
    }

    override fun <T> parse(
        url: String,
        branch: String,
        attributes: List<String>,
        type: FileType,
        processResult: (Code) -> T
    ): Map<Code, T> {
        val snippets = processRepository(url, branch, attributes, type)
        return snippets.flatMap { lst -> lst.map { it to processResult(it) } }.toMap()
    }

    override fun <T> parse(
        url: String,
        branch: String,
        attributes: List<String>,
        type: FileType,
        processResult: (List<List<Code>>) -> T
    ): T {
        val snippets = processRepository(url, branch, attributes, type)
        return processResult(snippets)
    }

    private fun processRepository(
        url: String,
        branch: String,
        attributes: List<String>,
        type: FileType
    ): List<List<Code>> {
        val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))
        return try {
            logger.info("Cloning repository...")
            cloneRepository(dir, url, branch)
            return processFiles(dir, attributes, type)
        } catch (e: GitException) {
            logger.error("${e.message}")
            emptyList()
        } catch (e: IOException) {
            logger.error("${e.message}")
            emptyList()
        } finally {
            if (dir.isDirectory) {
                FileUtils.deleteDirectory(dir)
            } else {
                dir.delete()
            }
        }
    }

    private fun processFiles(
        directory: File,
        attributes: List<String>,
        type: FileType
    ): List<List<Code>> {
        val snippets = mutableListOf<List<Code>>()
        Files.walk(directory.toPath()).use {
            it.forEach { path: Path ->
                val file = path.toFile()
                val fileSnippets = when (type) {
                    FileType.MD -> {
                        if (file.extension == "md") {
                            logger.info("Processing ${file}...")
                            processFile(file, type, attributes)
                        } else emptyList()
                    }
                    FileType.HTML -> {
                        if (file.extension == "html") {
                            logger.info("Processing ${file}...")
                            processFile(file, type, attributes)
                        } else emptyList()
                    }
                }
                if (fileSnippets.isNotEmpty()) snippets.add(fileSnippets)
            }
        }
        return snippets
    }
}