package com.multivpn.app.vpn

import org.json.JSONArray
import org.json.JSONObject

/**
 * Generates the JSON config consumed by the core binary.
 *
 * The config creates a local SOCKS5 inbound on 127.0.0.1:10808 so the VPN
 * service can route the TUN traffic through it. The outbound is left as a
 * direct/freedom outbound — the user is expected to edit this config (in the
 * Pro settings screen) to point at their actual upstream server.
 */
object CoreConfig {

    const val LOCAL_HOST = "127.0.0.1"
    const val LOCAL_PORT = 10808

    /**
     * Build a minimal sing-box/mihomo-compatible config. If the user provided
     * a custom config via the Pro settings, it takes precedence.
     */
    fun build(customConfig: String): String {
        if (customConfig.isNotBlank()) {
            return validateOrFallback(customConfig)
        }
        val inbound = JSONObject()
            .put("type", "socks")
            .put("tag", "socks-in")
            .put("listen", LOCAL_HOST)
            .put("listen_port", LOCAL_PORT)

        val outbound = JSONObject()
            .put("type", "direct")
            .put("tag", "direct")

        return JSONObject()
            .put("inbounds", JSONArray().put(inbound))
            .put("outbounds", JSONArray().put(outbound))
            .put("log", JSONObject().put("level", "info"))
            .toString()
    }

    private fun validateOrFallback(raw: String): String {
        return try {
            JSONObject(raw).toString()
        } catch (e: Exception) {
            // Not valid JSON — fall back to the default config so the core at
            // least starts and the user can see the error in the log.
            build("")
        }
    }
}
