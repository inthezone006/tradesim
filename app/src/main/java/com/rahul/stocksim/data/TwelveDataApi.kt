package com.rahul.stocksim.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface TwelveDataApi {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): JsonElement

    @GET("time_series")
    suspend fun getTimeSeries(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("outputsize") outputSize: Int = 30,
        @Query("apikey") apiKey: String
    ): TwelveDataTimeSeriesResponse

    @GET("rsi")
    suspend fun getRSI(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("time_period") timePeriod: Int = 14,
        @Query("apikey") apiKey: String
    ): TwelveDataIndicatorResponse

    @GET("macd")
    suspend fun getMACD(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("apikey") apiKey: String
    ): TwelveDataMACDResponse

    @GET("ema")
    suspend fun getEMA(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("time_period") timePeriod: Int = 20,
        @Query("apikey") apiKey: String
    ): TwelveDataIndicatorResponse

    @GET("sma")
    suspend fun getSMA(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("time_period") timePeriod: Int = 50,
        @Query("apikey") apiKey: String
    ): TwelveDataIndicatorResponse

    @GET("bbands")
    suspend fun getBollingerBands(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("apikey") apiKey: String
    ): TwelveDataBBandsResponse

    @GET("stoch")
    suspend fun getStochastic(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("apikey") apiKey: String
    ): TwelveDataStochResponse

    @GET("earnings_calendar")
    suspend fun getEarningsCalendar(
        @Query("symbol") symbol: String?,
        @Query("apikey") apiKey: String
    ): TwelveDataEarningsCalendarResponse

    @GET("dividends_calendar")
    suspend fun getDividendsCalendar(
        @Query("symbol") symbol: String?,
        @Query("apikey") apiKey: String
    ): TwelveDataDividendsCalendarResponse

    @GET("ipo_calendar")
    suspend fun getIpoCalendar(
        @Query("apikey") apiKey: String
    ): TwelveDataIpoCalendarResponse
}

data class TwelveDataEarningsCalendarResponse(
    val status: String?,
    val data: List<TwelveDataEarningsEntry>?
)

data class TwelveDataEarningsEntry(
    val symbol: String,
    val name: String?,
    val date: String,
    val time: String?,
    val eps_estimate: String?,
    val eps_actual: String?,
    val difference: String?,
    val surprise_prc: String?
)

data class TwelveDataDividendsCalendarResponse(
    val status: String?,
    val data: List<TwelveDataDividendsEntry>?
)

data class TwelveDataDividendsEntry(
    val symbol: String,
    val name: String?,
    val date: String,
    val amount: String?,
    val currency: String?,
    val payable_date: String?
)

data class TwelveDataIpoCalendarResponse(
    val status: String?,
    val data: List<TwelveDataIpoEntry>?
)

data class TwelveDataIpoEntry(
    val symbol: String,
    val name: String?,
    val date: String,
    val exchange: String?,
    val price_range: String?,
    val shares_offered: String?
)

data class TwelveDataBBandsResponse(
    val meta: TwelveDataMeta?,
    val values: List<TwelveDataBBandsValue>?,
    val status: String?
)

data class TwelveDataBBandsValue(
    val datetime: String,
    val upper_band: String,
    val middle_band: String,
    val lower_band: String
)

data class TwelveDataStochResponse(
    val meta: TwelveDataMeta?,
    val values: List<TwelveDataStochValue>?,
    val status: String?
)

data class TwelveDataStochValue(
    val datetime: String,
    val slow_k: String,
    val slow_d: String
)

data class TwelveDataQuote(
    val symbol: String,
    val name: String?,
    val open: String?,
    val high: String?,
    val low: String?,
    val close: String?,
    val volume: String?,
    val previous_close: String?,
    val change: String?,
    val percent_change: String?,
    val timestamp: Long?,
    val is_market_open: Boolean?
)

data class TwelveDataTimeSeriesResponse(
    val meta: TwelveDataMeta?,
    val values: List<TwelveDataTimeSeriesValue>?,
    val status: String?
)

data class TwelveDataMeta(
    val symbol: String,
    val interval: String,
    val currency: String?,
    val exchange_timezone: String?,
    val exchange: String?,
    val type: String?
)

data class TwelveDataTimeSeriesValue(
    val datetime: String,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val volume: String
)

data class TwelveDataIndicatorResponse(
    val meta: TwelveDataMeta?,
    val values: List<TwelveDataIndicatorValue>?,
    val status: String?
)

data class TwelveDataIndicatorValue(
    val datetime: String,
    @SerializedName("rsi") val rsi: String?,
    @SerializedName("ema") val ema: String?,
    @SerializedName("sma") val sma: String?
)

data class TwelveDataMACDResponse(
    val meta: TwelveDataMeta?,
    val values: List<TwelveDataMACDValue>?,
    val status: String?
)

data class TwelveDataMACDValue(
    val datetime: String,
    val macd: String,
    val macd_signal: String,
    val macd_hist: String
)
