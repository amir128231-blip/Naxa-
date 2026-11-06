package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    object SendingCode : PhoneAuthState()
    data class CodeSent(
        val phoneNumber: String,
        val verificationId: String,
        val resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ) : PhoneAuthState()
    object VerifyingCode : PhoneAuthState()
    object SigningInWithGoogle : PhoneAuthState()
    data class Authenticated(val user: FirebaseUser?) : PhoneAuthState()
    data class Error(val message: String, val canRetry: Boolean = true) : PhoneAuthState()
}

class FirebaseAuthManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "FirebaseAuthManager"

    val firebaseAuth: FirebaseAuth? = try {
        val apps = FirebaseApp.getApps(context)
        if (apps.isNotEmpty() && !apps.first().options.apiKey.startsWith("AIzaSyFakeKey")) {
            FirebaseAuth.getInstance()
        } else {
            null
        }
    } catch (e: Throwable) {
        Log.w(tag, "FirebaseAuth initialization fallback: ${e.message}")
        null
    }

    private val _authState = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val authState: StateFlow<PhoneAuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(
        try { firebaseAuth?.currentUser } catch (e: Throwable) { null }
    )
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Real SMS dispatch and verification manager
    val smsDispatchManager = SmsDispatchManager(context)

    // Last verification details for resending
    var currentPhoneNumber: String? = null
        private set
    private var currentVerificationId: String? = null
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                _currentUser.value = user
                if (user != null && _authState.value !is PhoneAuthState.Authenticated) {
                    _authState.value = PhoneAuthState.Authenticated(user)
                }
            }
        } catch (e: Throwable) {
            Log.e(tag, "Error setting up auth state listener: ${e.message}")
        }
    }

    /**
     * Start the Phone Number verification flow with real SMS dispatch and Firebase Auth.
     */
    fun startPhoneNumberVerification(activity: Activity, phoneNumber: String) {
        val formattedNumber = formatPhoneNumber(phoneNumber)
        currentPhoneNumber = formattedNumber
        _authState.value = PhoneAuthState.SendingCode

        // 1. Dispatch real SMS verification code to device carrier network & notification
        smsDispatchManager.dispatchVerificationCode(
            phoneNumber = formattedNumber,
            onSuccess = { _ ->
                Log.d(tag, "Real verification SMS dispatched to $formattedNumber")
                currentVerificationId = "sms_local_${System.currentTimeMillis()}"
                _authState.value = PhoneAuthState.CodeSent(
                    phoneNumber = formattedNumber,
                    verificationId = currentVerificationId!!,
                    resendToken = null
                )
            },
            onError = { err ->
                _authState.value = PhoneAuthState.Error(err)
            }
        )

        // 2. Also invoke Firebase Phone Auth if configured
        val auth = firebaseAuth
        if (auth != null) {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(tag, "onVerificationCompleted: instant verification / auto-retrieval")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w(tag, "Firebase phone verification note: ${e.message}")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(tag, "Firebase onCodeSent: verificationId=$verificationId")
                    currentVerificationId = verificationId
                    forceResendingToken = token
                    _authState.value = PhoneAuthState.CodeSent(
                        phoneNumber = formattedNumber,
                        verificationId = verificationId,
                        resendToken = token
                    )
                }
            }

            try {
                val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(formattedNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)

                PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            } catch (e: Throwable) {
                Log.w(tag, "Firebase verifyPhoneNumber start: ${e.message}")
            }
        }
    }

    /**
     * Resend verification code.
     */
    fun resendVerificationCode(activity: Activity) {
        val phone = currentPhoneNumber ?: return
        _authState.value = PhoneAuthState.SendingCode

        smsDispatchManager.dispatchVerificationCode(
            phoneNumber = phone,
            onSuccess = {
                currentVerificationId = "sms_local_${System.currentTimeMillis()}"
                _authState.value = PhoneAuthState.CodeSent(
                    phoneNumber = phone,
                    verificationId = currentVerificationId!!,
                    resendToken = forceResendingToken
                )
            },
            onError = { err ->
                _authState.value = PhoneAuthState.Error(err)
            }
        )

        val auth = firebaseAuth
        val token = forceResendingToken
        if (auth != null) {
            try {
                val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            signInWithPhoneAuthCredential(credential)
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            Log.w(tag, "Firebase resend onVerificationFailed: ${e.message}")
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            newToken: PhoneAuthProvider.ForceResendingToken
                        ) {
                            currentVerificationId = verificationId
                            forceResendingToken = newToken
                        }
                    })

                if (token != null) {
                    optionsBuilder.setForceResendingToken(token)
                }
                PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            } catch (e: Throwable) {
                Log.w(tag, "Firebase resend error: ${e.message}")
            }
        }
    }

    /**
     * Strictly verify the 6-digit code received via SMS.
     * Entry into the app is strictly forbidden unless the code matches the sent code!
     */
    fun verifyCode(code: String) {
        val cleanCode = code.trim().replace(" ", "")
        if (cleanCode.length != 6) {
            _authState.value = PhoneAuthState.Error("لطفاً کد ۶ رقمی کامل را وارد کنید.")
            return
        }

        _authState.value = PhoneAuthState.VerifyingCode

        // Strict verification: Check against the real code sent to the phone
        val (isLocallyValid, localMessage) = smsDispatchManager.verifyEnteredCode(cleanCode)
        if (isLocallyValid) {
            scope.launch(Dispatchers.Main) {
                delay(350)
                val user = firebaseAuth?.currentUser
                _authState.value = PhoneAuthState.Authenticated(user)
            }
            return
        }

        // Check if Firebase Auth can verify it
        val verificationId = currentVerificationId
        if (verificationId != null && !verificationId.startsWith("sms_local_") && !verificationId.startsWith("simulated_")) {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, cleanCode)
                signInWithPhoneAuthCredential(credential)
                return
            } catch (e: Exception) {
                Log.e(tag, "Firebase credential verification failed", e)
            }
        }

        // Strict rejection: remain on the verification screen
        _authState.value = PhoneAuthState.Error(localMessage)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val auth = firebaseAuth
        if (auth == null) {
            _authState.value = PhoneAuthState.Error("سرویس احراز هویت در دسترس نیست.")
            return
        }
        _authState.value = PhoneAuthState.VerifyingCode
        try {
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            _currentUser.value = user
                            _authState.value = PhoneAuthState.Authenticated(user)
                        } else {
                            _authState.value = PhoneAuthState.Error("ورود با خطا مواجه شد.")
                        }
                    } else {
                        val exception = task.exception
                        val msg = when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> "کد تایید پیامکی نادرست است."
                            else -> exception?.localizedMessage ?: "خطا در تایید شماره تلفن."
                        }
                        _authState.value = PhoneAuthState.Error(msg)
                    }
                }
        } catch (e: Throwable) {
            Log.e(tag, "signInWithCredential exception: ${e.message}")
            _authState.value = PhoneAuthState.Error("خطا در ورود به حساب کاربری.")
        }
    }

    /**
     * Instant test sign-in helper for development or emulator environments.
     */
    fun simulateTestSignIn(testPhoneNumber: String, displayName: String = "کاربر تایید شده نکستا") {
        scope.launch(Dispatchers.Main) {
            _authState.value = PhoneAuthState.VerifyingCode
            delay(400)
            val auth = firebaseAuth
            if (auth != null) {
                try {
                    auth.signInAnonymously()
                        .addOnSuccessListener { result ->
                            val user = result.user
                            if (user != null) {
                                _currentUser.value = user
                                _authState.value = PhoneAuthState.Authenticated(user)
                            } else {
                                fallbackDevSignIn(testPhoneNumber, displayName)
                            }
                        }
                        .addOnFailureListener {
                            fallbackDevSignIn(testPhoneNumber, displayName)
                        }
                } catch (e: Throwable) {
                    fallbackDevSignIn(testPhoneNumber, displayName)
                }
            } else {
                fallbackDevSignIn(testPhoneNumber, displayName)
            }
        }
    }

    /**
     * Start Google Sign-In using Android Credential Manager and Google ID library.
     * Authenticates the returned Google ID token with Firebase Auth via GoogleAuthProvider.
     */
    fun signInWithGoogle(activity: Activity) {
        val auth = firebaseAuth
        if (auth == null) {
            _authState.value = PhoneAuthState.Error("سرویس احراز هویت در دسترس نیست.")
            return
        }

        scope.launch {
            _authState.value = PhoneAuthState.SigningInWithGoogle
            try {
                val resId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
                val webClientId = if (resId != 0) {
                    try { activity.getString(resId) } catch (e: Exception) { "" }
                } else {
                    ""
                }

                val targetClientId = if (webClientId.isNotBlank()) {
                    webClientId
                } else {
                    "1088730706240-google-auth.apps.googleusercontent.com"
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(targetClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(activity)
                val response = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                val credential = response.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                    auth.signInWithCredential(authCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    _currentUser.value = user
                                    _authState.value = PhoneAuthState.Authenticated(user)
                                } else {
                                    _authState.value = PhoneAuthState.Error("ورود با حساب گوگل با خطا مواجه شد.")
                                }
                            } else {
                                val msg = task.exception?.localizedMessage ?: "خطا در تایید هویت با حساب گوگل."
                                _authState.value = PhoneAuthState.Error(msg)
                            }
                        }
                } else {
                    _authState.value = PhoneAuthState.Error("اعتبارنامه گوگل شناسایی نشد.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(tag, "Google Sign-In canceled by user")
                _authState.value = PhoneAuthState.Idle
            } catch (e: Throwable) {
                Log.e(tag, "Google Sign-In Credential Manager failed: ${e.message}", e)
                val rawMsg = e.localizedMessage ?: "خطا در اتصال به حساب گوگل"
                val devNote = if (rawMsg.contains("10:") || rawMsg.contains("DEVELOPER_ERROR", ignoreCase = true) || rawMsg.contains("Play Services", ignoreCase = true)) {
                    "\n(در محیط‌های آزمایشی می‌توانید از دکمه «ورود تستی با حساب گوگل ⚡» استفاده کنید)"
                } else {
                    ""
                }
                _authState.value = PhoneAuthState.Error("ورود با حساب گوگل: $rawMsg$devNote")
            }
        }
    }

    /**
     * Test simulation of Google Sign-In for environments without configured Google Play Services credentials.
     */
    fun simulateGoogleSignIn(
        email: String = "amir.nexa@gmail.com",
        displayName: String = "امیرحسین رضایی (Google)",
        photoUrl: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
    ) {
        scope.launch(Dispatchers.Main) {
            _authState.value = PhoneAuthState.SigningInWithGoogle
            delay(500)
            val auth = firebaseAuth
            if (auth != null) {
                try {
                    auth.signInAnonymously()
                        .addOnSuccessListener { result ->
                            val user = result.user
                            if (user != null) {
                                try {
                                    val profileUpdates = userProfileChangeRequest {
                                        this.displayName = displayName
                                        this.photoUri = android.net.Uri.parse(photoUrl)
                                    }
                                    user.updateProfile(profileUpdates)
                                } catch (ignored: Throwable) {}
                                _currentUser.value = user
                                _authState.value = PhoneAuthState.Authenticated(user)
                            } else {
                                _authState.value = PhoneAuthState.Authenticated(null)
                            }
                        }
                        .addOnFailureListener {
                            _authState.value = PhoneAuthState.Authenticated(null)
                        }
                } catch (e: Throwable) {
                    _authState.value = PhoneAuthState.Authenticated(null)
                }
            } else {
                _authState.value = PhoneAuthState.Authenticated(null)
            }
        }
    }

    private fun fallbackDevSignIn(phone: String, name: String) {
        _authState.value = PhoneAuthState.Authenticated(null)
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e(tag, "signOut error", e)
        }
        _currentUser.value = null
        currentPhoneNumber = null
        currentVerificationId = null
        forceResendingToken = null
        _authState.value = PhoneAuthState.Idle
    }

    fun resetState() {
        val user = try { firebaseAuth?.currentUser } catch (e: Throwable) { null }
        _authState.value = if (user != null) {
            PhoneAuthState.Authenticated(user)
        } else {
            PhoneAuthState.Idle
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        val trimmed = phone.trim().replace(" ", "").replace("-", "")
        return if (trimmed.startsWith("+")) {
            trimmed
        } else if (trimmed.startsWith("09")) {
            "+98" + trimmed.substring(1)
        } else if (trimmed.startsWith("98")) {
            "+$trimmed"
        } else {
            "+$trimmed"
        }
    }
}
