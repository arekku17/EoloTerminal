package com.argento.eoloapp.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.argento.eoloapp.R
import com.argento.eoloapp.components.StatusBadge
import com.argento.eoloapp.data.ReservationDetailWrapper
import com.argento.eoloapp.session.SessionManager
import com.argento.eoloapp.ui.theme.BrandColor
import com.argento.eoloapp.utils.TarifaUtils
import com.argento.eoloapp.utils.formatDetailDate
import com.argento.eoloapp.viewmodel.MovementDetailState
import com.argento.eoloapp.viewmodel.MovementDetailViewModel
import com.argento.eoloapp.viewmodel.MovementDetailViewModelFactory
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementDetailScreen(navController: NavController, movementId: String, parkingId: String) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: MovementDetailViewModel = viewModel(
        factory = MovementDetailViewModelFactory(sessionManager, movementId, parkingId)
    )
    val state by viewModel.uiState.collectAsState()

    val currentBackStackEntry = navController.currentBackStackEntry
    val refreshState = currentBackStackEntry?.savedStateHandle?.getStateFlow("refresh", false)?.collectAsState()

    LaunchedEffect(refreshState?.value) {
        if (refreshState?.value == true) {
            viewModel.fetchReservationDetail()
            currentBackStackEntry.savedStateHandle["refresh"] = false
        }
    }

    Scaffold(
        topBar = {
            MovementDetailTopBar(
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            if (state is MovementDetailState.Success) {
                val reservaWrapper = (state as MovementDetailState.Success).reserva
                if (reservaWrapper.reserva.MontoTotal == null) {
                    val totalCalculado = if (reservaWrapper.reserva.fechaSalida == null) {
                        TarifaUtils.calcularTotal(
                            fechaIngreso = reservaWrapper.reserva.fechaIngreso ?: 0L,
                            fechaSalida = System.currentTimeMillis(),
                            logicaTarifa = reservaWrapper.tarifa.logicaTarifa,
                            frecuencia = reservaWrapper.tarifa.frecuencia,
                            tarifa = reservaWrapper.tarifa.tarifa,
                            tarifaJson = reservaWrapper.reserva.TarifaJSON
                        )
                    } else {
                        reservaWrapper.reserva.MontoTotal ?: 0.0
                    }
                    val folio = reservaWrapper.reserva.folio ?: "N/A"

                    val idEstacionamiento = reservaWrapper.reserva.id

                    BottomActionButtons(onCobrarClick = {
                        navController.navigate("PaymentMethodScreen/$idEstacionamiento/$folio/$totalCalculado")
                    })
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
        ) {
            when (state) {
                is MovementDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MovementDetailState.Error -> {
                    val error = (state as MovementDetailState.Error).message
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MovementDetailState.Success -> {
                    val reserva = (state as MovementDetailState.Success).reserva
                    MovementDetailContent(reserva = reserva)
                }
            }
        }
    }
}

@Composable
fun MovementDetailTopBar(onBackClick: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "eolo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF2C3E50))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Detalle de Transacción",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            }
        }
    }
}

