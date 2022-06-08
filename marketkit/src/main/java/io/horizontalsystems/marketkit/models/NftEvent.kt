package io.horizontalsystems.marketkit.models

import java.util.*

data class NftEvent(
    val asset: NftAsset,
    val type: EventType?,
    val date: Date?,
    val amount: NftPrice?
) {

    enum class EventType(
        val value: String
    ) {
        All("all"),
        List("list"),
        Sale("sale"),
        OfferEntered("offer"),
        BidEntered("bid"),
        BidWithdrawn("bid_cancel"),
        Transfer("transfer"),
        Approve("approve"),
        Custom("custom"),
        Payout("payout"),
        Cancel("cancel"),
        BulkCancel("bulk_cancel"),
        Unknown("unknown");

        companion object {
            private val map = values().associateBy(EventType::value)

            fun fromString(value: String?): EventType? = map[value]
        }
    }

}

data class PagedNftEvents(
    val assets: List<NftEvent>,
    val cursor: String?
)
