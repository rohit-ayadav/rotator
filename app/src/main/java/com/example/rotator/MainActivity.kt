package com.example.rotator

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rotator.core.ControllerState
import com.example.rotator.core.OrientationMode
import com.example.rotator.core.PreferencesManager
import com.example.rotator.core.ServiceState
import com.example.rotator.databinding.ActivityMainBinding
import com.example.rotator.service.RotationService
import com.example.rotator.util.PermissionManager
import kotlinx.coroutines.launch

/**
 * Main dashboard activity providing control over orientation enforcement,
 * floating overlay visibility, and permission status onboarding.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updatePermissionViews()
        if (!isGranted) {
            Toast.makeText(
                this,
                "Notification permission is needed for background controls.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupListeners()
        observeControllerState()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionViews()
    }

    private fun setupListeners() {
        // Start Controller
        binding.btnStartService.setOnClickListener {
            try {
                if (!PermissionManager.hasOverlayPermission(this)) {
                    Toast.makeText(
                        this,
                        "Overlay permission is required. Please enable 'Display over other apps'.",
                        Toast.LENGTH_LONG
                    ).show()
                    PermissionManager.requestOverlayPermission(this)
                    return@setOnClickListener
                }
                RotationService.startService(this, preferencesManager.lastOrientationMode)
            } catch (e: Exception) {
                Toast.makeText(this, "Could not start service: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Pause / Resume Toggle
        binding.btnPauseResumeService.setOnClickListener {
            try {
                val currentState = RotationService.controllerState.value
                if (currentState.isPaused) {
                    RotationService.sendAction(this, RotationService.ACTION_RESUME)
                } else {
                    RotationService.sendAction(this, RotationService.ACTION_PAUSE)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Action failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Stop Controller
        binding.btnStopService.setOnClickListener {
            try {
                RotationService.sendAction(this, RotationService.ACTION_STOP)
            } catch (e: Exception) {
                Toast.makeText(this, "Stop failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Toggle Floating Panel
        binding.btnTogglePanel.setOnClickListener {
            try {
                if (!PermissionManager.hasOverlayPermission(this)) {
                    PermissionManager.requestOverlayPermission(this)
                    return@setOnClickListener
                }
                RotationService.sendAction(this, RotationService.ACTION_TOGGLE_PANEL)
            } catch (e: Exception) {
                Toast.makeText(this, "Toggle failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 5 Orientation Buttons
        binding.btnPortrait.setOnClickListener {
            handleOrientationSelection(OrientationMode.PORTRAIT)
        }
        binding.btnReversePortrait.setOnClickListener {
            handleOrientationSelection(OrientationMode.REVERSE_PORTRAIT)
        }
        binding.btnLandscapeLeft.setOnClickListener {
            handleOrientationSelection(OrientationMode.LANDSCAPE_LEFT)
        }
        binding.btnLandscapeRight.setOnClickListener {
            handleOrientationSelection(OrientationMode.LANDSCAPE_RIGHT)
        }
        binding.btnAutoRotate.setOnClickListener {
            handleOrientationSelection(OrientationMode.AUTO_ROTATE)
        }

        // Permission Grant Buttons
        binding.btnGrantOverlay.setOnClickListener {
            PermissionManager.requestOverlayPermission(this)
        }

        binding.btnGrantNotif.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Start on Boot Preference Switch
        binding.switchStartOnBoot.isChecked = preferencesManager.startOnBoot
        binding.switchStartOnBoot.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.startOnBoot = isChecked
        }
    }

    private fun handleOrientationSelection(mode: OrientationMode) {
        try {
            preferencesManager.lastOrientationMode = mode
            val currentState = RotationService.controllerState.value

            if (currentState.serviceState == ServiceState.STOPPED) {
                if (!PermissionManager.hasOverlayPermission(this)) {
                    Toast.makeText(
                        this,
                        "Overlay permission is required. Please enable 'Display over other apps'.",
                        Toast.LENGTH_LONG
                    ).show()
                    PermissionManager.requestOverlayPermission(this)
                    return
                }
                RotationService.startService(this, mode)
            } else {
                RotationService.sendAction(this, RotationService.ACTION_SET_MODE, mode)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Selection failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeControllerState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RotationService.controllerState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: ControllerState) {
        // Status Badge & Controls
        when (state.serviceState) {
            ServiceState.STOPPED -> {
                binding.tvStatusBadge.text = getString(R.string.status_stopped)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary))
                binding.btnStartService.isEnabled = true
                binding.btnPauseResumeService.isEnabled = false
                binding.btnStopService.isEnabled = false
                binding.btnTogglePanel.isEnabled = false
            }

            ServiceState.RUNNING -> {
                if (state.isPanelHidden) {
                    binding.tvStatusBadge.text = getString(R.string.status_panel_hidden)
                } else {
                    binding.tvStatusBadge.text = getString(R.string.status_running)
                }
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success))
                binding.btnStartService.isEnabled = false
                binding.btnPauseResumeService.isEnabled = true
                binding.btnStopService.isEnabled = true
                binding.btnTogglePanel.isEnabled = true
                binding.btnPauseResumeService.text = getString(R.string.action_pause)
                binding.btnPauseResumeService.setIconResource(R.drawable.ic_pause)
            }

            ServiceState.PAUSED -> {
                binding.tvStatusBadge.text = getString(R.string.status_paused)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.warning))
                binding.btnStartService.isEnabled = false
                binding.btnPauseResumeService.isEnabled = true
                binding.btnStopService.isEnabled = true
                binding.btnTogglePanel.isEnabled = true
                binding.btnPauseResumeService.text = getString(R.string.action_resume)
                binding.btnPauseResumeService.setIconResource(R.drawable.ic_play)
            }
        }

        // Panel Toggle Text & Icon
        if (state.isPanelHidden) {
            binding.btnTogglePanel.text = getString(R.string.action_show_panel)
            binding.btnTogglePanel.setIconResource(R.drawable.ic_visibility)
        } else {
            binding.btnTogglePanel.text = getString(R.string.action_hide_panel)
            binding.btnTogglePanel.setIconResource(R.drawable.ic_visibility_off)
        }

        // Current Orientation Mode Display
        val mode = state.orientationMode
        binding.ivCurrentMode.setImageResource(mode.iconRes)
        binding.tvCurrentMode.text = getString(mode.titleRes)

        // Highlight Active Mode Button
        val activeBg = ContextCompat.getDrawable(this, R.drawable.bg_button_circle_active)
        val defaultBg = ContextCompat.getDrawable(this, R.drawable.bg_button_circle)

        binding.btnPortrait.background = if (mode == OrientationMode.PORTRAIT) activeBg else defaultBg
        binding.btnReversePortrait.background = if (mode == OrientationMode.REVERSE_PORTRAIT) activeBg else defaultBg
        binding.btnLandscapeLeft.background = if (mode == OrientationMode.LANDSCAPE_LEFT) activeBg else defaultBg
        binding.btnLandscapeRight.background = if (mode == OrientationMode.LANDSCAPE_RIGHT) activeBg else defaultBg
        binding.btnAutoRotate.background = if (mode == OrientationMode.AUTO_ROTATE) activeBg else defaultBg
    }

    private fun updatePermissionViews() {
        val hasOverlay = PermissionManager.hasOverlayPermission(this)
        val hasNotif = PermissionManager.hasNotificationPermission(this)

        // Overlay status
        if (hasOverlay) {
            binding.ivOverlayPermStatus.setImageResource(R.drawable.ic_check_circle)
            binding.ivOverlayPermStatus.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success))
            binding.btnGrantOverlay.visibility = View.GONE
        } else {
            binding.ivOverlayPermStatus.setImageResource(R.drawable.ic_warning)
            binding.ivOverlayPermStatus.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.warning))
            binding.btnGrantOverlay.visibility = View.VISIBLE
        }

        // Notification status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotif) {
                binding.ivNotifPermStatus.setImageResource(R.drawable.ic_check_circle)
                binding.ivNotifPermStatus.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success))
                binding.btnGrantNotif.visibility = View.GONE
            } else {
                binding.ivNotifPermStatus.setImageResource(R.drawable.ic_warning)
                binding.ivNotifPermStatus.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.warning))
                binding.btnGrantNotif.visibility = View.VISIBLE
            }
        } else {
            binding.ivNotifPermStatus.setImageResource(R.drawable.ic_check_circle)
            binding.ivNotifPermStatus.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success))
            binding.btnGrantNotif.visibility = View.GONE
        }
    }
}
