package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.models.*

@Database(
    entities = [
        MarketAlert::class,
        FDRate::class,
        IpoInfo::class,
        StockTicker::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BankingDatabase : RoomDatabase() {
    abstract fun marketAlertDao(): MarketAlertDao
    abstract fun fdRateDao(): FDRateDao
    abstract fun ipoInfoDao(): IpoInfoDao
    abstract fun stockTickerDao(): StockTickerDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: BankingDatabase? = null

        fun getDatabase(context: Context): BankingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BankingDatabase::class.java,
                    "banking_analysis_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
