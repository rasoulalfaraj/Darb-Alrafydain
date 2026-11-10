package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface IraqDao {
    @Query("SELECT * FROM favorite_places ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoritePlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(place: FavoritePlaceEntity)

    @Query("DELETE FROM favorite_places WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int)

    @Query("SELECT * FROM navigation_history ORDER BY timestamp DESC LIMIT 50")
    fun getNavigationHistory(): Flow<List<NavigationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: NavigationHistoryEntity)

    @Query("DELETE FROM navigation_history")
    suspend fun clearAllHistory()

    @Query("DELETE FROM navigation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("SELECT * FROM offline_governorates ORDER BY isDownloaded DESC, nameAr ASC")
    fun getAllOfflineGovernorates(): Flow<List<OfflineGovernorateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineGovernorates(list: List<OfflineGovernorateEntity>)

    @Query("UPDATE offline_governorates SET isDownloaded = :downloaded, lastUpdatedTimestamp = :timestamp WHERE code = :code")
    suspend fun updateGovernorateDownloadState(code: String, downloaded: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM offline_governorates")
    suspend fun countOfflineGovernorates(): Int

    @Query("SELECT COUNT(*) FROM favorite_places")
    suspend fun countFavorites(): Int
}

@Database(
    entities = [
        FavoritePlaceEntity::class,
        NavigationHistoryEntity::class,
        OfflineGovernorateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IraqDatabase : RoomDatabase() {
    abstract fun iraqDao(): IraqDao

    companion object {
        @Volatile
        private var INSTANCE: IraqDatabase? = null

        fun getInstance(context: Context): IraqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IraqDatabase::class.java,
                    "iraq_navigation_darb.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class IraqRepository(private val dao: IraqDao) {
    val favorites: Flow<List<FavoritePlaceEntity>> = dao.getAllFavorites()
    val history: Flow<List<NavigationHistoryEntity>> = dao.getNavigationHistory()
    val offlineGovernorates: Flow<List<OfflineGovernorateEntity>> = dao.getAllOfflineGovernorates()

    suspend fun addFavorite(place: FavoritePlaceEntity) = dao.insertFavorite(place)
    suspend fun removeFavorite(id: Int) = dao.deleteFavoriteById(id)

    suspend fun recordNavigation(entry: NavigationHistoryEntity) = dao.insertHistory(entry)
    suspend fun removeHistoryItem(id: Int) = dao.deleteHistoryById(id)
    suspend fun clearHistory() = dao.clearAllHistory()

    suspend fun setGovernorateDownloaded(code: String, downloaded: Boolean) {
        dao.updateGovernorateDownloadState(code, downloaded)
    }

    suspend fun downloadAllIraqGovernorates(governorates: List<OfflineGovernorateEntity>) {
        dao.insertOfflineGovernorates(
            governorates.map { it.copy(isDownloaded = true, lastUpdatedTimestamp = System.currentTimeMillis()) }
        )
    }

    suspend fun ensureSeedData(
        defaultGovernorates: List<OfflineGovernorateEntity>,
        defaultFavorites: List<FavoritePlaceEntity>,
        defaultHistory: List<NavigationHistoryEntity>
    ) {
        if (dao.countOfflineGovernorates() == 0) {
            dao.insertOfflineGovernorates(defaultGovernorates)
        }
        if (dao.countFavorites() == 0) {
            defaultFavorites.forEach { dao.insertFavorite(it) }
            defaultHistory.forEach { dao.insertHistory(it) }
        }
    }
}
