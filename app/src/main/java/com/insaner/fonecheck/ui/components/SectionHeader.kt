package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * Opens a section: a small uppercase monospace label with a full-weight rule beneath it. The
 * optional [trailing] value sits on the same baseline, for a count or a coverage figure.
 *
 * Pass [label] and [trailing] in their natural casing; the component uppercases what it draws and
 * keeps the original wording for screen readers.
 */
@Composable
fun SectionHeader(
    label: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    val locale = LocalLocale.current.platformLocale
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = FonecheckTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label.uppercase(locale),
                style = FonecheckTheme.type.sectionLabel,
                color = FonecheckTheme.colors.textMuted,
                modifier =
                    Modifier.semantics {
                        heading()
                        contentDescription = label
                    },
            )
            if (trailing != null) {
                Text(
                    text = trailing.uppercase(locale),
                    style = FonecheckTheme.type.sectionLabel,
                    color = FonecheckTheme.colors.textMuted,
                    modifier = Modifier.semantics { contentDescription = trailing },
                )
            }
        }
        StrongRule()
    }
}
