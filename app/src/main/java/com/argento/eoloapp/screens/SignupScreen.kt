package com.argento.eoloapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.argento.eoloapp.R
import com.argento.eoloapp.components.BottomSignupTextComponent
import com.argento.eoloapp.components.HeadingTextComponent
import com.argento.eoloapp.components.ImageComponent
import com.argento.eoloapp.components.SignupTermsAndPrivacyText

@Composable
fun SignupScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        color = Color.White
    ) {
        Column {
            ImageComponent(image = R.drawable.black_cat, 120.dp)
            Spacer(modifier = Modifier.height(10.dp))
            HeadingTextComponent(heading = "Sign Up")
            Spacer(modifier = Modifier.height(20.dp))
            Column {
//                MyTextField(labelVal = "email ID", icon = R.drawable.at_symbol)
                Spacer(modifier = Modifier.height(15.dp))
//                MyTextField(labelVal = "full name", icon = R.drawable.lockperson)
                Spacer(modifier = Modifier.height(15.dp))
//                MyTextField(labelVal = "mobile", icon = R.drawable.lockphone)
            }
            Spacer(modifier = Modifier.height(20.dp))
            SignupTermsAndPrivacyText()
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
//                    MyButton(labelVal = "Continue", navController = navController)
                    Spacer(modifier = Modifier.height(10.dp))
                    BottomSignupTextComponent(navController)
                }
            }
            
        }
    }
}