package com.example.gemini

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// --- Moshi Data Classes for Gemini REST API ---

data class GeminiPart(
    val text: String
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

data class GeminiResponseCandidate(
    val content: GeminiContent?
)

data class GeminiResponse(
    val candidates: List<GeminiResponseCandidate>?
)

object GeminiClient {
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
        Ti si "E-Knjigovođa Poreski Savetnik", stručni digitalni računovođa i poreski savetnik specijalizovan za preduzetnike paušalce i mikro preduzetnike u Republici Srbiji.
        Tvoj cilj je da pomogneš korisnicima sa svim pitanjima u vezi sa:
        1. Paušalnim oporezivanjem (limiti od 6 miliona RSD za paušal, 8 miliona za PDV, plaćanje poreza i doprinosa, poreska rešenja).
        2. Testom samostalnosti (9 kriterijuma, kako preduzetnik dokazuje samostalnost u odnosu na klijente, rizik od padanja testa).
        3. Legalnim podizanjem novca sa poslovnog računa preduzetnika (paušalci mogu slobodno podizati novac bez pravdanja, ali moraju voditi knjigu KPO).
        4. Fakturisanjem (domaće i inostrane fakture, devizni prilivi, izrada faktura).
        5. Otvaranjem i zatvaranjem preduzetničke radnje (APR, poreska uprava, digitalni sertifikati).
        6. Poreskim kalendarom (rok za plaćanje obaveza je 15. u mesecu).

        Komuniciraj na srpskom jeziku, profesionalno, precizno i prijateljski. Koristi jasne pasuse i liste za preglednost.
        Na kraju svakog odgovora dodaj kratku, nenametljivu napomenu: 
        "Napomena: Ja sam AI asistent. Za zvanične odluke, posavetujte se sa licenciranim knjigovođom ili Poreskom upravom."
    """

    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Greška: GEMINI_API_KEY nije podešen u AI Studio sekretima. Molimo podesite ključ da biste aktivirali AI Poreskog Savetnika."
        }

        val requestBodyData = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = SYSTEM_PROMPT)))
        )

        val jsonRequest = jsonAdapter.toJson(requestBodyData)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequest.toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Došlo je do greške prilikom pozivanja poreskog savetnika (HTTP ${response.code}). Proverite internet vezu ili validnost API ključa."
                }
                val rawResponse = response.body?.string() ?: return@withContext "Dobijen je prazan odgovor od servera."
                val parsed = responseAdapter.fromJson(rawResponse)
                val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                return@withContext text ?: "Nažalost, nisam uspeo da generišem odgovor. Molimo pokušajte ponovo."
            }
        } catch (e: Exception) {
            return@withContext "Greška u komunikaciji: ${e.localizedMessage ?: "Nepoznata greška"}. Proverite da li imate aktivnu internet vezu."
        }
    }
}
