package com.example.nekoeco.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nekoeco.R


// ============================================================
// CUSTOMER PROFILE SCREEN
//
// Full-screen profile: account details from sign-up
// (name is editable, email is read-only) and a logout
// card at the bottom — same design as the old home
// logout card. Logout is a two-step (details → confirm).
// ============================================================

@Composable
fun CustomerProfileScreen(

    onBack: () -> Unit,

    onLogout: () -> Unit

) {

    val customer =
        CustomerDataStore.currentCustomer


    var showLogoutConfirm by remember {
        mutableStateOf(false)
    }


    var showEditName by remember {
        mutableStateOf(false)
    }


    var nameInput by remember {
        mutableStateOf(customer?.name ?: "")
    }


    var showEditEmail by remember {
        mutableStateOf(false)
    }


    var emailInput by remember {
        mutableStateOf(customer?.email ?: "")
    }


    // Local display values — updated immediately on
    // save (currentCustomer is not a snapshot state,
    // so it wouldn't recompose this screen on its own).
    var displayName by remember {
        mutableStateOf(customer?.name ?: "Customer")
    }


    var displayEmail by remember {
        mutableStateOf(customer?.email ?: "")
    }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
    ) {


        // ====================================================
        // HEADER
        // ====================================================

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 6.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick =
                    onBack
            ) {

                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,

                    contentDescription =
                        "Back"
                )
            }


            Text(
                text =
                    "Profile",

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )
        }


        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 20.dp
                    )
        ) {


            // ================================================
            // AVATAR
            // ================================================

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            Box(
                modifier =
                    Modifier.fillMaxWidth(),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(110.dp)
                            .clip(
                                CircleShape
                            )
                ) {

                    Image(
                        painter =
                            painterResource(
                                R.drawable.profile_customer
                            ),

                        contentDescription =
                            "Profile picture",

                        contentScale =
                            ContentScale.Crop,

                        modifier =
                            Modifier.fillMaxSize()
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )


            // ================================================
            // DETAILS
            // ================================================

            Card(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {


                    // ========================================
                    // NAME (editable)
                    // ========================================

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "Name",

                                fontSize =
                                    12.sp,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )


                        Text(
                            text =
                                displayName,

                            fontSize =
                                15.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )
                        }


                        TextButton(
                            onClick = {

                                nameInput =
                                    customer?.name ?: ""

                                showEditName =
                                    true
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Edit,

                                contentDescription =
                                    "Edit name",

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,

                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    // ========================================
                    // EMAIL (editable)
                    // ========================================

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "Email",

                                fontSize =
                                    12.sp,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )


                            Text(
                                text =
                                    displayEmail,

                                fontSize =
                                    15.sp,

                                fontWeight =
                                    FontWeight.SemiBold,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                            )
                        }


                        TextButton(
                            onClick = {

                                emailInput =
                                    displayEmail

                                showEditEmail =
                                    true
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Edit,

                                contentDescription =
                                    "Edit email",

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,

                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.weight(1f)
            )


            // ================================================
            // LOGOUT CARD
            //
            // Same design as the old home logout card.
            // ================================================

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {

                            showLogoutConfirm =
                                true
                        },

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            1.dp
                    )
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Logout,

                        contentDescription =
                            "Logout",

                        modifier =
                            Modifier.size(27.dp),

                        tint =
                            MaterialTheme
                                .colorScheme
                                .error
                    )


                    Spacer(
                        modifier =
                            Modifier.width(15.dp)
                    )


                    Column {

                        Text(
                            text =
                                "Log out",

                            fontSize =
                                16.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )


                        Spacer(
                            modifier =
                                Modifier.height(2.dp)
                        )


                        Text(
                            text =
                                "Sign out of your account",

                            fontSize =
                                13.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )
        }
    }


    // ========================================================
    // EDIT NAME DIALOG
    // ========================================================

    if (showEditName) {

        AlertDialog(
            onDismissRequest = {
                showEditName = false
            },

            title = {

                Text(
                    text =
                        "Edit Name",

                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                OutlinedTextField(
                    value =
                        nameInput,

                    onValueChange = {
                        nameInput = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text("Name")
                    },

                    singleLine =
                        true
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        CustomerDataStore
                            .updateCustomerName(
                                newName =
                                    nameInput
                            )

                        displayName =
                            CustomerDataStore
                                .currentCustomer
                                ?.name
                                ?: nameInput.trim()

                        showEditName =
                            false
                    }
                ) {

                    Text(
                        text =
                            "Save",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showEditName = false
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


    // ========================================================
    // EDIT EMAIL DIALOG
    // ========================================================

    if (showEditEmail) {

        AlertDialog(
            onDismissRequest = {
                showEditEmail = false
            },

            title = {

                Text(
                    text =
                        "Edit Email",

                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                OutlinedTextField(
                    value =
                        emailInput,

                    onValueChange = {
                        emailInput = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text("Email")
                    },

                    singleLine =
                        true
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        CustomerDataStore
                            .updateCustomerEmail(
                                newEmail =
                                    emailInput
                            )

                        displayEmail =
                            CustomerDataStore
                                .currentCustomer
                                ?.email
                                ?: displayEmail

                        showEditEmail =
                            false
                    }
                ) {

                    Text(
                        text =
                            "Save",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showEditEmail = false
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


    // ========================================================
    // LOGOUT CONFIRMATION
    // ========================================================

    if (showLogoutConfirm) {

        AlertDialog(
            onDismissRequest = {
                showLogoutConfirm = false
            },

            title = {

                Text(
                    text =
                        "Log out?",

                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        "You will need to sign in again to access your account."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showLogoutConfirm =
                            false

                        onLogout()
                    }
                ) {

                    Text(
                        text =
                            "Log Out",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showLogoutConfirm = false
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
}
