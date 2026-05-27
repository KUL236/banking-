package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BankingViewModel
import com.example.ui.viewmodel.AppLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainDashboard(
    viewModel: BankingViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val fds by viewModel.fdRates.collectAsStateWithLifecycle()
    val ipos by viewModel.ipos.collectAsStateWithLifecycle()
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val isAnalyzingAlert by viewModel.isAnalyzingAlert.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val getStr = { key: String -> AppStrings.get(key, currentLanguage) }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Monitoring, 1: Alerts, 2: AI Analyst
    var activeSubTab by remember { mutableIntStateOf(0) } // For Tracker: 0: Stocks, 1: FDs, 2: IPOs

    // Calculate Bank Nifty live Index based on weighted stocks
    val bankNiftyIndex = remember(stocks) {
        if (stocks.isEmpty()) 48500.0 else {
            val sum = stocks.sumOf { it.price }
            sum * 10.0 // Scaled multiplier to emulate standard index pricing digits
        }
    }
    val bankNiftyChangePercent = remember(stocks) {
        if (stocks.isEmpty()) 0.0 else {
            stocks.map { it.changePercent }.average()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundPolish,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfacePolish)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Polished Indigo Initial Icon Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryPolish),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "B",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(SecondaryPolish)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = getStr("market_pulse_live"),
                                    color = SecondaryPolish,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = getStr("bank_pulse_ai"),
                                color = TextPolishPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Premium English / Hindi language toggler
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightAccentBlue)
                                .border(1.dp, PrimaryPolish.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleLanguage() }
                                .padding(horizontal = 12.dp)
                                .testTag("language_toggle_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.EN) "हिं" else "EN",
                                color = PrimaryPolish,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Ticker Index Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfacePolishVariant)
                                .border(1.dp, BorderPolish, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = getStr("bank_nifty_index"),
                                    color = TextPolishMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${String.format("%,.2f", bankNiftyIndex)}",
                                        color = TextPolishPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (bankNiftyChangePercent >= 0) {
                                            "+${String.format("%.2f", bankNiftyChangePercent)}%"
                                        } else {
                                            "${String.format("%.2f", bankNiftyChangePercent)}%"
                                        },
                                        color = if (bankNiftyChangePercent >= 0) SecondaryPolish else AlertRose,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderPolish, thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfacePolish,
                tonalElevation = 8.dp,
                modifier = Modifier.border(0.5.dp, BorderPolish, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Monitoring Stream") },
                    label = { Text(getStr("tab_monitor")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryPolish,
                        selectedTextColor = PrimaryPolish,
                        unselectedIconColor = TextPolishMuted,
                        unselectedTextColor = TextPolishMuted,
                        indicatorColor = LightAccentBlue
                    ),
                    modifier = Modifier.testTag("tab_monitor_stream")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (alerts.isNotEmpty()) {
                                Badge(containerColor = AlertRose) {
                                    Text(alerts.size.toString(), color = Color.White)
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Active Alerts")
                        }
                    },
                    label = { Text(getStr("tab_alerts")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryPolish,
                        selectedTextColor = PrimaryPolish,
                        unselectedIconColor = TextPolishMuted,
                        unselectedTextColor = TextPolishMuted,
                        indicatorColor = LightAccentBlue
                    ),
                    modifier = Modifier.testTag("tab_smart_alerts")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Filled.Face, contentDescription = "AI Assistant Advisor") },
                    label = { Text(getStr("tab_consultant")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryPolish,
                        selectedTextColor = PrimaryPolish,
                        unselectedIconColor = TextPolishMuted,
                        unselectedTextColor = TextPolishMuted,
                        indicatorColor = LightAccentBlue
                    ),
                    modifier = Modifier.testTag("tab_ai_advisor")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundPolish)
        ) {
            when (activeTab) {
                0 -> {
                    // TAB 1: Dashboard Tracker with SubTabs & Shock simulations
                    DashboardStreamView(
                        stocks = stocks,
                        fds = fds,
                        ipos = ipos,
                        activeSubTab = activeSubTab,
                        onSubTabSelected = { activeSubTab = it },
                        onShockTriggered = { viewModel.triggerMarketShock(it) },
                        lang = currentLanguage
                    )
                }
                1 -> {
                    // TAB 2: Smart Alerts with AI insight triggers
                    SmartAlertsView(
                        alerts = alerts,
                        isAnalyzingMap = isAnalyzingAlert,
                        onAnalyzeClicked = { viewModel.fetchAlertInsight(it) },
                        lang = currentLanguage
                    )
                }
                2 -> {
                    // TAB 3: AI Advisor Chat Console
                    AIAnalystChatView(
                        chatMessages = chatMessages,
                        isChatLoading = isChatLoading,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onClearChat = { viewModel.clearChat() },
                        lang = currentLanguage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardStreamView(
    stocks: List<StockTicker>,
    fds: List<FDRate>,
    ipos: List<IpoInfo>,
    activeSubTab: Int,
    onSubTabSelected: (Int) -> Unit,
    onShockTriggered: (String) -> Unit,
    lang: AppLanguage
) {
    val getStr = { key: String -> AppStrings.get(key, lang) }
    Column(modifier = Modifier.fillMaxSize()) {
        // Shocks Control Center card header styled like the premium dark insight banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0F172A)) // Crisp slate dark background for the insight center
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryPolish.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = getStr("ai_control_hub"),
                            color = Color(0xFFA5B4FC), // Indigo-300
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getStr("scenario_injector"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = getStr("scenario_desc"),
                    color = Color(0xFF94A3B8), // slate-400
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onShockTriggered("CRASH") },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRose),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("shock_crash")
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Crash Stocks", modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == AppLanguage.HI) "स्टॉक फ्लैश क्रैश" else "Stocks Flash Crash", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onShockTriggered("FD_BOOM") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPolish),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("shock_fd_boom")
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Boost FD Slabs", modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == AppLanguage.HI) "FD ब्याज दर बढ़ाएं (+50 bps)" else "Boost FD Rates (+50 bps)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onShockTriggered("IPO_BURST") },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPolish),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("shock_ipo_frenzy")
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Boost IPO premium", modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == AppLanguage.HI) "IPO जीएमपी सर्ज (+15%)" else "IPO GMP Surge (+15%)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sector Tab Selection Layout
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = BackgroundPolish,
            contentColor = PrimaryPolish,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = PrimaryPolish
                )
            },
            divider = { HorizontalDivider(color = BorderPolish, thickness = 1.dp) }
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { onSubTabSelected(0) },
                text = { Text(getStr("stocks"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (activeSubTab == 0) PrimaryPolish else TextPolishMuted) },
                modifier = Modifier.testTag("subtab_stocks")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { onSubTabSelected(1) },
                text = { Text(getStr("fds"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (activeSubTab == 1) PrimaryPolish else TextPolishMuted) },
                modifier = Modifier.testTag("subtab_fds")
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { onSubTabSelected(2) },
                text = { Text(getStr("ipos"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (activeSubTab == 2) PrimaryPolish else TextPolishMuted) },
                modifier = Modifier.testTag("subtab_ipos")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Track lists depending on selected subclass
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            when (activeSubTab) {
                0 -> StocksMonitoringList(stocks, lang)
                1 -> FDComparisonsAndOptimizer(fds, lang)
                2 -> IPOListingTelemetry(ipos, lang)
            }
        }
    }
}

@Composable
fun StocksMonitoringList(stocks: List<StockTicker>, lang: AppLanguage) {
    val getStr = { key: String -> AppStrings.get(key, lang) }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(stocks, key = { it.symbol }) { stock ->
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = SurfacePolish),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPolish, RoundedCornerShape(16.dp))
                    .testTag("stock_item_${stock.symbol}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.symbol,
                                color = PrimaryPolish,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LightAccentBlue)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = getStr("volatile"),
                                    color = PrimaryPolish,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = stock.name,
                            color = TextPolishPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${getStr("high")}${String.format("%.2f", stock.high)}  ${getStr("low")}${String.format("%.2f", stock.low)}",
                            color = TextPolishMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${String.format("%,.2f", stock.price)}",
                            color = TextPolishPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (stock.changePercent >= 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (stock.changePercent >= 0) "UP" else "DOWN",
                                tint = if (stock.changePercent >= 0) SecondaryPolish else AlertRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format("%.2f", stock.changePercent)}%",
                                color = if (stock.changePercent >= 0) SecondaryPolish else AlertRose,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FDComparisonsAndOptimizer(fds: List<FDRate>, lang: AppLanguage) {
    val getStr = { key: String -> AppStrings.get(key, lang) }
    var advisorState by remember { mutableStateOf<String?>(null) }
    var selectedGoal by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Smart FD Tenure Allocator Wizard Card
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = SurfacePolish),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPolish, RoundedCornerShape(16.dp))
                    .testTag("fd_optimizer_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Home, contentDescription = "Optimizer", tint = PrimaryPolish, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(getStr("fd_optimizer_title"), color = PrimaryPolish, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = getStr("fd_optimizer_desc"),
                        color = TextPolishPrimary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val goals = if (lang == AppLanguage.HI) {
                            listOf(
                                "अल्पावधि" to "1 Year",
                                "मध्यमावधि" to "3 Years",
                                "दीर्घावधि" to "5 Years"
                            )
                        } else {
                            listOf(
                                "Short-Term" to "1 Year",
                                "Mid-Term" to "3 Years",
                                "Long-Term" to "5 Years"
                            )
                        }
                        
                        goals.forEach { (goalLabel, tenure) ->
                            val isSelected = selectedGoal == goalLabel
                            OutlinedButton(
                                onClick = {
                                    selectedGoal = goalLabel
                                    // Local rule recommendation logic
                                    val topFd = fds.filter { it.tenure == tenure }.maxByOrNull { it.interestRate }
                                    advisorState = if (topFd != null) {
                                        if (lang == AppLanguage.HI) {
                                            "🎯 **शीर्ष सुरक्षित अनुशंसा**: $goalLabel होल्डिंग्स ($tenure अवधि) के लिए, **${topFd.bankName}** में निवेश करें जो **${topFd.interestRate}%** (और वरिष्ठ नागरिकों के लिए **${topFd.seniorCitizenRate}%**) की अधिकतम ब्याज दर प्रदान करता है। बाजार के उतार-चढ़ाव से बचने के लिए पूंजी को स्थानांतरित करने की सलाह दी जाती है।"
                                        } else {
                                            "🎯 **Top Safe Recommendation**: For $goalLabel holdings ($tenure tenure), allocate into **${topFd.bankName}** which locks in the maximum interest slab of **${topFd.interestRate}%** (and **${topFd.seniorCitizenRate}%** for Senior Citizens). Shifting capital now is advised to hedge stock market downswings."
                                        }
                                    } else {
                                        if (lang == AppLanguage.HI) "कोई जमा डेटा उपलब्ध नहीं है।" else "No deposit data available."
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryPolish else BorderPolish
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) LightAccentBlue else Color.Transparent
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 3.dp)
                                    .heightIn(min = 40.dp)
                                    .testTag("goal_btn_${goalLabel}")
                            ) {
                                Text(goalLabel, fontSize = 11.sp, color = if (isSelected) PrimaryPolish else TextPolishPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    advisorState?.let { recommendation ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                             modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfacePolishVariant)
                                .border(1.dp, BorderPolish, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = recommendation,
                                color = TextPolishPrimary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(getStr("slabs_title"), color = TextPolishMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Group FDs by bank
        val groupedFDs = fds.groupBy { it.bankName }
        items(groupedFDs.keys.toList()) { bankName ->
            val list = groupedFDs[bankName] ?: emptyList()
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = SurfacePolish),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPolish, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = bankName.uppercase(),
                        color = TextPolishPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        list.forEach { rate ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfacePolishVariant)
                                    .border(1.dp, BorderPolish, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(rate.tenure, color = TextPolishMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${rate.interestRate}%", color = SecondaryPolish, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("${getStr("sr_citizen")}${rate.seniorCitizenRate}%", color = TextPolishMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IPOListingTelemetry(ipos: List<IpoInfo>, lang: AppLanguage) {
    val getStr = { key: String -> AppStrings.get(key, lang) }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(ipos) { ipo ->
            val isGmpPositive = ipo.gmp >= 0.0
            val statusColor = when (ipo.status) {
                "OPEN" -> SecondaryPolish
                "UPCOMING" -> TertiaryPolish
                else -> TextPolishMuted
            }

            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = SurfacePolish),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPolish, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ipo.companyName, color = TextPolishPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("${getStr("listing_timeline")}${ipo.listingDate}", color = TextPolishMuted, fontSize = 11.sp)
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(ipo.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderPolish, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(getStr("gmp_premium"), color = TextPolishMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isGmpPositive) "+${ipo.gmp}%" else "${ipo.gmp}%",
                                color = if (isGmpPositive) SecondaryPolish else AlertRose,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(getStr("price_band"), color = TextPolishMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(ipo.priceBand, color = TextPolishPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(getStr("subscription"), color = TextPolishMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${ipo.subscriptionX}x", color = PrimaryPolish, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartAlertsView(
    alerts: List<MarketAlert>,
    isAnalyzingMap: Map<Int, Boolean>,
    onAnalyzeClicked: (MarketAlert) -> Unit,
    lang: AppLanguage
) {
    val getStr = { key: String -> AppStrings.get(key, lang) }
    if (alerts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "No alerts", tint = TextPolishMuted, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(getStr("no_alerts"), color = TextPolishPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(getStr("no_alerts_desc"), color = TextPolishMuted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Alerts Active", tint = AlertRose, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(getStr("warnings_title"), color = AlertRose, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(alerts, key = { it.id }) { alert ->
                val severityColor = when (alert.severity) {
                    "HIGH" -> AlertRose
                    "MEDIUM" -> TertiaryPolish
                    else -> PrimaryPolish
                }

                val icon = when (alert.type) {
                    "VOLATILITY" -> Icons.Default.Warning
                    "FD" -> Icons.Default.Home
                    "IPO" -> Icons.Default.Add
                    else -> Icons.Default.Notifications
                }

                val isLoading = isAnalyzingMap[alert.id] ?: false

                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = SurfacePolish),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (alert.severity == "HIGH") AlertRose.copy(alpha = 0.5f) else BorderPolish,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sector Tag
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(severityColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(alert.severity, color = severityColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(alert.type, color = TextPolishMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "Tick: ${android.text.format.DateFormat.format("HH:mm:ss", alert.timestamp)}",
                                color = TextPolishMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Icon(icon, contentDescription = alert.type, tint = severityColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(if (lang == AppLanguage.HI && alert.title.contains("VOLATILITY SPIKE")) "मार्केट अस्थिरता स्पाइक!" else alert.title, color = TextPolishPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(alert.description, color = TextPolishMuted, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gemini Advisor expand button or generated insight box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfacePolishVariant)
                                .border(1.dp, BorderPolish, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            if (alert.aiInsight != null) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(PrimaryPolish)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(getStr("ai_intelligence_title"), color = PrimaryPolish, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = alert.aiInsight,
                                        color = TextPolishPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1.0f)
                                    ) {
                                        Icon(Icons.Filled.Info, contentDescription = "AI icon", tint = PrimaryPolish, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(getStr("ask_reallocs"), color = TextPolishMuted, fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = { onAnalyzeClicked(alert) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPolish),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        enabled = !isLoading,
                                        modifier = Modifier
                                            .heightIn(min = 36.dp)
                                            .testTag("analyze_btn_${alert.id}")
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(getStr("thinking"), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(getStr("ai_insights_btn"), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIAnalystChatView(
    chatMessages: List<ChatMessage>,
    isChatLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    lang: AppLanguage
) {
    val getStr = { key: String -> AppStrings.get(key, lang) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty() || isChatLoading) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.size + if (isChatLoading) 1 else 0)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfacePolish)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Face, contentDescription = "Terminal Agent", tint = PrimaryPolish, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = getStr("gemini_context_title"),
                    color = TextPolishPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            IconButton(onClick = onClearChat, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = getStr("clear_chat"), tint = TextPolishMuted, modifier = Modifier.size(16.dp))
            }
        }
        HorizontalDivider(color = BorderPolish, thickness = 1.dp)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Welcome card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfacePolishVariant)
                        .border(1.dp, BorderPolish, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = getStr("query_guide_title"),
                            color = PrimaryPolish,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getStr("query_guide_desc"),
                            color = TextPolishPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(chatMessages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (message.isFromUser) 14.dp else 2.dp,
                                    bottomEnd = if (message.isFromUser) 2.dp else 14.dp
                                )
                            )
                            .background(if (message.isFromUser) PrimaryPolish else SurfacePolish)
                            .border(
                                1.dp,
                                if (message.isFromUser) Color.Transparent else BorderPolish,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Column {
                            Text(
                                text = if (message.isFromUser) getStr("investor_query") else getStr("ai_advisor_title"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (message.isFromUser) Color.White.copy(alpha = 0.8f) else PrimaryPolish,
                                modifier = Modifier.padding(bottom = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = message.text,
                                color = if (message.isFromUser) Color.White else TextPolishPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp))
                                .background(SurfacePolish)
                                .border(1.dp, BorderPolish, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = PrimaryPolish,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = getStr("advisor_computing"),
                                    color = TextPolishMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chip prompts for instant suggestions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            val suggestions = if (lang == AppLanguage.HI) {
                listOf(
                    getStr("suggest_fd"),
                    getStr("suggest_ipo"),
                    getStr("suggest_analysis")
                )
            } else {
                listOf(
                    "Optimize Stocks vs FDs ratio",
                    "Should I subscribe to PayTech IPO?",
                    "Is State Bank of India stock bearish?"
                )
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach { suggestion ->
                    OutlinedButton(
                        onClick = {
                            onSendMessage(suggestion)
                            focusManager.clearFocus()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderPolish),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfacePolish),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 36.dp)
                    ) {
                        Text(suggestion, fontSize = 10.sp, color = TextPolishPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Input Area
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .fillMaxWidth()
                .background(SurfacePolish)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text(getStr("chat_placeholder"), color = TextPolishMuted, fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfacePolishVariant,
                    unfocusedContainerColor = SurfacePolishVariant,
                    focusedTextColor = TextPolishPrimary,
                    unfocusedTextColor = TextPolishPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, BorderPolish, RoundedCornerShape(20.dp))
                    .testTag("chat_input_text")
            )

            Spacer(modifier = Modifier.width(10.dp))

            FloatingActionButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        onSendMessage(inputQuery.trim())
                        inputQuery = ""
                        focusManager.clearFocus()
                    }
                },
                containerColor = PrimaryPolish,
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send Message to AI",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
