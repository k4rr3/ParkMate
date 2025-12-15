package com.example.parkmate.auth

import androidx.compose.runtime.getValue


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.example.parkmate.screens.Screen
import com.example.parkmate.ui.components.PTextField
import com.example.parkmate.ui.components.PTextFieldConfig
import com.example.parkmate.ui.components.PasswordTextField
import com.example.parkmate.ui.components.PpwdFieldConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = hiltViewModel()

    val state by viewModel::isLoading
    val errorMessage by viewModel::errorMessage
    val successMessage by viewModel::successMessage
    val mailErrorMessage by viewModel::mailErrorMessage
    val passwordErrorMessage by viewModel::passwordErrorMessage
    val isEmailVerified by viewModel::isEmailVerified
    var showVerificationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        when (successMessage) {
            context.getString(R.string.signup_master_successful) -> navController.navigate(Screen.MapScreen.route)
            context.getString(R.string.signup_successful) -> showVerificationDialog = true
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SignUpHeader()
            Spacer(Modifier.height(16.dp))

            EmailInput(viewModel.email, viewModel::updateEmail, mailErrorMessage)
            Spacer(Modifier.height(8.dp))

            PasswordInput(viewModel.password, viewModel::updatePassword, passwordErrorMessage)
            Spacer(Modifier.height(16.dp))

            SignUpButton(state) { viewModel.signUpWithEmail() }
            Spacer(Modifier.height(8.dp))
            NavigateToLoginButton { navController.navigate(Screen.LoginScreen.route) }

            MessageTexts(errorMessage, successMessage)

            if (state) CircularProgressIndicator()
            if (showVerificationDialog) VerificationDialog(
                isEmailVerified = isEmailVerified,
                onCheck = viewModel::checkEmailVerification,
                onProceed = { navController.navigate(Screen.MapScreen.route) },
                onCancel = { viewModel.clearMessages(); showVerificationDialog = false }
            )
        }
    }
}

@Composable
private fun SignUpHeader() {
    Text(
        text = stringResource(R.string.sign_up),
        fontSize = 35.sp
    )
}

@Composable
private fun EmailInput(
    email: String,
    onEmailChange: (String) -> Unit,
    error: String?
) {
    PTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        config = PTextFieldConfig(
            label = { Text(stringResource(R.string.email), color = MaterialTheme.colorScheme.onBackground) },
            isError = error != null,
            supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        )
    )
}

@Composable
private fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?
) {
    PasswordTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        config = PpwdFieldConfig(
            label = { Text(stringResource(R.string.password), color = MaterialTheme.colorScheme.onBackground) },
            isError = error != null,
            supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true
        )
    )
}

@Composable
private fun SignUpButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) { Text(stringResource(R.string.sign_up)) }
}

@Composable
private fun NavigateToLoginButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(R.string.already_have_account)) }
}

@Composable
private fun MessageTexts(errorMessage: String?, successMessage: String?) {
    errorMessage?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
    successMessage?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun VerificationDialog(
    isEmailVerified: Boolean,
    onCheck: () -> Unit,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.verify_email)) },
        text = {
            Text(
                if (isEmailVerified) stringResource(R.string.email_verified)
                else stringResource(R.string.please_verify_email)
            )
        },
        confirmButton = {
            if (!isEmailVerified) {
                TextButton(onClick = onCheck) { Text(stringResource(R.string.check_verification)) }
            } else {
                Button(onClick = onProceed) { Text(stringResource(R.string.proceed)) }
            }
        },
        dismissButton = {
            if (!isEmailVerified) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}


