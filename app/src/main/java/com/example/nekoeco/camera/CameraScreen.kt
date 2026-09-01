package com.example.nekoeco.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat

import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.nekoeco.ai.AiScanResponse
import com.example.nekoeco.ai.GeminiRepository
import com.example.nekoeco.ai.RetrofitClient
import com.example.nekoeco.viewmodel.ScanViewModel
import com.example.nekoeco.viewmodel.ScanViewModelFactory

import java.io.File


// ==========================================================
// BOTTOM NAVIGATION ACTION
// ==========================================================

data class BottomNavAction(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val selected: Boolean = false,
    val onClick: () -> Unit = {}
)


// ==========================================================
// CAMERA SCREEN
// ==========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit = {},
    onResult: (AiScanResponse) -> Unit,
    bottomNavActions: List<BottomNavAction> = emptyList()
) {

    val context = LocalContext.current

    // ======================================================
    // VIEW MODEL
    // ======================================================

    val scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModelFactory(
            GeminiRepository(
                RetrofitClient.apiService
            )
        )
    )

    val result by scanViewModel.result.collectAsState()
    val isLoading by scanViewModel.isLoading.collectAsState()
    val error by scanViewModel.error.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current


    // ======================================================
    // CAMERA PERMISSION
    // ======================================================

    var hasCameraPermission by remember {

        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }


    // ======================================================
    // CAMERA STATES
    // ======================================================

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var capturedFile by remember {
        mutableStateOf<File?>(null)
    }

    var capturedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }


    // ======================================================
    // CAMERA PERMISSION LAUNCHER
    // ======================================================

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasCameraPermission = granted
        }


    // ======================================================
    // CAMERA SCREEN START
    // ======================================================
    //
    // IMPORTANT:
    // Clear the previous AI result every time the Camera
    // screen is opened.
    //
    // This prevents:
    //
    // Camera
    //   ↓
    // Result
    //   ↓
    // Back
    //   ↓
    // Home
    //   ↓
    // Camera
    //   ↓
    // OLD RESULT
    //
    // ======================================================

    LaunchedEffect(Unit) {

        // Clear previous scan result
        scanViewModel.clearResult()

        // Clear previous captured image state
        capturedFile = null
        capturedImageUri = null


        // Request camera permission if needed
        if (!hasCameraPermission) {

            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }


    // ======================================================
    // AI RESULT
    // ======================================================
    //
    // When a NEW scan finishes:
    //
    // result != null
    //      ↓
    // Navigate to Result
    //      ↓
    // Immediately clear the stored result
    //
    // This prevents the same result from being triggered
    // again when CameraScreen is opened later.
    //
    // ======================================================

    LaunchedEffect(result) {

        val scanResult = result

        if (scanResult != null) {

            // Navigate to Result screen
            onResult(scanResult)

            // Clear old result after navigation
            scanViewModel.clearResult()
        }
    }


    // ======================================================
    // SCREEN
    // ======================================================

    Scaffold(

        // ==================================================
        // EDGE-TO-EDGE CONTENT
        //
        // No system-bar insets — the camera preview must
        // fill the whole screen in every orientation
        // (otherwise the scaffold colour shows as a strip
        // beside the cutout in landscape).
        // ==================================================

        contentWindowInsets =
            WindowInsets(0.dp),

        // ==================================================
        // TOP BAR
        // ==================================================

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text(
                        text = "AI Scan Item"
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = {

                            // Clear everything before leaving
                            scanViewModel.clearResult()

                            capturedFile = null
                            capturedImageUri = null

                            // Go back
                            onBack()
                        }
                    ) {

                        Text(
                            text = "←",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(

                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .primary,

                        titleContentColor =
                            Color.White,

                        navigationIconContentColor =
                            Color.White
                    )
            )
        },


        // ==================================================
        // BOTTOM NAVIGATION
        // ==================================================

        bottomBar = {

            if (bottomNavActions.isNotEmpty()) {

                BottomNavBar(
                    actions = bottomNavActions
                )
            }
        }

    ) { paddingValues ->


        // ==================================================
        // CAMERA PERMISSION NOT GRANTED
        // ==================================================

        if (!hasCameraPermission) {

            Box(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(
                    text =
                        "Camera permission is required"
                )
            }

            return@Scaffold
        }


        // ==================================================
        // CAMERA CONTAINER
        // ==================================================

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
        ) {


            // ==================================================
            // CAMERA PREVIEW
            // ==================================================

            AndroidView(

                modifier =
                    Modifier.fillMaxSize(),

                factory = { ctx ->

                    val previewView =
                        PreviewView(ctx)

                    val cameraProviderFuture =
                        ProcessCameraProvider
                            .getInstance(ctx)


                    cameraProviderFuture.addListener({

                        val cameraProvider =
                            cameraProviderFuture.get()


                        // --------------------------------------
                        // PREVIEW
                        // --------------------------------------

                        val preview =
                            Preview.Builder()
                                .build()
                                .also {

                                    it.surfaceProvider =
                                        previewView
                                            .surfaceProvider
                                }


                        // --------------------------------------
                        // IMAGE CAPTURE
                        // --------------------------------------

                        val imageCaptureUseCase =
                            ImageCapture.Builder()
                                .setCaptureMode(
                                    ImageCapture
                                        .CAPTURE_MODE_MINIMIZE_LATENCY
                                )
                                .build()


                        imageCapture =
                            imageCaptureUseCase


                        // --------------------------------------
                        // BIND CAMERA
                        // --------------------------------------

                        try {

                            cameraProvider.unbindAll()

                            cameraProvider.bindToLifecycle(

                                lifecycleOwner,

                                CameraSelector
                                    .DEFAULT_BACK_CAMERA,

                                preview,

                                imageCaptureUseCase
                            )

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }

                    }, ContextCompat.getMainExecutor(ctx))


                    previewView
                }
            )


            // ==================================================
            // CAMERA BUTTONS
            // ==================================================

            Row(

                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(
                            bottom = 24.dp,
                            start = 32.dp,
                            end = 32.dp
                        ),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {


                // ==========================================
                // RETAKE / CLEAR BUTTON
                // ==========================================

                CaptureSideButton(

                    symbol = "✕",

                    enabled =
                        capturedFile != null,

                    onClick = {

                        capturedFile = null
                        capturedImageUri = null
                    }
                )


                // ==========================================
                // CAPTURE BUTTON
                // ==========================================

                FloatingActionButton(

                    onClick = {

                        val capture =
                            imageCapture
                                ?: return@FloatingActionButton


                        val fileName =
                            "NekoEco_${System.currentTimeMillis()}.jpg"


                        val photoFile =
                            File(
                                context.cacheDir,
                                fileName
                            )


                        val outputOptions =
                            ImageCapture
                                .OutputFileOptions
                                .Builder(
                                    photoFile
                                )
                                .build()


                        capture.takePicture(

                            outputOptions,

                            ContextCompat
                                .getMainExecutor(
                                    context
                                ),

                            object :
                                ImageCapture
                                .OnImageSavedCallback {


                                // ==========================
                                // PHOTO SAVED
                                // ==========================

                                override fun onImageSaved(
                                    outputFileResults:
                                    ImageCapture
                                    .OutputFileResults
                                ) {

                                    capturedFile =
                                        photoFile

                                    capturedImageUri =
                                        Uri.fromFile(
                                            photoFile
                                        )
                                }


                                // ==========================
                                // ERROR
                                // ==========================

                                override fun onError(
                                    exception:
                                    ImageCaptureException
                                ) {

                                    exception
                                        .printStackTrace()
                                }
                            }
                        )
                    },

                    shape =
                        CircleShape,

                    containerColor =
                        Color.White,

                    contentColor =
                        Color.Black

                ) {

                    Text(
                        text = "●"
                    )
                }


                // ==========================================
                // ANALYZE BUTTON
                // ==========================================

                CaptureSideButton(

                    symbol = "✓",

                    enabled =
                        capturedFile != null &&
                                !isLoading,

                    onClick = {

                        capturedFile?.let { file ->

                            scanViewModel.scanImage(
                                file
                            )
                        }
                    }
                )
            }


            // ==================================================
            // LOADING
            // ==================================================

            if (isLoading) {

                Text(

                    text =
                        "AI is analyzing...",

                    modifier =
                        Modifier
                            .align(
                                Alignment.Center
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = 0.65f
                                )
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),

                    color =
                        Color.White
                )
            }


            // ==================================================
            // ERROR
            // ==================================================

            error?.let { message ->

                Text(

                    text =
                        "Error: $message",

                    modifier =
                        Modifier
                            .align(
                                Alignment.Center
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = 0.65f
                                )
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),

                    color =
                        Color.White
                )
            }


            // ==================================================
            // PHOTO CAPTURED MESSAGE
            // ==================================================

            if (
                capturedImageUri != null &&
                !isLoading
            ) {

                Text(

                    text =
                        "Photo captured — tap ✓ to analyze",

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopCenter
                            )
                            .padding(
                                top = 16.dp
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = 0.65f
                                )
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),

                    color =
                        Color.White
                )
            }
        }
    }
}


