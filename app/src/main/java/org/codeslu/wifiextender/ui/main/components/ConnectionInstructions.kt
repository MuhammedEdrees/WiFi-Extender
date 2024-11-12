package org.codeslu.wifiextender.ui.main.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme

@Composable
fun ConnectionInstructionsSection(
    modifier: Modifier = Modifier,
    onMoreHelpClicked: () -> Unit = { }
) {
    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.android_devices),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(id = R.string.android_instructions),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W400,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.windows_and_mac),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val context = LocalContext.current

            val annotatedString = buildAnnotatedString {
                withStyle(style = androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("1. Connect the device to Wi-Fi Hotspot.\n")
                    append("2. Open ")
                }
                pushStringAnnotation(tag = "URL", annotation = "http://192.168.49.1:8181")
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("http://192.168.49.1:8181")
                }
                pop()
                withStyle(style = androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append(" in the device to setup the connection.")
                }
            }
            ClickableText(
                modifier = Modifier.padding(start = 16.dp),
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                }
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.other),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(id = R.string.other_instructions),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W400,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.address_and_port),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.more_help),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.W500,
            modifier = Modifier.clickable { onMoreHelpClicked() }
        )
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ConnectionInstructionsSectionPreview() {
    WiFiExtenderTheme {
        ConnectionInstructionsSection(modifier = Modifier.padding(16.dp))
    }
}