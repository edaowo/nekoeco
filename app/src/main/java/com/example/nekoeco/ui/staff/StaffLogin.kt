package com.example.nekoeco.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// ============================================================
// STAFF ACCOUNT
// ============================================================

data class StaffAccount(

    val staffId: String,

    val name: String,

    var password: String
)


// ============================================================
// STAFF ACCOUNTS
// ============================================================

object StaffAccounts {

    val accounts =
        mutableListOf(

            StaffAccount(
                staffId = "ST001",
                name = "VQ",
                password = "122507"
            ),

            StaffAccount(
                staffId = "ST002",
                name = "XL",
                password = "123123"
            ),

            StaffAccount(
                staffId = "ST003",
                name = "PY",
                password = "271199"
            ),

            StaffAccount(
                staffId = "ST004",
                name = "EDA",
                password = "123456"
            )
        )


    fun findStaff(
        staffId: String
    ): StaffAccount? {

        return accounts.find {

            it.staffId.equals(
                staffId,
                ignoreCase = true
            )
        }
    }


    fun login(

        staffId: String,

        password: String

    ): Boolean {

        val staff =
            findStaff(
                staffId
            )


        return staff != null &&
                staff.password == password
    }


    fun resetPassword(

        staffId: String,

        newPassword: String

    ): Boolean {

        val staff =
            findStaff(
                staffId
            )


        if (staff != null) {

            staff.password =
                newPassword

            return true
        }


        return false
    }
}


// ============================================================
// UI STATE
// ============================================================

data class StaffLoginUiState(

    // ========================================================
    // LOGIN
    // ========================================================

    val staffId: String = "",

    val password: String = "",

    val passwordVisible: Boolean =
        false,

    val loginError: Boolean =
        false,

    val loginSuccess: Boolean =
        false,


    // ========================================================
    // FORGOT PASSWORD DIALOG
    // ========================================================

    val showForgotPassword: Boolean =
        false,

    val resetStaffId: String =
        "",

    val staffName: String =
        "",

    val newPassword: String =
        "",

    val confirmPassword: String =
        "",

    val verified: Boolean =
        false,

    val resetErrorMessage: String =
        "",

    val newPasswordVisible: Boolean =
        false,

    val confirmPasswordVisible: Boolean =
        false
)


// ============================================================
// EVENTS
// ============================================================

sealed interface StaffLoginEvent {


    // ========================================================
    // LOGIN EVENTS
    // ========================================================

    data class StaffIdChanged(
        val staffId: String
    ) : StaffLoginEvent


    data class PasswordChanged(
        val password: String
    ) : StaffLoginEvent


    data object TogglePasswordVisibility :
        StaffLoginEvent


    data object LoginClicked :
        StaffLoginEvent


    data object LoginHandled :
        StaffLoginEvent


    // ========================================================
    // FORGOT PASSWORD
    // ========================================================

    data object ForgotPasswordClicked :
        StaffLoginEvent


    data object CloseForgotPassword :
        StaffLoginEvent


    data class ResetStaffIdChanged(
        val staffId: String
    ) : StaffLoginEvent


    data class StaffNameChanged(
        val name: String
    ) : StaffLoginEvent


    data object VerifyStaffClicked :
        StaffLoginEvent


    // ========================================================
    // NEW PASSWORD
    // ========================================================

    data class NewPasswordChanged(
        val password: String
    ) : StaffLoginEvent


    data class ConfirmPasswordChanged(
        val password: String
    ) : StaffLoginEvent


    data object ToggleNewPasswordVisibility :
        StaffLoginEvent


    data object ToggleConfirmPasswordVisibility :
        StaffLoginEvent


    data object SavePasswordClicked :
        StaffLoginEvent
}


// ============================================================
// VIEWMODEL
// ============================================================

class StaffLoginViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            StaffLoginUiState()
        )


    val uiState: StateFlow<StaffLoginUiState> =
        _uiState.asStateFlow()


    fun onEvent(
        event: StaffLoginEvent
    ) {

        when (event) {


            // =================================================
            // STAFF ID
            // =================================================

            is StaffLoginEvent.StaffIdChanged -> {

                _uiState.update {

                    it.copy(
                        staffId =
                            event.staffId,

                        loginError =
                            false
                    )
                }
            }


            // =================================================
            // PASSWORD
            // =================================================

            is StaffLoginEvent.PasswordChanged -> {

                _uiState.update {

                    it.copy(
                        password =
                            event.password,

                        loginError =
                            false
                    )
                }
            }


            // =================================================
            // PASSWORD VISIBILITY
            // =================================================

            StaffLoginEvent.TogglePasswordVisibility -> {

                _uiState.update {

                    it.copy(
                        passwordVisible =
                            !it.passwordVisible
                    )
                }
            }


            // =================================================
            // LOGIN
            // =================================================

            StaffLoginEvent.LoginClicked -> {

                login()
            }


            StaffLoginEvent.LoginHandled -> {

                _uiState.update {

                    it.copy(
                        loginSuccess =
                            false
                    )
                }
            }


            // =================================================
            // FORGOT PASSWORD
            // =================================================

            StaffLoginEvent.ForgotPasswordClicked -> {

                _uiState.update {

                    it.copy(
                        showForgotPassword =
                            true
                    )
                }
            }


            StaffLoginEvent.CloseForgotPassword -> {

                closeForgotPassword()
            }


            // =================================================
            // RESET STAFF ID
            // =================================================

            is StaffLoginEvent.ResetStaffIdChanged -> {

                _uiState.update {

                    it.copy(
                        resetStaffId =
                            event.staffId,

                        resetErrorMessage =
                            ""
                    )
                }
            }


            // =================================================
            // STAFF NAME
            // =================================================

            is StaffLoginEvent.StaffNameChanged -> {

                _uiState.update {

                    it.copy(
                        staffName =
                            event.name,

                        resetErrorMessage =
                            ""
                    )
                }
            }


            // =================================================
            // VERIFY STAFF
            // =================================================

            StaffLoginEvent.VerifyStaffClicked -> {

                verifyStaff()
            }


            // =================================================
            // NEW PASSWORD
            // =================================================

            is StaffLoginEvent.NewPasswordChanged -> {

                _uiState.update {

                    it.copy(
                        newPassword =
                            event.password,

                        resetErrorMessage =
                            ""
                    )
                }
            }


            // =================================================
            // CONFIRM PASSWORD
            // =================================================

            is StaffLoginEvent.ConfirmPasswordChanged -> {

                _uiState.update {

                    it.copy(
                        confirmPassword =
                            event.password,

                        resetErrorMessage =
                            ""
                    )
                }
            }


            // =================================================
            // NEW PASSWORD VISIBILITY
            // =================================================

            StaffLoginEvent.ToggleNewPasswordVisibility -> {

                _uiState.update {

                    it.copy(
                        newPasswordVisible =
                            !it.newPasswordVisible
                    )
                }
            }


            // =================================================
            // CONFIRM PASSWORD VISIBILITY
            // =================================================

            StaffLoginEvent.ToggleConfirmPasswordVisibility -> {

                _uiState.update {

                    it.copy(
                        confirmPasswordVisible =
                            !it.confirmPasswordVisible
                    )
                }
            }


            // =================================================
            // SAVE PASSWORD
            // =================================================

            StaffLoginEvent.SavePasswordClicked -> {

                savePassword()
            }
        }
    }


    // ========================================================
    // LOGIN LOGIC
    // ========================================================

    private fun login() {

        val state =
            _uiState.value


        val success =
            StaffAccounts.login(

                staffId =
                    state.staffId.trim(),

                password =
                    state.password
            )


        if (success) {

            _uiState.update {

                it.copy(
                    loginError =
                        false,

                    loginSuccess =
                        true
                )
            }

        } else {

            _uiState.update {

                it.copy(
                    loginError =
                        true,

                    loginSuccess =
                        false
                )
            }
        }
    }


    // ========================================================
    // VERIFY STAFF
    // ========================================================

    private fun verifyStaff() {

        val state =
            _uiState.value


        val staff =
            StaffAccounts.findStaff(
                state.resetStaffId.trim()
            )


        if (
            staff != null &&
            staff.name.equals(
                state.staffName.trim(),
                ignoreCase = true
            )
        ) {

            _uiState.update {

                it.copy(
                    verified =
                        true,

                    resetErrorMessage =
                        ""
                )
            }

        } else {

            _uiState.update {

                it.copy(
                    resetErrorMessage =
                        "Staff ID or Staff Name is incorrect."
                )
            }
        }
    }


    // ========================================================
    // SAVE NEW PASSWORD
    // ========================================================

    private fun savePassword() {

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
                    StaffAccounts.resetPassword(

                        staffId =
                            state.resetStaffId.trim(),

                        newPassword =
                            state.newPassword
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

                showForgotPassword =
                    false,

                resetStaffId =
                    "",

                staffName =
                    "",

                newPassword =
                    "",

                confirmPassword =
                    "",

                verified =
                    false,

                resetErrorMessage =
                    "",

                newPasswordVisible =
                    false,

                confirmPasswordVisible =
                    false
            )
        }
    }
}


