package com.powerfulboy.app.network.model

import com.google.gson.annotations.SerializedName

// ─── Open Food Facts ──────────────────────────────────────────────────────────
data class OFFSearchResponse(
    val products: List<OFFProduct> = emptyList(),
    val count: Int = 0
)

data class OFFProduct(
    val id: String = "",
    @SerializedName("product_name") val productName: String = "",
    val brands: String = "",
    val nutriments: OFFNutriments = OFFNutriments()
)

data class OFFNutriments(
    @SerializedName("energy-kcal_100g") val calories: Float? = null,
    @SerializedName("proteins_100g") val protein: Float? = null,
    @SerializedName("carbohydrates_100g") val carbs: Float? = null,
    @SerializedName("fat_100g") val fat: Float? = null
)

// ─── Anthropic ────────────────────────────────────────────────────────────────
data class AnthropicRequest(
    val model: String = "claude-haiku-3-5-20241022",
    val max_tokens: Int = 600,
    val system: String,
    val messages: List<AnthropicMessage>
)

data class AnthropicMessage(
    val role: String,
    val content: String
)

data class AnthropicResponse(
    val content: List<AnthropicContent> = emptyList()
)

data class AnthropicContent(
    val type: String = "",
    val text: String = ""
)

// ─── AI Suggestion payload ────────────────────────────────────────────────────
data class SuggestionPayload(
    val targets: MacroTargets,
    val remaining: MacroTargets,
    val restrictions: List<String>,
    val avoid: List<String>,
    val love: List<String>
)

data class MacroTargets(
    val cal: Int,
    val pro: Int,
    val carb: Int,
    val fat: Int
)

// ─── AI suggestion result ─────────────────────────────────────────────────────
data class MealSuggestion(
    val name: String,
    val description: String,
    val estimatedCal: Int,
    val estimatedPro: Int,
    val estimatedCarb: Int,
    val estimatedFat: Int,
    val ingredients: List<String>
)
