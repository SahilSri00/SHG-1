package com.shg.mahilashaktiunnati.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Members", "Savings", "Loans")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Mahila-Shakti Unnati",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Group Savings: ₹${"%.2f".format(state.totalGroupSavings)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    val summary = buildSummaryText(state)
                    onShareSummary(summary)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Summary (WhatsApp)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            when (selectedTab) {
                0 -> MemberDirectoryTab(state.members, onAddMember)
                1 -> SavingsTab(state.members, onAddSavings)
                else -> LoanTrackerTab(state.members, state.loans, onCreateLoan, onRepayment)
            }
        }
    }
}

private fun buildSummaryText(state: AppUiState): String {
    val lines = mutableListOf<String>()
    lines += "*Mahila-Shakti Unnati Summary*"
    lines += "Total Group Savings: ₹${"%.2f".format(state.totalGroupSavings)}\n"
    lines += "*Members*"
    state.members.forEach {
        lines += "${it.name} | Savings: ₹${"%.2f".format(it.totalSavings)}"
    }
    lines += "\n*Loans*"
    state.loans.forEach {
        lines += "${it.memberName} | Payable: ₹${"%.2f".format(it.totalPayable)} | Repaid: ₹${"%.2f".format(it.totalRepaid)} | ${if (it.isClosed) "Closed" else "Open"}"
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

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Member", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone (10 digits)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick Photo")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
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
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onAddMember(name, phone, address, selectedImageUri)
                            name = ""
                            phone = ""
                            address = ""
                            selectedImageUri = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Member")
                    }
                }
            }
        }

        items(members, key = { it.id }) { member ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (member.photoUri != null) {
                        AsyncImage(
                            model = member.photoUri,
                            contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "No Photo", modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(member.phone, style = MaterialTheme.typography.bodyMedium)
                        Text(member.address, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Savings: ₹${"%.2f".format(member.totalSavings)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavingsTab(
    members: List<MemberUi>,
    onAddSavings: (Long, String, Double, Boolean) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var weekLabel by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    val selectedMemberName = members.find { it.id == selectedMemberId }?.name ?: "Select Member"

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Savings Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMemberName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Member") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.name) },
                                    onClick = {
                                        selectedMemberId = member.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weekLabel,
                        onValueChange = { weekLabel = it },
                        label = { Text("Week Label (e.g., Week 12)") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (₹)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = paid,
                            onClick = { paid = true },
                            label = { Text("Paid") },
                            leadingIcon = if (paid) { { Icon(Icons.Default.Check, null) } } else null
                        )
                        FilterChip(
                            selected = !paid,
                            onClick = { paid = false },
                            label = { Text("Pending") },
                            leadingIcon = if (!paid) { { Icon(Icons.Default.Pending, null) } } else null
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            selectedMemberId?.let { onAddSavings(it, weekLabel, amount, paid) }
                            weekLabel = ""
                            amountText = ""
                            selectedMemberId = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Entry")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanTrackerTab(
    members: List<MemberUi>,
    loans: List<LoanUi>,
    onCreateLoan: (Long, Double, Double) -> Unit,
    onRepayment: (Long, Double) -> Unit
) {
    var loanMemberId by remember { mutableStateOf<Long?>(null) }
    var loanExpanded by remember { mutableStateOf(false) }
    
    var principalText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("2.0") }
    
    var repaymentLoanId by remember { mutableStateOf<Long?>(null) }
    var repaymentExpanded by remember { mutableStateOf(false) }
    var repaymentText by remember { mutableStateOf("") }

    val loanMemberName = members.find { it.id == loanMemberId }?.name ?: "Select Member"
    val openLoans = loans.filter { !it.isClosed }
    val repaymentLoanName = openLoans.find { it.id == repaymentLoanId }?.let { "${it.memberName} (Bal: ₹${"%.2f".format(it.totalPayable - it.totalRepaid)})" } ?: "Select Open Loan"

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Create Loan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = loanExpanded,
                        onExpandedChange = { loanExpanded = !loanExpanded }
                    ) {
                        OutlinedTextField(
                            value = loanMemberName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Member") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = loanExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = loanExpanded,
                            onDismissRequest = { loanExpanded = false }
                        ) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.name) },
                                    onClick = {
                                        loanMemberId = member.id
                                        loanExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = principalText,
                        onValueChange = { principalText = it },
                        label = { Text("Principal (₹)") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Interest Rate (%)") },
                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val principal = principalText.toDoubleOrNull() ?: 0.0
                            val rate = rateText.toDoubleOrNull() ?: 0.0
                            loanMemberId?.let { onCreateLoan(it, principal, rate) }
                            principalText = ""
                            loanMemberId = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Loan")
                    }
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Repayment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = repaymentExpanded,
                        onExpandedChange = { repaymentExpanded = !repaymentExpanded }
                    ) {
                        OutlinedTextField(
                            value = repaymentLoanName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Open Loan") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repaymentExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = repaymentExpanded,
                            onDismissRequest = { repaymentExpanded = false }
                        ) {
                            openLoans.forEach { loan ->
                                DropdownMenuItem(
                                    text = { Text("${loan.memberName} - Bal: ₹${"%.2f".format(loan.totalPayable - loan.totalRepaid)}") },
                                    onClick = {
                                        repaymentLoanId = loan.id
                                        repaymentExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repaymentText,
                        onValueChange = { repaymentText = it },
                        label = { Text("Repayment Amount (₹)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amount = repaymentText.toDoubleOrNull() ?: 0.0
                            repaymentLoanId?.let { onRepayment(it, amount) }
                            repaymentText = ""
                            repaymentLoanId = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Repayment")
                    }
                }
            }
        }
        items(loans, key = { it.id }) { loan ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (loan.isClosed) Icons.Default.CheckCircle else Icons.Default.MonetizationOn, 
                             contentDescription = null, 
                             tint = if (loan.isClosed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${loan.memberName} Loan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Badge(containerColor = if (loan.isClosed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer) {
                            Text(if (loan.isClosed) "Closed" else "Open", modifier = Modifier.padding(4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Principal: ₹${"%.2f".format(loan.principal)}")
                        Text("Interest: ${"%.2f".format(loan.interestRatePercent)}%")
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Payable: ₹${"%.2f".format(loan.totalPayable)}", fontWeight = FontWeight.Bold)
                        Text("Repaid: ₹${"%.2f".format(loan.totalRepaid)}", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
