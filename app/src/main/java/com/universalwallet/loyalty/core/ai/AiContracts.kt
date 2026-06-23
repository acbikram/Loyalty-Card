package com.universalwallet.loyalty.core.ai

import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard

/*
 * AI-ready architecture — INTERFACES ONLY. No implementations ship in this
 * phase. These contracts let future on-device/cloud models plug in without any
 * feature-code changes. Inputs are platform-agnostic references (a URI string or
 * audio path) so the contracts carry no Android dependency.
 */

/** A reference to an image to analyse (content URI or file path). */
data class AiImage(val uri: String)

/** A reference to a captured audio clip. */
data class AudioClip(val path: String)

/** Confidence-scored detection of card details from an image. */
data class CardRecognitionResult(
    val storeName: String?,
    val cardNumber: String?,
    val barcodeType: BarcodeType?,
    val category: CardCategory?,
    val confidence: Float,
)

data class OcrResult(val text: String, val blocks: List<String>, val confidence: Float)

data class ReceiptResult(
    val merchant: String?,
    val total: Double?,
    val purchasedAt: Long?,
    val suggestedStoreId: String?,
)

data class OfferSuggestion(val storeId: String, val title: String, val score: Float)
data class CardSuggestion(val card: LoyaltyCard, val reason: String, val score: Float)
data class DuplicateGroup(val cardIds: List<String>, val confidence: Float)
data class SmartSearchResult(val card: LoyaltyCard, val relevance: Float, val explanation: String)
data class NlQuery(val rawText: String, val category: CardCategory?, val storeName: String?, val favoritesOnly: Boolean)

/** Parsed voice command (e.g. "show my Lulu card"). */
sealed interface VoiceCommand {
    data class OpenCard(val storeNameOrNickname: String) : VoiceCommand
    data object ScanCard : VoiceCommand
    data class Search(val query: String) : VoiceCommand
    data object Unknown : VoiceCommand
}

/** Whether a given AI capability is available on this device/build. */
interface AiCapabilities {
    fun isCardRecognitionAvailable(): Boolean
    fun isOcrAvailable(): Boolean
    fun isVoiceAvailable(): Boolean
    fun isRecommendationAvailable(): Boolean
}

interface CardRecognizer { suspend fun recognize(image: AiImage): CardRecognitionResult }
interface OcrEngine { suspend fun extractText(image: AiImage): OcrResult }
interface ReceiptRecognizer { suspend fun parseReceipt(image: AiImage): ReceiptResult }
interface OfferRecommender { suspend fun recommend(cards: List<LoyaltyCard>): List<OfferSuggestion> }
interface CardSuggester { suspend fun suggest(cards: List<LoyaltyCard>, now: Long): List<CardSuggestion> }
interface DuplicateDetectorAi { suspend fun findDuplicates(cards: List<LoyaltyCard>): List<DuplicateGroup> }
interface SmartSearchEngine { suspend fun search(query: String, cards: List<LoyaltyCard>): List<SmartSearchResult> }
interface NaturalLanguageSearch { suspend fun parse(query: String): NlQuery }
interface VoiceSearchEngine { suspend fun transcribe(clip: AudioClip): String }
interface VoiceCommandInterpreter { suspend fun interpret(text: String): VoiceCommand }
