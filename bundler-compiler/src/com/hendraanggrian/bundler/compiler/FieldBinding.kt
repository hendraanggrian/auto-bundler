package com.hendraanggrian.bundler.compiler

import com.hendraanggrian.bundler.Extra
import com.hendraanggrian.bundler.State
import com.squareup.javapoet.CodeBlock
import javax.lang.model.element.Element
import javax.lang.model.util.Types

internal class FieldBinding(fieldElement: Element, typeUtils: Types) {

    private val type = BundleValueType.valueOf(fieldElement, typeUtils)
    private val name = fieldElement.simpleName
    private val annotation: Annotation
    private val key: String

    val isBindExtra: Boolean get() = annotation is Extra

    val codeBlock: CodeBlock
        get() = when {
            type != BundleValueType.PARCELER -> CodeBlock.of("\$L.\$L = \$L(\$S, \$L.\$L);\n",
                TARGET, name, type.getMethodName, key, TARGET, name)
            else -> CodeBlock.of("\$L.\$L = \$T.getParceler(\$L, \$S, \$L.\$L);\n",
                TARGET, name, TYPE_BUNDLER_UTILS, SOURCE, key, TARGET, name)
        }

    init {
        val extra = fieldElement.getAnnotation(Extra::class.java)
        val state = fieldElement.getAnnotation(State::class.java)
        if (extra != null && state != null) {
            throw IllegalStateException(name.toString() + " is annotated with Extra and State, " +
                "this is unsupported behavior.")
        } else if (extra != null) {
            annotation = extra
            val key = extra.value
            this.key = if (!key.isEmpty()) key else name.toString()
        } else if (state != null) {
            annotation = state
            val key = state.value
            this.key = if (!key.isEmpty()) key else name.toString()
        } else {
            throw IllegalStateException("Couldn't read key from " + name.toString())
        }
    }

    val checkRequiredCodeBlock: CodeBlock
        get() = CodeBlock.of("checkRequired(\$S, \$S);\n",
            key, name)

    val putCodeBlockWithList: CodeBlock
        get() = when {
            type != BundleValueType.PARCELER -> CodeBlock.of(
                "if(!\$L.isEmpty()) \$L.\$L(\$S, (\$L) nextArg());\n",
                ARGS, SOURCE, type.putMethodName, key, type.typeName.toString())
            else -> CodeBlock.of("if(!\$L.isEmpty()) \$T.putParceler(\$L, \$S, nextArg());\n",
                ARGS, TYPE_BUNDLER_UTILS, SOURCE, key)
        }

    val putCodeBlock: CodeBlock
        get() = when {
            type != BundleValueType.PARCELER -> CodeBlock.of("\$L.\$L(\$S, \$L.\$L);\n",
                SOURCE, type.putMethodName, key, TARGET, name)
            else -> CodeBlock.of("\$T.putParceler(\$L, \$S, \$L.\$L);\n",
                TYPE_BUNDLER_UTILS, SOURCE, key, TARGET, name)
        }
}