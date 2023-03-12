package ng.com.netpos.nibssapitest.data.models

import com.netpluspay.contactless.sdk.card.CardReadResult

data class CardResult(
    val cardReadResult: CardReadResult,
    val cardScheme: String
)
