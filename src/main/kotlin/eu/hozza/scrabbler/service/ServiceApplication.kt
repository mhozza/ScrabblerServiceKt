package eu.hozza.scrabbler.service

import eu.hozza.scrabbler.Scrabbler
import eu.hozza.scrabbler.buildTrie
import eu.hozza.scrabbler.filterDictionary
import eu.hozza.scrabbler.loadDictionary
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
        return getScrabblerForDictionary(dict, word, false, wildcard, useAllLetters, prefix).answer(
                word = word,
                regex = false,
                limit = minOf(limit, MAX_LIMIT),
                allowShorter = !useAllLetters,
                wildcard = wildcard,
                prefix = prefix)
    }

    @GetMapping("/regex")
    private fun filterRegex(
            @RequestParam(value = "dict") dict: String,
            @RequestParam(value = "word") word: String,
            @RequestParam(value = "prefix") prefix: String?,
            @RequestParam(value = "use_all_letters", defaultValue = "true") useAllLetters: Boolean,
            @RequestParam(value = "limit", defaultValue = "100") limit: Int,
            @RequestParam(value = "wildcard") wildcard: Char?,
    ): List<String> {
        return getScrabblerForDictionary(dict, word, true, wildcard, useAllLetters, prefix).answer(
                word = word,
                regex = true,
                limit = minOf(limit, MAX_LIMIT),
                allowShorter = !useAllLetters,
                wildcard = wildcard,
                prefix = prefix)
    }

    @GetMapping("/dicts")
    fun listDicts() = dicts.keys

    private fun getScrabblerForDictionary(dict: String, letters: String, regex: Boolean, wildcard: Char?, useAllLetters: Boolean, prefix: String?): Scrabbler {
        val words = filterDictionary(
                loadDictionary(requireNotNull(dicts[dict]) { "Unknown dictionary." }),
                letters,
                wildcard,
                useAllLetters,
                prefix)

        val trie = if (regex) null else buildTrie(words)
        return Scrabbler(words = words, trie = trie)
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