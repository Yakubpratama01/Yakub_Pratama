package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Item
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDashboardScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val allItemsList by viewModel.allItems.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()

    // Filter states
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSupplier by viewModel.selectedSupplier.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()

    // KPI states
    val totalStockValue by viewModel.totalStockValue.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val outOfStockCount by viewModel.outOfStockCount.collectAsStateWithLifecycle()
    val expiringCount by viewModel.expiringCount.collectAsStateWithLifecycle()

    val bestSellerName = remember(allItemsList) {
        allItemsList.maxByOrNull { it.stockQuantity * it.price }?.name ?: "Kopi Arabika"
    }

    // UI Panel visibility states
    var isAdvancedFilterOpen by remember { mutableStateOf(false) }
    val isFormSheetOpen by viewModel.isFormSheetOpen.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddForm() },
                containerColor = DeepCharcoal,
                contentColor = PureWhite,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .testTag("add_item_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Barang",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tambah Barang",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Header Toko Kutama
                HeaderSection()

                Spacer(modifier = Modifier.height(20.dp))

                // KPI Dashboard Row (Total Value, Low, Out, Expiring)
                KpiDashboardSection(
                    totalValue = totalStockValue,
                    lowStock = lowStockCount,
                    outOfStock = outOfStockCount,
                    expiring = expiringCount,
                    activeFilter = selectedStatus,
                    bestSellerName = bestSellerName,
                    onKpiClick = { status ->
                        viewModel.selectStatus(status)
                    }
                )

                // Low Stock Alert (Smart Tracker) - Sleek custom styling
                if (lowStockCount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = RedAlertBg),
                        border = BorderStroke(1.dp, RedAlertBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectStatus(FilterStatus.LOW_STOCK) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF28482), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Peringatan",
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Stok Kritis",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RedAlertText
                                )
                                Text(
                                    text = "$lowStockCount item memerlukan restock segera",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RedAlertText.copy(alpha = 0.85f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Detail",
                                tint = RedAlertText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Weekly Trend Chart Card (Apple-esque Sparkline)
                WeeklyTrendCard()

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Real-Time Search & Advanced Filter Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Cari Nama Barang, SKU, atau Kategori...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = BrandSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus Pencarian",
                                        tint = BrandSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepCharcoal,
                            unfocusedBorderColor = LightGrayBorder,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_bar")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    FilledIconButton(
                        onClick = { isAdvancedFilterOpen = !isAdvancedFilterOpen },
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isAdvancedFilterOpen || selectedCategory != null || selectedSupplier != null || selectedSort != SortOption.NAME_ASC) DeepCharcoal else MaterialTheme.colorScheme.surface,
                            contentColor = if (isAdvancedFilterOpen || selectedCategory != null || selectedSupplier != null || selectedSort != SortOption.NAME_ASC) PureWhite else DeepCharcoal
                        ),
                        modifier = Modifier
                            .size(56.dp)
                            .border(
                                1.dp,
                                if (isAdvancedFilterOpen) Color.Transparent else LightGrayBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Advanced Filter"
                        )
                    }
                }

                // Collapsible Advanced Filter Drawer Panel
                AnimatedVisibility(
                    visible = isAdvancedFilterOpen,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    AdvancedFilterPanel(
                        categories = categories,
                        suppliers = suppliers,
                        selectedCategory = selectedCategory,
                        selectedSupplier = selectedSupplier,
                        selectedSort = selectedSort,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSelectSupplier = { viewModel.selectSupplier(it) },
                        onSelectSort = { viewModel.selectSort(it) },
                        onClearAll = { viewModel.clearAllFilters() }
                    )
                }

                // Active filter chip status summary
                if (selectedCategory != null || selectedSupplier != null || selectedStatus != FilterStatus.ALL) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ActiveFiltersRow(
                        category = selectedCategory,
                        supplier = selectedSupplier,
                        status = selectedStatus,
                        onRemoveCategory = { viewModel.selectCategory(null) },
                        onRemoveSupplier = { viewModel.selectSupplier(null) },
                        onRemoveStatus = { viewModel.selectStatus(FilterStatus.ALL) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Inventaris (${items.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )

                    Text(
                        text = selectedSort.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item lists
                if (items.isEmpty()) {
                    EmptyStateView(
                        hasQuery = searchQuery.isNotEmpty() || selectedCategory != null || selectedSupplier != null || selectedStatus != FilterStatus.ALL,
                        onReset = {
                            viewModel.clearAllFilters()
                            viewModel.setSearchQuery("")
                        }
                    )
                } else {
                    // Optimized Column Layout for smooth scrolling of items
                    items.forEach { item ->
                        ItemCard(
                            item = item,
                            onEdit = { viewModel.openEditForm(item) },
                            onDelete = { viewModel.deleteItem(item) },
                            onStockChange = { newStock ->
                                viewModel.updateStock(item, newStock)
                            },
                            getExpStatus = { viewModel.getExpirationStatus(it) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Extra padding so FAB doesn't cover content
            }

            // 3-Step Wizard Modal Dialog for adding/editing items
            if (isFormSheetOpen) {
                ItemFormWizardDialog(
                    viewModel = viewModel,
                    categories = categories,
                    suppliers = suppliers,
                    onDismiss = { viewModel.closeForm() }
                )
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Toko Kutama",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "DASHBOARD INVENTARIS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandSecondary,
                    letterSpacing = 2.sp
                )
            )
        }

        // Sleek notification-style circular button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PureWhite, CircleShape)
                .border(1.dp, LightGrayBorder, CircleShape)
                .clickable { /* No-op notifications */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifikasi",
                tint = BrandSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Compact Rupiah short currency formatting helper (e.g. Rp 128,4Jt or Rp 1,2M)
fun formatRupiahShort(value: Double): String {
    return when {
        value >= 1_000_000_000 -> {
            val bill = value / 1_000_000_000.0
            String.format(Locale.US, "Rp %.1fM", bill).replace(".", ",")
        }
        value >= 1_000_000 -> {
            val mill = value / 1_000_000.0
            String.format(Locale.US, "Rp %.1fJt", mill).replace(".", ",")
        }
        value >= 1_000 -> {
            val rb = value / 1_000.0
            String.format(Locale.US, "Rp %.1fRb", rb).replace(".", ",")
        }
        else -> {
            "Rp ${value.toInt()}"
        }
    }
}

@Composable
fun KpiDashboardSection(
    totalValue: Double,
    lowStock: Int,
    outOfStock: Int,
    expiring: Int,
    activeFilter: FilterStatus,
    bestSellerName: String,
    onKpiClick: (FilterStatus) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Sleek Bento Grid: Nilai Stok (payments) & Terlaris (trending_up)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Nilai Stok (White with forest green payments icon)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .weight(1f)
                    .height(128.dp)
                    .border(1.dp, LightGrayBorder, RoundedCornerShape(24.dp))
                    .clickable { onKpiClick(FilterStatus.ALL) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Nilai Stok",
                        tint = SageGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "NILAI STOK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = BrandSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatRupiahShort(totalValue),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DeepCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Card 2: Terlaris (Deep Charcoal with white trending_up icon)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                modifier = Modifier
                    .weight(1f)
                    .height(128.dp)
                    .clickable { /* No-op */ }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Terlaris",
                        tint = PureWhite.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "TERLARIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = PureWhite.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bestSellerName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Three secondary KPIs displayed as columns/grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Out of stock card
            KpiMiniCard(
                title = "Stok Habis",
                count = outOfStock.toString(),
                color = SunsetOrange,
                isActive = activeFilter == FilterStatus.OUT_OF_STOCK,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (activeFilter == FilterStatus.OUT_OF_STOCK) {
                        onKpiClick(FilterStatus.ALL)
                    } else {
                        onKpiClick(FilterStatus.OUT_OF_STOCK)
                    }
                }
            )

            // Low Stock Card
            KpiMiniCard(
                title = "Hampir Habis",
                count = lowStock.toString(),
                color = WarningGold,
                isActive = activeFilter == FilterStatus.LOW_STOCK,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (activeFilter == FilterStatus.LOW_STOCK) {
                        onKpiClick(FilterStatus.ALL)
                    } else {
                        onKpiClick(FilterStatus.LOW_STOCK)
                    }
                }
            )

            // Expiring card
            KpiMiniCard(
                title = "Kedaluwarsa",
                count = expiring.toString(),
                color = SunsetOrange,
                isActive = activeFilter == FilterStatus.EXPIRING_SOON,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (activeFilter == FilterStatus.EXPIRING_SOON) {
                        onKpiClick(FilterStatus.ALL)
                    } else {
                        onKpiClick(FilterStatus.EXPIRING_SOON)
                    }
                }
            )
        }
    }
}

@Composable
fun KpiMiniCard(
    title: String,
    count: String,
    color: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) color.copy(alpha = 0.12f) else PureWhite
        ),
        modifier = modifier
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) color else LightGrayBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = BrandSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = DeepCharcoal
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
}

