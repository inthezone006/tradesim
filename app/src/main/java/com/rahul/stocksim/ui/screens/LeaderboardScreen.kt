package com.rahul.stocksim.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

data class LeaderboardUser(
    val id: String,
    val name: String,
    val totalAccountValue: Double,
    val photoUrl: String? = null,
    val level: Int = 4
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LeaderboardScreen(mainNavController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    val haptic = LocalHapticFeedback.current
    
    val levels = listOf(0, 1, 2, 3, 4, 5, 6, 7)
    val pagerState = rememberPagerState(pageCount = { levels.size })
    val coroutineScope = rememberCoroutineScope()

    var leaders by remember { mutableStateOf<List<LeaderboardUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val selectedLevelFilter = pagerState.currentPage

    val fetchLeaders = {
        coroutineScope.launch {
            errorMessage = null
            if (!isRefreshing) isLoading = true
            try {
                // Determine the query
                val baseQuery = if (selectedLevelFilter == 0) {
                    firestore.collection("users")
                        .orderBy("totalAccountValue", Query.Direction.DESCENDING)
                } else {
                    firestore.collection("users")
                        .whereEqualTo("level", selectedLevelFilter)
                        .orderBy("totalAccountValue", Query.Direction.DESCENDING)
                }

                // Increase limit to show more users
                val snapshot = try {
                    baseQuery.limit(100).get().await()
                } catch (e: Exception) {
                    if (e.message?.contains("PERMISSION_DENIED") == true) {
                        errorMessage = "Access denied. Please check your internet or account."
                        null
                    } else throw e
                }

                if (snapshot != null) {
                    leaders = snapshot.documents.map { doc ->
                        LeaderboardUser(
                            id = doc.id,
                            name = doc.getString("displayName") ?: doc.getString("email")?.split("@")?.get(0) ?: "Trader",
                            totalAccountValue = (doc.get("totalAccountValue") as? Number)?.toDouble() 
                                ?: (doc.get("balance") as? Number)?.toDouble() ?: 0.0,
                            photoUrl = doc.getString("photoUrl"),
                            level = ((doc.get("level") as? Number)?.toLong() ?: 4L).toInt()
                        )
                    }
                    
                    if (leaders.isEmpty()) {
                        Log.d("Leaderboard", "No users found for level $selectedLevelFilter")
                    }
                }
            } catch (e: Exception) {
                Log.e("Leaderboard", "Query failed, falling back to local filter", e)
                if (e !is kotlinx.coroutines.CancellationException && 
                    e !is java.net.UnknownHostException &&
                    e !is java.net.SocketTimeoutException) {
                    Firebase.crashlytics.recordException(e)
                }
                
                // Fallback: Fetch all users and filter locally if the index isn't ready
                try {
                    val fallbackSnapshot = firestore.collection("users")
                        .orderBy("totalAccountValue", Query.Direction.DESCENDING)
                        .limit(200)
                        .get().await()
                    
                    val allUsers = fallbackSnapshot.documents.map { doc ->
                        LeaderboardUser(
                            id = doc.id,
                            name = doc.getString("displayName") ?: doc.getString("email")?.split("@")?.get(0) ?: "Trader",
                            totalAccountValue = (doc.get("totalAccountValue") as? Number)?.toDouble() 
                                ?: (doc.get("balance") as? Number)?.toDouble() ?: 0.0,
                            photoUrl = doc.getString("photoUrl"),
                            level = ((doc.get("level") as? Number)?.toLong() ?: 4L).toInt()
                        )
                    }
                    
                    leaders = if (selectedLevelFilter == 0) {
                        allUsers
                    } else {
                        allUsers.filter { it.level == selectedLevelFilter }
                    }
                } catch (fallbackEx: Exception) {
                    if (fallbackEx !is kotlinx.coroutines.CancellationException && 
                        fallbackEx !is java.net.UnknownHostException &&
                        fallbackEx !is java.net.SocketTimeoutException) {
                        com.google.firebase.Firebase.crashlytics.recordException(fallbackEx)
                    }
                    leaders = emptyList()
                    errorMessage = "Leaderboard currently unavailable."
                }
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(selectedLevelFilter) {
        fetchLeaders()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212))
    ) {
        // Static Header Area
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Level Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Color.White
                    )
                }
            ) {
                levels.forEachIndexed { index, level ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            if (pagerState.currentPage != index) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        text = { 
                            Text(
                                text = if (level == 0) "All" else "Level $level",
                                fontSize = 14.sp
                            ) 
                        }
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { pageIndex ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    val currentUserLeader = leaders.find { it.id == currentUserId }
                    val currentUserRank = leaders.indexOfFirst { it.id == currentUserId }.let { if (it != -1) it + 1 else null }
                    
                    if (currentUserLeader != null) {
                        CurrentUserSummary(rank = currentUserRank, userValue = currentUserLeader.totalAccountValue)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (isLoading && !isRefreshing) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.7f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator(color = Color.White)
                        }
                    }
                } else if (leaders.isEmpty() && errorMessage == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.7f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (levels[pageIndex] == 0) "No traders found." else "No traders found for Level ${levels[pageIndex]}.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else if (errorMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.7f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(errorMessage!!, color = Color.Gray)
                        }
                    }
                } else {
                    itemsIndexed(leaders) { index, user ->
                        LeaderCard(
                            rank = index + 1,
                            user = user, 
                            isCurrentUser = user.id == currentUserId
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun CurrentUserSummary(rank: Int?, userValue: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Your Global Rank", color = Color.Gray, fontSize = 12.sp)
                Text(
                    text = if (rank != null) "#$rank" else "Processing...",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Portfolio Value", color = Color.Gray, fontSize = 12.sp)
                Text(
                    text = "$${String.format("%,.2f", userValue)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}





@Composable
fun LeaderCard(rank: Int, user: LeaderboardUser, isCurrentUser: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF2C2C2C) else Color(0xFF1F1F1F)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isCurrentUser) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                color = when(rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.Gray
                },
                fontWeight = FontWeight.Bold,
                fontSize = if (rank >= 100) 14.sp else 18.sp,
                modifier = Modifier.width(44.dp),
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            ) {
                if (user.photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(user.name.firstOrNull()?.toString()?.uppercase() ?: "?", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name, 
                        color = Color.White, 
                        fontWeight = if (isCurrentUser) FontWeight.ExtraBold else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "👑", fontSize = 14.sp)
                    }
                }
                Text(text = "Level ${user.level}", color = Color.Gray, fontSize = 12.sp)
            }

            Text(
                text = "$${String.format("%,.0f", user.totalAccountValue)}",
                color = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}