// ============================================================
// STAFF LOGIN SCREEN
// ============================================================

@Composable
fun StaffLoginScreen(

    onLoginSuccess: () -> Unit,

    onBackClick: () -> Unit,

    viewModel: StaffLoginViewModel =
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
                StaffLoginEvent.LoginHandled
            )
        }
    }


    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
    ) {


        // ====================================================
        // BACK
        // ====================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.Start,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick =
                    onBackClick
            ) {

                Icon(

                    imageVector =
                        Icons.AutoMirrored
                            .Filled
                            .ArrowBack,

                    contentDescription =
                        "Back"
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        // ====================================================
        // HEADER
        // ====================================================

        Column(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text =
                    "Staff Login",

                fontSize =
                    30.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(

                text =
                    "Welcome back, Staff",

                fontSize =
                    15.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }


        Spacer(
            modifier =
                Modifier.height(40.dp)
        )


        // ====================================================
        // STAFF ID
        // ====================================================

        OutlinedTextField(

            value =
                uiState.staffId,

            onValueChange = {

                viewModel.onEvent(
                    StaffLoginEvent
                        .StaffIdChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {

                Text(
                    text =
                        "Staff ID"
                )
            },

            placeholder = {

                Text(
                    text =
                        "Enter Staff ID"
                )
            },

            leadingIcon = {

                Icon(

                    imageVector =
                        Icons.Default.Person,

                    contentDescription =
                        "Staff ID"
                )
            },

            singleLine =
                true,

            isError =
                uiState.loginError
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // ====================================================
        // PASSWORD
        // ====================================================

        OutlinedTextField(

            value =
                uiState.password,

            onValueChange = {

                viewModel.onEvent(
                    StaffLoginEvent
                        .PasswordChanged(it)
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            label = {

                Text(
                    text =
                        "Password"
                )
            },

            placeholder = {

                Text(
                    text =
                        "Enter Password"
                )
            },

            leadingIcon = {

                Icon(

                    imageVector =
                        Icons.Default.Lock,

                    contentDescription =
                        "Password"
                )
            },

            trailingIcon = {

                IconButton(

                    onClick = {

                        viewModel.onEvent(
                            StaffLoginEvent
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

                                "Hide Password"

                            } else {

                                "Show Password"
                            }
                    )
                }
            },

            visualTransformation =

                if (
                    uiState.passwordVisible
                ) {

                    VisualTransformation.None

                } else {

                    PasswordVisualTransformation()
                },

            singleLine =
                true,

            isError =
                uiState.loginError
        )


        // ====================================================
        // LOGIN ERROR
        // ====================================================

        if (
            uiState.loginError
        ) {

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(

                text =
                    "Invalid Staff ID or Password",

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
                Modifier.height(24.dp)
        )


        // ====================================================
        // LOGIN BUTTON
        // ====================================================

        Button(

            onClick = {

                viewModel.onEvent(
                    StaffLoginEvent
                        .LoginClicked
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(55.dp),

            shape =
                RoundedCornerShape(16.dp)

        ) {

            Text(

                text =
                    "LOGIN",

                fontSize =
                    16.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // ====================================================
        // FORGOT PASSWORD
        // ====================================================

        TextButton(

            onClick = {

                viewModel.onEvent(
                    StaffLoginEvent
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
// FORGOT PASSWORD DIALOG
// ============================================================

@Composable
fun ForgotPasswordDialog(

    uiState: StaffLoginUiState,

    onEvent: (StaffLoginEvent) -> Unit

) {

    AlertDialog(

        onDismissRequest = {

            onEvent(
                StaffLoginEvent
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
                            "Enter your Staff ID and Staff Name to verify your account.",

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
                            uiState.resetStaffId,

                        onValueChange = {

                            onEvent(
                                StaffLoginEvent
                                    .ResetStaffIdChanged(
                                        it
                                    )
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "Staff ID"
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
                            uiState.staffName,

                        onValueChange = {

                            onEvent(
                                StaffLoginEvent
                                    .StaffNameChanged(
                                        it
                                    )
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text =
                                    "Staff Name"
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
                                StaffLoginEvent
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
                                        StaffLoginEvent
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


                    // =================================================
                    // CONFIRM PASSWORD
                    // =================================================

                    OutlinedTextField(

                        value =
                            uiState.confirmPassword,

                        onValueChange = {

                            onEvent(
                                StaffLoginEvent
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
                                        StaffLoginEvent
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
                            StaffLoginEvent
                                .SavePasswordClicked
                        )

                    } else {

                        onEvent(
                            StaffLoginEvent
                                .VerifyStaffClicked
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
                        StaffLoginEvent
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