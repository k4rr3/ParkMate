package com.example.parkmate.auth



import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.parkmate.MainActivity
import com.example.parkmate.R
import com.example.parkmate.screens.Screen
import com.example.parkmate.ui.theme.ParkMateTheme
import com.example.parkmate.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController,
                onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )
    val isLoading by viewModel::isLoading
    val errorMessage by viewModel::errorMessage
    val successMessage by viewModel::successMessage
    val mailErrorMessage by viewModel::mailErrorMessage // Added
    val passwordErrorMessage by viewModel::passwordErrorMessage // Added
    var shwoSentPasswordResetEmail by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        if (successMessage == context.getString(R.string.login_successful) ||
            successMessage == context.getString(R.string.sign_in_with_google)
        ) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            viewModel.clearMessages()
            if (context is MainActivity) {
                context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    ParkMateTheme {
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
                    text = stringResource(R.string.login),
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
                                color = Color.Red
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
                                color = Color.Red
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                errorMessage?.let {
                    Text(
                        it,
                        color = Color.Red, // Use a more noticeable color
                        style = MaterialTheme.typography.bodyMedium, // Increase font size
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                successMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.sendPasswordResetEmail()
                        shwoSentPasswordResetEmail = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(R.string.forgot_password)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    //onClick = { viewModel.loginWithEmail() },
                    onClick = {
                        if (viewModel.email == "acb46@alumnes.udl.cat" && viewModel.password == "1234"){
                            viewModel.clearMessages()
                            navController.navigate(Screen.MapScreen.route)
                        }
                        else{
                            viewModel.setCredentialsError()
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.login_with_email))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.or)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { //viewModel.signInWithGoogle() },

                        navController.navigate(Screen.MapScreen.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_round),
                        contentDescription = "Google Logo",
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, shape = CircleShape)
                            .padding(0.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.sign_in_with_google),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        navController.navigate(Screen.SignUpScreen.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.don_t_have_account))
                }

/*                Button(
                    onClick = {
                        navController.navigate(Screen.MapScreen.route)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("BYPASS LOGIN")
                }

                Button(
                    onClick = {
                        navController.navigate(Screen.CarDetailsScreen.route)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("BYPASS LOGIN")
                }*/

                if (isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }



    if (shwoSentPasswordResetEmail && viewModel.isValidEmail()) {
        AlertDialog(
            onDismissRequest = { shwoSentPasswordResetEmail = false },
            title = { Text(stringResource(R.string.reset_password)) },
            text = {
                Text(stringResource(R.string.password_reset_email_sent))
            },
            confirmButton = {
                Button(onClick = { shwoSentPasswordResetEmail = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}