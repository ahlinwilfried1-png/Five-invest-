package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Query("SELECT * FROM users WHERE referredBy = :referralCode ORDER BY id DESC")
    fun getReferredUsers(referralCode: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments WHERE userId = :userId ORDER BY id DESC")
    fun getInvestmentsForUserFlow(userId: Int): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments ORDER BY id DESC")
    fun getAllInvestmentsFlow(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE id = :id LIMIT 1")
    suspend fun getInvestmentById(id: Int): InvestmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: InvestmentEntity): Long

    @Update
    suspend fun updateInvestment(investment: InvestmentEntity)

    @Delete
    suspend fun deleteInvestment(investment: InvestmentEntity)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY timestamp DESC")
    fun getDepositsForUserFlow(userId: Int): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits ORDER BY timestamp DESC")
    fun getAllDepositsFlow(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Int): DepositEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getWithdrawalsForUserFlow(userId: Int): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Int): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLogsForUserFlow(userId: Int): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 0 ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: Int): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: NotificationEntity)
}

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        InvestmentEntity::class,
        DepositEntity::class,
        WithdrawalEntity::class,
        ActivityLogEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun depositDao(): DepositDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun notificationDao(): NotificationDao
}
