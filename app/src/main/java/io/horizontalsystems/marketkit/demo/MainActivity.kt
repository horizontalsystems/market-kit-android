package io.horizontalsystems.marketkit.demo

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.R
import io.horizontalsystems.marketkit.managers.CoinCategoryManager
import io.horizontalsystems.marketkit.managers.CoinManager
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinCategoryStorage
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.MarketDatabase
import io.horizontalsystems.marketkit.syncers.CoinCategorySyncer
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, ViewModelFactory(this)).get(MainViewModel::class.java)

        syncButton.setOnClickListener {
            viewModel.syncCoins()
        }
    }

}

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val coinStorage = CoinStorage(MarketDatabase.getInstance(context))
    private val coinCategoryStorage = CoinCategoryStorage(MarketDatabase.getInstance(context))
    private val coinManager = CoinManager(coinStorage)
    private val coinCategoryManager = CoinCategoryManager(coinCategoryStorage)
    private val coinSyncer = CoinSyncer(HsProvider(), coinManager)
    private val coinCategorySyncer = CoinCategorySyncer(HsProvider(), coinCategoryManager)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(coinSyncer, coinCategorySyncer) as T
    }
}
