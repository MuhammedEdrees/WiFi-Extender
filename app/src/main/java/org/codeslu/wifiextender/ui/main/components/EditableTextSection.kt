package org.codeslu.wifiextender.ui.main.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme

@Composable
fun EditableTextSection(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    title: String,
    value: String,
    onUpdateValue: (String) -> Unit
){
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }
    Column (
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.W400,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Crossfade(targetState = isActive to isEditing, label = "Text Field") { state ->
            when{
                !state.first -> {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "—",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                state.second -> {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    OutlinedTextField (
                        value = textValue,
                        onValueChange = {
                            textValue = it
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onUpdateValue(textValue)
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = value,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.W400,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(
                            modifier = Modifier.size(24.dp),
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                textValue = value
                                isEditing = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun EditableTextSectionPreview() {
    WiFiExtenderTheme {
        EditableTextSection(
            title = "Name",
            value = "DIRECT-5Q-SM-G973U",
            modifier = Modifier.padding(horizontal = 16.dp),
            isActive = true,
            onUpdateValue = {}
        )
    }
}