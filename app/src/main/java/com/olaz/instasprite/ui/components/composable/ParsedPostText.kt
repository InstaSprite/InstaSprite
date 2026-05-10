package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.olaz.instasprite.ui.theme.AppTheme

val HASHTAG_REGEX = Regex("(#[\\w]+)")
val MENTION_REGEX = Regex("(@[\\w]+)")

@Composable
fun ParsedPostText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = AppTheme.colors.TextColorLight,
    linkColor: Color = AppTheme.colors.LinkColor,
    style: TextStyle = TextStyle.Default,
    onHashtagClick: (String) -> Unit = {},
    onMentionClick: (String) -> Unit = {},
    onTextClick: () -> Unit = {}
) {
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        
        // Find all matches for hashtags and mentions
        val matches = (HASHTAG_REGEX.findAll(text) + MENTION_REGEX.findAll(text))
            .sortedBy { it.range.first }
            .toList()

        for (match in matches) {
            // Append plain text before the match
            if (match.range.first > lastIndex) {
                withStyle(style = SpanStyle(color = textColor)) {
                    append(text.substring(lastIndex, match.range.first))
                }
            }

            // Append the match with link styling
            val matchedText = match.value
            val annotationTag = if (matchedText.startsWith("#")) "HASHTAG" else "MENTION"
            
            pushStringAnnotation(tag = annotationTag, annotation = matchedText)
            withStyle(style = SpanStyle(color = linkColor, fontWeight = FontWeight.Bold)) {
                append(matchedText)
            }
            pop()

            lastIndex = match.range.last + 1
        }

        // Append remaining text
        if (lastIndex < text.length) {
            withStyle(style = SpanStyle(color = textColor)) {
                append(text.substring(lastIndex))
            }
        }
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "HASHTAG", start = offset, end = offset)
                .firstOrNull()?.let {
                    onHashtagClick(it.item)
                    return@ClickableText
                }

            annotatedString.getStringAnnotations(tag = "MENTION", start = offset, end = offset)
                .firstOrNull()?.let {
                    onMentionClick(it.item)
                    return@ClickableText
                }
            
            // If we click normal text, propagate to parent
            onTextClick()
        }
    )
}
