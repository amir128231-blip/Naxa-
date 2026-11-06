package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.PhoneAuthState
import com.example.ui.i18n.AuthStringsProvider
import com.example.ui.i18n.LocalAppLanguage
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

        // Blue (right: 330 to 35 deg)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 330f,
            sweepAngle = 65f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = strokeStyle
        )

        // Blue horizontal crossbar
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(cx - stroke * 0.2f, cy - stroke / 2f),
            size = Size(radius + stroke * 0.2f, stroke)
        )
    }
}

data class CountryInfo(
    val name: String,
    val flag: String,
    val dialCode: String,
    val sampleNumber: String
)

val supportedCountries = listOf(
    CountryInfo("ایران (Iran)", "🇮🇷", "+98", "912 345 6789"),
    CountryInfo("United States", "🇺🇸", "+1", "650 555 3434"),
    CountryInfo("United Kingdom", "🇬🇧", "+44", "7911 123456"),
    CountryInfo("Germany", "🇩🇪", "+49", "151 23456789"),
    CountryInfo("Turkey", "🇹🇷", "+90", "532 123 4567"),
    CountryInfo("United Arab Emirates", "🇦🇪", "+971", "50 123 4567"),
    CountryInfo("Canada", "🇨🇦", "+1", "416 555 0199"),
    CountryInfo("France", "🇫🇷", "+33", "6 12 34 56 78"),
    CountryInfo("Russia", "🇷🇺", "+7", "912 345 67 89"),
    CountryInfo("India", "🇮🇳", "+91", "98765 43210"),
    CountryInfo("Iraq", "🇮🇶", "+964", "770 123 4567"),
    CountryInfo("Saudi Arabia", "🇸🇦", "+966", "50 123 4567"),
    CountryInfo("Afghanistan", "🇦🇫", "+93", "70 123 4567")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    authState: PhoneAuthState,
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
    val currentLang = LocalAppLanguage.current
    val strings = remember(currentLang) { AuthStringsProvider.get(currentLang) }
    val isGoogleSigningIn = authState is PhoneAuthState.SigningInWithGoogle

    var selectedCountry by remember { mutableStateOf(supportedCountries[0]) }
    var phoneNumber by remember { mutableStateOf("") }
    var syncContactsChecked by remember { mutableStateOf(true) }
    var showCountryDialog by remember { mutableStateOf(false) }

    var smsCode by remember { mutableStateOf("") }
    var resendCountdown by remember { mutableIntStateOf(60) }

    val focusManager = LocalFocusManager.current
    val codeFocusRequester = remember { FocusRequester() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.SEND_SMS)
        permissions.add(Manifest.permission.RECEIVE_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        try {
            permissionLauncher.launch(permissions.toTypedArray())
        } catch (_: Throwable) {}
    }

    // Resend countdown timer when code is sent
    LaunchedEffect(authState) {
        if (authState is PhoneAuthState.CodeSent) {
            smsCode = ""
            resendCountdown = 60
            while (resendCountdown > 0) {
                delay(1000L)
                resendCountdown--
            }
        }
    }

    val isCodeSent = authState is PhoneAuthState.CodeSent ||
            (authState is PhoneAuthState.VerifyingCode) ||
            (authState is PhoneAuthState.Error && smsCode.isNotEmpty())

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isCodeSent) strings.codeTitle else strings.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    if (isCodeSent) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TelegramBlue
                            )
                        }
                    }
                },
                actions = {
                    // Auth is strictly required before entering Nexa
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!isCodeSent) {
                FloatingActionButton(
                    onClick = {
                        if (phoneNumber.isNotBlank()) {
                            focusManager.clearFocus()
                            val fullPhone = "${selectedCountry.dialCode}${phoneNumber.trim().removePrefix("0")}"
                            onSendCode(activity, fullPhone)
                        }
                    },
                    containerColor = TelegramBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("submit_phone_button")
                ) {
                    if (authState is PhoneAuthState.SendingCode || isGoogleSigningIn) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = strings.nextButton
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Telegram Logo / Phone Illustration
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(TelegramBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCodeSent) Icons.Default.Sms else Icons.Default.Phone,
                    contentDescription = "Phone Auth",
                    tint = TelegramBlue,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle instructions
            Text(
                text = if (isCodeSent) {
                    val phone = (authState as? PhoneAuthState.CodeSent)?.phoneNumber
                        ?: "${selectedCountry.dialCode}$phoneNumber"
                    strings.codeSubtitle(phone)
                } else {
                    strings.subtitle
                },
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!isCodeSent) {
                // Country selection row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { showCountryDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = selectedCountry.flag, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedCountry.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = selectedCountry.dialCode,
                            fontSize = 15.sp,
                            color = TelegramBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phone Number input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Dial Code badge
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TelegramBlue.copy(alpha = 0.08f))
                            .border(1.dp, TelegramBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedCountry.dialCode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelegramBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            // accept only digits
                            phoneNumber = input.filter { it.isDigit() }
                        },
                        placeholder = {
                            Text(
                                text = selectedCountry.sampleNumber,
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (phoneNumber.isNotBlank()) {
                                    val fullPhone = "${selectedCountry.dialCode}${phoneNumber.trim().removePrefix("0")}"
                                    onSendCode(activity, fullPhone)
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("phone_number_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramBlue,
                            cursorColor = TelegramBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sync contacts checkbox (Telegram standard)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncContactsChecked = !syncContactsChecked }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncContactsChecked,
                        onCheckedChange = { syncContactsChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = TelegramBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.syncContactsLabel,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Security reassurance note
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TelegramBlue.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = TelegramBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ورود به پیام‌رسان نکسا با تایید شماره تلفن همراه انجام می‌شود.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Verification Code State
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Edit Phone Number Row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TelegramBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.wrongNumber,
                            color = TelegramBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nexta SMS Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = TelegramBlue.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = TelegramBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "📩 پیامک از طرف: نکستا (Nexta)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TelegramBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "پیامک حاوی کد تایید ۶ رقمی به سیم‌کارت ارسال گردید.",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6-digit Code Input
                    OutlinedTextField(
                        value = smsCode,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                smsCode = input
                                if (smsCode.length == 6) {
                                    focusManager.clearFocus()
                                    onVerifyCode(smsCode)
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = "• • •  • • •",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp,
                                color = Color.Gray.copy(alpha = 0.4f),
                                letterSpacing = 8.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            letterSpacing = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelegramBlue
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (smsCode.length == 6) {
                                    focusManager.clearFocus()
                                    onVerifyCode(smsCode)
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .focusRequester(codeFocusRequester)
                            .testTag("sms_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramBlue,
                            cursorColor = TelegramBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Verify Button
                    Button(
                        onClick = {
                            if (smsCode.length == 6) {
                                focusManager.clearFocus()
                                onVerifyCode(smsCode)
                            }
                        },
                        enabled = smsCode.length == 6 && authState !is PhoneAuthState.VerifyingCode,
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_code_button")
                    ) {
                        if (authState is PhoneAuthState.VerifyingCode) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(strings.verifyingCode)
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.verifyButton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resend countdown or button
                    if (resendCountdown > 0) {
                        Text(
                            text = strings.resendIn(resendCountdown),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    } else {
                        TextButton(
                            onClick = { onResendCode(activity) },
                            enabled = authState !is PhoneAuthState.SendingCode
                        ) {
                            Text(
                                text = strings.resendCode,
                                color = TelegramBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
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
                        Text(
                            text = authState.message,
                            color = Color.Red,
                            fontSize = 12.5.sp,
                            modifier = Modifier.weight(1f)
                        )
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
            Text(text = "انتخاب کشور", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("جستجوی نام یا کد...") },
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
                Text("بستن")
            }
        }
    )
}
