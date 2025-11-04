package com.example.parkmate.auth

import androidx.compose.runtime.getValue


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.example.parkmate.screens.Screen
import com.example.parkmate.ui.components.PTextField
import com.example.parkmate.ui.theme.ParkMateTheme
import com.example.parkmate.ui.components.PasswordTextField




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = hiltViewModel() //

    val state by viewModel::isLoading
    val errorMessage by viewModel::errorMessage
    val successMessage by viewModel::successMessage
    val mailErrorMessage by viewModel::mailErrorMessage
    val passwordErrorMessage by viewModel::passwordErrorMessage
    val isEmailVerified by viewModel::isEmailVerified
    var showVerificationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        if (successMessage == context.getString(R.string.signup_master_successful)) {
            navController.navigate(Screen.MapScreen.route)
        } else if (successMessage == context.getString(R.string.signup_successful)) {
            showVerificationDialog = true
        }
    }


        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.sign_up),
                    fontSize = 35.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                PTextField(
                    value = viewModel.email,
                    onValueChange = viewModel::updateEmail,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.email)) },
                    isError = mailErrorMessage != null,
                    supportingText = {
                        mailErrorMessage?.let {
                            Text(
                                text = it,
                                color =  MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordTextField(
                    value = viewModel.password,
                    onValueChange = viewModel::updatePassword,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.password)) },
                    isError = passwordErrorMessage != null,
                    supportingText = {
                        passwordErrorMessage?.let {
                            Text(
                                text = it,
                                color =  MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                       /* if (viewModel.validRegister()){
                            navController.navigate(Screen.LoginScreen.route)
                        }*/
                    },
                    enabled = !state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.sign_up))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        navController.navigate(Screen.LoginScreen.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.already_have_account))
                }


                errorMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                successMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (state) {
                    CircularProgressIndicator()
                }

                if (showVerificationDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = {
                            Text(
                                stringResource(R.string.verify_email)
                            )
                        },
                        text = {
                            Text(
                                if (isEmailVerified)
                                    stringResource(R.string.email_verified)
                                else
                                    stringResource(R.string.please_verify_email)
                            )
                        },
                        confirmButton = {
                            if (!isEmailVerified) {
                                TextButton(
                                    onClick = {}
                                    //onClick = { viewModel.checkEmailVerification() }
                                ) {
                                    Text(stringResource(R.string.check_verification))
                                }
                            } else {
                                Button(onClick = {
                                    navController.navigate(Screen.MapScreen.route)
                                }) {
                                    Text(stringResource(R.string.proceed))
                                }
                            }
                        },
                        dismissButton = {
                            if (!isEmailVerified) {
                                TextButton(onClick = {
                                    viewModel.clearMessages(); showVerificationDialog = false
                                }) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        }
                    )
                }
            }
        }
    
}


