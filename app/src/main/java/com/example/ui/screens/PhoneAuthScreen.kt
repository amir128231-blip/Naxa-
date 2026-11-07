package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.PhoneAuthState
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.delay

@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = w * 0.18f
        val radius = (w - stroke) / 2f

        val strokeStyle = Stroke(width = stroke, cap = StrokeCap.Butt)

        // Red (top: 215 to 320 deg)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 215f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = strokeStyle
        )

        // Yellow (left: 135 to 215 deg)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = strokeStyle
        )

        // Green (bottom: 35 to 135 deg)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 35f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = strokeStyle
        )

        // Blue (right: 320 to 35 deg)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 320f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = strokeStyle
        )

        // Blue horizontal crossbar
        val barThickness = stroke
        val barLeft = cx - stroke * 0.2f
        val barTop = cy - barThickness / 2f
        val barWidth = radius + stroke / 2f + stroke * 0.2f
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(barLeft, barTop),
            size = Size(barWidth, barThickness)
        )
    }
}

data class CountryInfo(
    val code: String,
    val name: String,
    val dialCode: String,
    val flag: String
)

val supportedCountries = listOf(
    CountryInfo("US", "United States", "+1", "🇺🇸"),
    CountryInfo("IR", "Iran", "+98", "🇮🇷"),
    CountryInfo("GB", "United Kingdom", "+44", "🇬🇧"),
    CountryInfo("DE", "Germany", "+49", "🇩🇪"),
    CountryInfo("FR", "France", "+33", "🇫🇷"),
    CountryInfo("TR", "Turkey", "+90", "🇹🇷"),
    CountryInfo("AE", "United Arab Emirates", "+971", "🇦🇪"),
    CountryInfo("CA", "Canada", "+1", "🇨🇦"),
    CountryInfo("RU", "Russia", "+7", "🇷🇺")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    authState: PhoneAuthState,
    onSendEmailCode: (email: String) -> Unit,
    onResendEmailCode: () -> Unit,
    onDirectEmailSignIn: (email: String) -> Unit,
    onSendCode: (activity: Activity, fullPhone: String) -> Unit,
    onVerifyCode: (code: String) -> Unit,
    onResendCode: (activity: Activity) -> Unit,
    onGoogleSignIn: (activity: Activity) -> Unit,
    onSimulateGoogleSignIn: () -> Unit,
    onSimulateTestSignIn: (phone: String) -> Unit,
    onSkipToGuest: () -> Unit,
    onBack: () -> Unit,
    activity: Activity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isGoogleSigningIn = authState is PhoneAuthState.SigningInWithGoogle

    // Mode: 0 for Email (default), 1 for Phone
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    var emailInput by remember { mutableStateOf("") }
    var emailValidationError by remember { mutableStateOf<String?>(null) }

    var selectedCountry by remember { mutableStateOf(supportedCountries[0]) }
    var phoneNumber by remember { mutableStateOf("") }
    var phoneValidationError by remember { mutableStateOf<String?>(null) }
    var showCountryDialog by remember { mutableStateOf(false) }

    var enteredCode by remember { mutableStateOf("") }
    var resendCountdown by remember { mutableIntStateOf(60) }

    val focusManager = LocalFocusManager.current
    val codeFocusRequester = remember { FocusRequester() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        com.example.data.auth.SmsReceiver.onCodeReceivedListener = { receivedCode ->
            enteredCode = receivedCode
            onVerifyCode(receivedCode)
        }

        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        try {
            permissionLauncher.launch(permissions.toTypedArray())
        } catch (_: Throwable) {}
    }

    val isEmailCodeSent = authState is PhoneAuthState.EmailCodeSent
    val isPhoneCodeSent = authState is PhoneAuthState.CodeSent
    val isCodeVerificationScreen = isEmailCodeSent || isPhoneCodeSent

    LaunchedEffect(isCodeVerificationScreen) {
        if (isCodeVerificationScreen) {
            enteredCode = ""
            resendCountdown = 60
            while (resendCountdown > 0) {
                delay(1000)
                resendCountdown--
            }
        }
    }

    fun openEmailApp() {
        try {
            val emailIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_EMAIL)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            try {
                val gmailIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                if (gmailIntent != null) {
                    context.startActivity(gmailIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com"))
                    context.startActivity(browserIntent)
                }
            } catch (e2: Exception) {
                Toast.makeText(context, "Please open your Gmail or Email app.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Force LTR layout for authentic English UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isCodeVerificationScreen) "Verification" else "Sign in to Nexa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        if (isCodeVerificationScreen) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isCodeVerificationScreen) {
                    // Top Brand Graphic
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2AABEE), Color(0xFF229ED9), Color(0xFF1B85BA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedAuthTab == 0) Icons.Default.Email else Icons.Default.Send,
                            contentDescription = "Nexa Logo",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nexa Messenger",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Secure, fast and reliable sign-in with email code",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mode Tabs (Email vs Phone)
                    TabRow(
                        selectedTabIndex = selectedAuthTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = TelegramBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedAuthTab]),
                                color = TelegramBlue
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedAuthTab == 0,
                            onClick = { selectedAuthTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Email (Google)",
                                        fontWeight = if (selectedAuthTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedAuthTab == 1,
                            onClick = { selectedAuthTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Phone Number",
                                        fontWeight = if (selectedAuthTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedAuthTab == 0) {
                        // EMAIL AUTH SECTION (Default & Primary)
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                emailValidationError = null
                            },
                            label = { Text("Email address (Gmail, Yahoo, etc.)") },
                            placeholder = { Text("name@gmail.com") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = TelegramBlue)
                            },
                            trailingIcon = {
                                if (emailInput.isNotBlank()) {
                                    IconButton(onClick = { emailInput = "" }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            isError = emailValidationError != null,
                            supportingText = {
                                if (emailValidationError != null) {
                                    Text(text = emailValidationError!!, color = Color.Red, fontSize = 12.sp)
                                } else {
                                    Text(text = "A 6-digit security code will be sent to your inbox.", fontSize = 12.sp)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    val clean = emailInput.trim().lowercase()
                                    if (clean.contains("@") && clean.contains(".")) {
                                        onSendEmailCode(clean)
                                    } else {
                                        emailValidationError = "Please enter a valid email address."
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input_field")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Button 1: Send Code to Email
                        Button(
                            onClick = {
                                val clean = emailInput.trim().lowercase()
                                if (clean.contains("@") && clean.contains(".") && clean.length > 5) {
                                    emailValidationError = null
                                    focusManager.clearFocus()
                                    onSendEmailCode(clean)
                                } else {
                                    emailValidationError = "Please enter a valid email address (e.g. name@gmail.com)."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                            shape = RoundedCornerShape(12.dp),
                            enabled = authState !is PhoneAuthState.SendingCode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("send_email_code_button")
                        ) {
                            if (authState is PhoneAuthState.SendingCode) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Sending code to email...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send Verification Code ✉️",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 2: Direct Instant Entry with this email (No wait)
                        Button(
                            onClick = {
                                val clean = emailInput.trim().lowercase()
                                if (clean.contains("@") && clean.contains(".") && clean.length > 5) {
                                    emailValidationError = null
                                    focusManager.clearFocus()
                                    onDirectEmailSignIn(clean)
                                } else {
                                    emailValidationError = "Please enter a valid email (e.g. name@gmail.com) first."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("direct_email_sign_in_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant Sign-In with this Email ⚡",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "or",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        }

                        // Button 3: Google 1-Tap Sign-In
                        Surface(
                            onClick = {
                                if (!isGoogleSigningIn) {
                                    onGoogleSignIn(activity)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("google_auth_card"),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isGoogleSigningIn) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = TelegramBlue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Connecting to Google...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    GoogleLogo(modifier = Modifier.size(26.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Sign in with Google (Gmail)",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Secure 1-tap sign-in without code entry",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // PHONE NUMBER SECTION (Optional Secondary Tab)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .clickable { showCountryDialog = true },
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = selectedCountry.flag, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = selectedCountry.name,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = selectedCountry.dialCode,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TelegramBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                if (filtered.length <= 11) {
                                    phoneNumber = filtered
                                    phoneValidationError = null
                                }
                            },
                            label = { Text("Phone number (e.g. 09123456789)") },
                            placeholder = { Text("09123456789") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = TelegramBlue)
                            },
                            isError = phoneValidationError != null,
                            supportingText = {
                                if (phoneValidationError != null) {
                                    Text(text = phoneValidationError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Instant free login with this phone number
                        Button(
                            onClick = {
                                val cleanDigits = phoneNumber.trim().replace(" ", "").removePrefix("+98").removePrefix("98")
                                val isValid = cleanDigits.length >= 7
                                if (!isValid) {
                                    phoneValidationError = "Please enter a valid phone number."
                                    return@Button
                                }
                                val standardDigits = if (cleanDigits.startsWith("0")) cleanDigits.removePrefix("0") else cleanDigits
                                val fullPhone = "${selectedCountry.dialCode}$standardDigits"
                                onSimulateTestSignIn(fullPhone)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant Sign-in with this Phone ⚡",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val cleanDigits = phoneNumber.trim().replace(" ", "").removePrefix("+98").removePrefix("98")
                                val isValid = cleanDigits.length >= 7
                                if (!isValid) {
                                    phoneValidationError = "Please enter a valid phone number."
                                    return@OutlinedButton
                                }
                                val standardDigits = if (cleanDigits.startsWith("0")) cleanDigits.removePrefix("0") else cleanDigits
                                val fullPhone = "${selectedCountry.dialCode}$standardDigits"
                                onSendCode(activity, fullPhone)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = TelegramBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Send SMS Verification Code 📩", color = TelegramBlue, fontSize = 13.5.sp)
                        }
                    }
                } else {
                    // CODE VERIFICATION SCREEN (TELEGRAM & GOOGLE STYLE)
                    val targetLabel = if (isEmailCodeSent) {
                        (authState as PhoneAuthState.EmailCodeSent).email
                    } else {
                        (authState as PhoneAuthState.CodeSent).phoneNumber
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEmailCodeSent) Icons.Default.MarkEmailRead else Icons.Default.Security,
                            contentDescription = "Code verification",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enter Security Code",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isEmailCodeSent) {
                            "We sent a 6-digit verification code to:"
                        } else {
                            "We sent an SMS verification code to:"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Target Chip with Edit Action
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onBack() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isEmailCodeSent) Icons.Default.Email else Icons.Default.Phone,
                                contentDescription = null,
                                tint = TelegramBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = targetLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = TelegramBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 6-Digit PIN Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val digit = enteredCode.getOrNull(i)?.toString() ?: ""
                            val isCurrent = enteredCode.length == i
                            Box(
                                modifier = Modifier
                                    .size(48.dp, 56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .border(
                                        width = if (isCurrent) 2.dp else 1.dp,
                                        color = if (isCurrent) TelegramBlue else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hidden / Direct Input Field for keyboard typing
                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 6) {
                                enteredCode = digitsOnly
                                if (digitsOnly.length == 6) {
                                    focusManager.clearFocus()
                                    onVerifyCode(digitsOnly)
                                }
                            }
                        },
                        label = { Text("Type 6-digit code here") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(codeFocusRequester)
                            .testTag("verification_code_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verify Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onVerifyCode(enteredCode)
                        },
                        enabled = enteredCode.length == 6 && authState !is PhoneAuthState.VerifyingCode,
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_verify_button")
                    ) {
                        if (authState is PhoneAuthState.VerifyingCode) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Verifying security code...", fontSize = 15.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify Code & Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isEmailCodeSent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { openEmailApp() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = TelegramBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Gmail / Email App 📬",
                                fontWeight = FontWeight.SemiBold,
                                color = TelegramBlue,
                                fontSize = 13.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend timer or button
                    if (resendCountdown > 0) {
                        Text(
                            text = "Resend code in 00:${resendCountdown.toString().padStart(2, '0')}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    } else {
                        TextButton(
                            onClick = {
                                if (isEmailCodeSent) {
                                    onResendEmailCode()
                                } else {
                                    onResendCode(activity)
                                }
                            }
                        ) {
                            Text(
                                text = "Resend Verification Code 🔄",
                                color = TelegramBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Instant Bypass Button if email delay
                    TextButton(
                        onClick = {
                            if (isEmailCodeSent) {
                                onDirectEmailSignIn(targetLabel)
                            } else {
                                onSimulateTestSignIn(targetLabel)
                            }
                        }
                    ) {
                        Text(
                            text = "Didn't receive the code? Instant Sign-In ⚡",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }

                // Error display banner
                if (authState is PhoneAuthState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = Color.Red.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authState.message,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Country Picker Dialog
    if (showCountryDialog) {
        CountryPickerDialog(
            countries = supportedCountries,
            onDismiss = { showCountryDialog = false },
            onSelect = { country ->
                selectedCountry = country
                showCountryDialog = false
            }
        )
    }
}

@Composable
fun CountryPickerDialog(
    countries: List<CountryInfo>,
    onDismiss: () -> Unit,
    onSelect: (CountryInfo) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search) {
        if (search.isBlank()) countries
        else countries.filter {
            it.name.contains(search, ignoreCase = true) ||
            it.dialCode.contains(search, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select Country", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search country or dial code...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(filtered) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(country) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = country.flag, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = country.name,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = country.dialCode,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TelegramBlue
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
