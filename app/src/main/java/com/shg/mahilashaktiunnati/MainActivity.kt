package com.shg.mahilashaktiunnati

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shg.mahilashaktiunnati.ui.MahilaShaktiUnnatiApp
import com.shg.mahilashaktiunnati.ui.MahilaShaktiViewModel
import com.shg.mahilashaktiunnati.ui.MahilaShaktiViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MahilaShaktiViewModel = viewModel(
                factory = MahilaShaktiViewModelFactory(applicationContext)
            )
            val state by viewModel.uiState.collectAsState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MahilaShaktiUnnatiApp(
                    state = state,
                    onAddMember = { name: String, phone: String, address: String, photoUri: Uri? ->
                        viewModel.addMember(name, phone, address, photoUri?.toString())
                    },
                    onAddSavings = { memberId: Long, weekLabel: String, amount: Double, paid: Boolean ->
                        viewModel.addSavings(memberId, weekLabel, amount, paid)
                    },
                    onCreateLoan = { memberId: Long, principal: Double, rate: Double ->
                        viewModel.createLoan(memberId, principal, rate)
                    },
                    onRepayment = { loanId: Long, amount: Double ->
                        viewModel.addRepayment(loanId, amount)
                    },
                    onShareSummary = { text ->
                        ShareUtil.shareText(this, text)
                    },
                    onMessageShown = {
                        viewModel.clearMessage()
                    }
                )
            }
        }
    }
}