// ==========================================================
// CAMERA SIDE BUTTON
// ==========================================================

@Composable
private fun CaptureSideButton(

    symbol: String,

    enabled: Boolean,

    onClick: () -> Unit

) {

    FloatingActionButton(

        onClick = {

            if (enabled) {
                onClick()
            }
        },

        modifier =
            Modifier.size(48.dp),

        containerColor =
            if (enabled) {

                Color.White

            } else {

                Color.White.copy(
                    alpha = 0.4f
                )
            },

        contentColor =
            Color.Black

    ) {

        Text(
            text = symbol
        )
    }
}


// ==========================================================
// REAL BOTTOM NAVIGATION BAR
// ==========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    actions: List<BottomNavAction>
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {

        actions.forEach { action ->

            NavigationBarItem(
                selected = action.selected,
                onClick = action.onClick,

                icon = {
                    Icon(
                        imageVector = if (action.selected) {
                            action.selectedIcon
                        } else {
                            action.icon
                        },
                        contentDescription = action.label,
                        modifier = Modifier.size(28.dp)
                    )
                },

                label = {
                    Text(
                        text = action.label,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor =
                        MaterialTheme.colorScheme.onPrimary,

                    selectedTextColor =
                        MaterialTheme.colorScheme.primary,

                    indicatorColor =
                        MaterialTheme.colorScheme.primary,

                    unselectedIconColor =
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.6f
                        ),

                    unselectedTextColor =
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.6f
                        )
                )
            )
        }
    }
}