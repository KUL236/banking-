package com.example.data.database

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketAlertDao {
    @Query("SELECT * FROM market_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<MarketAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: MarketAlert): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<MarketAlert>)

    @Update
    suspend fun updateAlert(alert: MarketAlert)

    @Query("DELETE FROM market_alerts")
    suspend fun clearAll()
}

@Dao
interface FDRateDao {
    @Query("SELECT * FROM fd_rates ORDER BY interestRate DESC")
    fun getAllFDRates(): Flow<List<FDRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFDRates(rates: List<FDRate>)

    @Update
    suspend fun updateFDRate(rate: FDRate)
}

@Dao
interface IpoInfoDao {
    @Query("SELECT * FROM ipo_infos ORDER BY listingDate ASC")
    fun getAllIpos(): Flow<List<IpoInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIpos(ipos: List<IpoInfo>)

    @Update
    suspend fun updateIpo(ipo: IpoInfo)
}

@Dao
interface StockTickerDao {
    @Query("SELECT * FROM stock_tickers ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<StockTicker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockTicker>)

    @Update
    suspend fun updateStock(stock: StockTicker)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}
