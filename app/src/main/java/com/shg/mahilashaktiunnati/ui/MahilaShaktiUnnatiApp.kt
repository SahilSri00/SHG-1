package com.shg.mahilashaktiunnati.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MahilaShaktiUnnatiApp(
    state: AppUiState,
    onAddMember: (String, String, String, Uri?) -> Unit,
    onAddSavings: (Long, String, Double, Boolean) -> Unit,
    onCreateLoan: (Long, Double, Double) -> Unit,
    onRepayment: (Long, Double) -> Unit,
    onShareSummary: (String) -> Unit,
    onMessageShown: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Members", "Savings", "Loans")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Mahila-Shakti Unnati",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Total Group Savings: ${"%.2f".format(state.totalGroupSavings)}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = {
            val summary = buildSummaryText(state)
            onShareSummary(summary)
        }) {
            Text("Export Summary (WhatsApp)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        when (selectedTab) {
            0 -> MemberDirectoryTab(state.members, onAddMember)
            1 -> SavingsTab(state.members, onAddSavings)
            else -> LoanTrackerTab(state.members, state.loans, onCreateLoan, onRepayment)
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

private fun buildSummaryText(state: AppUiState): String {
    val lines = mutableListOf<String>()
    lines += "Mahila-Shakti Unnati Summary"
    lines += "Total Group Savings: ${"%.2f".format(state.totalGroupSavings)}"
    lines += ""
    lines += "Members"
    state.members.forEach {
        lines += "${it.name} | Savings: ${"%.2f".format(it.totalSavings)}"
    }
    lines += ""
    lines += "Loans"
    state.loans.forEach {
        lines += "${it.memberName} | Payable: ${"%.2f".format(it.totalPayable)} | Repaid: ${"%.2f".format(it.totalRepaid)} | ${if (it.isClosed) "Closed" else "Open"}"
    }
    return lines.joinToString("\n")
}

@Composable
private fun MemberDirectoryTab(
    members: List<MemberUi>,
    onAddMember: (String, String, String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Add Member", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { imagePicker.launch("image/*") }) {
                            Text("Pick Photo")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(selectedImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Member Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        onAddMember(name, phone, address, selectedImageUri)
                        name = ""
                        phone = ""
                        address = ""
                        selectedImageUri = null
                    }) {
                        Text("Save Member")
                    }
                }
            }
        }

        items(members, key = { it.id }) { member ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (member.photoUri != null) {
                        AsyncImage(
                            model = member.photoUri,
                            contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    } else {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                            contentDescription = "No Photo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(member.name, fontWeight = FontWeight.Bold)
                        Text(member.phone)
                        Text(member.address)
                        Text("Savings: ${"%.2f".format(member.totalSavings)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsTab(
    members: List<MemberUi>,
    onAddSavings: (Long, String, Double, Boolean) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf<Long?>(members.firstOrNull()?.id) }
    var weekLabel by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf(true) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Weekly Savings Entry", fontWeight = FontWeight.Bold)
                    Text("Select Member")
                    members.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMemberId = member.id }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val marker = if (selectedMemberId == member.id) "[x]" else "[ ]"
                            Text("$marker ${member.name}")
                        }
                    }
                    Divider()
                    OutlinedTextField(
                        value = weekLabel,
                        onValueChange = { weekLabel = it },
                        label = { Text("Week Label (e.g., Week 12)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { paid = true }) { Text(if (paid) "Paid (Selected)" else "Paid") }
                        OutlinedButton(onClick = { paid = false }) { Text(if (!paid) "Pending (Selected)" else "Pending") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        selectedMemberId?.let { onAddSavings(it, weekLabel, amount, paid) }
                        weekLabel = ""
                        amountText = ""
                    }) {
                        Text("Save Entry")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanTrackerTab(
    members: List<MemberUi>,
    loans: List<LoanUi>,
    onCreateLoan: (Long, Double, Double) -> Unit,
    onRepayment: (Long, Double) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf<Long?>(members.firstOrNull()?.id) }
    var principalText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("2.0") }
    var repaymentText by remember { mutableStateOf("") }
    var selectedLoanId by remember { mutableStateOf<Long?>(loans.firstOrNull()?.id) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Create Loan", fontWeight = FontWeight.Bold)
                    members.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMemberId = member.id }
                                .padding(vertical = 4.dp)
                        ) {
                            val marker = if (selectedMemberId == member.id) "[x]" else "[ ]"
                            Text("$marker ${member.name}")
                        }
                    }
                    OutlinedTextField(
                        value = principalText,
                        onValueChange = { principalText = it },
                        label = { Text("Principal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Interest Rate (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        val principal = principalText.toDoubleOrNull() ?: 0.0
                        val rate = rateText.toDoubleOrNull() ?: 0.0
                        selectedMemberId?.let { onCreateLoan(it, principal, rate) }
                        principalText = ""
                    }) {
                        Text("Record Loan")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Add Repayment", fontWeight = FontWeight.Bold)
                    loans.filter { !it.isClosed }.forEach { loan ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLoanId = loan.id }
                                .padding(vertical = 4.dp)
                        ) {
                            val marker = if (selectedLoanId == loan.id) "[x]" else "[ ]"
                            Text("$marker ${loan.memberName} - Balance ${"%.2f".format(loan.totalPayable - loan.totalRepaid)}")
                        }
                    }
                    OutlinedTextField(
                        value = repaymentText,
                        onValueChange = { repaymentText = it },
                        label = { Text("Repayment Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        val amount = repaymentText.toDoubleOrNull() ?: 0.0
                        selectedLoanId?.let { onRepayment(it, amount) }
                        repaymentText = ""
                    }) {
                        Text("Save Repayment")
                    }
                }
            }
        }
        items(loans, key = { it.id }) { loan ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${loan.memberName} Loan", fontWeight = FontWeight.Bold)
                    Text("Principal: ${"%.2f".format(loan.principal)}")
                    Text("Interest: ${"%.2f".format(loan.interestRatePercent)}%")
                    Text("Payable: ${"%.2f".format(loan.totalPayable)}")
                    Text("Repaid: ${"%.2f".format(loan.totalRepaid)}")
                    Text("Status: ${if (loan.isClosed) "Closed" else "Open"}")
                }
            }
        }
    }
}
