package com.example.composetutorial

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM User ORDER BY id DESC LIMIT 1")
    suspend fun getLastUser(): User?
}