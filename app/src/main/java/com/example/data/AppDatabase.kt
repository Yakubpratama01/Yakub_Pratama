package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toko_kutama_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.itemDao())
                }
            }
        }

        suspend fun populateDatabase(itemDao: ItemDao) {
            // Pre-seed sample items with different statuses for rich dashboard visualization
            val initialItems = listOf(
                Item(
                    name = "MacBook Pro M3 Max",
                    sku = "APL-MBP-M3MX",
                    category = "Elektronik",
                    stockQuantity = 14,
                    minStockQuantity = 5,
                    price = 45000000.0,
                    supplier = "Apple Inc.",
                    expirationDate = null,
                    notes = "Premium space black workstation"
                ),
                Item(
                    name = "iPhone 15 Pro Titanium",
                    sku = "APL-IP15P-256",
                    category = "Elektronik",
                    stockQuantity = 4, // Low stock (min is 10)
                    minStockQuantity = 10,
                    price = 21000000.0,
                    supplier = "Apple Inc.",
                    expirationDate = null,
                    notes = "Sage Green color edition"
                ),
                Item(
                    name = "iPad Pro M2 12.9",
                    sku = "APL-IPP-M2",
                    category = "Elektronik",
                    stockQuantity = 18,
                    minStockQuantity = 5,
                    price = 18500000.0,
                    supplier = "Apple Store Jakarta",
                    expirationDate = null,
                    notes = "Demo unit and customer stock"
                ),
                Item(
                    name = "Kopi Arabika Gayo 1kg",
                    sku = "COF-GAYO-1KG",
                    category = "Bahan Pangan",
                    stockQuantity = 48,
                    minStockQuantity = 15,
                    price = 245000.0,
                    supplier = "Koperasi Gayo Coffee",
                    expirationDate = "2026-12-15", // Expiring later
                    notes = "Medium roast single origin"
                ),
                Item(
                    name = "Susu Almond Oats Premium 1L",
                    sku = "MIL-ALM-1L",
                    category = "Minuman",
                    stockQuantity = 3, // Low stock! (min is 12)
                    minStockQuantity = 12,
                    price = 45000.0,
                    supplier = "Oatside Ltd.",
                    expirationDate = "2026-07-12", // Expiring very soon! (within a month)
                    notes = "Keep chilled under 4C"
                ),
                Item(
                    name = "Keychron Q1 Max Keyboard",
                    sku = "KCH-Q1M-BRN",
                    category = "Aksesoris",
                    stockQuantity = 8,
                    minStockQuantity = 3,
                    price = 2950000.0,
                    supplier = "Keychron Indonesia",
                    expirationDate = null,
                    notes = "Gateron Jupiter Brown Switches"
                ),
                Item(
                    name = "Xiaomi Monitor 34 Curved",
                    sku = "XIA-MON-34C",
                    category = "Elektronik",
                    stockQuantity = 0, // Out of Stock!
                    minStockQuantity = 5,
                    price = 5600000.0,
                    supplier = "Xiaomi Distribusi",
                    expirationDate = null,
                    notes = "WQHD Ultrawide Display"
                )
            )
            for (item in initialItems) {
                itemDao.insertItem(item)
            }
        }
    }
}
