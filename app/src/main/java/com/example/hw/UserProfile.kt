package com.example.hw
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "user_name") val username: String,
    @ColumnInfo(name = "image_path") val imagePath: String?
)
