package com.example.nekoeco.ui.role

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nekoeco.ui.theme.EcoGreen
import com.example.nekoeco.ui.theme.White

@Composable
fun RoleSelectionScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onCustomerClick: () -> Unit,
    onStaffClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp)
    ) {


        // =============================
        // TOP BAR
        // =============================

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "NekoEco",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EcoGreen
            )


            // DARK MODE BUTTON

            IconButton(
                onClick = {

                    onDarkModeChange(
                        !darkMode
                    )
                }
            ) {

                Icon(

                    imageVector =
                        if (darkMode) {

                            Icons.Default.LightMode

                        } else {

                            Icons.Default.DarkMode
                        },

                    contentDescription =
                        "Toggle Dark Mode"
                )
            }
        }


        Spacer(
            modifier = Modifier.height(70.dp)
        )


        // =============================
        // WELCOME TEXT
        // =============================

        Column(
            modifier = Modifier.fillMaxWidth(),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "Welcome!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Choose how you want to continue",
                fontSize = 15.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }


        Spacer(
            modifier = Modifier.height(40.dp)
        )


        // =============================
        // CUSTOMER
        // =============================

        RoleCard(

            title = "Customer",

            subtitle =
                "Continue as Customer",

            icon = {

                Icon(

                    imageVector =
                        Icons.Default.Person,

                    contentDescription =
                        "Customer",

                    tint = White
                )
            },

            onClick =
                onCustomerClick
        )


        Spacer(
            modifier = Modifier.height(18.dp)
        )


        // =============================
        // STAFF
        // =============================

        RoleCard(

            title = "Staff",

            subtitle =
                "Manage devices and appointments",

            icon = {

                Icon(

                    imageVector =
                        Icons.Default.Security,

                    contentDescription =
                        "Staff",

                    tint = White
                )
            },

            onClick =
                onStaffClick
        )
    }
}


// =====================================================
// ROLE CARD
// =====================================================

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {

                onClick()
            },

        shape =
            RoundedCornerShape(22.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
    ) {

        Row(

            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            // ICON

            icon()


            Spacer(
                modifier = Modifier.size(16.dp)
            )


            // TEXT

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text = title,

                    color = White,

                    fontSize = 19.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(

                    text = subtitle,

                    color =
                        White.copy(
                            alpha = 0.85f
                        ),

                    fontSize = 13.sp
                )
            }


            // ARROW

            Icon(

                imageVector =
                    Icons.AutoMirrored.Filled.ArrowBack,

                contentDescription =
                    "Continue",

                tint = White
            )
        }
    }
}