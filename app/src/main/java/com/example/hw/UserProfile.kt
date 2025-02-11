package com.example.hw
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 0, // singleton profile
    @ColumnInfo(name = "first_name") val username: String,
    @ColumnInfo(name = "last_name") val imagePath: String?
)