fun bitmapFromUrl(url: String): Bitmap? {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun resizeBitmap(bitmap: Bitmap, targetWidth: Int): Bitmap {
    val ratio = targetWidth.toFloat() / bitmap.width
    val height = (bitmap.height * ratio).toInt()
    return Bitmap.createScaledBitmap(bitmap, targetWidth, height, true)
}

@Composable
fun MovementDetailContent(reserva: ReservationDetailWrapper) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val printAction: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            try {
                val printer = EscPosPrinter(
                    BluetoothPrintersConnections.selectFirstPaired(),
                    203,
                    48f,
                    32,
                    EscPosCharsetEncoding("windows-1252", 16)
                )

                var imageHex = ""
                try {
                    val originalBitmap = bitmapFromUrl("https:" + reserva.estacionamiento.Imagen ?: "")
                    if (originalBitmap != null) {
                        val resizedBitmap = resizeBitmap(originalBitmap, 180)
                        imageHex = PrinterTextParserImg.bitmapToHexadecimalString(
                            printer,
                            resizedBitmap
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val monto1 = (reserva.reserva.MontoReserva1 ?: 0.0) + (reserva.reserva.MontoServicios1 ?: 0.0)
                val monto2 = (reserva.reserva.MontoReserva2 ?: 0.0) + (reserva.reserva.MontoServicios2 ?: 0.0)

                val pagosText = buildString {
                    append("[C]================================\n")
                    append("[L]<font size='tall'>Pagos:</font>\n")
                    append("[L]Metodo Pago 1: ${reserva.reserva.MetodoPago1}\n")
                    append("[L]Monto 1: ${formatCurrency(monto1)}\n")

                    if (reserva.reserva.MetodoPago2 != null) {
                        append("[L]\n")
                        append("[L]Metodo Pago 2: ${reserva.reserva.MetodoPago2}\n")
                        append("[L]Monto 2: ${formatCurrency(monto2)}\n")
                    }

                    append("[C]<font size='big'>Monto: ${formatCurrency(reserva.reserva.MontoTotal ?: 0.0)}</font>")
                }

                val imageTag = if (imageHex.isNotEmpty()) "[C]<img>${imageHex}</img>\n" else ""

                printer.printFormattedText(
                    imageTag +
                    "[L]<b>Folio: ${reserva.reserva.folio}\n<b>" +
                            "[L]Fecha: ${formatDetailDate(reserva.reserva.createdDate, reserva.estacionamiento.timeZoneId)}\n" +
                            "[C]<b>" + reserva.estacionamiento.nombre + "</b>\n" +
                            "[C]<b>Dirección: </b>" + reserva.estacionamiento.DirecciónTicket + "\n" +
                            "[L]\n" +
                            "[C]================================\n" +
                            "[L]<font size='tall'>General:</font>\n" +
                            "[L]Entrada: ${formatDetailDate(reserva.reserva.fechaIngreso ?: 0, reserva.estacionamiento.timeZoneId)}\n" +
                            "[L]Salida: ${if (reserva.reserva.fechaSalida != null && reserva.reserva.fechaSalida > 0) formatDetailDate(reserva.reserva.fechaSalida, reserva.estacionamiento.timeZoneId) else "--"}\n" +
                            "[L]Personal: ${reserva.reserva.nombreResponsable ?: "Desconocido"}\n" +
                            "[C]================================\n" +
                            "[L]<font size='tall'>Vehiculo:</font>\n" +
                            "[L]Descripcion: ${reserva.reserva.tipoVehiculo ?: "Desconocido"} ${reserva.reserva.categoriaVehiculo ?: ""}\n" +
                            "[L]Num. Economico: ${reserva.reserva.numEconomico?: "Desconocido"}\n" +
                            "[L]Empresa: ${reserva.reserva.EmpresaText?: "Desconocido"}\n" +
                            "[L]Placas: ${reserva.reserva.placaVehiculo}\n" +
                            (if (reserva.reserva.placaCaja1 != null) "[L]Caja 1: ${reserva.reserva.placaCaja1}\n" else "") +
                            (if (reserva.reserva.placaCaja2 != null) "[L]Caja 2: ${reserva.reserva.placaCaja2}\n" else "") +
                            (if(reserva.reserva.fechaSalida != null) "${pagosText}" else "") +
                            "[L]\n" +
                            "[C]Para pagar/facturar\n" +
                            "[C]<qrcode size='20'>https://eolo.app/p?id=${reserva.reserva.id}</qrcode>"
                )

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al imprimir: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            printAction()
        } else {
            Toast.makeText(context, "Permisos de Bluetooth denegados", Toast.LENGTH_SHORT).show()
        }
    }

    val totalCalculado = if (reserva.reserva.fechaSalida == null) {
         TarifaUtils.calcularTotal(
            fechaIngreso = reserva.reserva.fechaIngreso ?: 0L,
            fechaSalida = System.currentTimeMillis(),
            logicaTarifa = reserva.tarifa.logicaTarifa,
            frecuencia = reserva.tarifa.frecuencia,
            tarifa = reserva.tarifa.tarifa,
            tarifaJson = reserva.reserva.TarifaJSON
        )
    } else {
        reserva.reserva.MontoTotal ?: 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("Folio:", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "#${reserva.reserva.folio ?: "N/A"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                    }
                    StatusBadge(status = reserva.reserva.estatus ?: "Desconocido")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Monto Total", fontSize = 12.sp, color = Color.Gray)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatDetailCurrency(totalCalculado),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val permissionsToRequest = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                                }
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                    permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
                                }
                            } else {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                                    permissionsToRequest.add(Manifest.permission.BLUETOOTH)
                                }
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                                    permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
                                }
                            }

                            if (permissionsToRequest.isEmpty()) {
                                printAction()
                            } else {
                                permissionLauncher.launch(permissionsToRequest.toTypedArray())
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.print),
                            contentDescription = "Imprimir",
                            tint = Color(0xFF2C3E50),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // General Info Section
        SectionTitle(icon = Icons.Default.Person, title = "Información General")
        
        InfoCard {
            InfoRow(label = "Fecha y Hora Entrada", value = formatDetailDate(reserva.reserva.fechaIngreso ?: 0, reserva.estacionamiento.timeZoneId))
            Divider(color = Color(0xFFF0F0F0))
            InfoRow(label = "Fecha y Hora Salida", value = if (reserva.reserva.fechaSalida != null && reserva.reserva.fechaSalida > 0) formatDetailDate(reserva.reserva.fechaSalida, reserva.estacionamiento.timeZoneId) else "--")
            Divider(color = Color(0xFFF0F0F0))
            InfoRow(label = "Personal", value = reserva.reserva.nombreResponsable ?: "Desconocido")
        }

        // Vehicle Info Section
        SectionTitle(icon = Icons.Default.Info, title = "Desglose Financiero")

        InfoCard {
            val monto1 = (reserva.reserva.MontoReserva1 ?: 0.0) + (reserva.reserva.MontoServicios1 ?: 0.0)
            InfoRow(label = "Método de Pago", value = reserva.reserva.MetodoPago1 ?: "N/A")
            Divider(color = Color(0xFFF0F0F0))
            InfoRow(label = "Monto", value = formatCurrency(monto1))
            if(reserva.reserva.MetodoPago2 !== null){
                val monto2 = (reserva.reserva.MontoReserva2 ?: 0.0) + (reserva.reserva.MontoServicios2 ?: 0.0)
                Divider(color = Color(0xFFF0F0F0))
                InfoRow(label = "Método de Pago 2", value = reserva.reserva.MetodoPago2)
                Divider(color = Color(0xFFF0F0F0))
                InfoRow(label = "Monto2", value = formatCurrency(monto2))
            }
        }

        // Vehicle Info Section
        SectionTitle(icon = Icons.Default.Info, title = "Información del Vehículo")
        
        InfoCard {
            val vehicleInfo = "${reserva.reserva.tipoVehiculo ?: ""} ${reserva.reserva.categoriaVehiculo ?: ""}".trim()
            InfoRow(label = "Vehículo", value = if (vehicleInfo.isBlank()) "Desconocido" else vehicleInfo)
            Divider(color = Color(0xFFF0F0F0))
            InfoRow(label = "Placas", value = reserva.reserva.placaVehiculo ?: "N/A")
            if(reserva.reserva.placaCaja1 !== null) {
                Divider(color = Color(0xFFF0F0F0))
                InfoRow(label = "Caja 1", value = reserva.reserva.placaCaja1)
            }
            if(reserva.reserva.placaCaja2 !== null) {
                Divider(color = Color(0xFFF0F0F0))
                InfoRow(label = "Caja 2", value = reserva.reserva.placaCaja2)
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
    }
}

@Composable
fun BottomActionButtons(onCobrarClick: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Multar Button (Warning)
            Button(
                onClick = { /* Handle Multar */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(56.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp) 
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Multar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Button(
                onClick = onCobrarClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Cobrar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SectionTitle(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFE0E5EC), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2C3E50), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
    }
}

@Composable
fun InfoCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
    }
}


@Composable
fun formatDetailCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}
