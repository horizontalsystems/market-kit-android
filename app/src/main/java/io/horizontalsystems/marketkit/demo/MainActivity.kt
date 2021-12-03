package io.horizontalsystems.marketkit.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, ViewModelFactory(this)).get(MainViewModel::class.java)


        val methods = MainViewModel::class.memberFunctions.filter {
            it.name.startsWith("run")
        }

        val adapter = Adapter(methods, viewModel)
        rv.adapter = adapter
    }

}

class Adapter(private val methods: List<KFunction<*>>, private val viewModel: MainViewModel) : RecyclerView.Adapter<Adapter.VH>() {

    class VH(override val containerView: View, private val viewModel: MainViewModel) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val button = containerView.findViewById<TextView>(R.id.button)

        fun bind(method: KFunction<*>) {
            button.text = method.name.substringAfter("run")
            button.setOnClickListener {
                method.call(viewModel)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_item, parent, false), viewModel)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(methods[position])
    }

    override fun getItemCount() = methods.size

}

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val marketKit by lazy {
        MarketKit.getInstance(
            context,
            "https://api.blocksdecoded.com",
            defiYieldApiKey = "87e8671e-8267-427c-92c3-4627833445ae"
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(marketKit) as T
    }
}
