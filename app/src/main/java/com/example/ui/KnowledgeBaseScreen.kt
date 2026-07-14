package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(0) } // 0 = Vodiči, 1 = Test Samostalnosti, 2 = AI Savetnik

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper section tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("Vodiči", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Book, contentDescription = "Vodiči") }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Test Samostalnosti", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.FactCheck, contentDescription = "Test") }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("AI Savetnik", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSection) {
                0 -> GuidesTab()
                1 -> IndependenceTestTab()
                2 -> AiAdvisorTab(viewModel)
            }
        }
    }
}

// ==================== TAB 1: GUIDES ====================

data class GuideArticle(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val summary: String,
    val details: String,
    val color: Color
)

@Composable
fun GuidesTab() {
    val articles = remember { getSerbianGuideArticles() }
    var expandedIndex by remember { mutableStateOf(-1) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Baza Znanja i Saveti",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Pravni i finansijski vodiči kreirani za lakše poslovanje preduzetnika u Srbiji.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(articles.size) { index ->
            val article = articles[index]
            val isExpanded = expandedIndex == index

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) -1 else index }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(article.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = article.icon,
                                contentDescription = null,
                                tint = article.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = article.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = article.details,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 2: INDEPENDENCE TEST ====================

data class TestQuestion(
    val id: Int,
    val title: String,
    val description: String
)

@Composable
fun IndependenceTestTab() {
    val questions = remember { getIndependenceTestQuestions() }
    val checkedState = remember { mutableStateMapOf<Int, Boolean>() }

    // Count yes answers
    val yesCount = checkedState.values.count { it }

    val riskLevel = when {
        yesCount <= 2 -> "Nizak rizik (Bezbedno)"
        yesCount <= 5 -> "Umeren rizik (Oprez!)"
        else -> "Visok rizik (Rizično!)"
    }

    val riskColor = when {
        yesCount <= 2 -> SuccessGreen
        yesCount <= 5 -> WarningOrange
        else -> ErrorRed
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Test Samostalnosti",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Pravilo od 9 poreskih kriterijuma uvedeno 2020. Označite sve tvrdnje koje su TAČNE za Vašu saradnju sa najvećim klijentom:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Risk Meter Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Rezultat: $yesCount od 9",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Status: $riskLevel",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = riskColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(riskColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (yesCount >= 6) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = riskColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { yesCount / 9f },
                        color = riskColor,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Što više uslova (kriterijuma) ispunjavate, to je veći rizik da Vas poreska inspekcija proglasi nesamostalnim (tada se porezi plaćaju kao za stalno zaposlenog sa kaznama!). Cilj je imati manje od 5 ispunjenih kriterijuma.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        items(questions.size) { index ->
            val question = questions[index]
            val isChecked = checkedState[question.id] ?: false

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) riskColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checkedState[question.id] = !isChecked }
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checkedState[question.id] = it },
                        colors = CheckboxDefaults.colors(checkedColor = riskColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${question.id}. ${question.title}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isChecked) riskColor else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ==================== TAB 3: AI ADVISOR ====================

@Composable
fun AiAdvisorTab(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when a new message arrives
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Chat Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatBubble(message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.padding(end = 40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Savetnik razmišlja...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Očisti razgovor",
                    tint = ErrorRed
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Pitajte poreskog savetnika...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank() && !isLoading) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                        keyboardController?.hide()
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Pošalji",
                    tint = if (inputText.isNotBlank() && !isLoading) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (message.isUser) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (message.isUser) Arrangement.End else Arrangement.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = alignment
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier.padding(
                start = if (message.isUser) 40.dp else 0.dp,
                end = if (message.isUser) 0.dp else 40.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = textColor
                )
            }
        }
    }
}

// ==================== LOCAL DATA PROVIDERS ====================

