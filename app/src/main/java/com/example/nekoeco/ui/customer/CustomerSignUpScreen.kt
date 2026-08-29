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

data class CustomerSignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val errorMessage: String = "",
    val signUpSuccess: Boolean = false
)


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerSignUpEvent {

    data class NameChanged(
        val name: String
    ) : CustomerSignUpEvent

    data class EmailChanged(
        val email: String
    ) : CustomerSignUpEvent

    data class PasswordChanged(
        val password: String
    ) : CustomerSignUpEvent

    data class ConfirmPasswordChanged(
        val confirmPassword: String
    ) : CustomerSignUpEvent

    data object TogglePasswordVisibility :
        CustomerSignUpEvent

    data object ToggleConfirmPasswordVisibility :
        CustomerSignUpEvent

    data object SignUpClicked :
        CustomerSignUpEvent

    data object SignUpHandled :
        CustomerSignUpEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class CustomerSignUpViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CustomerSignUpUiState()
        )

    val uiState: StateFlow<CustomerSignUpUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: CustomerSignUpEvent
    ) {

        when (event) {

            is CustomerSignUpEvent.NameChanged -> {

                _uiState.update {
                    it.copy(
                        name = event.name,
                        errorMessage = ""
                    )
                }
            }


            is CustomerSignUpEvent.EmailChanged -> {

                _uiState.update {
                    it.copy(
                        email = event.email,
                        errorMessage = ""
                    )
                }
            }


            is CustomerSignUpEvent.PasswordChanged -> {

                _uiState.update {
                    it.copy(
                        password = event.password,
                        errorMessage = ""
                    )
                }
            }


            is CustomerSignUpEvent.ConfirmPasswordChanged -> {

                _uiState.update {
                    it.copy(
                        confirmPassword =
                            event.confirmPassword,
                        errorMessage = ""
                    )
                }
            }


            CustomerSignUpEvent.TogglePasswordVisibility -> {

                _uiState.update {
                    it.copy(
                        passwordVisible =
                            !it.passwordVisible
                    )
                }
            }


            CustomerSignUpEvent.ToggleConfirmPasswordVisibility -> {

                _uiState.update {
                    it.copy(
                        confirmPasswordVisible =
                            !it.confirmPasswordVisible
                    )
                }
            }


            CustomerSignUpEvent.SignUpClicked -> {

                signUp()
            }


            CustomerSignUpEvent.SignUpHandled -> {

                _uiState.update {
                    it.copy(
                        signUpSuccess = false
                    )
                }
            }
        }
    }


    private fun signUp() {

        val state =
            _uiState.value


        when {

            state.name.isBlank() -> {

                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Please enter your name."
                    )
                }
            }


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


            state.password.length < 6 -> {

                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Password must be at least 6 characters."
                    )
                }
            }


            state.password !=
                    state.confirmPassword -> {

                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Passwords do not match."
                    )
                }
            }


            else -> {

                val success =
                    CustomerDataStore.signUp(
                        name = state.name,
                        email = state.email,
                        password = state.password
                    )


                if (success) {

                    _uiState.update {
                        it.copy(
                            errorMessage = "",
                            signUpSuccess = true
                        )
                    }

                } else {

                    _uiState.update {
                        it.copy(
                            errorMessage =
                                "This email is already registered.",
                            signUpSuccess = false
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
fun CustomerSignUpScreen(

    onSignUpSuccess: () -> Unit,

    onBackClick: () -> Unit,

    viewModel: CustomerSignUpViewModel =
        viewModel()

) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()


    // ========================================================
    // SIGN UP SUCCESS
    // ========================================================

    LaunchedEffect(
        uiState.signUpSuccess
    ) {

        if (
            uiState.signUpSuccess
        ) {

            onSignUpSuccess()

            viewModel.onEvent(
                CustomerSignUpEvent
                    .SignUpHandled
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
            text = "Create Account",
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
                "Create your NekoEco customer account",

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // ==================================================
        // NAME
        // ==================================================

        OutlinedTextField(

            value =
                uiState.name,

            onValueChange = {

                viewModel.onEvent(
                    CustomerSignUpEvent
                        .NameChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Name")
            },

            singleLine = true
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // ==================================================
        // EMAIL
        // ==================================================

        OutlinedTextField(

            value =
                uiState.email,

            onValueChange = {

                viewModel.onEvent(
                    CustomerSignUpEvent
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
                Modifier.height(12.dp)
        )


        // ==================================================
        // PASSWORD
        // ==================================================

        OutlinedTextField(

            value =
                uiState.password,

            onValueChange = {

                viewModel.onEvent(
                    CustomerSignUpEvent
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
                            CustomerSignUpEvent
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


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // ==================================================
        // CONFIRM PASSWORD
        // ==================================================

        OutlinedTextField(

            value =
                uiState.confirmPassword,

            onValueChange = {

                viewModel.onEvent(
                    CustomerSignUpEvent
                        .ConfirmPasswordChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Confirm Password")
            },

            singleLine = true,

            visualTransformation =

                if (
                    uiState.confirmPasswordVisible
                ) {

                    VisualTransformation.None

                } else {

                    PasswordVisualTransformation()
                },

            trailingIcon = {

                IconButton(

                    onClick = {

                        viewModel.onEvent(
                            CustomerSignUpEvent
                                .ToggleConfirmPasswordVisibility
                        )
                    }

                ) {

                    Icon(

                        imageVector =

                            if (
                                uiState.confirmPasswordVisible
                            ) {

                                Icons.Default.VisibilityOff

                            } else {

                                Icons.Default.Visibility
                            },

                        contentDescription =

                            if (
                                uiState.confirmPasswordVisible
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
                Modifier.height(18.dp)
        )


        // ==================================================
        // SIGN UP
        // ==================================================

        Button(

            onClick = {

                viewModel.onEvent(
                    CustomerSignUpEvent
                        .SignUpClicked
                )
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Sign Up"
            )
        }


        Spacer(
            modifier =
                Modifier.height(8.dp)
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