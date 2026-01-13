package com.argento.eoloapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.argento.eoloapp.R
import com.argento.eoloapp.components.BottomComponent
import com.argento.eoloapp.components.HeadingTextComponent
import com.argento.eoloapp.components.ImageComponent
import com.argento.eoloapp.components.MyTextField
import com.argento.eoloapp.components.PasswordInputComponent
import com.argento.eoloapp.session.SessionManager
import com.argento.eoloapp.viewmodel.LoginViewModel
import com.argento.eoloapp.viewmodel.LoginViewModelFactory
import com.argento.eoloapp.viewmodel.LoginState

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(sessionManager))

    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val loginState by loginViewModel.loginState.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        color = Color.White
    ) {
        Column {
            Spacer(modifier = Modifier.height(50.dp))
            ImageComponent(image = R.drawable.eolo, 100.dp)
            Spacer(modifier = Modifier.height(50.dp))
            HeadingTextComponent(heading = "Iniciar Sesión")
            Spacer(modifier = Modifier.height(20.dp))
            MyTextField(labelVal = "Teléfono", icon = R.drawable.lockphone, value = phone, onValueChange = {
                if (it.length <= 10){
                    phone = it
                }
            })
            Spacer(modifier = Modifier.height(15.dp))
            PasswordInputComponent(labelVal = "PIN", value = pin, onValueChange = {
                if (it.length <= 4) {
                    pin = it
                }
            })
            Spacer(modifier = Modifier.height(15.dp))

            when (loginState) {
                is LoginState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is LoginState.Error -> {
                    val error = (loginState as LoginState.Error).message
                    Text(text = error, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is LoginState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("HomeScreen") {
                            popUpTo("LoginScreen") { inclusive = true }
                        }
                    }
                }
                else -> {}
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                BottomComponent(
                    onLoginClick = { loginViewModel.login(phone, pin) },
                    onSmsLoginClick = {
                        loginViewModel.sendOtp(phone)
                        navController.navigate("SmsLoginScreen/$phone")
                                      },
                    phone.isNotEmpty()
                )
            }
        }
    }
}