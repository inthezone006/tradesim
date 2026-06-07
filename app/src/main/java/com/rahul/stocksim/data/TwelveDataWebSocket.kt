package com.rahul.stocksim.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwelveDataWebSocket @Inject constructor() {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    
    private val _priceUpdates = MutableSharedFlow<PriceUpdate>(extraBufferCapacity = 64)
    val priceUpdates = _priceUpdates.asSharedFlow()

    fun connect(apiKey: String) {
        if (webSocket != null) return

        val request = Request.Builder()
            .url("wss://ws.twelvedata.com/v1/quotes/price?apikey=$apiKey")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("TwelveDataWS", "Connected to WebSocket")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val update = gson.fromJson(text, PriceUpdate::class.java)
                    if (update.event == "price") {
                        _priceUpdates.tryEmit(update)
                    }
                } catch (e: Exception) {
                    Log.e("TwelveDataWS", "Error parsing message: $text", e)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("TwelveDataWS", "WebSocket closed: $reason")
                this@TwelveDataWebSocket.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("TwelveDataWS", "WebSocket failure", t)
                this@TwelveDataWebSocket.webSocket = null
            }
        })
    }

    fun subscribe(symbols: List<String>) {
        val message = mapOf(
            "action" to "subscribe",
            "params" to mapOf("symbols" to symbols.joinToString(","))
        )
        webSocket?.send(gson.toJson(message))
    }

    fun unsubscribe(symbols: List<String>) {
        val message = mapOf(
            "action" to "unsubscribe",
            "params" to mapOf("symbols" to symbols.joinToString(","))
        )
        webSocket?.send(gson.toJson(message))
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    data class PriceUpdate(
        val event: String,
        val symbol: String,
        val currency: String,
        val exchange: String,
        val type: String,
        val timestamp: Long,
        val price: Double,
        val day_volume: Long?
    )
}
