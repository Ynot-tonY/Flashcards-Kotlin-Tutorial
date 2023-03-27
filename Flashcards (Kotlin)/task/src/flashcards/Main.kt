package flashcards

import java.util.*
import java.io.File

var cards = mutableMapOf<String, String>()
var stats = mutableMapOf<String, Int>()
var log = String()
var gC = 0
var exportPath: String? = null

fun main(args: Array<String>) {
    for (i in 0..args.size / 2 - 1) {
        if (args[i * 2] == "-import") {
            val fileName = args[i * 2 + 1]
            importer(fileName)
        } else if (args[i * 2] == "-export") {
            exportPath = args[i * 2 + 1]
        }
    }


    while (true) {
        // Read command
        println("Input the action (add, remove, import, export, flashcards.ask, exit):")
        val cmd = readln()
    
        // Act on command
        when (cmd) {
            "add"  -> addFC()
            "remove" -> removeFC()
            "import" -> importFC()
            "export" -> exportFC()
            "ask" -> ask()
            "exit" -> {
                exportPath?.let { exporter(it) }
                println("Bye bye!")
                break
            }
            "log" -> saveLog()
            "hardest card" -> showHC()
            "reset stats" -> {
                stats.clear()
                println("Card statistics have been reset.")
            }
        }
    }
}

fun showHC() {
    if (stats.isEmpty()) {
        println("There are no cards with errors.")
        return
    }
    val maxValue = stats.maxOf { it.value }
    val hardestCards = stats.filterValues { it == maxValue }

    if (hardestCards.count() > 1) {
        var text = "The hardest cards are "
        for ((k, v) in hardestCards) text = text.plus("\"$k\", ")
        text = text.substring(0, text.length - 2)
        text = text.plus(". You have ${hardestCards.maxOf { it.value }} errors answering them.")
        println(text)
    } else if (hardestCards.count() == 1) {
        println("The hardest card is \"${hardestCards.keys.toString().substring(1, 
            hardestCards.keys.toString().length - 1)}\". You have $maxValue errors answering it.")
    } else {
        println("There are no cards with errors.")
    }
}

fun saveLog() {
    println("File name:")
    val fileName = readln()
    val file = File(fileName)

    file.writeText(log)
    println("The log has been saved.")

    File("log$gC.txt").writeText(log)
}

fun addFC() {
    println("Card Term:")
    val term = readln()

    if (cards.containsKey(term)) {
        println("The card \"$term\" already exists.")
        return
    }
    
    println("The definition of the card:")
    val definition = readln()

    if (cards.containsValue(definition)) {
        println("The definition \"$definition\" already exists.")
        return
    }

    println("The pair (\"$term\":\"$definition\") has been added")
    cards[term] = definition
}

fun removeFC() {
    println("Which card?")
    val term = readln()

    if (cards.containsKey(term)) {
        cards.remove(term)
        println("The card has been removed.")
    } else {
        println("Can't remove \"$term\": there is no such card.")
    }
}

fun importFC() {
    println("File name:")
    val fileName = readln()

    importer(fileName)
}

fun importer(_fileName: String) {
    var cardCounter = 0
    val file = File(_fileName)
    if (file.exists()) {
        val lineReader = Scanner(file)
        while (lineReader.hasNext()) {
            val term = lineReader.nextLine()
            val def = lineReader.nextLine()
            val stat = lineReader.nextLine().toInt()
            cards[term] = def
            stats[term] = stat
            cardCounter++
        }
        println("$cardCounter cards have been loaded.")
    } else {
        println("File not found.")
    }
}

fun exportFC() {
    println("File name:")
    val fileName = readln()

    exporter(fileName)
}

fun exporter(_fileName: String) {
    val file = File(_fileName)

    var text = String()
    for ((k, v) in cards) {
        val stat = stats[k]?:0
        text = text.plus(k + "\n" + v + "\n" + stat + "\n")
    }
    file.writeText(text)

    println("${cards.count()} cards have been saved.")
}

fun ask() {
    println("How many times to flashcards?")
    val nC = readln().toInt()
    val cardIterator = cards.asIterable().iterator()

    for (i in 1..nC) {
        val (k, v) = cardIterator.next()
        println("Print the definition of \"$k\":")
        val ans = readln()
        
        if (v == ans){
            println("Correct!")
        } else if (cards.containsValue(ans)) {
            val tempMap = cards.filterValues { it == ans }
            val tempKey = tempMap.keys.toString().substring(1, tempMap.keys.toString().length - 1)
            println("Wrong. The right answer is \"$v\", but your definition is correct for \"$tempKey\".")
            addError(k)
        } else {
            println("Wrong. The right answer is \"$v\".")
            addError(k)
        }
    }
}

fun addError(k: String) {
    if (!stats.containsKey(k)) {
        stats[k] = 1
    } else {
        val temp = stats[k]
        if (temp != null) {
            stats[k] = temp + 1
        }
    }
}

fun println(_str: String) {
    kotlin.io.println(_str)
    log = log.plus("\n" + _str)
}

fun readln(): String {
    val str = kotlin.io.readln()
    log = log.plus("\n" + str)
    return str
}
