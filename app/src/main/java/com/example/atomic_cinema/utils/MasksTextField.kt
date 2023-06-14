package com.example.atomic_cinema.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.substring


class MaskNumberPhone : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        return maskFilterNumberPhone(text)
    }

    private fun maskFilterNumberPhone(input: AnnotatedString): TransformedText {
        val trimmed = if(input.text.length >= 10) input.text.substring(0..9) else input.text
        var out = ""
        for(i in trimmed.indices){
            out += trimmed[i]
            when(i){
                0 -> {
                    out = "+7$out"
                }
                2 -> {
                    out += "-"
                }
                5 -> {
                    out += "-"
                }
                7 -> {
                    out += "-"
                }
            }
        }
        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return offset
                if (offset <= 2) return offset + 2
                if (offset <= 5) return offset + 3
                if (offset <= 7) return offset + 4
                if (offset <= 9) return offset + 5
                return 15
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 1)  return offset
                if (offset <= 2)  return offset - 1
                if (offset <= 3)  return offset - 1
                if (offset <= 6)  return offset - 1
                if (offset <= 8)  return offset - 1
                if (offset <= 10) return offset - 1
                if (offset <= 15) return offset - 1
                return 14
            }
        }
        return TransformedText(AnnotatedString(out), numberOffsetTranslator)
    }
}

class MaskDate : VisualTransformation{
    override fun filter(text: AnnotatedString): TransformedText {
        return maskFilterDate(text)
    }

    private fun maskFilterDate(input : AnnotatedString) : TransformedText{
        val trimmed = if (input.text.length >= 8) input.text.substring(0..7) else input.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i % 2 == 1 && i < 4) out += "."
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset +1
                if (offset <= 8) return offset +2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <=2) return offset
                if (offset <=4) return offset -1
                if (offset <=9) return offset -2
                return 8
            }
        }
        return TransformedText(AnnotatedString(out), numberOffsetTranslator)
    }
}

class MaskNumberCard : VisualTransformation{
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.filter { it.isDigit() }.take(16).replace(" ", "")
        val formatted = buildString {
            trimmed.indices.forEach { index ->
                if (index % 4 == 0 && index > 0) {
                    append(" ")
                }
                append(trimmed[index])
            }
        }
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                if (offset <= 15) return offset + 3
                return 19
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 5) return offset
                if (offset <= 10) return offset - 1
                if (offset <= 17) return offset - 2
                return 17
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetTranslator)
    }


}

