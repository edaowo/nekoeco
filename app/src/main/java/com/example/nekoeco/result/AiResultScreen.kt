package com.example.nekoeco.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CalendarMonth

import com.example.nekoeco.ai.AiScanResponse
import com.example.nekoeco.ai.SellablePart
import com.example.nekoeco.ai.confidenceLabel
import com.example.nekoeco.camera.BottomNavAction
import com.example.nekoeco.camera.BottomNavBar

import com.example.nekoeco.ui.theme.EcoDarkGreen
import com.example.nekoeco.ui.theme.EcoGray
import com.example.nekoeco.ui.theme.EcoGreen
import com.example.nekoeco.ui.theme.EcoLightGreen
import com.example.nekoeco.ui.theme.EcoLightOrange
import com.example.nekoeco.ui.theme.EcoLightPurple
import com.example.nekoeco.ui.theme.EcoOrange
import com.example.nekoeco.ui.theme.EcoPurple
import com.example.nekoeco.ui.theme.EcoWhite


// ============================================================
// AI RESULT SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiResultScreen(

    result: AiScanResponse,

    isCustomer: Boolean,

    onBack: () -> Unit,

    onBookPickup: () -> Unit,

    bottomNavActions: List<BottomNavAction>

) {

    Scaffold(

        // ====================================================
        // TOP BAR
        // ====================================================

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text = "Analysis Results",

                            color =
                                EcoWhite,

                            fontWeight =
                                FontWeight.Bold,

                            fontSize = 19.sp
                        )

                        Text(
                            text =
                                "AI-powered device analysis",

                            color =
                                EcoWhite.copy(
                                    alpha = 0.75f
                                ),

                            fontSize = 11.sp
                        )
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,

                            contentDescription =
                                "Back",

                            tint =
                                EcoWhite
                        )
                    }
                },

                colors =
                    TopAppBarDefaults
                        .topAppBarColors(

                            containerColor =
                                EcoDarkGreen
                        )
            )
        },

        // ====================================================
        // BOTTOM NAVIGATION
        // ====================================================

        bottomBar = {

            if (
                bottomNavActions.isNotEmpty()
            ) {

                BottomNavBar(
                    actions =
                        bottomNavActions
                )
            }
        }

    ) { paddingValues ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme
                            .colorScheme
                            .background
                    )
                    .padding(
                        paddingValues
                    )
        ) {

            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 18.dp
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                // =================================================
                // TOP SUCCESS CARD
                // =================================================

                item {

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    AiSuccessCard(

                        deviceName =
                            result.device.device
                    )
                }


                // =================================================
                // ANALYSIS SUMMARY
                // =================================================

                item {

                    SectionTitle(

                        title =
                            "Analysis Summary",

                        icon =
                            Icons.Default.AutoAwesome
                    )
                }


                item {

                    SummaryGrid(
                        result =
                            result
                    )
                }


                // =================================================
                // COMPONENT BREAKDOWN
                // =================================================

                item {

                    SectionTitle(

                        title =
                            "Component Breakdown",

                        icon =
                            Icons.Default.Devices
                    )
                }


                items(
                    items =
                        result.parts
                ) { part ->

                    ComponentCard(
                        part =
                            part
                    )
                }


                // =================================================
                // RECYCLING TIPS
                // =================================================

                if (
                    result.tips.isNotEmpty()
                ) {

                    item {

                        SectionTitle(

                            title =
                                "Recycling Tips",

                            icon =
                                Icons.Default.TipsAndUpdates
                        )
                    }


                    item {

                        RecyclingTipsCard(

                            tips =
                                result.tips
                        )
                    }
                }


                // =================================================
                // SAVE DEVICE
                //
                // Booking is only offered for electronic /
                // electrical devices — a non-electrical object
                // cannot be booked for pickup.
                // =================================================

                if (isCustomer && result.device.isElectronic) {

                    item {

                        Spacer(
                            modifier =
                                Modifier.height(2.dp)
                        )

                        Button(

                            onClick =
                                onBookPickup,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),

                            shape =
                                RoundedCornerShape(16.dp),

                            colors =
                                ButtonDefaults
                                    .buttonColors(

                                        containerColor =
                                            EcoDarkGreen
                                    )
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.CalendarMonth,

                                contentDescription =
                                    null
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(

                                text =
                                    "Book Pickup",

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize =
                                    15.sp
                            )
                        }
                    }
                }


                if (isCustomer && !result.device.isElectronic) {

                    item {

                        Spacer(
                            modifier =
                                Modifier.height(2.dp)
                        )

                        Text(

                            text =
                                "This is not an electrical device, so it cannot be booked for a pickup.",

                            fontSize =
                                13.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }
                }


                item {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )
                }
            }
        }
    }
}


// ============================================================
// AI SUCCESS CARD
// ============================================================

@Composable
private fun AiSuccessCard(

    deviceName: String

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    EcoLightGreen
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

            Box(

                modifier =
                    Modifier
                        .size(48.dp)
                        .background(

                            color =
                                EcoWhite.copy(
                                    alpha = 0.8f
                                ),

                            shape =
                                RoundedCornerShape(14.dp)
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    imageVector =
                        Icons.Default.CalendarMonth,

                    contentDescription =
                        null,

                    tint =
                        EcoGreen,

                    modifier =
                        Modifier.size(28.dp)
                )
            }


            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        "Analysis Complete",

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        17.sp,

                    color =
                        EcoDarkGreen
                )


                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )


                Text(

                    text =
                        "AI successfully analyzed your device.",

                    fontSize =
                        12.sp,

                    color =
                        EcoDarkGreen.copy(
                            alpha = 0.75f
                        )
                )


                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )


                Text(

                    text =
                        deviceName,

                    fontWeight =
                        FontWeight.SemiBold,

                    fontSize =
                        13.sp,

                    color =
                        EcoDarkGreen
                )
            }
        }
    }
}


