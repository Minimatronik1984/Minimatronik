package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractGeneratorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val templates = viewModel.contractTemplates
    val selectedId by viewModel.selectedTemplateId.collectAsState()
    val compiledText by viewModel.compiledContractText.collectAsState()

    // Parameter states from ViewModel
    val clientName by viewModel.clientName.collectAsState()
    val clientPib by viewModel.clientPib.collectAsState()
    val clientAddress by viewModel.clientAddress.collectAsState()
    val contractDate by viewModel.contractDate.collectAsState()
    val contractAmount by viewModel.contractAmount.collectAsState()
    val contractWorkDescription by viewModel.contractWorkDescription.collectAsState()

    var showParametersForm by remember { mutableStateOf(true) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Title block
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "Generator Ugovora",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Generišite pravno validne ugovore o saradnji, NDA i ugovore o delu spremne za deljenje.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Template Selection Row
        item {
            Text(
                text = "1. Izaberite šablon ugovora",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                templates.forEach { template ->
                    val isSelected = template.id == selectedId
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectTemplate(template.id) }
                            .testTag("template_card_${template.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectTemplate(template.id) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = template.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Parameters input expander
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showParametersForm = !showParametersForm }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "2. Popunite podatke klijenta i detalje",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = if (showParametersForm) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand details"
                )
            }

            AnimatedVisibility(visible = showParametersForm) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { viewModel.clientName.value = it },
                            label = { Text("Naziv Klijenta / Firme partnera") },
                            placeholder = { Text("npr. InoPartner Ltd ili Domaća Firma DOO") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contract_client_name"),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = clientPib,
                                onValueChange = { viewModel.clientPib.value = it },
                                label = { Text("PIB / Registarski Broj") },
                                placeholder = { Text("npr. 102938475") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("contract_client_pib"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = contractDate,
                                onValueChange = { viewModel.contractDate.value = it },
                                label = { Text("Datum ugovora") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("contract_date"),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = clientAddress,
                            onValueChange = { viewModel.clientAddress.value = it },
                            label = { Text("Adresa sedišta klijenta") },
                            placeholder = { Text("npr. Knez Mihailova 10, Beograd") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contract_client_address"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = contractAmount,
                            onValueChange = { viewModel.contractAmount.value = it },
                            label = { Text("Ugovoreni iznos naknade (RSD / EUR)") },
                            placeholder = { Text("npr. 120,000 RSD mesečno") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contract_amount"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = contractWorkDescription,
                            onValueChange = { viewModel.contractWorkDescription.value = it },
                            label = { Text("Detaljan opis posla i obaveza") },
                            placeholder = { Text("npr. Pružanje usluga programiranja, testiranja...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contract_work_desc"),
                            maxLines = 4
                        )
                    }
                }
            }
        }

        // Preview & Actions Card
        item {
            Text(
                text = "3. Pregled i preuzimanje ugovora",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Document Preview Area with monospace font representing contract papers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp, max = 400.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            .padding(16.dp)
                            .verticalScrollStatePadding()
                    ) {
                        Text(
                            text = compiledText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                copyToClipboard(context, compiledText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_contract_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopiraj")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kopiraj ugovor")
                        }

                        OutlinedButton(
                            onClick = {
                                shareContract(context, compiledText)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_contract_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Podeli")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Podeli tekst")
                        }
                    }
                }
            }
        }
    }
}

// Helper scroll wrapper modifier
@Composable
fun Modifier.verticalScrollStatePadding(): Modifier {
    val scrollState = rememberScrollState()
    return this.then(Modifier.verticalScroll(scrollState))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("E-Knjigovođa Ugovor", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Ugovor je kopiran u privremenu memoriju (clipboard)!", Toast.LENGTH_SHORT).show()
}

private fun shareContract(context: Context, text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Pošalji ugovor preko:")
    context.startActivity(shareIntent)
}
