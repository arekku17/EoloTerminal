package com.argento.eoloapp.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.argento.eoloapp.R
import com.argento.eoloapp.ui.theme.BgSocial
import com.argento.eoloapp.ui.theme.BorderColor
import com.argento.eoloapp.ui.theme.BrandColor
import com.argento.eoloapp.ui.theme.Primary
import com.argento.eoloapp.ui.theme.Tertirary

@Composable
fun ImageComponent(image: Int, size: Dp) {
    Image(
        painter = painterResource(id = image),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .size(size)
    )
}

@Composable
fun HeadingTextComponent(heading: String) {
    Text(
        text = heading,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 39.sp,
        color = Primary,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp
    )
}

@Composable
fun ForgotPasswordHeadingTextComponent(action: String) {
    Column {
        Text(
            text = action,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 39.sp,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Password?",
            modifier = Modifier.fillMaxWidth().offset(y = (-18).dp),
            fontSize = 39.sp,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pendiente" -> Color(0xFFE8F8F5) to Color(0xFF2ECC71)
        "entrada" -> Color(0xFFFEF9E7) to Color(0xFFF1C40F)
        "salida" -> Color(0xFFE8F6F3) to Color(0xFF1ABC9C)
        else -> Color(0xFFF2F4F4) to Color(0xFF95A5A6)
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextField(labelVal: String, icon: Int, value: String, onValueChange: (String) -> Unit) {
    val typeOfKeyboard: KeyboardType = when (labelVal) {
        "email ID" -> KeyboardType.Email
        "Teléfono" -> KeyboardType.Phone
        else -> KeyboardType.Text
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = BrandColor,
            unfocusedBorderColor = BorderColor,
            focusedLeadingIconColor = BrandColor,
            unfocusedLeadingIconColor = Tertirary
        ),
        shape = MaterialTheme.shapes.small,
        placeholder = {
            Text(text = labelVal, color = Tertirary)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "at_symbol"
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = typeOfKeyboard,
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputComponent(labelVal: String, value: String, onValueChange: (String) -> Unit) {
    var isShowPassword by remember {
        mutableStateOf(false)
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = BrandColor,
            unfocusedBorderColor = BorderColor
        ),
        shape = MaterialTheme.shapes.small,
        placeholder = {
            Text(text = labelVal, color = Tertirary)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.lock),
                contentDescription = "at_symbol",
                tint = Tertirary
            )
        },
        trailingIcon = {
            val description = if (isShowPassword) "Show Password" else "Hide Password"
            val iconImage =
                if (isShowPassword) R.drawable.pheyeclosedfill__1_ else R.drawable.eye_closed
            IconButton(onClick = {
                isShowPassword = !isShowPassword
            }) {
                Icon(
                    painter = painterResource(id = iconImage),
                    contentDescription = description,
                    tint = Tertirary,
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        visualTransformation = if (isShowPassword) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun ForgotPasswordTextComponent(navController: NavHostController) {
    Text(
        text = "Forgot Password?",
        color = BrandColor,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.clickable {
            navController.navigate("ForgotPassword")
        }
    )
}

@Composable
fun MyButton(labelVal: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
    ) {
        Text(
            text = labelVal,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
fun BottomComponent(onLoginClick: () -> Unit, onSmsLoginClick: () -> Unit) {
    Column {
        MyButton(labelVal = "Iniciar Sesión", onClick = onLoginClick)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                thickness = 1.dp,
                color = Tertirary
            )
            Text(
                text = "O",
                modifier = Modifier.padding(10.dp),
                color = Tertirary,
                fontSize = 20.sp
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                thickness = 1.dp,
                color = Tertirary
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = onSmsLoginClick,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = BgSocial
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ingresar con SMS",
                    fontSize = 18.sp,
                    color = Tertirary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun BottomLoginTextComponent(initialText: String, action: String, navController: NavHostController) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Tertirary)) {
            append(initialText)
        }
        withStyle(style = SpanStyle(color = BrandColor, fontWeight = FontWeight.Bold)) {
            pushStringAnnotation(tag = action, annotation = action)
            append(action)
        }
    }

    ClickableText(text = annotatedString, onClick = {
        annotatedString.getStringAnnotations(it, it)
            .firstOrNull()?.also { span ->
                Log.d("BottomLoginTextComponent", "${span.item} is Clicked")
                if (span.item == "Join our coven!") {
                    navController.navigate("SignupScreen")
                }
            }
    })
}

@Composable
fun SignupTermsAndPrivacyText() {
    val initialText = "Join our coven and accept our "
    val termsNConditionText = "Terms & Conditions"
    val andText = " and "
    val privacyPolicyText = "Privacy Policy."
    val lastText = " Don't be afraid, we don't bite!"

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Tertirary)) {
            append(initialText)
        }
        withStyle(style = SpanStyle(color = BrandColor, fontWeight = FontWeight.Bold)) {
            pushStringAnnotation(tag = termsNConditionText, annotation = termsNConditionText)
            append(termsNConditionText)
        }
        withStyle(style = SpanStyle(color = Tertirary)) {
            append(andText)
        }
        withStyle(style = SpanStyle(color = BrandColor, fontWeight = FontWeight.Bold)) {
            pushStringAnnotation(tag = privacyPolicyText, annotation = privacyPolicyText)
            append(privacyPolicyText)
        }
        withStyle(style = SpanStyle(color = Tertirary)) {
            append(lastText)
        }
    }

    ClickableText(text = annotatedString, onClick = {
        annotatedString.getStringAnnotations(it, it)
            .firstOrNull()?.also { span ->
                Log.d("SignupTermsAndPrivacyText", span.item)
            }
    })
}

@Composable
fun BottomSignupTextComponent(navController: NavHostController) {
    val initialText = "Are you a familiar spirit? "
    val loginText = "Log In"
    val lastText = " again and join our Halloween party!"

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Tertirary)) {
            append(initialText)
        }
        withStyle(style = SpanStyle(color = BrandColor, fontWeight = FontWeight.Bold)) {
            pushStringAnnotation(tag = loginText, annotation = loginText)
            append(loginText)
        }
        withStyle(style = SpanStyle(color = Tertirary)) {
            append(lastText)
        }
    }

    ClickableText(text = annotatedString, onClick = {
        annotatedString.getStringAnnotations(it, it)
            .firstOrNull()?.also { span ->
                if (span.item == "Log In") {
                    navController.navigate("LoginScreen")
                }
            }
    })

}

@Composable
fun TextInfoComponent(textVal: String) {
    Text(text = textVal, color = Tertirary)
}