package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.*
import com.example.gemini.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val date: Date = Date()
)

data class ContractTemplate(
    val id: String,
    val title: String,
    val description: String,
    val rawText: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MainRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MainRepository(database)
    }

    // --- State Flows ---
    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<UserSettings> = repository.userSettings
        .map { it ?: UserSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    // --- AI Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Zdravo! Ja sam Vaš AI Poreski Savetnik. Pitajte me bilo šta o paušalu, testu samostalnosti, porezima ili poslovnim ugovorima u Srbiji.",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Contract Generator State ---
    private val _selectedTemplateId = MutableStateFlow("poslovno_tehnicka")
    val selectedTemplateId: StateFlow<String> = _selectedTemplateId.asStateFlow()

    // Contract Parameters
    val clientName = MutableStateFlow("")
    val clientPib = MutableStateFlow("")
    val clientAddress = MutableStateFlow("")
    val contractDate = MutableStateFlow(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()))
    val contractAmount = MutableStateFlow("")
    val contractWorkDescription = MutableStateFlow("Usluge programiranja i razvoja softverskih rešenja")

    // Compiled Contract Text
    val compiledContractText = combine(
        _selectedTemplateId,
        settings,
        clientName,
        clientPib,
        clientAddress,
        contractDate,
        contractAmount,
        contractWorkDescription
    ) { params ->
        val templateId = params[0] as String
        val currentSettings = params[1] as UserSettings
        val cName = params[2] as String
        val cPib = params[3] as String
        val cAddr = params[4] as String
        val cDate = params[5] as String
        val cAmt = params[6] as String
        val cDesc = params[7] as String

        val template = contractTemplates.find { it.id == templateId } ?: contractTemplates[0]
        
        template.rawText
            .replace("{MOJA_FIRMA}", currentSettings.companyName)
            .replace("{MOJ_PIB}", currentSettings.pib)
            .replace("{MOJA_SIFRA}", currentSettings.activityCode)
            .replace("{KLIJENT_NAZIV}", cName.ifBlank { "[Naziv Klijenta]" })
            .replace("{KLIJENT_PIB}", cPib.ifBlank { "[PIB Klijenta]" })
            .replace("{KLIJENT_ADRESA}", cAddr.ifBlank { "[Adresa Klijenta]" })
            .replace("{DATUM}", cDate)
            .replace("{IZNOS}", cAmt.ifBlank { "[Iznos u RSD/EUR]" })
            .replace("{OPIS_POSLA}", cDesc)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // --- Repository Operations ---
    fun addInvoice(clientName: String, invoiceNumber: String, amount: Double, isPaid: Boolean) {
        viewModelScope.launch {
            repository.insertInvoice(
                Invoice(
                    clientName = clientName,
                    invoiceNumber = invoiceNumber,
                    amountRsd = amount,
                    isPaid = isPaid
                )
            )
        }
    }

    fun toggleInvoicePaid(invoice: Invoice) {
        viewModelScope.launch {
            repository.updateInvoice(invoice.copy(isPaid = !invoice.isPaid))
        }
    }

    fun deleteInvoice(id: Int) {
        viewModelScope.launch {
            repository.deleteInvoiceById(id)
        }
    }

    fun updateSettings(companyName: String, pib: String, code: String, monthlyTax: Double) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.saveSettings(
                current.copy(
                    companyName = companyName,
                    pib = pib,
                    activityCode = code,
                    monthlyTaxRsd = monthlyTax
                )
            )
        }
    }

    // --- AI Operations ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(text = text, isUser = true)
        _chatMessages.update { it + userMsg }

        viewModelScope.launch {
            _isChatLoading.value = true
            val replyText = GeminiClient.askAssistant(text)
            val replyMsg = ChatMessage(text = replyText, isUser = false)
            _chatMessages.update { it + replyMsg }
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Zdravo! Očišćeno ćaskanje. Slobodno postavite novo pitanje u vezi sa poreskim poslovanjem preduzetnika u Srbiji.",
                isUser = false
            )
        )
    }

    fun selectTemplate(id: String) {
        _selectedTemplateId.value = id
    }

    // --- Hardcoded templates (legally compliant templates in Serbia) ---
    val contractTemplates = listOf(
        ContractTemplate(
            id = "poslovno_tehnicka",
            title = "Ugovor o poslovno-tehničkoj saradnji",
            description = "Idealan za dugoročnu saradnju sa domaćim ili inostranim klijentima. Definiše samostalnost preduzetnika.",
            rawText = """
                UGOGOR O POSLOVNO-TEHNIČKOJ SARADNJI
                
                Zaključen dana {DATUM} godine u Beogradu, između:
                
                1. {MOJA_FIRMA}, sa sedištem na adresi navedenoj u registru APR, PIB: {MOJ_PIB}, šifra delatnosti: {MOJA_SIFRA}, koga zastupa preduzetnik (u daljem tekstu: Izvršilac), i
                
                2. {KLIJENT_NAZIV}, sa sedištem na adresi {KLIJENT_ADRESA}, PIB: {KLIJENT_PIB}, koga zastupa ovlašćeno lice (u daljem tekstu: Naručilac).
                
                Član 1: Predmet ugovora
                Predmet ovog Ugovora je regulisanje međusobnih prava i obaveza ugovornih strana, po kojima će Izvršilac za potrebe Naručioca obavljati sledeće poslove: {OPIS_POSLA}.
                
                Član 2: Samostalnost Izvršioca
                Ugovorne strane saglasno konstatuju da Izvršilac obavlja ugovorene poslove samostalno, u svoje ime i za svoj račun. Izvršilac samostalno određuje vreme i mesto rada, koristi sopstvenu opremu i alate i nije podređen Naručiocu u smislu Zakona o radu, što predstavlja jedan od ključnih dokaza o samostalnosti preduzetnika prema poreskim propisima Republike Srbije.
                
                Član 3: Finansijske odredbe i plaćanje
                Naručilac se obavezuje da za izvršene usluge iz Člana 1. ovog Ugovora isplati Izvršiocu ugovoreni iznos od {IZNOS} RSD, po ispostavljenoj fakturi od strane Izvršioca.
                Rok za plaćanje fakture iznosi 15 dana od dana njenog prijema na račun Izvršioca.
                
                Član 4: Poverljivost podataka
                Ugovorne strane se obavezuju da će sve informacije do kojih dođu tokom realizacije ovog ugovora tretirati kao strogo poverljive i da ih neće otkrivati trećim licima bez prethodne pismene saglasnosti druge strane.
                
                Član 5: Trajanje i raskid ugovora
                Ovaj ugovor se zaključuje na neodređeno vreme. Svaka ugovorna strana može raskinuti ovaj ugovor sa otkaznim rokom od 30 dana, dostavljanjem pismenog obaveštenja drugoj strani.
                
                Član 6: Rešavanje sporova
                Za sve sporove koji proisteknu iz ovog Ugovora, a koji se ne mogu rešiti mirnim putem, nadležan je Privredni sud u Beogradu.
                
                Za Izvršioca: _____________________
                Za Naručioca: _____________________
            """.trimIndent()
        ),
        ContractTemplate(
            id = "nda",
            title = "Ugovor o poverljivosti (NDA)",
            description = "Standardni ugovor o čuvanju poslovne tajne i poverljivih informacija.",
            rawText = """
                UGOGOR O ČUVANJU POVERLJIVIH INFORMACIJA (NDA)
                
                Zaključen dana {DATUM} godine, između:
                
                1. {MOJA_FIRMA}, PIB: {MOJ_PIB} (u daljem tekstu: Strana koja prima informacije), i
                
                2. {KLIJENT_NAZIV}, PIB: {KLIJENT_PIB} (u daljem tekstu: Strana koja otkriva informacije).
                
                Član 1: Poverljive informacije
                Poverljivim informacijama smatraju se sve tehničke, finansijske, pravne, poslovne ili marketing informacije, softverski kod, baze podataka, nacrti i dokumenti koji su označeni kao poverljivi ili se po prirodi stvari mogu smatrati poverljivim.
                
                Član 2: Obaveze Strane koja prima informacije
                Strana koja prima informacije se obavezuje da će:
                - Čuvati poverljive informacije sa istom pažnjom sa kojom čuva sopstvene poverljive informacije.
                - Koristiti informacije isključivo u svrhu evaluacije i realizacije zajedničke poslovne saradnje.
                - Ograničiti pristup informacijama samo na zaposlene i saradnike kojima su te informacije neophodne za rad.
                
                Član 3: Izuzeci
                Obaveza čuvanja tajne ne važi za informacije koje su u trenutku otkrivanja bile javno dostupne, ili su naknadno postale javno dostupne bez krivice Strane koja prima informacije.
                
                Član 4: Ugovorna kazna
                U slučaju kršenja odredbi ovog ugovora, Strana koja prima informacije dužna je da nadoknadi svu direktnu i indirektnu štetu koju je pretrpela Strana koja otkriva informacije, sa minimalnim iznosom naknade od {IZNOS} RSD.
                
                Član 5: Nadležnost
                U slučaju spora, nadležan je sud u mestu sedišta Strane koja otkriva informacije.
                
                Primalac: _____________________
                Otkrivalac: _____________________
            """.trimIndent()
        ),
        ContractTemplate(
            id = "ugovor_o_delu",
            title = "Ugovor o delu za jednokratni projekat",
            description = "Idealan za definisanje jednokratnog posla (npr. dizajn logotipa, jednokratno programiranje aplikacije).",
            rawText = """
                UGOGOR O DELU
                
                Zaključen dana {DATUM} godine, između:
                
                1. {MOJA_FIRMA}, sa sedištem u Srbiji, PIB: {MOJ_PIB}, koga zastupa preduzetnik (u daljem tekstu: Poslenik), i
                
                2. {KLIJENT_NAZIV}, sa sedištem na adresi {KLIJENT_ADRESA}, PIB: {KLIJENT_PIB} (u daljem tekstu: Naručilac).
                
                Član 1: Opis posla
                Poslenik se obavezuje da za račun Naručioca obavi sledeći konkretan posao: {OPIS_POSLA}.
                Naručilac obezbeđuje sve neophodne ulazne podatke i specifikacije za nesmetano obavljanje posla.
                
                Član 2: Rok za završetak posla
                Poslenik se obavezuje da posao iz Člana 1. završi i preda Naručiocu najkasnije u roku predviđenom ovim ugovorom, a procenjeno vreme završetka rada je {DATUM}.
                
                Član 3: Cena i isplata
                Naručilac se obavezuje da za izvršeni rad isplati Posleniku ugovorenu naknadu u ukupnom iznosu od {IZNOS} RSD. Isplata će se izvršiti u roku od 7 dana nakon primopredaje rada i dostavljanja fakture.
                
                Član 4: Intelektualna svojina
                Sva autorska prava i prava intelektualne svojine na delu koje je predmet ovog ugovora prelaze u potpunosti na Naručioca nakon isplate ugovorene cene u celosti.
                
                Član 5: Raskid ugovora
                Naručilac može raskinuti ugovor u svakom trenutku pre završetka posla, ali je u tom slučaju dužan da Posleniku isplati srazmeran deo ugovorene naknade za do tada obavljeni rad, kao i da nadoknadi eventualne pretrpljene troškove.
                
                Član 6: Završne odredbe
                Ugovor je sačinjen u 2 (dva) istovetna primerka, od kojih svaka strana zadržava po 1 (jedan) primerak.
                
                Poslenik: _____________________
                Naručilac: _____________________
            """.trimIndent()
        )
    )
}
