/*
 * Melo - by ParallelogramFoundation
 * Licensed Under GPL-3.0
 */

package com.nikhil.yt.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nikhil.yt.LocalDatabase
import com.nikhil.yt.LocalPlayerAwareWindowInsets
import com.nikhil.yt.R
import com.nikhil.yt.constants.ExoMaxBufferKey
import com.nikhil.yt.constants.ExoMinBufferKey
import com.nikhil.yt.constants.SupabaseAnonKey
import com.nikhil.yt.constants.SupabaseUrlKey
import com.nikhil.yt.ui.component.ActionPromptDialog
import com.nikhil.yt.ui.component.EditTextPreference
import com.nikhil.yt.ui.component.IconButton
import com.nikhil.yt.ui.component.PreferenceEntry
import com.nikhil.yt.ui.component.PreferenceGroupTitle
import com.nikhil.yt.ui.component.TextFieldDialog
import com.nikhil.yt.ui.utils.backToMain
import com.nikhil.yt.utils.GlobalLog
import com.nikhil.yt.utils.LogEntry
import com.nikhil.yt.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()

    val (supabaseUrl, onSupabaseUrlChange) = rememberPreference(
        key = SupabaseUrlKey,
        defaultValue = ""
    )
    val (supabaseAnon, onSupabaseAnonChange) = rememberPreference(
        key = SupabaseAnonKey,
        defaultValue = ""
    )
    val (exoMinBuffer, onExoMinBufferChange) = rememberPreference(
        key = ExoMinBufferKey,
        defaultValue = 0
    )
    val (exoMaxBuffer, onExoMaxBufferChange) = rememberPreference(
        key = ExoMaxBufferKey,
        defaultValue = 0
    )

    // Database stats
    val songCount by database.getSongCount().collectAsState(initial = 0)
    val playlistCount by database.getPlaylistCount().collectAsState(initial = 0)
    val historyCount by database.getHistoryCount().collectAsState(initial = 0)
    val lyricsCount by database.getLyricsCount().collectAsState(initial = 0)

    // Supabase testing state
    var testingConnection by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Not Tested") }
    var connectionColor by remember { mutableStateOf(Color.Gray) }

    // Dialog state
    var showResetDbDialog by remember { mutableStateOf(false) }
    var showPurgeLyricsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showMinBufferDialog by remember { mutableStateOf(false) }
    var showMaxBufferDialog by remember { mutableStateOf(false) }

    // Live users state
    var liveUsersCount by remember { mutableStateOf<Int?>(null) }
    var isRefreshingLiveUsers by remember { mutableStateOf(false) }

    // App Update Broadcaster state
    var updateVersionCode by remember { mutableStateOf("") }
    var updateVersionName by remember { mutableStateOf("") }
    var updateDownloadUrl by remember { mutableStateOf("") }
    var updateChangelog by remember { mutableStateOf("") }
    var updateForceUpdate by remember { mutableStateOf(false) }
    var isBroadcasting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fetch current live users
        coroutineScope.launch {
            isRefreshingLiveUsers = true
            withContext(Dispatchers.IO) {
                try {
                    val fiveMinutesAgo = java.time.Instant.now().minusSeconds(300).toString()
                    val sessions = com.nikhil.yt.supabase.from("active_sessions")
                        .select {
                            filter {
                                gte("last_seen_at", fiveMinutesAgo)
                            }
                        }.decodeList<com.nikhil.yt.ActiveSession>()
                    liveUsersCount = sessions.size
                } catch (e: Exception) {
                    e.printStackTrace()
                    liveUsersCount = -1
                }
            }
            isRefreshingLiveUsers = false
        }

        // Fetch current broadcast configuration
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val updates = com.nikhil.yt.supabase.from("app_updates")
                        .select().decodeList<com.nikhil.yt.AppUpdate>()
                    val latest = updates.firstOrNull()
                    if (latest != null) {
                        withContext(Dispatchers.Main) {
                            updateVersionCode = latest.version_code.toString()
                            updateVersionName = latest.version_name
                            updateDownloadUrl = latest.download_url
                            updateChangelog = latest.changelog
                            updateForceUpdate = latest.force_update
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Developer Panel", "System Logs")
    var isAuthenticated by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAuthenticated) "Admin & Developer Panel" else "Admin Login") },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain,
                    ) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (!isAuthenticated) {
            AdminLoginScreen(
                onLoginSuccess = { isAuthenticated = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTabIndex == 0) {
                // Developer Controls Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    PreferenceGroupTitle(title = "Supabase Cloud Integration")

                    EditTextPreference(
                        title = { Text("Supabase URL") },
                        value = supabaseUrl,
                        onValueChange = onSupabaseUrlChange,
                        icon = { Icon(painterResource(R.drawable.settings), null) }
                    )

                    EditTextPreference(
                        title = { Text("Supabase Anon Key") },
                        value = supabaseAnon,
                        onValueChange = onSupabaseAnonChange,
                        icon = { Icon(painterResource(R.drawable.experiment), null) }
                    )

                    PreferenceEntry(
                        title = { Text("Test Supabase Connection") },
                        description = "Check status: $connectionStatus",
                        icon = { Icon(painterResource(R.drawable.info), null) },
                        trailingContent = {
                            if (testingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(connectionColor, shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (connectionStatus.startsWith("Success")) "Connected" else if (connectionStatus == "Not Tested") "Not Tested" else "Error",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        onClick = {
                            coroutineScope.launch {
                                testingConnection = true
                                connectionStatus = "Testing..."
                                connectionColor = Color(0xFFE6A23C)
                                withContext(Dispatchers.IO) {
                                    try {
                                        if (supabaseUrl.isBlank() || supabaseAnon.isBlank()) {
                                            connectionStatus = "URL/Key cannot be blank"
                                            connectionColor = Color.Red
                                            return@withContext
                                        }
                                        val client = okhttp3.OkHttpClient.Builder()
                                            .connectTimeout(5, TimeUnit.SECONDS)
                                            .readTimeout(5, TimeUnit.SECONDS)
                                            .build()
                                        val request = okhttp3.Request.Builder()
                                            .url(supabaseUrl.trimEnd('/') + "/rest/v1/")
                                            .addHeader("apikey", supabaseAnon)
                                            .addHeader("Authorization", "Bearer $supabaseAnon")
                                            .get()
                                            .build()
                                        client.newCall(request).execute().use { response ->
                                            if (response.isSuccessful || response.code == 404 || response.code == 400 || response.code == 401) {
                                                if (response.code == 401 || response.code == 400) {
                                                    connectionStatus = "Unauthorized (HTTP ${response.code})"
                                                    connectionColor = Color.Red
                                                } else {
                                                    connectionStatus = "Success (HTTP ${response.code})"
                                                    connectionColor = Color(0xFF4CAF50)
                                                }
                                            } else {
                                                connectionStatus = "Failed (HTTP ${response.code})"
                                                connectionColor = Color.Red
                                            }
                                        }
                                    } catch (e: Exception) {
                                        connectionStatus = e.localizedMessage ?: "Connection Timeout"
                                        connectionColor = Color.Red
                                    }
                                }
                                testingConnection = false
                            }
                        }
                    )

                    PreferenceEntry(
                        title = { Text("Launch Supabase Todo Quickstart") },
                        description = "Open the test TodoActivity connected to Supabase",
                        onClick = {
                            val intent = android.content.Intent(context, com.nikhil.yt.TodoActivity::class.java)
                            context.startActivity(intent)
                        },
                        icon = { Icon(painterResource(R.drawable.experiment), null) }
                    )

                    PreferenceEntry(
                        title = { Text("Live Users Online") },
                        description = when {
                            isRefreshingLiveUsers -> "Refreshing user count..."
                            liveUsersCount == null -> "Loading..."
                            liveUsersCount == -1 -> "Failed to fetch active users"
                            else -> "$liveUsersCount active users (last 5 min)"
                        },
                        icon = { Icon(painterResource(R.drawable.account), null) },
                        trailingContent = {
                            if (isRefreshingLiveUsers) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.cached),
                                    contentDescription = "Refresh",
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            isRefreshingLiveUsers = true
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    val fiveMinutesAgo = java.time.Instant.now().minusSeconds(300).toString()
                                                    val sessions = com.nikhil.yt.supabase.from("active_sessions")
                                                        .select {
                                                            filter {
                                                                gte("last_seen_at", fiveMinutesAgo)
                                                            }
                                                        }.decodeList<com.nikhil.yt.ActiveSession>()
                                                    liveUsersCount = sessions.size
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    liveUsersCount = -1
                                                }
                                            }
                                            isRefreshingLiveUsers = false
                                        }
                                    }
                                )
                            }
                        }
                    )

                    PreferenceGroupTitle(title = "App Update Broadcaster")

                    EditTextPreference(
                        title = { Text("Update Version Code") },
                        value = updateVersionCode,
                        onValueChange = { updateVersionCode = it },
                        icon = { Icon(painterResource(R.drawable.info), null) }
                    )

                    EditTextPreference(
                        title = { Text("Update Version Name") },
                        value = updateVersionName,
                        onValueChange = { updateVersionName = it },
                        icon = { Icon(painterResource(R.drawable.info), null) }
                    )

                    EditTextPreference(
                        title = { Text("Download Website URL") },
                        value = updateDownloadUrl,
                        onValueChange = { updateDownloadUrl = it },
                        icon = { Icon(painterResource(R.drawable.link), null) }
                    )

                    EditTextPreference(
                        title = { Text("Changelog") },
                        value = updateChangelog,
                        onValueChange = { updateChangelog = it },
                        icon = { Icon(painterResource(R.drawable.text_fields), null) }
                    )

                    PreferenceEntry(
                        title = { Text("Force Update") },
                        description = "Require users to install this update to continue",
                        trailingContent = {
                            Switch(
                                checked = updateForceUpdate,
                                onCheckedChange = { updateForceUpdate = it }
                            )
                        },
                        onClick = { updateForceUpdate = !updateForceUpdate },
                        icon = { Icon(painterResource(R.drawable.security), null) }
                    )

                    PreferenceEntry(
                        title = { Text("Broadcast Update Now") },
                        description = if (isBroadcasting) "Broadcasting..." else "Save configuration to Supabase",
                        trailingContent = {
                            if (isBroadcasting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(painterResource(R.drawable.done), null)
                            }
                        },
                        onClick = {
                            val codeInt = updateVersionCode.toIntOrNull()
                            if (codeInt == null || updateVersionName.isBlank() || updateDownloadUrl.isBlank()) {
                                Toast.makeText(context, "Fill in Version Code, Name, and URL", Toast.LENGTH_SHORT).show()
                            } else {
                                coroutineScope.launch {
                                    isBroadcasting = true
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val newUpdate = com.nikhil.yt.AppUpdate(
                                                id = 1,
                                                version_code = codeInt,
                                                version_name = updateVersionName,
                                                download_url = updateDownloadUrl,
                                                changelog = updateChangelog,
                                                force_update = updateForceUpdate
                                            )
                                            com.nikhil.yt.supabase.from("app_updates").upsert(newUpdate)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Update broadcasted successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Broadcast failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                    isBroadcasting = false
                                }
                            }
                        },
                        icon = { Icon(painterResource(R.drawable.sync), null) }
                    )

                    PreferenceGroupTitle(title = "Playback Tweaks (ExoPlayer)")

                    PreferenceEntry(
                        title = { Text("ExoPlayer Min Buffer") },
                        description = if (exoMinBuffer == 0) "Default (50s)" else "$exoMinBuffer seconds",
                        onClick = { showMinBufferDialog = true },
                        icon = { Icon(painterResource(R.drawable.slow_motion_video), null) }
                    )

                    PreferenceEntry(
                        title = { Text("ExoPlayer Max Buffer") },
                        description = if (exoMaxBuffer == 0) "Default (50s)" else "$exoMaxBuffer seconds",
                        onClick = { showMaxBufferDialog = true },
                        icon = { Icon(painterResource(R.drawable.slow_motion_video), null) }
                    )

                    PreferenceGroupTitle(title = "Local Database & Storage Inspector")

                    PreferenceEntry(
                        title = { Text("Database Stats") },
                        description = "Songs: $songCount | Playlists: $playlistCount | Lyrics: $lyricsCount | History: $historyCount",
                        icon = { Icon(painterResource(R.drawable.info), null) }
                    )

                    PreferenceEntry(
                        title = { Text("Purge Lyrics Cache") },
                        description = "Delete cached lyrics entries in database",
                        onClick = { showPurgeLyricsDialog = true },
                        icon = { Icon(painterResource(R.drawable.delete_history), null) }
                    )

                    PreferenceEntry(
                        title = { Text("Clear Listen History") },
                        description = "Purge listen history records",
                        onClick = { showClearHistoryDialog = true },
                        icon = { Icon(painterResource(R.drawable.delete_history), null) }
                    )

                    PreferenceEntry(
                        title = { Text("Reset Database") },
                        description = "CAUTION: Destructive action to wipe all local db tables",
                        onClick = { showResetDbDialog = true },
                        icon = { Icon(painterResource(R.drawable.clear_all), null) }
                    )
                }
            } else {
                // System Logs Tab
                val logs by GlobalLog.logs.collectAsState(initial = emptyList())
                val clipboardManager = LocalClipboardManager.current

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Timber Output",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            TextButton(
                                onClick = {
                                    val allLogsText = logs.joinToString("\n") { GlobalLog.format(it) }
                                    clipboardManager.setText(AnnotatedString(allLogsText))
                                    Toast.makeText(context, "Logs copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Copy Logs")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { GlobalLog.clear() }
                            ) {
                                Text("Clear", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val lazyListState = rememberLazyListState()
                        LaunchedEffect(logs.size) {
                            if (logs.isNotEmpty()) {
                                lazyListState.animateScrollToItem(logs.size - 1)
                            }
                        }

                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            items(logs) { entry ->
                                Text(
                                    text = GlobalLog.format(entry),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = when (entry.level) {
                                        android.util.Log.ERROR -> Color(0xFFF44336)
                                        android.util.Log.WARN -> Color(0xFFFFEB3B)
                                        android.util.Log.INFO -> Color(0xFF2196F3)
                                        else -> Color(0xFF4CAF50)
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // Dialog dialogs
        if (showMinBufferDialog) {
            TextFieldDialog(
                initialTextFieldValue = TextFieldValue(text = if (exoMinBuffer == 0) "" else exoMinBuffer.toString()),
                placeholder = { Text("Default (50)") },
                onDone = { text ->
                    onExoMinBufferChange(text.toIntOrNull() ?: 0)
                },
                onDismiss = { showMinBufferDialog = false },
                isInputValid = { it.isEmpty() || it.toIntOrNull() != null }
            )
        }

        if (showMaxBufferDialog) {
            TextFieldDialog(
                initialTextFieldValue = TextFieldValue(text = if (exoMaxBuffer == 0) "" else exoMaxBuffer.toString()),
                placeholder = { Text("Default (50)") },
                onDone = { text ->
                    onExoMaxBufferChange(text.toIntOrNull() ?: 0)
                },
                onDismiss = { showMaxBufferDialog = false },
                isInputValid = { it.isEmpty() || it.toIntOrNull() != null }
            )
        }

        if (showPurgeLyricsDialog) {
            ActionPromptDialog(
                title = "Purge Lyrics Cache",
                onDismiss = { showPurgeLyricsDialog = false },
                onConfirm = {
                    database.query { clearLyrics() }
                    showPurgeLyricsDialog = false
                    Toast.makeText(context, "Lyrics cache purged", Toast.LENGTH_SHORT).show()
                },
                onCancel = { showPurgeLyricsDialog = false },
                content = { Text("Are you sure you want to clear all locally cached lyrics from database? They will be refetched on demand.") }
            )
        }

        if (showClearHistoryDialog) {
            ActionPromptDialog(
                title = "Clear Listen History",
                onDismiss = { showClearHistoryDialog = false },
                onConfirm = {
                    database.query { clearListenHistory() }
                    showClearHistoryDialog = false
                    Toast.makeText(context, "Listen history cleared", Toast.LENGTH_SHORT).show()
                },
                onCancel = { showClearHistoryDialog = false },
                content = { Text("Are you sure you want to clear your local listen history?") }
            )
        }

        if (showResetDbDialog) {
            ActionPromptDialog(
                title = "Reset Database",
                onDismiss = { showResetDbDialog = false },
                onConfirm = {
                    database.query { clearAllTables() }
                    showResetDbDialog = false
                    Toast.makeText(context, "Database tables cleared", Toast.LENGTH_LONG).show()
                },
                onCancel = { showResetDbDialog = false },
                content = { Text("CAUTION: This will wipe all tables (songs, playlists, history, lyrics, maps) from the local database. This action is irreversible.") }
            )
        }
    }
}

@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.security),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Authentication Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter developer credentials to access the console",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                showError = false
            },
            label = { Text("Username") },
            leadingIcon = {
                Icon(painter = painterResource(R.drawable.account), contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                showError = false
            },
            label = { Text("Password") },
            leadingIcon = {
                Icon(painter = painterResource(R.drawable.lock), contentDescription = null)
            },
            trailingIcon = {
                TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Text(if (isPasswordVisible) "Hide" else "Show")
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (showError) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Invalid username or password",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (username == "FOUNDATION" && password == "DATAPRYSM") {
                    onLoginSuccess()
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Login", fontWeight = FontWeight.Bold)
        }
    }
}
