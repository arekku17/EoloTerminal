package com.argento.eoloapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.argento.eoloapp.components.StatusBadge
import com.argento.eoloapp.data.EstacionamientoDetailData
import com.argento.eoloapp.data.Reserva
import com.argento.eoloapp.data.Tarifa
import com.argento.eoloapp.session.SessionManager
import com.argento.eoloapp.utils.formatDetailDate
import com.argento.eoloapp.viewmodel.ParkingDetailState
import com.argento.eoloapp.viewmodel.ParkingDetailViewModel
import com.argento.eoloapp.viewmodel.ParkingDetailViewModelFactory
import com.argento.eoloapp.viewmodel.ReservationCreationState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ParkingDetailScreen(navController: NavController, parkingId: String) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: ParkingDetailViewModel = viewModel(
        factory = ParkingDetailViewModelFactory(sessionManager, parkingId)
    )
    val state by viewModel.detailState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val tariffs by viewModel.tariffsState.collectAsState()
    val reservationState by viewModel.reservationCreationState.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.fetchParkingDetail(isRefresh = true) }
    )

    // Bottom Sheet State
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Effect to close bottom sheet on success
    LaunchedEffect(reservationState) {
        if (reservationState is ReservationCreationState.Success) {
            showBottomSheet = false
            viewModel.resetReservationCreationState()
        }
    }

    Scaffold(
        topBar = {
            if (state is ParkingDetailState.Success) {
                val data = (state as ParkingDetailState.Success).data
                ParkingHeader(parkingName = data.Estacionamiento.nombre)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = Color(0xFF2C3E50),
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingreso")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
                .pullRefresh(pullRefreshState)
        ) {
            when (state) {
                is ParkingDetailState.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is ParkingDetailState.Error -> {
                    val error = (state as ParkingDetailState.Error).message
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ParkingDetailState.Success -> {
                    val data = (state as ParkingDetailState.Success).data
                    ParkingContent(data = data, navController = navController)
                    
                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomSheet = false },
                            sheetState = sheetState,
                            containerColor = Color.White
                        ) {
                            VehicleEntryBottomSheetContent(
                                tariffs = tariffs,
                                isLoading = reservationState is ReservationCreationState.Loading,
                                onDismiss = { showBottomSheet = false },
                                onAdd = { placas, numEco, tel, tipo, cat, tarifaId, _, placaCaja1, placaCaja2 ->
                                    viewModel.createReservation(
                                        placas = placas,
                                        numEco = numEco,
                                        tel = tel,
                                        tipo = tipo,
                                        cat = cat,
                                        tarifaId = tarifaId,
                                        caja1 = placaCaja1,
                                        caja2 = placaCaja2
                                    )
                                },
                                fetchTariffs = { tipo, categoria ->
                                    viewModel.fetchTarifas(tipo, categoria)
                                }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleEntryBottomSheetContent(
    tariffs: List<Tarifa>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String, String, String, String) -> Unit,
    fetchTariffs: (String, String) -> Unit
) {
    var placas by remember { mutableStateOf("") }
    var numeroEconomico by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var placaCaja1 by remember { mutableStateOf("") }
    var placaCaja2 by remember { mutableStateOf("") }

    // Dropdown states
    var selectedTipo by remember { mutableStateOf("Automovil") }
    var selectedCategoria by remember { mutableStateOf("Sedan") }
    var selectedTarifaId by remember { mutableStateOf("") }
    var selectedTarifaLabel by remember { mutableStateOf("") }
    
    // Dropdown Expansions
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedTarifa by remember { mutableStateOf(false) }

    // Data Lists
    val tiposVehiculo = listOf("Motocicleta", "Automovil", "Camion")
    
    val categoriasMap = mapOf(
        "Automovil" to listOf("Sedan", "Camioneta (SUV)", "Pickup", "Van/Furgoneta"),
        "Motocicleta" to listOf("Deportiva", "Chopper", "Scooter", "Montaña/Cross"),
        "Camion" to listOf(
            "3.5 Ton",
            "Pasajeros",
            "Rabón / Thorton",
            "Trailer / Tracto.",
            "Trailer / Tracto. 1 Caja",
            "Trailer / Tracto. 2 Cajas",
            "Trailer / Tracto. 1 Plataforma",
            "Trailer / Tracto. 2 Plataformas",
            "Trailer / Tracto. 1 Cisterna/Pipa",
            "Trailer / Tracto. 2 Cistenas/Pipas"),
        "Bicicleta" to listOf("Estándar")
    )

    val currentCategories = categoriasMap[selectedTipo] ?: emptyList()
    
    // Visibility logic for Placa Cajas
    val showPlacaCaja1 = selectedCategoria in listOf(
        "Trailer / Tracto. 1 Caja",
        "Trailer / Tracto. 2 Cajas",
        "Trailer / Tracto. 1 Plataforma",
        "Trailer / Tracto. 2 Plataformas",
        "Trailer / Tracto. 1 Cisterna/Pipa",
        "Trailer / Tracto. 2 Cistenas/Pipas"
    )
    
    val showPlacaCaja2 = selectedCategoria in listOf(
        "Trailer / Tracto. 2 Cajas",
        "Trailer / Tracto. 2 Plataformas",
        "Trailer / Tracto. 2 Cistenas/Pipas"
    )

    // Fetch tariffs when type or category changes
    LaunchedEffect(selectedTipo, selectedCategoria) {
        fetchTariffs(selectedTipo, selectedCategoria)
        selectedTarifaId = ""
        selectedTarifaLabel = ""
        
        // Reset Caja fields if hidden
        if (!showPlacaCaja1) placaCaja1 = ""
        if (!showPlacaCaja2) placaCaja2 = ""
    }

    // Get screen height to limit height to 60%
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = screenHeight * 0.8f) // Max 60% of screen height
            .verticalScroll(rememberScrollState()) // Enable scrolling
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp) 
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingresar Vehiculo al\nEstacionamiento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Row 1: Placas & Num Eco
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                LabelText("Placas", isRequired = true)
                OutlinedTextField(
                    value = placas,
                    onValueChange = { placas = it.uppercase() },
                    placeholder = { Text("Placas", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                LabelText("Número Económico", isRequired = false)
                OutlinedTextField(
                    value = numeroEconomico,
                    onValueChange = { numeroEconomico = it },
                    placeholder = { Text("ID del Vehiculo", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 2: Tipo & Categoria
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Tipo Dropdown
            Column(modifier = Modifier.weight(1f)) {
                LabelText("Tipo de Vehiculo", isRequired = true)
                Box {
                    OutlinedTextField(
                        value = selectedTipo,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedTipo = true },
                        enabled = false, 
                        colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledPlaceholderColor = Color.Black,
                            disabledLabelColor = Color.Black,
                            disabledLeadingIconColor = Color.Black,
                            disabledTrailingIconColor = Color.Black
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expandedTipo = true })
                    
                    DropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        tiposVehiculo.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedTipo = type
                                    selectedCategoria = categoriasMap[type]?.firstOrNull() ?: ""
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Categoria Dropdown
            Column(modifier = Modifier.weight(1f)) {
                LabelText("Categoria de Vehículo", isRequired = true)
                Box {
                    OutlinedTextField(
                        value = selectedCategoria,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth(),
                         colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledPlaceholderColor = Color.Black,
                            disabledLabelColor = Color.Black,
                            disabledLeadingIconColor = Color.Black,
                            disabledTrailingIconColor = Color.Black
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expandedCategoria = true })

                    DropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        currentCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategoria = cat
                                    expandedCategoria = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Row 3: Placas Caja 1 & Placas Caja 2 (Conditional)
        if (showPlacaCaja1) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("Placa Caja 1", isRequired = true)
                    OutlinedTextField(
                        value = placaCaja1,
                        onValueChange = { placaCaja1 = it.uppercase() },
                        placeholder = { Text("Placa Caja 1", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                if (showPlacaCaja2) {
                    Column(modifier = Modifier.weight(1f)) {
                        LabelText("Placa Caja 2", isRequired = true)
                        OutlinedTextField(
                            value = placaCaja2,
                            onValueChange = { placaCaja2 = it.uppercase() },
                            placeholder = { Text("Placa Caja 2", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                } else {
                     Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarifa
        LabelText("Tarifa", isRequired = true)
        Box {
             OutlinedTextField(
                value = selectedTarifaLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth(),
                 colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledPlaceholderColor = Color.Black,
                    disabledLabelColor = Color.Black,
                    disabledLeadingIconColor = Color.Black,
                    disabledTrailingIconColor = Color.Black
                )
            )
             Box(modifier = Modifier.matchParentSize().clickable { expandedTarifa = true })
            
            DropdownMenu(
                expanded = expandedTarifa,
                onDismissRequest = { expandedTarifa = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                tariffs.forEach { tarifa ->
                    val tarifaPrice = tarifa.tarifa ?: 0.0
                    val isSimple = tarifa.logicaTarifa == "Sencilla"
                    val tarifaSuffix = if (isSimple) "(${formatCurrency(tarifaPrice)})" else ""
                    val displayText = "${tarifa.tipoVehiculo ?: ""} ${tarifa.nombre ?: ""} ${tarifa.frecuencia ?: ""} $tarifaSuffix".trim()
                    
                    DropdownMenuItem(
                        text = { Text(displayText) },
                        onClick = {
                            selectedTarifaId = tarifa.id
                            selectedTarifaLabel = displayText
                            expandedTarifa = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telefono
        LabelText("Teléfono para envío de Ticket", isRequired = false)
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            placeholder = { Text("Teléfono", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar")
                }
            }
            
            Button(
                onClick = {
                    onAdd(placas, numeroEconomico, telefono, selectedTipo, selectedCategoria, selectedTarifaId, selectedTarifaLabel, placaCaja1, placaCaja2)
                },
                enabled = !isLoading && placas.isNotBlank() && selectedTarifaId.isNotBlank() &&
                        (!showPlacaCaja1 || placaCaja1.isNotBlank()) &&
                        (!showPlacaCaja2 || (placaCaja1.isNotBlank() && placaCaja2.isNotBlank())),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

@Composable
fun LabelText(text: String, isRequired: Boolean) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        if (isRequired) {
            Text(
                text = "*",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }
}

@Composable
fun ParkingHeader(parkingName: String) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "eolo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = parkingName.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ParkingContent(data: EstacionamientoDetailData, navController: NavController) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubtotalCard(
                title = "SUBTOTAL RESERVA",
                total = data.ReservaTotal,
                efectivo = data.R_EfectivoTotal,
                terminal = data.R_TerminalTotal,
                credito = data.R_CreditoTotal
            )
        }
        item {
            SubtotalCard(
                title = "SUBTOTAL SERVICIOS",
                total = data.ServicioTotal,
                efectivo = data.S_EfectivoTotal,
                terminal = data.S_TerminalTotal,
                credito = data.S_CreditoTotal
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallInfoCard(
                    title = "Inventario",
                    value = data.Inventario.toString(),
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f)
                )
                SmallInfoCard(
                    title = "Total",
                    value = formatCurrency(data.Total),
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Movimientos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Row {
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .background(Color(0xFFE0E5EC), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF2C3E50), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .background(Color(0xFFE0E5EC), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.List, contentDescription = "Filter", tint = Color(0xFF2C3E50), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        items(data.Reservas) { reserva ->
            MovementCard(reserva = reserva) {
                navController.navigate("MovementDetailScreen/${reserva.id}")
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SubtotalCard(title: String, total: Double, efectivo: Double, terminal: Double, credito: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = formatCurrency(total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PaymentDetailItem("EFECTIVO", efectivo)
                PaymentDetailItem("TERMINAL", terminal)
                PaymentDetailItem("CRÉDITO", credito)
            }
        }
    }
}

@Composable
fun PaymentDetailItem(label: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
        Text(text = formatCurrency(amount), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3E50))
    }
}

@Composable
fun SmallInfoCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F3F8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF2C3E50))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementCard(reserva: Reserva, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = reserva.ID2 ?: "N/A",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2C3E50)
                    )
                    Text(
                        text = formatCurrency(reserva.MontoTotal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2C3E50)
                    )
                }
                
                Text(
                    text = formatDetailDate(reserva.createdDate),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${reserva.tipoVehiculo ?: ""} ${reserva.placaVehiculo ?: ""}".trim(),
                        fontSize = 14.sp,
                        color = Color(0xFF5D6D7E)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reserva.MetodoPago1 ?: "N/A",
                        fontSize = 14.sp,
                        color = Color(0xFF5D6D7E)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                 StatusBadge(status = reserva.Estatus ?: "Desconocido")
                 Spacer(modifier = Modifier.height(16.dp))
                 Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}