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

    override fun collect(url: String, attributes: List<String>, type: FileType): Map<Code, ExecutionResult> {
        val results = hashMapOf<Code, ExecutionResult>()
        processRepository(url, attributes, type) {
            it.map { code ->
                val result = executionHelper.executeCode(code)
                results[code] = result
            }
        }
        return results
    }

    override fun check(url: String, attributes: List<String>, type: FileType) {
        processRepository(url, attributes, type) { snippets ->
            snippets.map { code ->
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
    }

    override fun <T> parse(
        url: String,
        attributes: List<String>,
        type: FileType,
        processResult: (Code) -> T
    ): Map<Code, T> {
        val results = hashMapOf<Code, T>()
        processRepository(url, attributes, type) {
            it.map { code ->
                results[code] = processResult(code)
            }
        }
        return results
    }

    override fun <T> parse(
        url: String,
        attributes: List<String>,
        type: FileType,
        processResult: (List<List<Code>>) -> T
    ): T {
        val codeSnippets = mutableListOf<List<Code>>()
        processRepository(url, attributes, type) { snippets ->
            codeSnippets.add(snippets)
        }
        return processResult(codeSnippets)
    }

    private fun processRepository(
        url: String,
        attributes: List<String>,
        type: FileType,
        processResult: (List<Code>) -> Unit
    ) {
        val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))
        try {
            logger.info("Cloning repository...")
            cloneRepository(dir, url)
            processFiles(dir, attributes, type, processResult)
        } catch (e: GitException) {
            //TODO
            logger.error("${e.message}")
        } catch (e: IOException) {
            //TODO
            logger.error("${e.message}")
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
        type: FileType,
        processResult: (List<Code>) -> Unit
    ) {
        Files.walk(directory.toPath()).use {
            it.forEach { path: Path ->
                val file = path.toFile()
                when (type) {
                    FileType.MD -> {
                        if (file.extension == "md") {
                            logger.info("Processing ${file}...")
                            processFile(file, type, attributes, processResult)
                        }
                    }
                    FileType.HTML -> {
                        if (file.extension == "html") {
                            logger.info("Processing ${file}...")
                            processFile(file, type, attributes, processResult)
                        }
                    }
                }
            }
        }
    }
}