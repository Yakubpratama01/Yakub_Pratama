package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Item
import com.example.data.ItemRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
    NAME_ASC("Nama (A-Z)"),
    NAME_DESC("Nama (Z-A)"),
    STOCK_LOW_TO_HIGH("Stok: Sedikit - Banyak"),
    STOCK_HIGH_TO_LOW("Stok: Banyak - Sedikit"),
    VALUE_HIGH_TO_LOW("Total Nilai: Tinggi - Rendah"),
    EXPIRATION_SOONEST("Kedaluwarsa Terdekat")
}

enum class FilterStatus(val displayName: String) {
    ALL("Semua Status"),
    LOW_STOCK("Hampir Habis"),
    OUT_OF_STOCK("Habis"),
    EXPIRING_SOON("Akan Kedaluwarsa")
}

enum class ExpirationStatus {
    NONE, SAFE, EXPIRING_SOON, EXPIRED
}

class StockViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ItemRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ItemRepository(database.itemDao())
    }

    // Raw sources from DB
    val allItems: StateFlow<List<Item>> = repository.allItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<String>> = repository.categories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val suppliers: StateFlow<List<String>> = repository.suppliers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filter and Sort states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<String?>(null)
    val selectedSupplier: StateFlow<String?> = _selectedSupplier.asStateFlow()

    private val _selectedStatus = MutableStateFlow(FilterStatus.ALL)
    val selectedStatus: StateFlow<FilterStatus> = _selectedStatus.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.NAME_ASC)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    // 5-flow combine is directly supported by Kotlin coroutines:
    private val filterStateFlow = combine(
        _searchQuery,
        _selectedCategory,
        _selectedSupplier,
        _selectedStatus,
        _selectedSort
    ) { query, category, supplier, status, sort ->
        FilterState(query, category, supplier, status, sort)
    }

    // Filtered and Sorted list for the UI
    val filteredItems: StateFlow<List<Item>> = allItems.combine(filterStateFlow) { items, filter ->
        var list = items
        val query = filter.query
        val category = filter.category
        val supplier = filter.supplier
        val status = filter.status
        val sort = filter.sort

        // 1. Search Query Filter
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.sku.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }

        // 2. Category Filter
        if (category != null) {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 3. Supplier Filter
        if (supplier != null) {
            list = list.filter { it.supplier.equals(supplier, ignoreCase = true) }
        }

        // 4. Status Filter
        list = when (status) {
            FilterStatus.ALL -> list
            FilterStatus.LOW_STOCK -> list.filter { it.stockQuantity > 0 && it.stockQuantity <= it.minStockQuantity }
            FilterStatus.OUT_OF_STOCK -> list.filter { it.stockQuantity == 0 }
            FilterStatus.EXPIRING_SOON -> list.filter {
                val expStatus = getExpirationStatus(it.expirationDate)
                expStatus == ExpirationStatus.EXPIRING_SOON || expStatus == ExpirationStatus.EXPIRED
            }
        }

        // 5. Sort Option
        when (sort) {
            SortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
            SortOption.STOCK_LOW_TO_HIGH -> list.sortedBy { it.stockQuantity }
            SortOption.STOCK_HIGH_TO_LOW -> list.sortedByDescending { it.stockQuantity }
            SortOption.VALUE_HIGH_TO_LOW -> list.sortedByDescending { it.price * it.stockQuantity }
            SortOption.EXPIRATION_SOONEST -> list.sortedWith { a, b ->
                val dateA = a.expirationDate ?: "9999-12-31"
                val dateB = b.expirationDate ?: "9999-12-31"
                dateA.compareTo(dateB)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // KPI Values
    val totalStockValue: StateFlow<Double> = allItems.map { items ->
        items.sumOf { it.price * it.stockQuantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lowStockCount: StateFlow<Int> = allItems.map { items ->
        items.count { it.stockQuantity > 0 && it.stockQuantity <= it.minStockQuantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockCount: StateFlow<Int> = allItems.map { items ->
        items.count { it.stockQuantity == 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiringCount: StateFlow<Int> = allItems.map { items ->
        items.count {
            val expStatus = getExpirationStatus(it.expirationDate)
            expStatus == ExpirationStatus.EXPIRING_SOON || expStatus == ExpirationStatus.EXPIRED
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Form inputs & Editing states
    var isFormSheetOpen = MutableStateFlow(false)
    var editingItem = MutableStateFlow<Item?>(null)

    // Form State fields
    val formName = MutableStateFlow("")
    val formSku = MutableStateFlow("")
    val formCategory = MutableStateFlow("")
    val formStockQuantity = MutableStateFlow("")
    val formMinStockQuantity = MutableStateFlow("")
    val formPrice = MutableStateFlow("")
    val formSupplier = MutableStateFlow("")
    val formExpirationDate = MutableStateFlow("")
    val formNotes = MutableStateFlow("")

    // Methods to change filter states
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectSupplier(supplier: String?) {
        _selectedSupplier.value = supplier
    }

    fun selectStatus(status: FilterStatus) {
        _selectedStatus.value = status
    }

    fun selectSort(sort: SortOption) {
        _selectedSort.value = sort
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedSupplier.value = null
        _selectedStatus.value = FilterStatus.ALL
        _selectedSort.value = SortOption.NAME_ASC
    }

    // Form operations
    fun openAddForm() {
        editingItem.value = null
        formName.value = ""
        formSku.value = ""
        formCategory.value = ""
        formStockQuantity.value = ""
        formMinStockQuantity.value = ""
        formPrice.value = ""
        formSupplier.value = ""
        formExpirationDate.value = ""
        formNotes.value = ""
        isFormSheetOpen.value = true
    }

    fun openEditForm(item: Item) {
        editingItem.value = item
        formName.value = item.name
        formSku.value = item.sku
        formCategory.value = item.category
        formStockQuantity.value = item.stockQuantity.toString()
        formMinStockQuantity.value = item.minStockQuantity.toString()
        formPrice.value = item.price.toString()
        formSupplier.value = item.supplier
        formExpirationDate.value = item.expirationDate ?: ""
        formNotes.value = item.notes
        isFormSheetOpen.value = true
    }

    fun closeForm() {
        isFormSheetOpen.value = false
        editingItem.value = null
    }

    fun saveItem(): Boolean {
        val name = formName.value.trim()
        val sku = formSku.value.trim().uppercase()
        val category = formCategory.value.trim()
        val stockQty = formStockQuantity.value.toIntOrNull() ?: 0
        val minQty = formMinStockQuantity.value.toIntOrNull() ?: 5
        val price = formPrice.value.toDoubleOrNull() ?: 0.0
        val supplier = formSupplier.value.trim()
        val expDate = formExpirationDate.value.trim().takeIf { it.isNotBlank() }
        val notes = formNotes.value.trim()

        if (name.isBlank() || sku.isBlank() || category.isBlank()) {
            return false // Validation failed
        }

        val itemToSave = Item(
            id = editingItem.value?.id ?: 0,
            name = name,
            sku = sku,
            category = category,
            stockQuantity = stockQty,
            minStockQuantity = minQty,
            price = price,
            supplier = supplier,
            expirationDate = expDate,
            notes = notes,
            timestamp = editingItem.value?.timestamp ?: System.currentTimeMillis()
        )

        viewModelScope.launch {
            if (editingItem.value == null) {
                repository.insert(itemToSave)
            } else {
                repository.update(itemToSave)
            }
        }
        closeForm()
        return true
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun updateStock(item: Item, newStock: Int) {
        viewModelScope.launch {
            repository.update(item.copy(stockQuantity = newStock))
        }
    }

    // Helper functions
    fun getExpirationStatus(expDateStr: String?): ExpirationStatus {
        if (expDateStr.isNullOrBlank()) return ExpirationStatus.NONE
        val todayStr = "2026-06-24" // Current local time from platform metadata
        if (expDateStr < todayStr) return ExpirationStatus.EXPIRED

        // Threshold for warning (approx 30 days = same year & next month)
        // Or simple string check: since today is 2026-06-24, expiring soon is on or before 2026-07-24
        val soonThresholdStr = "2026-07-24"
        if (expDateStr <= soonThresholdStr) {
            return ExpirationStatus.EXPIRING_SOON
        }
        return ExpirationStatus.SAFE
    }
}

data class FilterState(
    val query: String,
    val category: String?,
    val supplier: String?,
    val status: FilterStatus,
    val sort: SortOption
)