fun getSerbianGuideArticles(): List<GuideArticle> {
    return listOf(
        GuideArticle(
            title = "Kako preduzetnik legalno podiže novac",
            icon = Icons.Default.AttachMoney,
            summary = "Saznajte kako paušalci u Srbiji slobodno koriste zarađen novac.",
            color = SuccessGreen,
            details = """
                Preduzetnik paušalac u Republici Srbiji je fizičko lice koje obavlja registrovanu delatnost. To znači da ne postoji pravna razlika između Vas kao pojedinca i Vaše preduzetničke radnje.
                
                Ključna prednost:
                Sav novac koji ostane na računu Vaše preduzetničke radnje NAKON što platite mesečni porez i doprinose (poresko rešenje) je Vaš LIČNI novac.
                
                Možete ga slobodno podizati:
                1. Karticom na bankomatu (direktno sa biznis računa).
                2. Prenosom na Vaš lični račun građanina preko e-bankinga (šifra plaćanja 240 ili 241).
                3. Podizanjem na šalteru banke (sa svrhom podizanja 'materijalni troškovi' ili 'lična primanja preduzetnika').
                
                Da li morate pravdati troškove?
                NE! Paušalci ne moraju pravdati svoje troškove računima niti dostavljati dokaze o tome na šta troše novac. Jedina zakonska obaveza jeste da vodite KPO knjigu (Knjiga o ostvarenom prometu) i da ne pređete limit od 6,000,000 RSD prometa u kalendarskoj godini.
            """.trimIndent()
        ),
        GuideArticle(
            title = "Poreski kalendar za 2026. godinu",
            icon = Icons.Default.CalendarMonth,
            summary = "Najvažniji datumi i rokovi kako biste izbegli kamate i kazne.",
            color = WarningOrange,
            details = """
                Kao paušalac, u 2026. godini morate paziti na sledeće važne poreske datume i rokove:
                
                • 15. U SVAKOM MESECU:
                Ovo je najvažniji datum! To je zakonski rok za uplatu poreza i doprinosa za prethodni mesec. Na primer, porez za januar 2026. mora biti uplaćen najkasnije do 15. februara 2026. Ako zakasnite, Poreska uprava zaračunava dnevnu zateznu kamatu.
                
                • FEBRUAR/MART (Godišnja poreska prijava):
                Poreska uprava automatski donosi nova rešenja za paušalce početkom godine. Rešenje stiže isključivo u poresko sanduče na portalu ePorezi, tako da je obavezno redovno proveravati portal (ili ovlastiti knjigovođu).
                
                • 31. DECEMBAR:
                Kraj kalendarske godine. Vaš ukupno naplaćeni promet na poslovnom računu (i domaćem i deviznom) sabira se od 1. januara do 31. decembra. Limit iznosi 6,000,000 RSD za preduzetnika koji želi da ostane u sistemu paušalnog oporezivanja.
            """.trimIndent()
        ),
        GuideArticle(
            title = "Kako preživeti poresku kontrolu",
            icon = Icons.Default.Shield,
            summary = "Saveti i pravila za bezbrižan sastanak sa poreskim inspektorom.",
            color = DeepNavy,
            details = """
                Poreska kontrola (inspekcija) je stresna za svakog preduzetnika, ali ako radite po zakonu, nema mesta panici. Evo šta treba da pripremite:
                
                1. Uredna KPO knjiga:
                Kao paušalac, Vaša jedina i najvažnija knjiga je KPO. Ona mora sadržati hronološki upisane sve izdate fakture. KPO knjiga se može voditi u elektronskom obliku (npr. Excel ili kroz aplikaciju) ili papirnom.
                
                2. Izvodi iz banke:
                Inspektor će tražiti izvode iz svih poslovnih banaka (domaći i devizni računi). Svaka faktura upisana u KPO mora imati odgovarajući priliv na izvodu.
                
                3. Ugovori sa klijentima:
                Posedovanje jasnih, pravno valjanih ugovora je ključno, posebno ako radite sa stranim klijentima. Ugovori moraju jasno definisati da ste Vi samostalni i da niste u prikrivenom radnom odnosu (izbegavajte reči kao što su 'radno vreme', 'godišnji odmor', 'plata', 'poslodavac').
                
                4. Potvrda o deviznim prilivima:
                Ako naplaćujete u devizama, čuvajte potvrde o izvršenom rasporedu priliva koje dobijate od banke nakon prijema novca iz inostranstva.
            """.trimIndent()
        )
    )
}

fun getIndependenceTestQuestions(): List<TestQuestion> {
    return listOf(
        TestQuestion(
            1,
            "Kontrola nad radnim vremenom i odmorima",
            "Naručilac određuje Vaše radno vreme, u koje sate morate raditi ili Vam direktno odobrava godišnji odmor / slobodne dane."
        ),
        TestQuestion(
            2,
            "Korišćenje prostorija i opreme naručioca",
            "Naručilac Vam obezbeđuje računar, softverske licence, kancelariju ili drugo osnovno sredstvo za svakodnevni rad."
        ),
        TestQuestion(
            3,
            "Rukovodstvo i organizacija posla",
            "Naručilac neposredno rukovodi i organizuje Vaš proces rada (morate podnositi redovne detaljne satne izveštaje ili raditi pod direktnim nadzorom menadžera naručioca)."
        ),
        TestQuestion(
            4,
            "Ugovorna zabrana rada sa drugim klijentima",
            "Ugovor sadrži ekskluzivitet ili zabranu konkurencije koja Vam praktično brani da radite za bilo kog drugog klijenta na tržištu."
        ),
        TestQuestion(
            5,
            "Zavisnost od prihoda (Preko 70% prihoda)",
            "Više od 70% Vašeg ukupnog prometa u poslednjih 12 meseci dolazi od samo jednog klijenta (naručioca) ili povezanih lica."
        ),
        TestQuestion(
            6,
            "Snositi poslovni rizik",
            "Naručilac snosi sav rizik poslovanja prema krajnjim kupcima, dok Vi ne odgovarate za kvalitet ili greške svojom imovinom."
        ),
        TestQuestion(
            7,
            "Finansiranje stručnog usavršavanja",
            "Naručilac Vam plaća kurseve, konferencije, stručne sertifikate ili obuke koje pohađate."
        ),
        TestQuestion(
            8,
            "Radne aktivnosti i uobičajen posao",
            "Posao koji obavljate za naručioca je deo njegove redovne pretežne delatnosti (npr. programer u programerskoj firmi, a ne programer u pekari)."
        ),
        TestQuestion(
            9,
            "Otpremnina i naknada za raskid",
            "Ugovor definiše pravo na novčanu naknadu u slučaju raskida ugovora sličnu otpremnini, ili garantuje fiksni broj meseci isplate bez obzira na isporučeni rad."
        )
    )
}
