package com.multivpn.app.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.multivpn.app.R
import com.multivpn.app.databinding.ActivityConnectionStatsBinding
import com.multivpn.app.network.ConnectionEntry
import com.multivpn.app.network.ConnectionTracker
import com.multivpn.app.network.ConnectionTrackerHolder

class ConnectionStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionStatsBinding
    private val handler = Handler(Looper.getMainLooper())
    private val adapter = ConnectionStatsAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.connection_stats)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.statsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.statsRecyclerView.adapter = adapter
        binding.resetButton.setOnClickListener {
            ConnectionTrackerHolder.tracker.reset()
            refresh()
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(refreshRunnable)
        super.onPause()
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    private fun refresh() {
        val entries = ConnectionTrackerHolder.tracker.snapshot()
        adapter.update(entries)
        binding.summaryValue.text = if (entries.isEmpty()) {
            getString(R.string.stats_no_data)
        } else {
            val totalTx = entries.sumOf { it.uploadedBytes }
            val totalRx = entries.sumOf { it.downloadedBytes }
            "${entries.size} ${getString(R.string.stats_domains)} • " +
                "↑ ${formatBytes(totalTx)} • ↓ ${formatBytes(totalRx)}"
        }
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 1000L
    }
}

data class ConnectionStat(
    val domain: String,
    val uploaded: String,
    val downloaded: String,
    val connections: String
)

class ConnectionStatsAdapter(private var items: List<ConnectionEntry>) :
    RecyclerView.Adapter<ConnectionStatsAdapter.ConnectionHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ConnectionHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_connection_stat, parent, false)
        return ConnectionHolder(view)
    }

    override fun onBindViewHolder(holder: ConnectionHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<ConnectionEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ConnectionHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val domainText = view.findViewById<android.widget.TextView>(R.id.domainText)
        private val uploadText = view.findViewById<android.widget.TextView>(R.id.uploadText)
        private val downloadText = view.findViewById<android.widget.TextView>(R.id.downloadText)
        private val connectionCountText = view.findViewById<android.widget.TextView>(R.id.connectionCountText)

        fun bind(item: ConnectionEntry) {
            domainText.text = item.domain
            uploadText.text = "↑ ${formatBytes(item.uploadedBytes)}"
            downloadText.text = "↓ ${formatBytes(item.downloadedBytes)}"
            connectionCountText.text = "${item.connectionCount} conn"
        }

        private fun formatBytes(bytes: Long): String = when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
