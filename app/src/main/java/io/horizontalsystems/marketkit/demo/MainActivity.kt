package io.horizontalsystems.marketkit.demo

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, ViewModelFactory(this)).get(MainViewModel::class.java)

        runButton.setOnClickListener {
            viewModel.run()
        }
    }

}

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val marketKit by lazy {
        MarketKit.getInstance(
            context,
            "https://markets-dev.horizontalsystems.xyz",
            "https://markets.horizontalsystems.xyz"
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(marketKit) as T
    }
}