// ============================================================
// SECTION TITLE
// ============================================================

@Composable
private fun SectionTitle(

    title: String,

    icon:
    androidx.compose.ui.graphics.vector.ImageVector

) {

    Row(

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(

            imageVector =
                icon,

            contentDescription =
                null,

            tint =
                EcoDarkGreen,

            modifier =
                Modifier.size(20.dp)
        )


        Spacer(
            modifier =
                Modifier.width(8.dp)
        )


        Text(

            text =
                title,

            fontWeight =
                FontWeight.Bold,

            fontSize =
                17.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )
    }
}


// ============================================================
// SUMMARY GRID
// ============================================================

@Composable
private fun SummaryGrid(

    result: AiScanResponse

) {

    Column(

        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            SummaryCard(

                value =
                    result.device.recyclability,

                label =
                    "Recyclability",

                background =
                    EcoLightGreen,

                valueColor =
                    EcoGreen,

                modifier =
                    Modifier.weight(1f)
            )


            SummaryCard(

                value =
                    "${result.parts.size}",

                label =
                    "Total Parts",

                background =
                    EcoLightPurple,

                valueColor =
                    EcoPurple,

                modifier =
                    Modifier.weight(1f)
            )
        }


        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            SummaryCard(

                value =
                    "%.2f kg"
                        .format(
                            result.totalWeight
                        ),

                label =
                    "Estimated Weight",

                background =
                    EcoLightGreen,

                valueColor =
                    EcoGreen,

                modifier =
                    Modifier.weight(1f)
            )


            SummaryCard(

                value =
                    "RM %.2f"
                        .format(
                            result.totalRecoveryValue
                        ),

                label =
                    "Recovery Value",

                background =
                    EcoLightOrange,

                valueColor =
                    EcoOrange,

                modifier =
                    Modifier.weight(1f)
            )
        }
    }
}


// ============================================================
// SUMMARY CARD
// ============================================================

@Composable
private fun SummaryCard(

    value: String,

    label: String,

    background: Color,

    valueColor: Color,

    modifier: Modifier

) {

    Card(

        modifier =
            modifier.height(92.dp),

        shape =
            RoundedCornerShape(17.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    background
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(14.dp),

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(

                text =
                    value,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    valueColor,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )


            Text(

                text =
                    label,

                fontSize =
                    12.sp,

                color =
                    EcoGray
            )
        }
    }
}


// ============================================================
// COMPONENT CARD
// ============================================================

@Composable
private fun ComponentCard(

    part: SellablePart

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(17.dp),

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

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
        ) {

            Row(

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(

                    modifier =
                        Modifier
                            .size(42.dp)
                            .background(

                                EcoLightPurple,

                                RoundedCornerShape(12.dp)
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Devices,

                        contentDescription =
                            null,

                        tint =
                            EcoPurple,

                        modifier =
                            Modifier.size(22.dp)
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )


                Column(

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(

                        text =
                            part.name,

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.Bold,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )


                    if (
                        part.weight > 0
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )


                        Text(

                            text =
                                "%.2f kg"
                                    .format(
                                        part.weight
                                    ),

                            fontSize =
                                12.sp,

                            color =
                                EcoGray
                        )
                    }
                }


                Card(

                    shape =
                        RoundedCornerShape(9.dp),

                    colors =
                        CardDefaults.cardColors(

                            containerColor =
                                EcoLightPurple
                        )
                ) {

                    Text(

                        text =
                            confidenceLabel(
                                part.confidence
                            ),

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            EcoPurple,

                        modifier =
                            Modifier.padding(

                                horizontal =
                                    8.dp,

                                vertical =
                                    5.dp
                            )
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Eco,

                        contentDescription =
                            null,

                        tint =
                            EcoGreen,

                        modifier =
                            Modifier.size(17.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.width(5.dp)
                    )


                    Text(

                        text =
                            "Recoverable component",

                        fontSize =
                            11.sp,

                        color =
                            EcoGray
                    )
                }


                Text(

                    text =
                        "RM %.2f"
                            .format(
                                part.price
                            ),

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        EcoDarkGreen
                )
            }
        }
    }
}


// ============================================================
// RECYCLING TIPS
// ============================================================

@Composable
private fun RecyclingTipsCard(

    tips: List<String>

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(17.dp),

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
                Modifier.padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            tips.forEach { tip ->

                Row(

                    verticalAlignment =
                        Alignment.Top
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(28.dp)
                                .background(

                                    EcoLightGreen,

                                    RoundedCornerShape(8.dp)
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.TipsAndUpdates,

                            contentDescription =
                                null,

                            tint =
                                EcoGreen,

                            modifier =
                                Modifier.size(16.dp)
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )


                    Text(

                        text =
                            tip,

                        modifier =
                            Modifier.weight(1f),

                        fontSize =
                            12.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface,

                        lineHeight =
                            18.sp
                    )
                }
            }
        }
    }
}