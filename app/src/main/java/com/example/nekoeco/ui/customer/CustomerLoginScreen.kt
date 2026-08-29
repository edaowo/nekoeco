package com.example.nekoeco.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// ============================================================
// UI STATE
// ============================================================

data class CustomerLoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val errorMessage: String = "",
    val loginSuccess: Boolean = false
)


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerLoginEvent {

    data class EmailChanged(
        val email: String
    ) : CustomerLoginEvent


    data class PasswordChanged(
        val password: String
    ) : CustomerLoginEvent


    data object TogglePasswordVisibility :
        CustomerLoginEvent


    data object LoginClicked :
        CustomerLoginEvent


    data object LoginHandled :
        CustomerLoginEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerLoginViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerLoginUiState()
        )


    val uiState: StateFlow<CustomerLoginUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: CustomerLoginEvent
    ) {

        when (event) {


            // =============================================
            // EMAIL
            // =============================================

            is CustomerLoginEvent.EmailChanged -> {

                _uiState.update {

                    it.copy(
                        email = event.email,
                        errorMessage = ""
                    )
                }
            }


            // =============================================
            // PASSWORD
            // =============================================

            is CustomerLoginEvent.PasswordChanged -> {

                _uiState.update {

                    it.copy(
                        password = event.password,
                        errorMessage = ""
                    )
                }
            }


            // =============================================
            // PASSWORD VISIBILITY
            // =============================================

            CustomerLoginEvent.TogglePasswordVisibility -> {

                _uiState.update {

                    it.copy(
                        passwordVisible =
                            !it.passwordVisible
                    )
                }
            }


            // =============================================
            // LOGIN
            // =============================================

            CustomerLoginEvent.LoginClicked -> {

                login()
            }


            // =============================================
            // LOGIN HANDLED
            // =============================================

            CustomerLoginEvent.LoginHandled -> {

                _uiState.update {

                    it.copy(
                        loginSuccess = false
                    )
                }
            }
        }
    }


    private fun login() {

        val state =
            _uiState.value


        when {

            state.email.isBlank() -> {

                _uiState.update {

                    it.copy(
                        errorMessage =
                            "Please enter your email."
                    )
                }
            }


            !state.email.contains("@") -> {

                _uiState.update {

                    it.copy(
                        errorMessage =
                            "Please enter a valid email."
                    )
                }
            }


            state.password.isBlank() -> {

                _uiState.update {

                    it.copy(
                        errorMessage =
                            "Please enter your password."
                    )
                }
            }


            else -> {

                val customer =
                    CustomerDataStore.login(
                        email = state.email,
                        password = state.password
                    )


                if (customer != null) {

                    _uiState.update {

                        it.copy(
                            errorMessage = "",
                            loginSuccess = true
                        )
                    }

                } else {

                    _uiState.update {

                        it.copy(
                            errorMessage =
                                "Invalid email or password.",
                            loginSuccess = false
                        )
                    }
                }
            }
        }
    }
}


// ============================================================
// SCREEN
// ============================================================

@Composable
fun CustomerLoginScreen(

    onLoginSuccess: () -> Unit,

    onSignUpClick: () -> Unit,

    onBackClick: () -> Unit,

    viewModel: CustomerLoginViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // LOGIN SUCCESS
    // ========================================================

    LaunchedEffect(
        uiState.loginSuccess
    ) {

        if (
            uiState.loginSuccess
        ) {

            onLoginSuccess()

            viewModel.onEvent(
                CustomerLoginEvent
                    .LoginHandled
            )
        }
    }


    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        verticalArrangement =
            Arrangement.Center
    ) {


        // ==================================================
        // TITLE
        // ==================================================

        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )


        Text(
            text =
                "Login to your NekoEco account",

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(28.dp)
        )


        // ==================================================
        // EMAIL
        // ==================================================

        OutlinedTextField(

            value =
                uiState.email,

            onValueChange = {

                viewModel.onEvent(
                    CustomerLoginEvent
                        .EmailChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Email")
            },

            singleLine = true
        )


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        // ==================================================
        // PASSWORD
        // ==================================================

        OutlinedTextField(

            value =
                uiState.password,

            onValueChange = {

                viewModel.onEvent(
                    CustomerLoginEvent
                        .PasswordChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Password")
            },

            singleLine = true,

            visualTransformation =

                if (
                    uiState.passwordVisible
                ) {

                    VisualTransformation.None

                } else {

                    PasswordVisualTransformation()
                },

            trailingIcon = {

                IconButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerLoginEvent
                                .TogglePasswordVisibility
                        )
                    }

                ) {

                    Icon(

                        imageVector =

                            if (
                                uiState.passwordVisible
                            ) {

                                Icons.Default.VisibilityOff

                            } else {

                                Icons.Default.Visibility
                            },

                        contentDescription =

                            if (
                                uiState.passwordVisible
                            ) {

                                "Hide password"

                            } else {

                                "Show password"
                            }
                    )
                }
            }
        )


        // ==================================================
        // ERROR
        // ==================================================

        if (
            uiState.errorMessage
                .isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(
                text =
                    uiState.errorMessage,

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                fontSize =
                    13.sp
            )
        }


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        // ==================================================
        // LOGIN
        // ==================================================

        Button(

            onClick = {

                viewModel.onEvent(
                    CustomerLoginEvent
                        .LoginClicked
                )
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Login"
            )
        }


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        // ==================================================
        // SIGN UP
        // ==================================================

        TextButton(

            onClick =
                onSignUpClick,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    "Don't have an account? Sign Up"
            )
        }


        Spacer(
            modifier =
                Modifier.height(4.dp)
        )


        // ==================================================
        // BACK
        // ==================================================

        TextButton(

            onClick =
                onBackClick,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text = "← Back"
            )
        }
    }
}