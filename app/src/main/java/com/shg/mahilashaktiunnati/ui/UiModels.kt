package com.shg.mahilashaktiunnati.ui

data class MemberUi(
    val id: Long,
    val name: String,
    val phone: String,
    val address: String,
    val photoUri: String?,
    val totalSavings: Double
)

data class LoanUi(
    val id: Long,
    val memberId: Long,
    val memberName: String,
    val principal: Double,
    val interestRatePercent: Double,
    val totalPayable: Double,
    val totalRepaid: Double,
    val isClosed: Boolean
)

data class AppUiState(
    val members: List<MemberUi> = emptyList(),
    val loans: List<LoanUi> = emptyList(),
    val totalGroupSavings: Double = 0.0,
    val message: String? = null
)
