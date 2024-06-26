package com.syndicate.carsharing.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow

//TODO: Сделать функционал нажатий на кнопки
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LeftMenu(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (sheetState.targetValue == ModalBottomSheetValue.Expanded) {
                    Modifier.alpha(0f)
                } else {
                    Modifier.alpha(1f)
                }
            )
            .withShadow(
                Shadow(
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    radius = 4.dp,
                    color = Color(0, 0, 0, 40)
                ),
                RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable {
                onClick()
            }
            .padding(10.dp)
    ) {
        Image(imageVector = ImageVector.vectorResource(id = R.drawable.profileicon), contentDescription = null)
    }
}