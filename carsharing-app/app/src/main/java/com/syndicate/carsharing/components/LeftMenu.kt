package com.syndicate.carsharing.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R

@Composable
fun LeftMenu(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(10.dp)
    ) {
        Image(imageVector = ImageVector.vectorResource(id = R.drawable.profileicon), contentDescription = null)
    }
}