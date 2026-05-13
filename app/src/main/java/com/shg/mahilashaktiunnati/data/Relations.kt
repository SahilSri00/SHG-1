package com.shg.mahilashaktiunnati.data

import androidx.room.Embedded
import androidx.room.Relation

data class MemberWithSavings(
    @Embedded val member: MemberEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "memberId"
    )
    val savings: List<SavingsEntryEntity>
)

data class LoanWithRepayments(
    @Embedded val loan: LoanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "loanId"
    )
    val repayments: List<LoanRepaymentEntity>
)
