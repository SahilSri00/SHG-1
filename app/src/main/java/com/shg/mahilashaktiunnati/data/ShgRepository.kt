package com.shg.mahilashaktiunnati.data

import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToLong

class ShgRepository(private val dao: ShgDao) {
    val membersWithSavings: Flow<List<MemberWithSavings>> = dao.observeMembersWithSavings()
    val loansWithRepayments: Flow<List<LoanWithRepayments>> = dao.observeLoansWithRepayments()
    val totalSavings: Flow<Double?> = dao.observeTotalSavings()

    suspend fun addMember(name: String, phone: String, address: String, photoUri: String?) {
        dao.insertMember(
            MemberEntity(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                photoUri = photoUri
            )
        )
    }

    suspend fun addSavings(memberId: Long, weekLabel: String, amount: Double, isPaid: Boolean) {
        dao.insertSavingsEntry(
            SavingsEntryEntity(
                memberId = memberId,
                weekLabel = weekLabel.trim(),
                amount = amount,
                isPaid = isPaid
            )
        )
    }

    suspend fun createLoan(memberId: Long, principal: Double, ratePercent: Double): Result<Long> {
        val openLoan = dao.getOpenLoanForMember(memberId)
        if (openLoan != null) {
            return Result.failure(
                IllegalStateException("Member already has an unpaid loan.")
            )
        }
        val loanId = dao.insertLoan(
            LoanEntity(
                memberId = memberId,
                principal = principal,
                interestRatePercent = ratePercent
            )
        )
        return Result.success(loanId)
    }

    suspend fun addRepayment(loanId: Long, amount: Double): Result<Unit> {
        val allLoans = dao.getLoansWithRepaymentsSnapshot()
        val loanWithRepayments = allLoans.firstOrNull { it.loan.id == loanId }
            ?: return Result.failure(IllegalArgumentException("Loan not found."))
        if (loanWithRepayments.loan.isClosed) {
            return Result.failure(IllegalStateException("Loan is already closed."))
        }

        dao.insertRepayment(LoanRepaymentEntity(loanId = loanId, amount = amount))

        val totalRepaid = loanWithRepayments.repayments.sumOf { it.amount } + amount
        val payable = totalPayable(
            principal = loanWithRepayments.loan.principal,
            ratePercent = loanWithRepayments.loan.interestRatePercent
        )
        if (totalRepaid >= payable) {
            dao.updateLoanStatus(loanId, true)
        }
        return Result.success(Unit)
    }

    fun totalPayable(principal: Double, ratePercent: Double): Double {
        val simpleInterest = principal * (ratePercent / 100.0)
        return ((principal + simpleInterest) * 100.0).roundToLong() / 100.0
    }
}