@Composable
fun WeeklyTrendCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGrayBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik Tren Transaksi Stok",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Aktivitas keluar-masuk barang 7 hari terakhir",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandSecondary
                    )
                }
                Text(
                    text = "+14.8%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SageGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Minimalist canvas-drawn line sparkline representing weekly stock trend
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val points = listOf(
                        Offset(0f, 80f),
                        Offset(0.16f, 65f),
                        Offset(0.33f, 75f),
                        Offset(0.5f, 40f),
                        Offset(0.66f, 50f),
                        Offset(0.83f, 20f),
                        Offset(1f, 30f)
                    )

                    val width = size.width
                    val height = size.height

                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { i, pt ->
                        val x = pt.x * width
                        val y = (pt.y / 100f) * height

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = points[i - 1].x * width
                            val prevY = (points[i - 1].y / 100f) * height
                            // Add gorgeous smooth bezier curve
                            path.cubicTo(
                                prevX + (x - prevX) / 2f, prevY,
                                prevX + (x - prevX) / 2f, y,
                                x, y
                            )
                            fillPath.cubicTo(
                                prevX + (x - prevX) / 2f, prevY,
                                prevX + (x - prevX) / 2f, y,
                                x, y
                            )
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.lineTo(0f, height)
                    fillPath.close()

                    // Draw area gradient representing glassmorphic light glow
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SageGreen.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw line stroke
                    drawPath(
                        path = path,
                        color = SageGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw decorative interactive nodes for points
                    points.forEachIndexed { i, pt ->
                        if (i == 5 || i == 6) { // Highlight last points representing current peak
                            val x = pt.x * width
                            val y = (pt.y / 100f) * height
                            drawCircle(
                                color = PureWhite,
                                radius = 5.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = SageGreen,
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Days labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Kam", "Jum", "Sab", "Min", "Sen", "Sel", "Rab")
                days.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandTertiary
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedFilterPanel(
    categories: List<String>,
    suppliers: List<String>,
    selectedCategory: String?,
    selectedSupplier: String?,
    selectedSort: SortOption,
    onSelectCategory: (String?) -> Unit,
    onSelectSupplier: (String?) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onClearAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(1.dp, LightGrayBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Canggih & Pengurutan",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepCharcoal
                )
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = SunsetOrange)
                ) {
                    Text(
                        "Reset",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Divider(color = LightGrayBorder, modifier = Modifier.padding(vertical = 8.dp))

            // 1. Sort Section
            Text(
                text = "Urutkan Berdasarkan",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = BrandSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SortOption.values()) { option ->
                    val isSelected = selectedSort == option
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) DeepCharcoal else AppleLightGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectSort(option) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = option.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) PureWhite else DeepCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Category Section
            Text(
                text = "Kategori Barang",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = BrandSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = selectedCategory == null
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) DeepCharcoal else AppleLightGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectCategory(null) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Semua Kategori",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) PureWhite else DeepCharcoal
                        )
                    }
                }

                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) DeepCharcoal else AppleLightGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectCategory(cat) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) PureWhite else DeepCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Supplier Section
            Text(
                text = "Pemasok (Supplier)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = BrandSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = selectedSupplier == null
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) DeepCharcoal else AppleLightGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectSupplier(null) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Semua Supplier",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) PureWhite else DeepCharcoal
                        )
                    }
                }

                items(suppliers) { sup ->
                    val isSelected = selectedSupplier == sup
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) DeepCharcoal else AppleLightGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectSupplier(sup) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sup,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) PureWhite else DeepCharcoal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFiltersRow(
    category: String?,
    supplier: String?,
    status: FilterStatus,
    onRemoveCategory: () -> Unit,
    onRemoveSupplier: () -> Unit,
    onRemoveStatus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Filter Aktif:",
            style = MaterialTheme.typography.labelSmall,
            color = BrandSecondary
        )

        if (status != FilterStatus.ALL) {
            FilterBadge(text = "Status: ${status.displayName}", onRemove = onRemoveStatus)
        }

        if (category != null) {
            FilterBadge(text = "Kat: $category", onRemove = onRemoveCategory)
        }

        if (supplier != null) {
            FilterBadge(text = "Sup: $supplier", onRemove = onRemoveSupplier)
        }
    }
}

