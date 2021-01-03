package com.mhozza.scrabbler.service

import com.mhozza.scrabbler.Dictionary
import com.mhozza.scrabbler.Scrabbler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.nio.file.Paths

@SpringBootApplication
class ServiceApplication

fun main(args: Array<String>) {
    runApplication<ServiceApplication>(*args)
}

const val DICT_EXT = ".dic"
const val MAX_LIMIT = 200

@RestController
class ScrabblerController constructor(@Value("\${DICTIONARY_DIR}") private val dictLocation: String) {
    private val dicts = loadDictionaries()

    @GetMapping("/permutations")
    fun getPermutations(
        @RequestParam(value = "dict") dict: String,
        @RequestParam(value = "word") word: String,
        @RequestParam(value = "prefix") prefix: String?,
        @RequestParam(value = "use_all_letters", defaultValue = "true") useAllLetters: Boolean,
        @RequestParam(value = "limit", defaultValue = "100") limit: Int,
        @RequestParam(value = "wildcard") wildcard: Char?,
    ): List<String> {
        return getScrabblerForDictionary(dict).findPermutations(
            word = word,
            limit = minOf(limit, MAX_LIMIT),
            useAllLetters = useAllLetters,
            wildcard = wildcard,
            prefix = prefix
        )
    }

    @GetMapping("/regex")
    private fun filterRegex(
        @RequestParam(value = "dict") dict: String,
        @RequestParam(value = "word") word: String,
        @RequestParam(value = "limit", defaultValue = "100") limit: Int,
    ): List<String> {
        return getScrabblerForDictionary(dict).findByRegex(
            word = word,
            limit = minOf(limit, MAX_LIMIT),
        )
    }

    @GetMapping("/dicts")
    fun listDicts() = dicts.keys

    private fun getScrabblerForDictionary(
        dict: String
    ): Scrabbler {
        val dictionary = Dictionary.load(requireNotNull(dicts[dict]) { "Unknown dictionary." })
        return Scrabbler(dictionary)
    }

    private fun loadDictionaries(): Map<String, String> {
        val dictDirectory = File(dictLocation)
        require(dictDirectory.exists()) { "$dictLocation does not exist." }
        require(dictDirectory.isDirectory) { "$dictLocation does is not directory." }
        return dictDirectory
            .list()
            .filter { it.endsWith(DICT_EXT) }
            .map { it.removeSuffix(DICT_EXT) to Paths.get(dictDirectory.toString(), it).toString() }
            .toMap()
    }
}