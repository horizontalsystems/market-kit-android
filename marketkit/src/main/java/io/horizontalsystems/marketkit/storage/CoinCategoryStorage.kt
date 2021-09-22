package io.horizontalsystems.marketkit.storage

import io.horizontalsystems.marketkit.models.CoinCategory

class CoinCategoryStorage(marketDatabase: MarketDatabase) {
    private val coinCategoryDao = marketDatabase.coinCategoryDao()

    fun coinCategories(): List<CoinCategory> {
        return coinCategoryDao.getCoinCategories()
    }

    fun save(coinCategories: List<CoinCategory>) {
        coinCategoryDao.insert(coinCategories)
    }
}
