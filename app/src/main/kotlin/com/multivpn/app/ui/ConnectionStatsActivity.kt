package com.multivpn.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.multivpn.app.R
import com.multivpn.app.databinding.ActivityConnectionStatsBinding

class ConnectionStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.connection_stats)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.statsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.statsRecyclerView.adapter = ConnectionStatsAdapter(
            listOf(
                ConnectionStat("api.example.com", "24.8 MB", "18.2 MB", "12"),
                ConnectionStat("cdn.example.net", "8.4 MB", "5.6 MB", "7"),
                ConnectionStat("auth.example.org", "2.1 MB", "3.1 MB", "4")
            )
        )
    }
}

data class ConnectionStat(
    val domain: String,
    val uploaded: String,
    val downloaded: String,
    val connections: String
)

class ConnectionStatsAdapter(private val items: List<ConnectionStat>) : RecyclerView.Adapter<ConnectionStatsAdapter.ConnectionHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ConnectionHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_connection_stat, parent, false)
        return ConnectionHolder(view)
    }

    override fun onBindViewHolder(holder: ConnectionHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class ConnectionHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val domainText = view.findViewById<android.widget.TextView>(R.id.domainText)
        private val uploadText = view.findViewById<android.widget.TextView>(R.id.uploadText)
        private val downloadText = view.findViewById<android.widget.TextView>(R.id.downloadText)
        private val connectionCountText = view.findViewById<android.widget.TextView>(R.id.connectionCountText)

        fun bind(item: ConnectionStat) {
            domainText.text = item.domain
            uploadText.text = "↑ ${item.uploaded}"
            downloadText.text = "↓ ${item.downloaded}"
            connectionCountText.text = "${item.connections} conn"
        }
    }
}
