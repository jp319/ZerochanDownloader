package com.jp319.zerochan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import zerochan.composeapp.generated.resources.Res
import zerochan.composeapp.generated.resources.logo

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = "ZeroChan logo",
        modifier = modifier.size(size), // caller can override entirely if needed
    )
}
