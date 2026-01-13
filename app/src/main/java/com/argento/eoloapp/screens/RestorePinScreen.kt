package com.argento.eoloapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.argento.eoloapp.components.HeadingTextComponent
import com.argento.eoloapp.components.PasswordInputComponent
import com.argento.eoloapp.session.SessionManager
import com.argento.eoloapp.viewmodel.RestorePinViewModel
import com.argento.eoloapp.viewmodel.RestorePinViewModelFactory
import com.argento.eoloapp.viewmodel.RestorePinState

@Composable
fun RestorePinScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val restorePinViewModel: RestorePinViewModel = viewModel(factory = RestorePinViewModelFactory(sessionManager))

    var pin by remember { mutableStateOf("") }
    val restorePinState by restorePinViewModel.restorePinState.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        color = Color.White
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(50.dp))
            HeadingTextComponent(heading = "Asignar nuevo PIN")
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Asigne un nuevo pin de 4 dígitos para su próximo inicio de sesión.")
            Spacer(modifier = Modifier.height(20.dp))
            PasswordInputComponent(labelVal = "Nuevo PIN", value = pin, onValueChange = {
                if (it.length <= 4) {
                    pin = it
                }
            })
            Spacer(modifier = Modifier.height(15.dp))

            when (restorePinState) {
                is RestorePinState.Loading -> {
                    CircularProgressIndicator()
                }
                is RestorePinState.Error -> {
                    val error = (restorePinState as RestorePinState.Error).message
                    Text(text = error, color = Color.Red)
                }
                is RestorePinState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("HomeScreen") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = { restorePinViewModel.assignPin(pin) },
                enabled = pin.length == 4
            ) {
                Text(text = "Guardar nuevo PIN")
            }
        }
    }
}
