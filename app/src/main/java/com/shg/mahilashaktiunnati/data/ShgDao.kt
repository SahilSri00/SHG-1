package com.shg.mahilashaktiunnati.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShgDao {
    @Insert
    suspend fun insertMember(member: MemberEntity): Long

    @Insert
    suspend fun insertSavingsEntry(entry: SavingsEntryEntity): Long

    @Insert
    suspend fun insertLoan(loan: LoanEntity): Long

    @Insert
    suspend fun insertRepayment(repayment: LoanRepaymentEntity): Long

    @Query("SELECT * FROM members ORDER BY name ASC")
    fun observeMembers(): Flow<List<MemberEntity>>

    @Transaction
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun observeMembersWithSavings(): Flow<List<MemberWithSavings>>

    @Transaction
    @Query("SELECT * FROM loans ORDER BY issuedAtMillis DESC")
    fun observeLoansWithRepayments(): Flow<List<LoanWithRepayments>>

    @Transaction
    @Query("SELECT * FROM loans ORDER BY issuedAtMillis DESC")
    suspend fun getLoansWithRepaymentsSnapshot(): List<LoanWithRepayments>

    @Query("SELECT * FROM loans WHERE memberId = :memberId AND isClosed = 0 LIMIT 1")
    suspend fun getOpenLoanForMember(memberId: Long): LoanEntity?

    @Query("UPDATE loans SET isClosed = :isClosed WHERE id = :loanId")
    suspend fun updateLoanStatus(loanId: Long, isClosed: Boolean)

    @Query("SELECT SUM(amount) FROM savings_entries")
    fun observeTotalSavings(): Flow<Double?>
}
