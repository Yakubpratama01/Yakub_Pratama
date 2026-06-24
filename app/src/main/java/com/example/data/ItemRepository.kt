package com.example.data

import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {
    val allItems: Flow<List<Item>> = itemDao.getAllItems()
    val categories: Flow<List<String>> = itemDao.getCategories()
    val suppliers: Flow<List<String>> = itemDao.getSuppliers()

    suspend fun insert(item: Item) {
        itemDao.insertItem(item)
    }

    suspend fun update(item: Item) {
        itemDao.updateItem(item)
    }

    suspend fun delete(item: Item) {
        itemDao.deleteItem(item)
    }

    suspend fun getItemById(id: Int): Item? {
        return itemDao.getItemById(id)
    }
}
