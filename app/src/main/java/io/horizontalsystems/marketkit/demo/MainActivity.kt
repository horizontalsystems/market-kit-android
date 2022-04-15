package io.horizontalsystems.marketkit.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.databinding.ActivityMainBinding
import io.horizontalsystems.marketkit.databinding.ViewHolderItemBinding
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this, ViewModelFactory(this)).get(MainViewModel::class.java)

        val methods = MainViewModel::class.memberFunctions.filter {
            it.name.startsWith("run")
        }

        val adapter = Adapter(methods, viewModel)
        binding.rv.adapter = adapter
    }

}

class Adapter(private val methods: List<KFunction<*>>, private val viewModel: MainViewModel) :
    RecyclerView.Adapter<Adapter.VH>() {

    class VH(private val binding: ViewHolderItemBinding, private val viewModel: MainViewModel) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(method: KFunction<*>) {
            binding.button.text = method.name.substringAfter("run")
            binding.button.setOnClickListener {
                method.call(viewModel)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(
            ViewHolderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            viewModel
        )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(methods[position])
    }

    override fun getItemCount() = methods.size

}

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val marketKit by lazy {
        MarketKit.getInstance(
            context,
            "https://api-dev.blocksdecoded.com",
            "IQf1uAjkthZp1i2pYzkXFDom",
            defiYieldApiKey = "87e8671e-8267-427c-92c3-4627833445ae"
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(marketKit) as T
    }
}