@Composable
fun FilterBadge(text: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .background(DeepCharcoal, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = PureWhite
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Hapus",
                tint = PureWhite,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockChange: (Int) -> Unit,
    getExpStatus: (String?) -> ExpirationStatus
) {
    // Define reactive states for low-stock bar visual coloring
    val ratio = if (item.minStockQuantity > 0) {
        item.stockQuantity.toFloat() / item.minStockQuantity.toFloat()
    } else {
        1f
    }

    val isOutOfStock = item.stockQuantity == 0
    val isLowStock = item.stockQuantity in 1..item.minStockQuantity

    val trackerColor = when {
        isOutOfStock -> SunsetOrange
        isLowStock -> WarningGold
        else -> SageGreen
    }

    val trackerLabel = when {
        isOutOfStock -> "Stok Habis!"
        isLowStock -> "Hampir Habis!"
        else -> "Stok Aman"
    }

    val expStatus = getExpStatus(item.expirationDate)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGrayBorder, RoundedCornerShape(16.dp))
            .testTag("item_card_${item.sku}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Product Info & Code / SKU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEBECEF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandSecondary
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    when {
                                        isOutOfStock -> Color(0xFFFEE4E2)
                                        isLowStock -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFD1FAE5)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    isOutOfStock -> "CRITICAL"
                                    isLowStock -> "WARNING"
                                    else -> "STABIL"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                color = when {
                                    isOutOfStock -> Color(0xFFB42318)
                                    isLowStock -> Color(0xFFB45309)
                                    else -> SageGreen
                                }
                            )
                        }

                        Text(
                            text = item.sku,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = BrandTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Delete & Edit quick icons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BrandSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = SunsetOrange.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expiration Alert Banner if active
            if (expStatus != ExpirationStatus.NONE) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (expStatus == ExpirationStatus.EXPIRED || expStatus == ExpirationStatus.EXPIRING_SOON) {
                                SunsetOrange.copy(alpha = 0.08f)
                            } else {
                                SageGreen.copy(alpha = 0.08f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (expStatus == ExpirationStatus.EXPIRED || expStatus == ExpirationStatus.EXPIRING_SOON) {
                                SunsetOrange.copy(alpha = 0.2f)
                            } else {
                                SageGreen.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (expStatus == ExpirationStatus.EXPIRED || expStatus == ExpirationStatus.EXPIRING_SOON) SunsetOrange else SageGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (expStatus) {
                                ExpirationStatus.EXPIRED -> "Kedaluwarsa! (${item.expirationDate})"
                                ExpirationStatus.EXPIRING_SOON -> "Kedaluwarsa Segera! (${item.expirationDate})"
                                else -> "Kedaluwarsa: ${item.expirationDate} (Sertifikasi Aman)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (expStatus == ExpirationStatus.EXPIRED || expStatus == ExpirationStatus.EXPIRING_SOON) SunsetOrange else SageGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stock progress tracking slider/progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Level Stok: ${item.stockQuantity} / ${item.minStockQuantity} unit",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )

                    Text(
                        text = trackerLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = trackerColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bar changing colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFEBECEF), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio.coerceAtMost(1f))
                            .fillMaxHeight()
                            .background(trackerColor, RoundedCornerShape(4.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing & Fast Stock Incrementor/Decrementor Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Harga Satuan",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandTertiary
                    )
                    Text(
                        text = formatRupiah(item.price),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = DeepCharcoal
                    )
                }

                // Plus / Minus direct operation panel (Apple tactile feel)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(AppleLightGray, RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { if (item.stockQuantity > 0) onStockChange(item.stockQuantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Kurang Stok",
                            tint = DeepCharcoal,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = item.stockQuantity.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .testTag("qty_label_${item.sku}"),
                        color = DeepCharcoal
                    )

                    IconButton(
                        onClick = { onStockChange(item.stockQuantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Stok",
                            tint = DeepCharcoal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Catatan: ${item.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    hasQuery: Boolean,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFEBECEF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasQuery) Icons.Default.SearchOff else Icons.Default.Inbox,
                    contentDescription = null,
                    tint = BrandSecondary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasQuery) "Barang Tidak Ditemukan" else "Stok Toko Kosong",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepCharcoal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (hasQuery) "Coba atur ulang kata kunci pencarian atau matikan filter aktif Anda." else "Mulai dengan mengetuk tombol tambah barang di sudut kanan bawah.",
                style = MaterialTheme.typography.bodyMedium,
                color = BrandSecondary,
                textAlign = TextAlign.Center
            )

            if (hasQuery) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Matikan Semua Filter", color = PureWhite)
                }
            }
        }
    }
}

