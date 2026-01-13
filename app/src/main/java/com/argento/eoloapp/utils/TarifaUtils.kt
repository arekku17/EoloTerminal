package com.argento.eoloapp.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Date
import kotlin.math.ceil
import kotlin.math.min

object TarifaUtils {

    fun calcularTotal(
        fechaIngreso: Long,
        fechaSalida: Long,
        logicaTarifa: String?,
        frecuencia: String?,
        tarifa: Double?,
        tarifaJson: String?
    ): Double {
        if (logicaTarifa == null) return 0.0

        return when (logicaTarifa.lowercase()) {
            "sencilla" -> calcularSencilla(fechaIngreso, fechaSalida, frecuencia, tarifa)
            "escalonado" -> calcularEscalonada(fechaIngreso, fechaSalida, tarifaJson)
            "por horario" -> calcularPorHorario(fechaIngreso, fechaSalida, tarifaJson)
            else -> 0.0
        }
    }

    private fun calcularSencilla(
        fechaIngreso: Long,
        fechaSalida: Long,
        frecuencia: String?,
        tarifa: Double?): Double {
        if (tarifa == null || frecuencia == null) return 0.0
        if (fechaSalida < fechaIngreso) return tarifa

        val diff = fechaSalida - fechaIngreso

        return when (frecuencia.lowercase()) {
            "por día" -> {
                val days = diff.toDouble() / (24 * 60 * 60 * 1000)
                ceil(days) * tarifa
            }
            "por mes" -> {
                val calIngreso = Calendar.getInstance().apply { timeInMillis = fechaIngreso }
                val calSalida = Calendar.getInstance().apply { timeInMillis = fechaSalida }

                var months = 0.0
                while (calIngreso.before(calSalida)) {
                    calIngreso.add(Calendar.MONTH, 1)
                    if (calIngreso.before(calSalida) || calIngreso.timeInMillis == calSalida.timeInMillis) {
                        months++
                    } else {
                        // If adding a month goes past the salida date, count it as a partial month
                        months += 1.0 // This will be rounded up by ceil
                    }
                }
                ceil(months) * tarifa
            }
            "por hora" -> {
                val hours = diff.toDouble() / (60 * 60 * 1000)
                ceil(hours) * tarifa
            }
            else -> 0.0
        }
    }

    private data class EscalonadaConfig(
        val tarifas: List<EscalonadaTarifa>?,
        val adicional: Double?,
        val tiempo_adicional: Double?
    )

    private data class EscalonadaTarifa(
        val horas: Double,
        val precio: Double
    )

    private fun calcularEscalonada(fechaIngreso: Long, fechaSalida: Long, tarifaJson: String?): Double {
        if (tarifaJson.isNullOrBlank()) return 0.0

        try {
            val gson = Gson()
            val config = gson.fromJson(tarifaJson, EscalonadaConfig::class.java) ?: return 0.0
            
            val tarifas = config.tarifas ?: return 0.0
            if (tarifas.isEmpty()) return 0.0

            val ms = fechaSalida - fechaIngreso
            val horasTotales = ms.toDouble() / 1000 / 60 / 60

            val tarifasOrdenadas = tarifas.sortedBy { it.horas }
            
            var tarifaAplicable = tarifasOrdenadas[0]

            for (t in tarifasOrdenadas) {
                if (horasTotales <= t.horas) {
                    tarifaAplicable = t
                    break
                }
            }

            val maxTarifa = tarifasOrdenadas.last()

            if (horasTotales > maxTarifa.horas) {
                tarifaAplicable = maxTarifa
                val horasExtra = horasTotales - maxTarifa.horas
                
                val tiempoAdicional = config.tiempo_adicional ?: 0.0
                val adicional = config.adicional ?: 0.0

                val bloques = if (tiempoAdicional > 0) ceil(horasExtra / tiempoAdicional) else 0.0
                
                return tarifaAplicable.precio + (bloques * adicional)
            }

            return tarifaAplicable.precio

        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }
    }

    private data class HorarioBloque(
        val start: String,
        val end: String,
        val price: Double
    )

    private fun calcularPorHorario(fechaIngreso: Long, fechaSalida: Long, tarifaJson: String?): Double {
        if (tarifaJson.isNullOrBlank()) return 0.0

        try {
            val gson = Gson()
            val type = object : TypeToken<List<List<HorarioBloque>>>() {}.type
            val tarifasPorDia: List<List<HorarioBloque>> = gson.fromJson(tarifaJson, type) ?: return 0.0

            val salidaDate = Date(fechaSalida)
            var total = 0.0
            var actual = Date(fechaIngreso)
            
            var loops = 0
            val maxLoops = 10000

            while (actual.before(salidaDate) && loops < maxLoops) {
                loops++

                val cal = Calendar.getInstance()
                cal.time = actual
                val diaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1 // Sunday is 1 -> 0

                if (diaSemana < 0 || diaSemana >= tarifasPorDia.size) break

                val tarifasDelDia = tarifasPorDia[diaSemana]
                val bloque = obtenerBloque(cal, tarifasDelDia)

                if (bloque == null) {
                    break
                }

                val finBloque = obtenerFinDeBloque(cal, bloque)
                val finTramoTime = min(finBloque.time, salidaDate.time)
                val finTramo = Date(finTramoTime)

                val minutosEnBloque = (finTramo.time - actual.time).toDouble() / 1000 / 60
                val horasReales = minutosEnBloque / 60
                val horasCobradas = ceil(horasReales)

                total += bloque.price * horasCobradas

                actual = Date(finTramo.time)
                
                if (actual.before(salidaDate) && actual.time < finTramo.time + 1) {
                     // Force advance if we are stuck (though logic shouldn't get stuck if finTramo > actual)
                     // If finTramo == actual (0 mins), loop might stick.
                     actual.time += 1
                }
            }
            return total

        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }
    }

    private fun obtenerBloque(fecha: Calendar, tarifas: List<HorarioBloque>): HorarioBloque? {
        val minutosActual = fecha.get(Calendar.HOUR_OF_DAY) * 60 + fecha.get(Calendar.MINUTE)

        for (t in tarifas) {
            val start = convertirHoraMinutos(t.start)
            var end = convertirHoraMinutos(t.end)

            if (end == 0 && t.end == "00:00") end = 1440

            if (minutosActual >= start && minutosActual < end) {
                return t
            }
        }
        return null
    }

    private fun obtenerFinDeBloque(fecha: Calendar, bloque: HorarioBloque): Date {
        var end = convertirHoraMinutos(bloque.end)
        if (end == 0 && bloque.end == "00:00") end = 1440

        val hora = end / 60
        val minuto = end % 60

        val fin = fecha.clone() as Calendar
        fin.set(Calendar.HOUR_OF_DAY, hora)
        fin.set(Calendar.MINUTE, minuto)
        fin.set(Calendar.SECOND, 0)
        fin.set(Calendar.MILLISECOND, 0)

        // Handle day overflow if 24:00
        if (hora == 24) {
             // Calendar set HOUR_OF_DAY to 24 might behave as next day 00:00 automatically
             // But to be sure:
             fin.add(Calendar.DAY_OF_YEAR, 0) // Force recomputation?
        }
        
        return fin.time
    }

    private fun convertirHoraMinutos(hora: String): Int {
        val parts = hora.split(":")
        if (parts.size != 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }
}
