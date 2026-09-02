package com.multivpn.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.multivpn.app.R
import com.multivpn.app.databinding.ActivityPluginInfoBinding
import com.multivpn.app.plugin.PluginCore

/**
 * Shown right after the OOBE level picker. Describes each downloadable core
 * so the user understands what sing-box / Xray / Clash are before reaching the
 * main screen where they can download the binaries.
 */
class PluginInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPluginInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPluginInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.plugin_info_title)
        binding.toolbar.setNavigationOnClickListener {
            goToMain()
        }

        binding.pluginInfoRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pluginInfoRecyclerView.adapter = PluginInfoAdapter(OobeActivity.OOBE_CORES)

        binding.continueButton.setOnClickListener { goToMain() }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}

private class PluginInfoAdapter(private val items: List<PluginCore>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<PluginInfoAdapter.Holder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): Holder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plugin_info, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class Holder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<android.widget.TextView>(R.id.coreNameText)
        private val desc = view.findViewById<android.widget.TextView>(R.id.coreDescriptionText)
        private val version = view.findViewById<android.widget.TextView>(R.id.coreVersionText)

        fun bind(core: PluginCore) {
            name.text = core.displayName
            desc.text = core.description
            version.text = "v${core.version} • ${core.architecture}"
        }
    }
}
