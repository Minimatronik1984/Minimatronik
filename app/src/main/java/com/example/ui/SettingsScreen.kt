package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var companyName by remember { mutableStateOf("") }
    var pib by remember { mutableStateOf("") }
    var activityCode by remember { mutableStateOf("") }
    var monthlyTaxText by remember { mutableStateOf("") }

    // Synchronize local states with Room data once it loads
    LaunchedEffect(settings) {
        companyName = settings.companyName
        pib = settings.pib
        activityCode = settings.activityCode
        monthlyTaxText = settings.monthlyTaxRsd.toInt().toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Block
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = "Podešavanja Profila",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Konfigurišite poreske i poslovne podatke za automatski obračun i popunjavanje ugovora.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Main Settings Card Form
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Poslovni Podaci",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Zvanični naziv Vaše firme (APR)") },
                    placeholder = { Text("npr. Slobodan Marković PR Softversko Programiranje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_company_name"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pib,
                        onValueChange = { pib = it },
                        label = { Text("Poreski Broj (PIB)") },
                        placeholder = { Text("9 cifara, npr. 109876543") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("settings_pib"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = activityCode,
                        onValueChange = { activityCode = it },
                        label = { Text("Šifra delatnosti") },
                        placeholder = { Text("npr. 6201") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_activity_code"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = monthlyTaxText,
                    onValueChange = { monthlyTaxText = it },
                    label = { Text("Mesečni porezi i doprinosi (RSD)") },
                    placeholder = { Text("Zaduženje iz rešenja, npr. 32000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_monthly_tax"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val tax = monthlyTaxText.toDoubleOrNull()
                        if (companyName.isNotBlank() && pib.isNotBlank() && activityCode.isNotBlank() && tax != null && tax >= 0) {
                            viewModel.updateSettings(companyName, pib, activityCode, tax)
                            Toast.makeText(context, "Podaci su uspešno sačuvani!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Sva polja moraju biti ispravno popunjena!", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("settings_save_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Sačuvaj")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sačuvaj podatke")
                }
            }
        }

        // Info card explaining local safety and offline behavior
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = "Bezbednost",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Vaša privatnost je sigurna",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Svi uneti podaci o prihodima, klijentima, kao i Vaši generisani ugovori se čuvaju isključivo na Vašem uređaju u lokalnoj šifrovanoj bazi. Podaci nikada ne napuštaju telefon niti se šalju trećim licima.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Version info and credits
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "E-Knjigovođa v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
            Text(
                text = "Sistem za bezbrižno poslovanje paušalaca u Srbiji.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        }
    }
}
