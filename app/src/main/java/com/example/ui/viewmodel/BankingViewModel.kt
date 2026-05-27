package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.models.*
import com.example.data.repository.BankingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppLanguage {
    EN, HI
}

class BankingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BankingRepository(application)

    val alerts: StateFlow<List<MarketAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fdRates: StateFlow<List<FDRate>> = repository.allFDRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ipos: StateFlow<List<IpoInfo>> = repository.allIpos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stocks: StateFlow<List<StockTicker>> = repository.allStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _isAnalyzingAlert = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isAnalyzingAlert: StateFlow<Map<Int, Boolean>> = _isAnalyzingAlert.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            startMonitoringSimulation()
        }
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.EN) AppLanguage.HI else AppLanguage.EN
    }

    private fun startMonitoringSimulation() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(6000) // Continues standard stock ticks every 6 seconds
                repository.tickStocksAndCheckAlerts()
            }
        }
    }

    fun triggerMarketShock(type: String) {
        viewModelScope.launch {
            repository.triggerShockEvent(type)
        }
    }

    fun fetchAlertInsight(alert: MarketAlert) {
        if (alert.aiInsight != null) return
        
        viewModelScope.launch {
            _isAnalyzingAlert.update { it + (alert.id to true) }
            val prompt = """
                Analyse this banking market alert and provide professional, highly actionable investment insights. Tell me which instruments (FDs, stock trades, or IPOs) to buy/avoid/hold based on this alert:
                
                Alert: ${alert.title}
                Details: ${alert.description}
                Sector: ${alert.type}
                Severity: ${alert.severity}
            """.trimIndent()
            
            val languageInstruction = if (_currentLanguage.value == AppLanguage.HI) {
                " You MUST respond in Hindi (हिंदी). Offer clear, professional, and easily understandable terminal advisories in Hindi. Use simple vocabulary or standard Devanagari script for terms if helpful."
            } else {
                " Respond in English."
            }
            
            val systemPrompt = "You are a senior hedge-fund partner and banking analyst under the system name BankPulse AI. Give concise, highly polished bullet points advising retail and HNI investors on security allocation in stocks, FDs, and IPO lists. Highlight specific recommendations. Keep answers concise.$languageInstruction"
            
            val response = RetrofitClient.getAnalystResponse(prompt, systemPrompt)
            repository.updateAlertInsight(alert.id, response)
            _isAnalyzingAlert.update { it + (alert.id to false) }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.saveChatMessage(text, isFromUser = true)
            _isChatLoading.value = true
            
            val languageInstruction = if (_currentLanguage.value == AppLanguage.HI) {
                " The user is asking in Hindi or prefers Hindi. You MUST response inside Devanagari script using appropriate professional Hindi (हिंदी). Explain financial insights using clear, simple terminal bulletins in Hindi."
            } else {
                " Respond in English."
            }
            
            val systemPrompt = "You are BankPulse AI, a continuous automated banking monitoring advisor. You analyze FD rates, IPO lists (including premium/pricing/GMP), and Banking Stocks (including technical tickers). Answer queries precisely, with structured professional terminal bulletins.$languageInstruction"
            
            val currentStocks = stocks.value.joinToString("\n") { "${it.symbol}: Price ₹${String.format("%.2f", it.price)} (${String.format("%.2f", it.changePercent)}%)" }
            val currentFDs = fdRates.value.joinToString("\n") { "${it.bankName} (${it.tenure}): Rate ${it.interestRate}%" }
            val currentIpos = ipos.value.joinToString("\n") { "${it.companyName}: GMP ${it.gmp}% Subscription ${it.subscriptionX}x Status ${it.status}" }
            
            val fullPrompt = """
                User Query: $text
                
                Current Market Monitoring Stream:
                -- STOCKS --
                $currentStocks
                
                -- FIXED DEPOSITS --
                $currentFDs
                
                -- LISTING IPOS --
                $currentIpos
                
                Provide clear, formatted Terminal responses.
            """.trimIndent()

            val aiResponse = RetrofitClient.getAnalystResponse(fullPrompt, systemPrompt)
            repository.saveChatMessage(aiResponse, isFromUser = false)
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}
