package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val balance: Double = 0.0,
    val dailyIncome: Double = 0.0,
    val totalIncome: Double = 0.0,
    val referralCode: String,
    val referredBy: String? = null,
    val mobileMoneyProvider: String = "",
    val mobileMoneyNumber: String = "",
    val isAdmin: Boolean = false,
    val isBlocked: Boolean = false,
    val otpCode: String = "",
    val isOtpVerified: Boolean = false
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val dailyIncome: Double,
    val durationDays: Int, // e.g., 30 days
    val bonus: Double = 0.0,
    val purchaseLimit: Int = 5,
    val isActive: Boolean = true,
    val imageType: String = "gold" // "classic", "premium", "bronze", "gold", "silver", "crypto"
)

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val pricePaid: Double,
    val dailyIncome: Double,
    val purchaseTimestamp: Long = System.currentTimeMillis(),
    val durationDays: Int,
    val daysRemaining: Int,
    val isActive: Boolean = true,
    val lastClaimedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val proofImagePath: String = "", // e.g. mock camera/gallery proof uri or file name
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val transactionId: String,
    val adminNotes: String = ""
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val mobileProvider: String,
    val mobileNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val transactionId: String,
    val adminNotes: String = ""
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "INVESTMENT", "REFERRAL_BONUS", "DAILY_INTEREST", "AUTH"
    val amount: Double = 0.0,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // 0 means broadcast notification for all users
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
