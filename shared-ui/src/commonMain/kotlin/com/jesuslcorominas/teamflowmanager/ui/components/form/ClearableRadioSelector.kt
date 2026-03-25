package com.jesuslcorominas.teamflowmanager.ui.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.ui.components.AppTitle
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.clear

interface SelectableItem {
    val id: Long
    val label: String
}

@Composable
fun <T : SelectableItem> ClearableRadioSelector(
    titleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    title: String? = null,
    selectedOption: Long?,
    onSelect: (Long) -> Unit,
    onClear: () -> Unit,
    items: List<T>,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ClearableRadioSelectorHeader(
            title = title ?: "",
            selectedOption = selectedOption,
            onClear = onClear,
            modifier = titleModifier,
        )

        Spacer(Modifier.height(TFMSpacing.spacing02))

        ClearableRadioSelectorList(
            items = items,
            selectedOption = selectedOption,
            onSelect = onSelect,
            modifier = Modifier.padding(horizontal = TFMSpacing.spacing02),
        )
    }
}

@Composable
fun ClearableRadioSelectorHeader(
    title: String,
    selectedOption: Long?,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TFMSpacing.spacing02, vertical = TFMSpacing.spacing02),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTitle(modifier = Modifier.weight(1F), title = title)

            Button(onClick = { onClear() }, enabled = selectedOption != null) {
                Icon(
                    modifier = Modifier.size(TFMSpacing.spacing04),
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(Res.string.clear),
                )
                Spacer(Modifier.width(TFMSpacing.spacing02))
                Text(stringResource(Res.string.clear), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun <T : SelectableItem> ClearableRadioSelectorList(
    items: List<T>,
    selectedOption: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option.id) }
                        .padding(horizontal = TFMSpacing.spacing02, vertical = TFMSpacing.spacing01),
            ) {
                RadioButton(
                    selected = selectedOption == option.id,
                    onClick = { onSelect(option.id) },
                )
                Text(option.label)
            }
        }
    }
}
