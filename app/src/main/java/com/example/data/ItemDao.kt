package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): Item?

    @Query("SELECT DISTINCT category FROM items WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT supplier FROM items WHERE supplier IS NOT NULL AND supplier != '' ORDER BY supplier ASC")
    fun getSuppliers(): Flow<List<String>>
}
