package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JournalEntry
import com.example.data.ProtocolDefinition
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun getProtocolColor(key: String): Color {
    val dark = isSystemInDarkTheme()
    return when (key) {
        "GRATITUDE" -> if (dark) RoseHighlightDark else RoseHighlightLight
        "STOIC_DECISION" -> if (dark) EmeraldHighlightDark else EmeraldHighlightLight
        "DAILY_INTENTION" -> if (dark) AmberHighlightDark else AmberHighlightLight
        "FEAR_SETTING" -> if (dark) IndigoHighlightDark else IndigoHighlightLight
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
fun getProtocolIcon(key: String) = when (key) {
    "GRATITUDE" -> Icons.Default.Favorite
    "STOIC_DECISION" -> Icons.Default.Star
    "DAILY_INTENTION" -> Icons.Default.Settings
    "FEAR_SETTING" -> Icons.Default.Info
    else -> Icons.Default.Create
}

// Struct for cycling quotes
data class StoicQuote(val text: String, val author: String)

val StoicQuotes = listOf(
    StoicQuote("We are more often frightened than hurt; and we suffer more from imagination than from reality.", "Seneca"),
    StoicQuote("He is a wise man who does not grieve for the things which he has not, but rejoices for those which he has.", "Epictetus"),
    StoicQuote("No random actions, none not based on underlying principles.", "Marcus Aurelius"),
    StoicQuote("The happiness of your life depends upon the quality of your thoughts.", "Marcus Aurelius"),
    StoicQuote("Control what is inside you. Accept what is outside.", "Epictetus"),
    StoicQuote("Nothing is enough for the man to whom enough is too little.", "Epicurus"),
    StoicQuote("Receive without pride, let go without struggle.", "Marcus Aurelius")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDashboard(
    viewModel: JournalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val entries by viewModel.entriesList.collectAsState()
    val isWriting by viewModel.isWriting.collectAsState()
    val activeProtocol by viewModel.activeProtocol.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    // Pick a quote based on the current day's index so it updates daily
    val quoteOfDay = remember {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        StoicQuotes[dayOfYear % StoicQuotes.size]
    }

    if (isWriting) {
        BackHandler {
            viewModel.cancelDraft()
        }
        
        // Active entry composer mode
        ProtocolWriterScreen(
            protocol = activeProtocol,
            draftResponses = viewModel.draftResponses,
            onValueChange = { key, valStr -> viewModel.updateDraftResponse(key, valStr) },
            onCancel = { viewModel.cancelDraft() },
            onSave = { viewModel.saveDraft() }
        )
    } else {
        // Main list screen view
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Notebook Protocols",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp
                        )
                    },
                    actions = {
                        if (entries.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    shareAllEntries(context, viewModel.getFullBackupMarkdown(entries))
                                },
                                modifier = Modifier.testTag("export_all_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export backup of all entries"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Quote Section
                item {
                    QuoteHeaderCard(quote = quoteOfDay)
                }

                // Section title
                item {
                    Text(
                        text = "Reflexive Practices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Horizontal alignment list of protocols to launch composition
                item {
                    HorizontalProtocolSelector(
                        onSelect = { protocol ->
                            viewModel.startNewEntry(protocol)
                        }
                    )
                }

                // Feed Section Header & Search
                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "Historic Notebook Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("Filter logs by text content...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Feed display list
                if (entries.isEmpty()) {
                    item {
                        EmptyFeedState(isSearching = searchQuery.isNotEmpty())
                    }
                } else {
                    items(
                        items = entries,
                        key = { it.id }
                    ) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onEdit = { viewModel.startEditing(entry) },
                            onDelete = { viewModel.deleteEntry(entry.id) },
                            onExport = { shareEntry(context, entry) }
                        )
                    }
                }
                
                // Bottom padding spacer
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun QuoteHeaderCard(quote: StoicQuote) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "“${quote.text}”",
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "— ${quote.author}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun HorizontalProtocolSelector(
    onSelect: (ProtocolDefinition) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ProtocolDefinition.ALL_PROTOCOLS.forEach { protocol ->
            val colorAccent = getProtocolColor(protocol.key)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, colorAccent.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(protocol) }
                    .testTag("launch_protocol_${protocol.key.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getProtocolIcon(protocol.key),
                            contentDescription = null,
                            tint = colorAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = protocol.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = protocol.tagline,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowBack, // Rotate to act as forward arrow
                        contentDescription = "Select protocol",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyFeedState(isSearching: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Default.Search else Icons.Default.Create,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearching) "No matching logs found" else "Your Reflective Space is Empty",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isSearching) "Try clearing search keywords or trying another query." else "Select one of the structured protocols above to commit your daily entry.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val accentColor = getProtocolColor(entry.protocolKey)
    val sdf = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
    val formattedDate = remember(entry.timestamp) { sdf.format(Date(entry.timestamp)) }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("entry_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Accent bar & top details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Micro-bar reflecting primary protocol selection color
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.protocolTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collaspe" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            // Rendered Content text
            val responses = remember { ProtocolDefinition.parseResponses(entry.promptResponseMapJson) }
            val definition = remember { ProtocolDefinition.getByKey(entry.protocolKey) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                if (expanded) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 12.dp))
                    
                    definition.fields.forEach { field ->
                        val resp = responses[field.key]?.trim() ?: ""
                        if (resp.isNotEmpty()) {
                            Text(
                                text = field.prompt,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = accentColor,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = resp,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                    
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Trigger buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onExport,
                            modifier = Modifier.testTag("export_entry_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export entry as markdown text",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                        
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.testTag("edit_entry_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check, // Edit symbol fallback or check
                                contentDescription = "Edit historical response",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                        
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_entry_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove entry",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
                            )
                        }
                    }
                } else {
                    // Minimal list preview (shows only the first prompt response)
                    val firstFieldKey = definition.fields.firstOrNull()?.key
                    val previewTxt = firstFieldKey?.let { responses[it]?.trim() } ?: ""
                    
                    if (previewTxt.isNotEmpty()) {
                        Text(
                            text = previewTxt,
                            maxLines = 2,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(
                            text = "Tap to expand and view response fragments.",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocolWriterScreen(
    protocol: ProtocolDefinition,
    draftResponses: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    val accentColor = getProtocolColor(protocol.key)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = protocol.title,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.testTag("close_writer_button")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Discard and close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        colors = ButtonDefaults.textButtonColors(contentColor = accentColor),
                        modifier = Modifier.testTag("save_writer_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Protocol overview card explaining intent
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = accentColor.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Practice Wisdom",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = protocol.description,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
            
            // Loop fields
            protocol.fields.forEach { field ->
                val textValue = draftResponses[field.key] ?: ""
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = field.prompt,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = field.helperText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { onValueChange(field.key, it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .testTag("field_input_${field.key}"),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedLabelColor = accentColor
                        ),
                        placeholder = { 
                            Text(
                                "Write reflection here...", 
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                            ) 
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("commit_protocol_button")
            ) {
                Text(
                    text = "Commit Reflection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Share utility to share an entry using Android's native share Intent
 */
private fun shareEntry(context: Context, entry: JournalEntry) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Reflection: ${entry.protocolTitle}")
            putExtra(Intent.EXTRA_TEXT, entry.formattedText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Reflection Via"))
    } catch (e: Exception) {
        // Safe catch
    }
}

/**
 * Share utility to mass export multiple entries
 */
private fun shareAllEntries(context: Context, entireBackupText: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Protocol Journal Export")
            putExtra(Intent.EXTRA_TEXT, entireBackupText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Complete Journal Via"))
    } catch (e: Exception) {
        // Safe catch
    }
}
