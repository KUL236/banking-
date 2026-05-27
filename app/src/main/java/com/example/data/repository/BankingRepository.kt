package com.example.data.repository

import android.content.Context
import com.example.data.database.BankingDatabase
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class BankingRepository(context: Context) {
    private val db = BankingDatabase.getDatabase(context)
    private val alertDao = db.marketAlertDao()
    private val fdDao = db.fdRateDao()
    private val ipoDao = db.ipoInfoDao()
    private val stockDao = db.stockTickerDao()
    private val chatDao = db.chatMessageDao()

    val allAlerts: Flow<List<MarketAlert>> = alertDao.getAllAlerts()
    val allFDRates: Flow<List<FDRate>> = fdDao.getAllFDRates()
    val allIpos: Flow<List<IpoInfo>> = ipoDao.getAllIpos()
    val allStocks: Flow<List<StockTicker>> = stockDao.getAllStocks()
    val chatHistory: Flow<List<ChatMessage>> = chatDao.getChatHistory()

    suspend fun seedDatabaseIfEmpty() {
        // Seed Stocks
        val stockCount = stockDao.getAllStocks().firstOrNull()?.size ?: 0
        if (stockCount == 0) {
            val initialStocks = listOf(
                StockTicker("HDFC", "HDFC Bank Ltd", 1620.40, 1618.00, 0.15, high = 1630.0, low = 1610.0),
                StockTicker("ICICI", "ICICI Bank Ltd", 1120.15, 1105.50, 1.32, high = 1125.0, low = 1102.0),
                StockTicker("SBIN", "State Bank of India", 785.60, 792.40, -0.86, high = 795.0, low = 782.0),
                StockTicker("KOTAK", "Kotak Mahindra Bank", 1811.20, 1815.00, -0.21, high = 1822.0, low = 1805.0),
                StockTicker("AXIS", "Axis Bank Ltd", 1075.50, 1062.10, 1.26, high = 1081.0, low = 1058.0)
            )
            stockDao.insertStocks(initialStocks)
        }

        // Seed FDs
        val fdCount = fdDao.getAllFDRates().firstOrNull()?.size ?: 0
        if (fdCount == 0) {
            val initialFDs = listOf(
                FDRate("SBI", "1 Year", 6.80, 7.30),
                FDRate("SBI", "3 Years", 7.00, 7.50),
                FDRate("SBI", "5 Years", 6.90, 7.40),
                FDRate("HDFC Bank", "1 Year", 7.10, 7.60),
                FDRate("HDFC Bank", "3 Years", 7.25, 7.75),
                FDRate("HDFC Bank", "5 Years", 7.20, 7.70),
                FDRate("ICICI Bank", "1 Year", 6.90, 7.40),
                FDRate("ICICI Bank", "3 Years", 7.15, 7.65),
                FDRate("ICICI Bank", "5 Years", 7.10, 7.60),
                FDRate("Axis Bank", "1 Year", 7.20, 7.70),
                FDRate("Axis Bank", "3 Years", 7.10, 7.60),
                FDRate("Axis Bank", "5 Years", 7.00, 7.50)
            )
            fdDao.insertFDRates(initialFDs)
        }

        // Seed IPOs
        val ipoCount = ipoDao.getAllIpos().firstOrNull()?.size ?: 0
        if (ipoCount == 0) {
            val initialIpos = listOf(
                IpoInfo(companyName = "PayTech Finance Ltd", gmp = 35.2, priceBand = "₹850 - ₹890", subscriptionX = 18.4, status = "OPEN", listingDate = "2026-06-02"),
                IpoInfo(companyName = "Zenith Alliance Bank", gmp = 12.4, priceBand = "₹120 - ₹130", subscriptionX = 2.1, status = "UPCOMING", listingDate = "2026-06-15"),
                IpoInfo(companyName = "Swift Crypto Bank Group", gmp = -4.5, priceBand = "₹320 - ₹335", subscriptionX = 0.8, status = "CLOSED", listingDate = "2026-05-24")
            )
            ipoDao.insertIpos(initialIpos)
        }

        // Seed Alerts
        val alertCount = alertDao.getAllAlerts().firstOrNull()?.size ?: 0
        if (alertCount == 0) {
            val initialAlerts = listOf(
                MarketAlert(
                    title = "Bank Nifty Volatility Warning",
                    description = "Bank Nifty index falls 1.85% intraday due to macro bond yield pressure. Elevated volume observed in stock liquidate blocks.",
                    type = "VOLATILITY",
                    severity = "MEDIUM"
                ),
                MarketAlert(
                    title = "HDFC Bank FD Rate Expansion",
                    description = "HDFC raises premium 3-Year Fixed Deposit rates to a record 7.25% (7.75% for Senior Citizens), shifting domestic liquidity deposit ratios.",
                    type = "FD",
                    severity = "LOW"
                )
            )
            alertDao.insertAlerts(initialAlerts)
        }
    }

    suspend fun updateAlertInsight(alertId: Int, insight: String) {
        val alertList = alertDao.getAllAlerts().firstOrNull() ?: return
        val matched = alertList.find { it.id == alertId } ?: return
        alertDao.insertAlert(matched.copy(aiInsight = insight))
    }

    // Fluctuates stock prices and checks for spike anomalies to trigger real-time alerts automatically
    suspend fun tickStocksAndCheckAlerts() {
        val current = stockDao.getAllStocks().firstOrNull() ?: return
        val updated = current.map { stock ->
            val change = Random.nextDouble(-1.2, 1.3) / 100.0 // -1.2% to +1.3%
            val newPrice = stock.price * (1.0 + change)
            val newChangePct = ((newPrice - stock.prevPrice) / stock.prevPrice) * 100.0
            
            // Track high/low
            val newHigh = if (newPrice > stock.high) newPrice else stock.high
            val newLow = if (newPrice < stock.low) newPrice else stock.low

            // Check if there's a mini-crash/spike that should trigger alerts
            if (change < -0.01) { // Drop greater than 1% in a single tick
                val severity = if (change < -0.015) "HIGH" else "MEDIUM"
                val title = "Volatility Alert: Sudden fall in ${stock.symbol}"
                val description = "Critical sellout pressure detected on ${stock.name} (${stock.symbol}). Price dropped to ₹${String.format("%.2f", newPrice)} instantly."
                alertDao.insertAlert(
                    MarketAlert(
                        title = title,
                        description = description,
                        type = "VOLATILITY",
                        severity = severity
                    )
                )
            } else if (change > 0.011) { // Rise greater than 1.1%
                val title = "Investment Trend: Breakout on ${stock.symbol}"
                val description = "Unusual bullish momentum observed on ${stock.name}. Price hits ₹${String.format("%.2f", newPrice)} with elevated volumes."
                alertDao.insertAlert(
                    MarketAlert(
                        title = title,
                        description = description,
                        type = "TREND",
                        severity = "LOW"
                    )
                )
            }

            stock.copy(
                price = newPrice,
                changePercent = newChangePct,
                high = newHigh,
                low = newLow,
                timestamp = System.currentTimeMillis()
            )
        }
        stockDao.insertStocks(updated)
    }

    suspend fun triggerShockEvent(type: String) {
        val stocks = stockDao.getAllStocks().firstOrNull() ?: return
        when (type) {
            "CRASH" -> {
                // Simulate a banking stock flash crash
                val collapsed = stocks.map { stock ->
                    val crashDrop = 0.045 // 4.5% instantly
                    val newPrice = stock.price * (1.0 - crashDrop)
                    val newPct = ((newPrice - stock.prevPrice) / stock.prevPrice) * 100.0
                    stock.copy(price = newPrice, changePercent = newPct, low = newPrice)
                }
                stockDao.insertStocks(collapsed)
                alertDao.insertAlert(
                    MarketAlert(
                        title = "⚠️ FLASH CRASH: Financial Sector Liquidity Surge",
                        description = "Simulated market shock event: Central Bank introduces aggressive retail inflation guidelines. Banking stocks slide 4.5% in minutes.",
                        type = "VOLATILITY",
                        severity = "HIGH"
                    )
                )
            }
            "FD_BOOM" -> {
                // Boost FD interest rates across all banks by 0.5%
                val fds = fdDao.getAllFDRates().firstOrNull() ?: return
                val boosted = fds.map { fd ->
                    fd.copy(
                        interestRate = fd.interestRate + 0.5,
                        seniorCitizenRate = fd.seniorCitizenRate + 0.5
                    )
                }
                fdDao.insertFDRates(boosted)
                alertDao.insertAlert(
                    MarketAlert(
                        title = "📈 FD Rate Shock: General Interest Yield Boost",
                        description = "State and Private banks jointly increase Fixed Deposit slabs by +50 bps relative to sovereign interest yields. Ideal opportunity for conservative risk-hedging.",
                        type = "FD",
                        severity = "MEDIUM"
                    )
                )
            }
            "IPO_BURST" -> {
                // Instantly boost IPO gray market premium
                val ipos = ipoDao.getAllIpos().firstOrNull() ?: return
                val listingIpos = ipos.map { ipo ->
                    if (ipo.status != "CLOSED") {
                        ipo.copy(
                            gmp = ipo.gmp + 15.0,
                            subscriptionX = ipo.subscriptionX * 1.5
                        )
                    } else ipo
                }
                ipoDao.insertIpos(listingIpos)
                alertDao.insertAlert(
                    MarketAlert(
                        title = "🔥 IPO Frenzy: GMP Volatility Alert",
                        description = "Dynamic block orders in pre-IPO lists have boosted Gray Market Premiums by an average of +15.0%. Retail subscriber applications hit record maximums.",
                        type = "IPO",
                        severity = "MEDIUM"
                    )
                )
            }
        }
    }

    suspend fun saveChatMessage(text: String, isFromUser: Boolean) {
        chatDao.insertMessage(ChatMessage(text = text, isFromUser = isFromUser))
    }

    suspend fun clearChatHistory() {
        chatDao.clearChat()
    }
}
