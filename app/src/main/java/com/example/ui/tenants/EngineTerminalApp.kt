package com.example.ui.tenants

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditLogEntry
import com.example.model.EngineTelemetry
import com.example.model.GovernanceAuditResult
import com.example.model.VerificationState
import com.example.ui.theme.*

@Composable
fun EngineTerminalApp(
    telemetry: EngineTelemetry,
    auditLogs: List<AuditLogEntry>,
    testbenchInput: String,
    testbenchResult: GovernanceAuditResult?,
    onTestbenchInputChange: (String) -> Unit,
    onRunTestbench: () -> Unit,
    onUpdateParameters: (Float, Float, Float) -> Unit
) {
    var tempSlider by remember(telemetry.temperature) { mutableFloatStateOf(telemetry.temperature) }
    var cadenceSlider by remember(telemetry.cadenceRigidity) { mutableFloatStateOf(telemetry.cadenceRigidity) }
    var toleranceSlider by remember(telemetry.governanceTolerance) { mutableFloatStateOf(telemetry.governanceTolerance) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App 04 Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                ElyG3Axiom.copy(alpha = 0.15f),
                                ElyCyan.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .border(1.dp, ElyG3Axiom.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "APP 04 // GOVERNANCE MATRIX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyG3Axiom
                        )
                        Text(
                            text = "G1 (Identity) → G2 (Harmony) → G3 (Calibration) + SHA-256 Seal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElyTextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElyG3Axiom.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CALIBRATION OK",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyG3Axiom,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Parameter Calibration Dials
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElySurfaceCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ELYZARETH ENGINE HYPERPARAMETERS",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextSecondary
                    )

                    // Temperature Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inference Temperature", fontSize = 11.sp, color = ElyTextPrimary)
                            Text("${"%.2f".format(tempSlider)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ElyCyan)
                        }
                        Slider(
                            value = tempSlider,
                            onValueChange = { tempSlider = it; onUpdateParameters(it, cadenceSlider, toleranceSlider) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = ElyCyan, activeTrackColor = ElyCyan)
                        )
                    }

                    // Cadence Rigidity Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cadence Rigidity (Meter Enforcement)", fontSize = 11.sp, color = ElyTextPrimary)
                            Text("${"%.2f".format(cadenceSlider)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ElyViolet)
                        }
                        Slider(
                            value = cadenceSlider,
                            onValueChange = { cadenceSlider = it; onUpdateParameters(tempSlider, it, toleranceSlider) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = ElyViolet, activeTrackColor = ElyViolet)
                        )
                    }

                    // Governance Tolerance
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("G3 Performance Calibration Strictness", fontSize = 11.sp, color = ElyTextPrimary)
                            Text("${"%.2f".format(toleranceSlider)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ElyG3Axiom)
                        }
                        Slider(
                            value = toleranceSlider,
                            onValueChange = { toleranceSlider = it; onUpdateParameters(tempSlider, cadenceSlider, it) },
                            valueRange = 0.5f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = ElyG3Axiom, activeTrackColor = ElyG3Axiom)
                        )
                    }
                }
            }
        }

        // Forensic Testbench
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElySurfaceCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "MANUAL FORENSIC TESTBENCH",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextSecondary
                    )
                    OutlinedTextField(
                        value = testbenchInput,
                        onValueChange = onTestbenchInputChange,
                        placeholder = { Text("Feed candidate text into G1-G2-G3 pipeline...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Button(
                        onClick = onRunTestbench,
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElyG3Axiom)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audit via G1 → G2 → G3", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (testbenchResult != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF090D16))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = testbenchResult.g1Details, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyG1Lexical)
                            Text(text = testbenchResult.g2Details, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyG2Harmony)
                            Text(text = testbenchResult.g3Details, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyG3Axiom)
                        }
                    }
                }
            }
        }

        // Live Audit Log Stream
        item {
            Text(
                text = "REAL-TIME GOVERNANCE AUDIT STREAM",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ElyTextSecondary
            )
        }

        items(auditLogs) { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F172A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${log.layer}] ${log.timestamp}",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when (log.layer) {
                                "G1_LEXICAL" -> ElyG1Lexical
                                "G2_HARMONY" -> ElyG2Harmony
                                "G3_AXIOM" -> ElyG3Axiom
                                "INTEGRATOR" -> ElyCyanBright
                                else -> ElyCyan
                            }
                        )
                        Text(
                            text = log.hashStamp,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyTextTertiary
                        )
                    }
                    Text(
                        text = log.message,
                        fontSize = 10.sp,
                        color = ElyTextPrimary
                    )
                }
            }
        }
    }
}
