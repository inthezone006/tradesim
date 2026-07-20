package com.rahul.stocksim.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rahul.stocksim.model.Stock
import com.rahul.stocksim.ui.viewmodels.InsightsUiState
import com.rahul.stocksim.ui.viewmodels.InsightsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onStockClick: (Stock) -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        when (val state = uiState) {
            is InsightsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is InsightsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Text(
                            text = "Market Insights",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Global Indices
                    item {
                        Column {
                            Text(
                                "Global Indices",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.insights.indices) { index ->
                                    IndexCard(index) { onStockClick(index) }
                                }
                            }
                        }
                    }

                    // Sector Performance
                    item {
                        Column {
                            Text(
                                "Sector Performance",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            state.insights.sectors.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { sector ->
                                        SectorItem(sector, modifier = Modifier.weight(1f), onClick = { onStockClick(sector) })
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    // Top Movers
                    item {
                        var selectedTab by remember { mutableIntStateOf(0) }
                        Column {
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                divider = {},
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                        color = Color.White
                                    )
                                }
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("Top Gainers") }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Top Losers") }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val movers = if (selectedTab == 0) state.insights.gainers else state.insights.losers
                            movers.forEach { mover ->
                                MoverItem(mover, onClick = { onStockClick(mover) })
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
            is InsightsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun IndexCard(stock: Stock, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stock.symbol, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                // Small Logo
                if (!stock.logoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = stock.logoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text("$${String.format(Locale.US, "%,.2f", stock.price)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            
            val color = if (stock.change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            val prefix = if (stock.change >= 0) "+" else ""
            Text(
                text = "$prefix${String.format(Locale.US, "%.2f", stock.percentChange)}%",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectorItem(stock: Stock, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color(0xFF1F1F1F),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getSectorIcon(stock.symbol),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(stock.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                
                val color = if (stock.percentChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                val prefix = if (stock.percentChange >= 0) "+" else ""
                Text(
                    text = "$prefix${String.format(Locale.US, "%.2f", stock.percentChange)}%",
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getSectorIcon(symbol: String): ImageVector = when(symbol) {
    "XLK" -> Icons.Default.Memory
    "XLF" -> Icons.Default.AccountBalance
    "XLV" -> Icons.Default.MedicalServices
    "XLY" -> Icons.Default.ShoppingBag
    "XLP" -> Icons.Default.ShoppingCart
    "XLE" -> Icons.Default.Bolt
    "XLI" -> Icons.Default.Business
    "XLB" -> Icons.Default.Construction
    "XLRE" -> Icons.Default.HomeWork
    "XLU" -> Icons.Default.Lightbulb
    else -> Icons.Default.Category
}

@Composable
fun MoverItem(stock: Stock, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFF1F1F1F),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                if (!stock.logoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = stock.logoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = stock.symbol.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(stock.symbol, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(stock.name, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format(Locale.US, "%,.2f", stock.price)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                
                val color = if (stock.change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                val prefix = if (stock.change >= 0) "+" else ""
                Text(
                    text = "$prefix${String.format(Locale.US, "%.2f", stock.percentChange)}%",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
