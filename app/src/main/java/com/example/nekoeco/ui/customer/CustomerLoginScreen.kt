package com.example.nekoeco.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.material3.AlertDialog
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
    val loginSuccess: Boolean = false,


    // ========================================================
    // FORGOT PASSWORD DIALOG
    // ========================================================

    val showForgotPassword: Boolean = false,

    val resetEmail: String = "",

    val resetName: String = "",

    val newPassword: String = "",

    val confirmPassword: String = "",

    val verified: Boolean = false,

    val resetErrorMessage: String = "",

    val newPasswordVisible: Boolean = false,

    val confirmPasswordVisible: Boolean = false
)


// ============================================================
// EVENTS
// ============================================================

sealed interface CustomerLoginEvent {


    // ========================================================
    // LOGIN EVENTS
    // ========================================================

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


    // ========================================================
    // FORGOT PASSWORD
    // ========================================================

    data object ForgotPasswordClicked :
        CustomerLoginEvent


    data object CloseForgotPassword :
        CustomerLoginEvent


    data class ResetEmailChanged(
        val email: String
    ) : CustomerLoginEvent


    data class ResetNameChanged(
        val name: String
    ) : CustomerLoginEvent


    data object VerifyCustomerClicked :
        CustomerLoginEvent


    data class NewPasswordChanged(
        val password: String
    ) : CustomerLoginEvent


    data class ConfirmPasswordChanged(
        val password: String
    ) : CustomerLoginEvent


    data object ToggleNewPasswordVisibility :
        CustomerLoginEvent


    data object ToggleConfirmPasswordVisibility :
        CustomerLoginEvent


    data object SavePasswordClicked :
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


            // =============================================
            // FORGOT PASSWORD
            // =============================================

            CustomerLoginEvent.ForgotPasswordClicked -> {

                _uiState.update {

                    it.copy(
                        showForgotPassword = true
                    )
                }
            }


            CustomerLoginEvent.CloseForgotPassword -> {

                closeForgotPassword()
            }


            // =============================================
            // RESET EMAIL
            // =============================================

            is CustomerLoginEvent.ResetEmailChanged -> {

                _uiState.update {

                    it.copy(
                        resetEmail = event.email,
                        resetErrorMessage = ""
                    )
                }
            }


            // =============================================
            // RESET NAME
            // =============================================

            is CustomerLoginEvent.ResetNameChanged -> {

                _uiState.update {

                    it.copy(
                        resetName = event.name,
                        resetErrorMessage = ""
                    )
                }
            }


            // =============================================
            // VERIFY CUSTOMER
            // =============================================

            CustomerLoginEvent.VerifyCustomerClicked -> {

                verifyCustomer()
            }


            // =============================================
            // NEW PASSWORD
            // =============================================

            is CustomerLoginEvent.NewPasswordChanged -> {

                _uiState.update {

                    it.copy(
                        newPassword = event.password,
                        resetErrorMessage = ""
                    )
                }
            }


            // =============================================
            // CONFIRM PASSWORD
            // =============================================

            is CustomerLoginEvent.ConfirmPasswordChanged -> {

                _uiState.update {

                    it.copy(
                        confirmPassword = event.password,
                        resetErrorMessage = ""
                    )
                }
            }


            // =============================================
            // NEW PASSWORD VISIBILITY
            // =============================================

            CustomerLoginEvent.ToggleNewPasswordVisibility -> {

                _uiState.update {

                    it.copy(
                        newPasswordVisible =
                            !it.newPasswordVisible
                    )
                }
            }


            // =============================================
            // CONFIRM PASSWORD VISIBILITY
            // =============================================

            CustomerLoginEvent.ToggleConfirmPasswordVisibility -> {

                _uiState.update {

                    it.copy(
                        confirmPasswordVisible =
                            !it.confirmPasswordVisible
                    )
                }
            }


            // =============================================
            // SAVE PASSWORD
            // =============================================

