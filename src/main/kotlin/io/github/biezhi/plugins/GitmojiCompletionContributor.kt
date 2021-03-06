package io.github.biezhi.plugins

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiPlainText
import com.intellij.util.ProcessingContext


/**
 * GitmojiCompletionContributor
 *
 * @author biezhi
 * @date 2017/12/30
 */
class GitmojiCompletionContributor : CompletionContributor() {

    val mapping = EmojiMapping()

    val logger = Logger.getInstance(javaClass)

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(PsiPlainText::class.java), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                if (parameters.editor.isOneLineMode) {
                    return
                }

                val editor = parameters.editor
                val charsSequence = editor.document.charsSequence

                var message = charsSequence.toString().toLowerCase()

                if (message.length < 2) {
                    return
                }

                val caretModel = editor.caretModel
                val offset = caretModel.offset
                val pos = Math.max(0, Math.max(message.lastIndexOf(":", offset), message.lastIndexOf(" ", offset)))

                logger.info("lastIndexOf= ${message.lastIndexOf(":", offset)}")
                logger.info("lastIndexOf= ${message.lastIndexOf(" ", offset)}")

                message = message.substring(pos).trim()

                logger.info("$pos $offset $message")

                val split = message.split(Regex("\\s"))
                message = split[0]

                val eb = message.indexOf(":") == 0

                if (eb) {
                    message = message.substring(1)
                }

                if (eb) {
                    mapping.actions
                            .forEach {
                                val ej = mapping.getText(it) as String

                                if (ej.toLowerCase().indexOf(message) != -1) {
                                    result.addElement(LookupElementBuilder.create("$ej:").withIcon(mapping.getIcon(it)))
                                }
                            }
                }

                mapping.actions
                        //.filterIndexed { _, it -> it.toLowerCase().indexOf(message.toLowerCase()) != -1 }
                        .forEach {
                            if (it.toLowerCase().indexOf(message) != -1) {
                                result.addElement(LookupElementBuilder.create("${if (eb) "" else ":"}${mapping.getText(it)}: " + it).withIcon(mapping.getIcon(it)))
                            }
                        }

            }
        })
    }
}