// 3-Step Wizard Layout Form Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormWizardDialog(
    viewModel: StockViewModel,
    categories: List<String>,
    suppliers: List<String>,
    onDismiss: () -> Unit
) {
    val editingItem by viewModel.editingItem.collectAsStateWithLifecycle()

    var currentStep by remember { mutableStateOf(1) } // Steps 1 to 3

    // Observables for text fields
    val name by viewModel.formName.collectAsStateWithLifecycle()
    val sku by viewModel.formSku.collectAsStateWithLifecycle()
    val category by viewModel.formCategory.collectAsStateWithLifecycle()
    val stockQuantity by viewModel.formStockQuantity.collectAsStateWithLifecycle()
    val minStockQuantity by viewModel.formMinStockQuantity.collectAsStateWithLifecycle()
    val price by viewModel.formPrice.collectAsStateWithLifecycle()
    val supplier by viewModel.formSupplier.collectAsStateWithLifecycle()
    val expirationDate by viewModel.formExpirationDate.collectAsStateWithLifecycle()
    val notes by viewModel.formNotes.collectAsStateWithLifecycle()

    // Form Dropdown expanded helpers
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isSupplierDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, LightGrayBorder, RoundedCornerShape(24.dp))
                .testTag("item_form_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingItem == null) "Tambah Barang Baru" else "Ubah Detail Barang",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = DeepCharcoal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Step Indicator Display (Apple minimalist pill style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (step in 1..3) {
                        val isPassed = step < currentStep
                        val isCurrent = step == currentStep
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isPassed -> DeepCharcoal
                                        isCurrent -> DeepCharcoal
                                        else -> Color(0xFFEBECEF)
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val stepTitles = listOf("1. Informasi Utama", "2. Stok & Harga", "3. Detail Logistik")
                    Text(
                        text = stepTitles[currentStep - 1],
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Langkah $currentStep dari 3",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandSecondary
                    )
                }

                Divider(color = LightGrayBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Dynamic Form Content depending on step
                when (currentStep) {
                    1 -> {
                        // STEP 1: Main Info
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // SKU Field
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { viewModel.formSku.value = it.uppercase() },
                                label = { Text("Kode SKU Barang (Wajib)") },
                                placeholder = { Text("Contoh: APL-MBP-14") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepCharcoal,
                                    unfocusedBorderColor = LightGrayBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_sku_input")
                            )

                            // Item Name Field
                            OutlinedTextField(
                                value = name,
                                onValueChange = { viewModel.formName.value = it },
                                label = { Text("Nama Barang (Wajib)") },
                                placeholder = { Text("Contoh: Macbook Pro Air") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepCharcoal,
                                    unfocusedBorderColor = LightGrayBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_name_input")
                            )

                            // Category with Smart Autocomplete suggestions drop-down
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {
                                        viewModel.formCategory.value = it
                                        isCategoryDropdownExpanded = true
                                    },
                                    label = { Text("Kategori (Wajib)") },
                                    placeholder = { Text("Ketik atau pilih kategori...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }) {
                                            Icon(
                                                imageVector = if (isCategoryDropdownExpanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = "Tampilkan Kategori"
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepCharcoal,
                                        unfocusedBorderColor = LightGrayBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("form_category_input")
                                )

                                val filteredCats = categories.filter {
                                    it.contains(category, ignoreCase = true)
                                }

                                if (isCategoryDropdownExpanded && (filteredCats.isNotEmpty() || categories.isNotEmpty())) {
                                    DropdownMenu(
                                        expanded = isCategoryDropdownExpanded,
                                        onDismissRequest = { isCategoryDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        val itemsToShow = filteredCats.ifEmpty { categories }
                                        itemsToShow.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    viewModel.formCategory.value = cat
                                                    isCategoryDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // STEP 2: Stocks & Prices
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Price Field
                            OutlinedTextField(
                                value = price,
                                onValueChange = { viewModel.formPrice.value = it },
                                label = { Text("Harga Satuan (Rupiah)") },
                                placeholder = { Text("Contoh: 15000000") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepCharcoal,
                                    unfocusedBorderColor = LightGrayBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_price_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Stock Quantity
                                OutlinedTextField(
                                    value = stockQuantity,
                                    onValueChange = { viewModel.formStockQuantity.value = it },
                                    label = { Text("Stok Saat Ini") },
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepCharcoal,
                                        unfocusedBorderColor = LightGrayBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("form_stock_input")
                                )

                                // Minimum Stock Quantity threshold for alert
                                OutlinedTextField(
                                    value = minStockQuantity,
                                    onValueChange = { viewModel.formMinStockQuantity.value = it },
                                    label = { Text("Batas Minim") },
                                    placeholder = { Text("5") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepCharcoal,
                                        unfocusedBorderColor = LightGrayBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("form_min_stock_input")
                                )
                            }
                        }
                    }

                    3 -> {
                        // STEP 3: Logistics Details
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Supplier Auto-complete field
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = supplier,
                                    onValueChange = {
                                        viewModel.formSupplier.value = it
                                        isSupplierDropdownExpanded = true
                                    },
                                    label = { Text("Nama Pemasok (Supplier)") },
                                    placeholder = { Text("Ketik atau pilih supplier...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { isSupplierDropdownExpanded = !isSupplierDropdownExpanded }) {
                                            Icon(
                                                imageVector = if (isSupplierDropdownExpanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = "Tampilkan Supplier"
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepCharcoal,
                                        unfocusedBorderColor = LightGrayBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("form_supplier_input")
                                )

                                val filteredSups = suppliers.filter {
                                    it.contains(supplier, ignoreCase = true)
                                }

                                if (isSupplierDropdownExpanded && (filteredSups.isNotEmpty() || suppliers.isNotEmpty())) {
                                    DropdownMenu(
                                        expanded = isSupplierDropdownExpanded,
                                        onDismissRequest = { isSupplierDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        val supsToShow = filteredSups.ifEmpty { suppliers }
                                        supsToShow.forEach { sup ->
                                            DropdownMenuItem(
                                                text = { Text(sup) },
                                                onClick = {
                                                    viewModel.formSupplier.value = sup
                                                    isSupplierDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Expiration Date Field
                            OutlinedTextField(
                                value = expirationDate,
                                onValueChange = { viewModel.formExpirationDate.value = it },
                                label = { Text("Tanggal Kedaluwarsa (YYYY-MM-DD)") },
                                placeholder = { Text("Contoh: 2026-12-31") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepCharcoal,
                                    unfocusedBorderColor = LightGrayBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_exp_input")
                            )

                            // Notes field
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { viewModel.formNotes.value = it },
                                label = { Text("Catatan Internal Gudang") },
                                placeholder = { Text("Keterangan lokasi penyimpanan, dll...") },
                                singleLine = false,
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepCharcoal,
                                    unfocusedBorderColor = LightGrayBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LightGrayBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepCharcoal),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kembali")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp)) // Placeholder to align next buttons correctly
                    }

                    // Next / Save Button
                    if (currentStep < 3) {
                        Button(
                            onClick = {
                                // Simple validations before step progression
                                when (currentStep) {
                                    1 -> {
                                        if (name.isNotBlank() && sku.isNotBlank() && category.isNotBlank()) {
                                            currentStep++
                                        }
                                    }
                                    2 -> currentStep++
                                }
                            },
                            enabled = when (currentStep) {
                                1 -> name.isNotBlank() && sku.isNotBlank() && category.isNotBlank()
                                else -> true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal, disabledContainerColor = Color(0xFFEBECEF)),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("form_next_button")
                        ) {
                            Text("Lanjut", color = if (name.isNotBlank() && sku.isNotBlank() && category.isNotBlank()) PureWhite else BrandTertiary)
                        }
                    } else {
                        // SAVE on step 3
                        Button(
                            onClick = {
                                val succeeded = viewModel.saveItem()
                                if (succeeded) {
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("form_submit_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = PureWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Barang", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Rupiah visual currency formatting helper
fun formatRupiah(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(value).replace("Rp", "Rp ").substringBefore(",")
}