            CustomerLoginEvent.SavePasswordClicked -> {

                saveNewPassword()
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


    // ========================================================
    // VERIFY CUSTOMER (forgot-password step 1)
    // ========================================================

    private fun verifyCustomer() {

        val state =
            _uiState.value


        if (
            state.resetEmail.isBlank() ||
            state.resetName.isBlank()
        ) {

            _uiState.update {

                it.copy(
                    resetErrorMessage =
                        "Please enter your email and name."
                )
            }

            return
        }


        val ok =
            CustomerDataStore.verifyCustomerForPasswordReset(

                email = state.resetEmail,

                name = state.resetName
            )


        if (ok) {

            _uiState.update {

                it.copy(
                    verified = true,
                    resetErrorMessage = ""
                )
            }

        } else {

            _uiState.update {

                it.copy(
                    resetErrorMessage =
                        "Email or Name is incorrect."
                )
            }
        }
    }


    // ========================================================
    // SAVE NEW PASSWORD (forgot-password step 2)
    // ========================================================

    private fun saveNewPassword() {

        val state =
            _uiState.value


        when {

            state.newPassword.isBlank() -> {

                _uiState.update {

                    it.copy(
                        resetErrorMessage =
                            "Please enter a new password."
                    )
                }
            }


            state.newPassword.length < 6 -> {

                _uiState.update {

                    it.copy(
                        resetErrorMessage =
                            "Password must be at least 6 characters."
                    )
                }
            }


            state.newPassword !=
                    state.confirmPassword -> {

                _uiState.update {

                    it.copy(
                        resetErrorMessage =
                            "Passwords do not match."
                    )
                }
            }


            else -> {

                val success =
                    CustomerDataStore.resetCustomerPassword(

                        email = state.resetEmail,

                        name = state.resetName,

                        newPassword = state.newPassword
                    )


                if (success) {

                    closeForgotPassword()

                } else {

                    _uiState.update {

                        it.copy(
                            resetErrorMessage =
                                "Unable to reset password."
                        )
                    }
                }
            }
        }
    }


    // ========================================================
    // CLOSE / RESET FORGOT PASSWORD
    // ========================================================

    private fun closeForgotPassword() {

        _uiState.update {

            it.copy(

                showForgotPassword = false,

                resetEmail = "",

                resetName = "",

                newPassword = "",

                confirmPassword = "",

                verified = false,

                resetErrorMessage = "",

                newPasswordVisible = false,

                confirmPasswordVisible = false
            )
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
                .verticalScroll(
                    rememberScrollState()
                )
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
        // FORGOT PASSWORD
        // ==================================================

        TextButton(

            onClick = {

                viewModel.onEvent(
                    CustomerLoginEvent
                        .ForgotPasswordClicked
                )
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(

                text =
                    "Forgot Password?",

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }


        Spacer(
            modifier =
                Modifier.height(8.dp)
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


    // ========================================================
    // FORGOT PASSWORD DIALOG
    // ========================================================

    if (
        uiState.showForgotPassword
    ) {

        ForgotPasswordDialog(

            uiState =
                uiState,

            onEvent =
                viewModel::onEvent
        )
    }
}


// ============================================================
// CUSTOMER FORGOT PASSWORD DIALOG
// ============================================================

@Composable
fun ForgotPasswordDialog(

    uiState: CustomerLoginUiState,

    onEvent: (CustomerLoginEvent) -> Unit

) {

    AlertDialog(

        onDismissRequest = {

            onEvent(
                CustomerLoginEvent
                    .CloseForgotPassword
            )
        },

        title = {

            Text(

                text =

                    if (
                        uiState.verified
                    ) {

                        "Set New Password"

                    } else {

                        "Forgot Password"
                    },

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            Column {


                // =================================================
                // VERIFY ACCOUNT
                // =================================================

                if (
                    !uiState.verified
                ) {

                    Text(

                        text =
                            "Enter your email and name to verify your account.",

                        fontSize =
                            13.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(15.dp)
                    )


                    OutlinedTextField(

                        value =
                            uiState.resetEmail,

                        onValueChange = {

                            onEvent(
                                CustomerLoginEvent
                                    .ResetEmailChanged(
                                        it
                                    )
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "Email"
                            )
                        },

                        singleLine =
                            true
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    OutlinedTextField(

                        value =
                            uiState.resetName,

                        onValueChange = {

                            onEvent(
                                CustomerLoginEvent
                                    .ResetNameChanged(
                                        it
                                    )
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "Name"
                            )
                        },

                        singleLine =
                            true
                    )

                } else {


                    // =================================================
                    // NEW PASSWORD
                    // =================================================

                    Text(

                        text =
                            "Account verified. Enter your new password.",

                        fontSize =
                            13.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(15.dp)
                    )


                    OutlinedTextField(

                        value =
                            uiState.newPassword,

                        onValueChange = {

                            onEvent(
                                CustomerLoginEvent
                                    .NewPasswordChanged(
                                        it
                                    )
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "New Password"
                            )
                        },

                        trailingIcon = {

                            IconButton(

                                onClick = {

                                    onEvent(
                                        CustomerLoginEvent
                                            .ToggleNewPasswordVisibility
                                    )
                                }

                            ) {

                                Icon(

                                    imageVector =

                                        if (
                                            uiState.newPasswordVisible
                                        ) {

                                            Icons.Default.VisibilityOff

                                        } else {

                                            Icons.Default.Visibility
                                        },

                                    contentDescription =

                                        if (
                                            uiState.newPasswordVisible
                                        ) {

                                            "Hide Password"

                                        } else {

                                            "Show Password"
                                        }
                                )
                            }
                        },

                        visualTransformation =

                            if (
                                uiState.newPasswordVisible
                            ) {

                                VisualTransformation.None

                            } else {

                                PasswordVisualTransformation()
                            },

                        singleLine =
                            true
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    OutlinedTextField(

                        value =
                            uiState.confirmPassword,

                        onValueChange = {

                            onEvent(
                                CustomerLoginEvent
                                    .ConfirmPasswordChanged(
                                        it
                                    )
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "Confirm Password"
                            )
                        },

                        trailingIcon = {

                            IconButton(

                                onClick = {

                                    onEvent(
                                        CustomerLoginEvent
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

                                            "Hide Password"

                                        } else {

                                            "Show Password"
                                        }
                                )
                            }
                        },

                        visualTransformation =

                            if (
                                uiState.confirmPasswordVisible
                            ) {

                                VisualTransformation.None

                            } else {

                                PasswordVisualTransformation()
                            },

                        singleLine =
                            true
                    )
                }


                // =================================================
                // ERROR
                // =================================================

                if (
                    uiState
                        .resetErrorMessage
                        .isNotEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    Text(

                        text =
                            uiState.resetErrorMessage,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        fontSize =
                            12.sp
                    )
                }
            }
        },


        // ====================================================
        // CONFIRM
        // ====================================================

        confirmButton = {

            TextButton(

                onClick = {

                    if (
                        uiState.verified
                    ) {

                        onEvent(
                            CustomerLoginEvent
                                .SavePasswordClicked
                        )

                    } else {

                        onEvent(
                            CustomerLoginEvent
                                .VerifyCustomerClicked
                        )
                    }
                }

            ) {

                Text(

                    text =

                        if (
                            uiState.verified
                        ) {

                            "Save Password"

                        } else {

                            "Verify"
                        }
                )
            }
        },


        // ====================================================
        // CANCEL
        // ====================================================

        dismissButton = {

            TextButton(

                onClick = {

                    onEvent(
                        CustomerLoginEvent
                            .CloseForgotPassword
                    )
                }

            ) {

                Text(
                    text =
                        "Cancel"
                )
            }
        }
    )
}