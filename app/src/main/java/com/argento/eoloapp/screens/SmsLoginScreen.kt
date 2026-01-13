package com.argento.eoloapp.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.argento.eoloapp.R
import com.argento.eoloapp.components.HeadingTextComponent
import com.argento.eoloapp.components.TextInfoComponent
import com.argento.eoloapp.session.SessionManager
import com.argento.eoloapp.ui.theme.BorderColor
import com.argento.eoloapp.ui.theme.BrandColor
import com.argento.eoloapp.ui.theme.Tertirary
import com.argento.eoloapp.viewmodel.LoginState
import com.argento.eoloapp.viewmodel.LoginViewModel
import com.argento.eoloapp.viewmodel.LoginViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsLoginScreen(navController: NavController, phoneNumber: String) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(sessionManager))
    val loginState by loginViewModel.loginState.collectAsState()


    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column{
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            if (navController.previousBackStackEntry != null){
                                navController.popBackStack()
                            }
                    }
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            HeadingTextComponent(heading = "Iniciar Sesión con SMS")
            TextInfoComponent(
                textVal = "Te enviamos un código por SMS a tu número de teléfono ingresado: $phoneNumber"
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.length <= 6) {
                        pin = it
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = BrandColor,
                    unfocusedBorderColor = BorderColor,
                    focusedLeadingIconColor = BrandColor,
                    unfocusedLeadingIconColor = Tertirary
                ),
                shape = MaterialTheme.shapes.small,
                label = { Text("PIN de 6 dígitos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                        navController.navigate("RestorePinScreen")
                    }
                }
                else -> {}
            }

            Button(
                onClick = { loginViewModel.verifySmsCode(phoneNumber, pin) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }

}