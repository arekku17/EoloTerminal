package com.argento.eoloapp.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.argento.eoloapp.ui.theme.BrandColor
import com.argento.eoloapp.R
import com.argento.eoloapp.data.CobrarReservationRequest
import com.argento.eoloapp.data.Result
import com.argento.eoloapp.data.UserPreferences
import com.argento.eoloapp.viewmodel.PaymentMethodViewModelFactory
import com.argento.eoloapp.viewmodel.PaymentMethodViewModel
import com.argento.eoloapp.session.SessionManager
import com.mercadolibre.android.point_integration_sdk.nativesdk.MPManager
import com.mercadolibre.android.point_integration_sdk.nativesdk.message.utils.doIfError
import com.mercadolibre.android.point_integration_sdk.nativesdk.message.utils.doIfSuccess
import com.mercadolibre.android.point_integration_sdk.nativesdk.payment.data.PaymentFlowRequestData
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(navController: NavController, folio: String, amountString: String, idEstacionamiento: String) {
    val context = LocalContext.current
    val amount = amountString.toDoubleOrNull() ?: 0.0

    val sessionManager = remember { SessionManager(context) }
    val viewModel: PaymentMethodViewModel = viewModel(factory = PaymentMethodViewModelFactory(sessionManager))
    val cobroState by viewModel.cobroState.collectAsState()
    val isLoading = cobroState is Result.Loading

    var isCashSelected by remember { mutableStateOf(false) }
    var isTerminalSelected by remember { mutableStateOf(false) }

    var cashReceived by remember { mutableStateOf("") }

    val cashReceivedValue = cashReceived.toDoubleOrNull() ?: 0.0

    // Calculations
    val remainderForTerminal = if (isCashSelected && isTerminalSelected) {
        if (cashReceivedValue >= amount) 0.0 else amount - cashReceivedValue
    } else if (isTerminalSelected) {
        amount
    } else {
        0.0
    }

    val change = if (isCashSelected) {
        if (cashReceivedValue > amount) cashReceivedValue - amount else 0.0
    } else {
        0.0
    }

    val isPaymentMethodSelected = isCashSelected || isTerminalSelected

    val isCashValid = if (isCashSelected && !isTerminalSelected) {
        cashReceived.isNotEmpty() && cashReceivedValue >= amount
    } else {
        !isCashSelected || cashReceived.isNotEmpty()
    }

    val isButtonEnabled = isPaymentMethodSelected && isCashValid

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetCobroState()
        }
    }

    LaunchedEffect(cobroState) {
        when (cobroState) {
            is Result.Success -> {
                Toast.makeText(context, "Pago en Efectivo Completado", Toast.LENGTH_SHORT).show()
                navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                navController.popBackStack()
            }
            is Result.Error -> {
                val errorMessage = (cobroState as Result.Error).exception.message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }
    

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Método de Pago", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FOLIO", fontSize = 10.sp, color = Color.Gray)
                            Text(folio, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandColor)
                        }
                        Surface(
                            color = Color(0xFFE0F2F1),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Por cobrar",
                                color = Color(0xFF00695C),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("MONTO A PAGAR", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US).format(amount),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandColor
                    )
                }
            }

            Text("Elige una opción", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            // Payment Options
            PaymentOptionCard(
                title = "Efectivo",
                subtitle = "Pago directo en caja",
                selected = isCashSelected,
                icon = {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                        Text("$", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                },
                onSelect = { isCashSelected = !isCashSelected }
            )

            PaymentOptionCard(
                title = "Terminal Bancaria",
                subtitle = "Tarjeta Crédito / Débito",
                selected = isTerminalSelected,
                icon = {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(id = R.drawable.lock), contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(24.dp))
                    }
                },
                onSelect = { isTerminalSelected = !isTerminalSelected }
            )

            // Logic Sections
            if (isCashSelected) {
                OutlinedTextField(
                    value = cashReceived,
                    onValueChange = { cashReceived = it },
                    label = { Text("Efectivo Recibido") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = BrandColor,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                if (change > 0) {
                    Text("Cambio: ${NumberFormat.getCurrencyInstance(Locale.US).format(change)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }

            if (isTerminalSelected) {
                if (isCashSelected) {
                    Text("Restante a cobrar en terminal: ${NumberFormat.getCurrencyInstance(Locale.US).format(remainderForTerminal)}", fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, contentDescription = "Info", tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Al seleccionar Terminal, se va a cobrar el monto en este dispositivo",
                            fontSize = 12.sp,
                            color = Color(0xFF0D47A1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Button
            Button(
                onClick = {
                    val amountToCharge = if (isTerminalSelected && isCashSelected) remainderForTerminal else if (isTerminalSelected) amount else 0.0

                    if (isTerminalSelected && amountToCharge > 0) {

                        MPManager.paymentMethodsTools.getPaymentMethods { response ->
                            response.doIfSuccess { result ->
                                val paymentFlow = MPManager.paymentFlow
                                val paymentFlowRequestData = PaymentFlowRequestData(
                                    amount = amountToCharge,
                                    description = "Pago Folio $folio",
                                    paymentMethod = result[0],
                                    printOnTerminal = false
                                )
                                paymentFlow.launchPaymentFlow(paymentFlowRequestData = paymentFlowRequestData) { mpResponse ->
                                    mpResponse.doIfSuccess { paymentResult ->
                                        Toast.makeText(context, "Pago exitoso: ${paymentResult.paymentReference}", Toast.LENGTH_LONG).show()
                                    }.doIfError { error ->
                                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }.doIfError { exception ->
                                Toast.makeText(context, "Error MP: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                        }

                        Toast.makeText(context, "Simulando cobro Terminal: ${NumberFormat.getCurrencyInstance(Locale.US).format(amountToCharge)}", Toast.LENGTH_SHORT).show()
                    } else if (isCashSelected) {
                            val request = CobrarReservationRequest(
                                monto_saldo = 0.0,
                                wallet = "asdasd",
                                montoreserva = amount,
                                metodopago1 = "Efectivo",
                                metodopago2 = null,
                                tipotarjeta = "",
                                monto1 = amount,
                                monto2 = 0.0,
                                reserva = idEstacionamiento
                            )
                            viewModel.cobrarReservation(request)

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
                shape = RoundedCornerShape(8.dp),
                enabled = isButtonEnabled && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirmar Pago", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun PaymentOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFF0F4F8) else Color.White),
        border = if (selected) BorderStroke(2.dp, BrandColor) else null,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C3E50))
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = BrandColor)
            )
        }
    }
}

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BrandColor)
    }
}