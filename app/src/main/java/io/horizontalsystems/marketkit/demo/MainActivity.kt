package io.horizontalsystems.marketkit.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.demo.databinding.ActivityMainBinding
import io.horizontalsystems.marketkit.demo.databinding.ViewHolderItemBinding
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

        viewModel.exportDumpUri.observe(this, Observer { fileUri ->
            this.startActivity(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "text/*"
            })
        })

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.exportAsDump -> {
                    viewModel.exportAsDump(applicationContext)
                    true
                }
                else -> false
            }
        }

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
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(marketKit) as T
    }
}
