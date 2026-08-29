package com.phoneaccessqr.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.phoneaccessqr.app.models.AccessPermission

@Database(entities = [AccessPermission::class], version = 1, exportSchema = false)
abstract class PhoneAccessQRDatabase : RoomDatabase() {

    abstract fun accessPermissionDao(): AccessPermissionDao

    companion object {
        @Volatile
        private var INSTANCE: PhoneAccessQRDatabase? = null

        fun getInstance(context: Context): PhoneAccessQRDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhoneAccessQRDatabase::class.java,
                    "phone_access_qr_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
