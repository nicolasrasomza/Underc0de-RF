package org.underc0de.undercoderf.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.underc0de.undercoderf.R

val TerminalBlack = Color(0xFF070B10)
val CardBackground = Color(0xFF0D141F)
val NeonCyan = Color(0xFF00E5FF)
val NeonPink = Color(0xFFFF2A6D)
val NeonGreen = Color(0xFF05FFA1)
val WarningAmber = Color(0xFFFFB703)
val TextWhite = Color(0xFFE2F1FF)

@Composable
fun TopHeaderBar(status: String = "IDLE") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalBlack)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SYS://UNDERC0DE.ORG · $status",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "root@underc0de:~$ ./radio --scan",
                color = TextWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
        Image(
            painter = painterResource(id = R.drawable.logo_header),
            contentDescription = "Underc0de Logo",
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)),
        color = CardBackground,
        shape = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}
