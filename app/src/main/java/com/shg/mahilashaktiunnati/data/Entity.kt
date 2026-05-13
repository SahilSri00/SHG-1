package com.shg.mahilashaktiunnati.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val photoUri: String?,
    val joinedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings_entries",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"])]
)
data class SavingsEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val weekLabel: String,
    val amount: Double,
    val isPaid: Boolean,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"])]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val principal: Double,
    val interestRatePercent: Double,
    val issuedAtMillis: Long = System.currentTimeMillis(),
    val isClosed: Boolean = false
)

@Entity(
    tableName = "loan_repayments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanId"])]
)
data class LoanRepaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanId: Long,
    val amount: Double,
    val paidAtMillis: Long = System.currentTimeMillis()
)
