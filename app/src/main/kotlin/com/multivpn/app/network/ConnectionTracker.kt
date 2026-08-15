package com.multivpn.app.network

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.TrafficStats
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * One row in the connections list: which domain is being contacted and how
 * much data has flowed to/from it.
 */
data class ConnectionEntry(
    val domain: String,
    val uploadedBytes: Long,
    val downloadedBytes: Long,
    val connectionCount: Int
)

/**
 * Tracks per-domain traffic flowing through the VPN tunnel.
 *
 * Because Android's {@link TrafficStats} reports byte counters per UID (not
 * per domain), this tracker combines two sources:
 *  1. Total VPN traffic reported by {@link TrafficStats} as a baseline total.
 *  2. Per-domain records added by the running core via {@link #recordDomain}
 *     (the core logs DNS lookups / connection targets which we attribute here).
 *
 * Until a core is running and feeding domain records, the tracker exposes the
 * overall tunnel totals so the UI is never empty.
 */
class ConnectionTracker(context: Context) {

    private val appContext = context.applicationContext
    private val domains = ConcurrentHashMap<String, DomainAccumulator>()

    /** Snapshot of {@link TrafficStats} counters captured when the tunnel started. */
    private var startTx = 0L
    private var startRx = 0L

    /** True while the tunnel is considered up. */
    @Volatile
    private var tracking = false

    fun start() {
        startTx = TrafficStats.getTotalTxBytes()
        startRx = TrafficStats.getTotalRxBytes()
        tracking = true
        Timber.d("ConnectionTracker started (tx=$startTx rx=$startRx)")
    }

    fun stop() {
        tracking = false
        Timber.d("ConnectionTracker stopped")
    }

    /**
     * Called by the core log parser when a domain is contacted. The first
     * sight of a domain creates an entry; subsequent calls accumulate traffic.
     */
    fun recordDomain(domain: String, tx: Long = 0L, rx: Long = 0L) {
        val acc = domains.computeIfAbsent(domain) { DomainAccumulator() }
        acc.tx.addAndGet(tx)
        acc.rx.addAndGet(rx)
        acc.count.incrementAndGet()
    }

    /**
     * Current snapshot of all recorded connections, plus a synthetic entry for
     * "Tunnel total" reflecting the VPN-level TrafficStats counters so the UI
     * shows real bytes even before per-domain attribution is wired.
     */
    fun snapshot(): List<ConnectionEntry> {
        val entries = domains.map { (domain, acc) ->
            ConnectionEntry(domain, acc.tx.get(), acc.rx.get(), acc.count.get().toInt())
        }.sortedByDescending { it.uploadedBytes + it.downloadedBytes }

        if (!tracking) return entries

        val txDelta = (TrafficStats.getTotalTxBytes() - startTx).coerceAtLeast(0)
        val rxDelta = (TrafficStats.getTotalRxBytes() - startRx).coerceAtLeast(0)
        if (txDelta == 0L && rxDelta == 0L && entries.isEmpty()) {
            return emptyList()
        }
        return entries + ConnectionEntry("Tunnel total", txDelta, rxDelta, entries.size)
    }

    fun reset() {
        domains.clear()
        startTx = TrafficStats.getTotalTxBytes()
        startRx = TrafficStats.getTotalRxBytes()
    }

    private class DomainAccumulator {
        val tx = AtomicLong(0)
        val rx = AtomicLong(0)
        val count = AtomicLong(0)
    }
}
