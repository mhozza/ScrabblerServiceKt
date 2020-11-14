package eu.hozza.scrabbler.service

import eu.hozza.scrabbler.Scrabbler
import eu.hozza.scrabbler.buildTrie
import eu.hozza.scrabbler.loadDictionary
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Paths

@SpringBootApplication
class ServiceApplication

fun main(args: Array<String>) {
    runApplication<ServiceApplication>(*args)
}

const val MAX_LIMIT = 200

@RestController
class ScrabblerController {
    private val dicts = listOf("/home/mio/source/scrabbler-service/dict/sk.dic")
            .map { Paths.get(it).fileName.toString().removeSuffix(".dic") to it }.toMap()

    @GetMapping("/scrabble")
    fun scrabble(
            @RequestParam(value = "dict") dict: String,
            @RequestParam(value = "word") word: String,
            @RequestParam(value = "prefix") prefix: String?,
            @RequestParam(value = "regex", defaultValue = "false") regex: Boolean,
            @RequestParam(value = "use_all_letters", defaultValue = "true") useAllLetters: Boolean,
            @RequestParam(value = "limit", defaultValue = "100") limit: Int,
            @RequestParam(value = "wildcard") wildcard: Char?,
    ): List<String> {
        return getScrabblerForDictionary(dict).answer(
                word = word,
                regex = regex,
                limit = minOf(limit, MAX_LIMIT),
                allowShorter = !useAllLetters,
                wildcard = wildcard,
                prefix = prefix)
    }

    @GetMapping("/list_dicts")
    fun listDicts() = dicts.keys

    private fun getScrabblerForDictionary(dict: String): Scrabbler {
        val words = loadDictionary(requireNotNull(dicts[dict]) { "Unknown dictionary." })
        return Scrabbler(words = words, trie = buildTrie(words))
    }
}