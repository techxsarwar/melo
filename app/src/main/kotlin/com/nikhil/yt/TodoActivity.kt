package com.nikhil.yt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val supabase = createSupabaseClient(
    supabaseUrl = "https://xeojpcqnzugmddeuetdo.supabase.co",
    supabaseKey = "sb_publishable_LjCz2rKxJW7T6nLQI9JPYA_zwaH0MFP"
) {
    install(Postgrest)
}

class TodoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoList()
                }
            }
        }
    }
}

@Composable
fun TodoList() {
    var items by remember { mutableStateOf<List<TodoItem>>(listOf()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                items = supabase.from("todos")
                    .select().decodeList<TodoItem>()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    LazyColumn {
        items(
            items,
            key = { item -> item.id },
        ) { item ->
            Text(
                item.name,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
