package com.jesuslcorominas.teamflowmanager.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.ui.TeamFlowManagerIcon
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.account_exists_with_other_provider
import teamflowmanager.shared_ui.generated.resources.app_name
import teamflowmanager.shared_ui.generated.resources.ic_google
import teamflowmanager.shared_ui.generated.resources.login_subtitle
import teamflowmanager.shared_ui.generated.resources.sign_in_with_google

/**
 * CMP LoginScreen — platform-agnostic.
 *
 * [onSignInWithGoogle] is a suspend lambda that performs the platform-specific
 * sign-in flow (CredentialManager on Android, GIDSignIn on iOS) and returns
 * the Google ID token. The screen calls viewModel.signInWithGoogle(idToken)
 * once the token is obtained.
 *
 * Sign in with Apple is iOS only: [onSignInWithApple] runs the native flow and
 * returns the credential, or null when the user dismisses Apple's sheet, and
 * [appleSignInButton] draws Apple's own button. Both default to null, which
 * leaves the screen exactly as it is on Android.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onSignInWithGoogle: suspend () -> String,
    onLoginSuccess: () -> Unit,
    /**
     * Platform-specific callback to request POST_NOTIFICATIONS permission.
     * On Android 13+ the caller should launch the permission request and then
     * call [LoginViewModel.onNotificationPermissionResult] with the result.
     * Pass null on platforms where notification permission is not applicable (iOS).
     */
    onRequestNotificationPermission: (() -> Unit)? = null,
    onSignInWithApple: (suspend () -> AppleSignInCredential?)? = null,
    appleSignInButton: (@Composable (enabled: Boolean, onClick: () -> Unit) -> Unit)? = null,
) {
    TrackScreenView(screenName = ScreenName.LOGIN, screenClass = "LoginScreen")

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val accountExistsMessage = stringResource(Res.string.account_exists_with_other_provider)
    // Which button started the sign-in, so the spinner shows where the user tapped.
    // Android only has Google, so there it always stays on the Google button.
    var appleSignInInProgress by remember { mutableStateOf(false) }
    val isLoading = uiState is UiState.Loading

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            is UiState.AccountExistsWithOtherProvider -> {
                snackbarHostState.showSnackbar(accountExistsMessage)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LoginViewModel.UiEvent.RequestNotificationPermission ->
                    onRequestNotificationPermission?.invoke()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TeamFlowManagerIcon()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedButton(
                    onClick = {
                        appleSignInInProgress = false
                        scope.launch {
                            try {
                                val idToken = onSignInWithGoogle()
                                viewModel.signInWithGoogle(idToken)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    e.message ?: "Error al iniciar sesión",
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                ) {
                    if (isLoading && !appleSignInInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Image(
                            painter = painterResource(Res.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.sign_in_with_google),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                if (onSignInWithApple != null && appleSignInButton != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading && appleSignInInProgress) {
                        // Apple's native button cannot show progress, so while the
                        // Firebase sign-in runs it is swapped for a look-alike with a spinner.
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(Color.Black, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        }
                    } else {
                        appleSignInButton(!isLoading) {
                            appleSignInInProgress = true
                            scope.launch {
                                try {
                                    // null: the user dismissed Apple's sheet, nothing to report.
                                    val credential = onSignInWithApple() ?: return@launch
                                    viewModel.signInWithApple(credential)
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        e.message ?: "Error al iniciar sesión",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
