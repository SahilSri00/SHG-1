package com.shg.mahilashaktiunnati.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shg.mahilashaktiunnati.data.AppDatabase
import com.shg.mahilashaktiunnati.data.ShgRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MahilaShaktiViewModel(
    private val repository: ShgRepository
) : ViewModel() {

    private val messageFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AppUiState> = combine(
        repository.membersWithSavings,
        repository.loansWithRepayments,
        repository.totalSavings,
        messageFlow
    ) { membersWithSavings, loansWithRepayments, totalSavingsRaw, message ->
        val members = membersWithSavings.map { item ->
            MemberUi(
                id = item.member.id,
                name = item.member.name,
                phone = item.member.phone,
                address = item.member.address,
                photoUri = item.member.photoUri,
                totalSavings = item.savings.sumOf { it.amount }
            )
        }
        val nameById = members.associateBy { it.id }
        val loans = loansWithRepayments.map { loanItem ->
            val repaid = loanItem.repayments.sumOf { it.amount }
            val payable = repository.totalPayable(
                principal = loanItem.loan.principal,
                ratePercent = loanItem.loan.interestRatePercent
            )
            LoanUi(
                id = loanItem.loan.id,
                memberId = loanItem.loan.memberId,
                memberName = nameById[loanItem.loan.memberId]?.name ?: "Unknown",
                principal = loanItem.loan.principal,
                interestRatePercent = loanItem.loan.interestRatePercent,
                totalPayable = payable,
                totalRepaid = repaid,
                isClosed = loanItem.loan.isClosed
            )
        }
        AppUiState(
            members = members,
            loans = loans,
            totalGroupSavings = totalSavingsRaw ?: 0.0,
            message = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState()
    )

    fun clearMessage() {
        messageFlow.value = null
    }

    fun addMember(name: String, phone: String, address: String, photoUri: String?) {
        viewModelScope.launch {
            if (name.isBlank()) {
                messageFlow.value = "Member name is required."
                return@launch
            }
            if (!phone.matches(Regex("^\\d{10}$"))) {
                messageFlow.value = "Phone number must be exactly 10 digits."
                return@launch
            }
            if (address.isBlank()) {
                messageFlow.value = "Address is required."
                return@launch
            }
            repository.addMember(name, phone, address, photoUri)
            messageFlow.value = "Member added."
        }
    }

    fun addSavings(memberId: Long, weekLabel: String, amount: Double, isPaid: Boolean) {
        viewModelScope.launch {
            if (weekLabel.isBlank()) {
                messageFlow.value = "Week label is required."
                return@launch
            }
            if (amount <= 0.0) {
                messageFlow.value = "Savings amount must be positive."
                return@launch
            }
            repository.addSavings(memberId, weekLabel, amount, isPaid)
            messageFlow.value = "Savings entry added."
        }
    }

    fun createLoan(memberId: Long, principal: Double, interestRatePercent: Double) {
        viewModelScope.launch {
            if (principal <= 0.0) {
                messageFlow.value = "Principal must be a positive number."
                return@launch
            }
            if (interestRatePercent < 0.0) {
                messageFlow.value = "Interest rate cannot be negative."
                return@launch
            }
            val result = repository.createLoan(memberId, principal, interestRatePercent)
            messageFlow.value = result.exceptionOrNull()?.message ?: "Loan recorded."
        }
    }

    fun addRepayment(loanId: Long, amount: Double) {
        viewModelScope.launch {
            if (amount <= 0.0) {
                messageFlow.value = "Repayment amount must be positive."
                return@launch
            }
            val currentLoan = uiState.value.loans.find { it.id == loanId }
            if (currentLoan != null) {
                val remaining = currentLoan.totalPayable - currentLoan.totalRepaid
                if (amount > remaining) {
                    messageFlow.value = "Repayment amount exceeds the remaining balance (${"%.2f".format(remaining)})."
                    return@launch
                }
            }
            val result = repository.addRepayment(loanId, amount)
            messageFlow.value = result.exceptionOrNull()?.message ?: "Repayment recorded."
        }
    }

}

class MahilaShaktiViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.create(context)
        val repository = ShgRepository(database.shgDao())
        @Suppress("UNCHECKED_CAST")
        return MahilaShaktiViewModel(repository) as T
    }
}
