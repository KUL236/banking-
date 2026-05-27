package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "market_alerts")
@JsonClass(generateAdapter = true)
data class MarketAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val type: String, // VOLATILITY, TREND, IPO, FD
    val severity: String, // HIGH, MEDIUM, LOW
    val timestamp: Long = System.currentTimeMillis(),
    val aiInsight: String? = null
)

@Entity(tableName = "fd_rates", primaryKeys = ["bankName", "tenure"])
@JsonClass(generateAdapter = true)
data class FDRate(
    val bankName: String,
    val tenure: String, // "1 Year", "3 Years", "5 Years"
    val interestRate: Double,
    val seniorCitizenRate: Double
)

@Entity(tableName = "ipo_infos")
@JsonClass(generateAdapter = true)
data class IpoInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyName: String,
    val gmp: Double, // Gray Market Premium (%)
    val priceBand: String,
    val subscriptionX: Double, // subscription times (eg. 14.5)
    val status: String, // "OPEN", "UPCOMING", "CLOSED"
    val listingDate: String
)

@Entity(tableName = "stock_tickers")
@JsonClass(generateAdapter = true)
data class StockTicker(
    @PrimaryKey val symbol: String,
    val name: String,
    val price: Double,
    val prevPrice: Double,
    val changePercent: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val high: Double,
    val low: Double
)

@Entity(tableName = "chat_messages")
@JsonClass(generateAdapter = true)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
