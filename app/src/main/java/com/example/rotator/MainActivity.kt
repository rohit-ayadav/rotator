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
import androidx.core.text.HtmlCompat
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        checkFirstTimePermissionRationale()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionViews()
    }

    private fun setupListeners() {
        // Start / Stop Toggle Controller
        binding.btnStartStopService.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            try {
                val currentState = RotationService.controllerState.value
                if (currentState.serviceState == ServiceState.STOPPED) {
                    if (!PermissionManager.hasOverlayPermission(this)) {
                        showOverlayPermissionRationaleDialog()
                        return@setOnClickListener
                    }
                    RotationService.startService(this, preferencesManager.lastOrientationMode)
                } else {
                    RotationService.sendAction(this, RotationService.ACTION_STOP)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Action failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Pause / Resume Toggle
        binding.btnPauseResumeService.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
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

        // Toggle Floating Panel
        binding.btnTogglePanel.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
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
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            handleOrientationSelection(OrientationMode.PORTRAIT)
        }
        binding.btnReversePortrait.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            handleOrientationSelection(OrientationMode.REVERSE_PORTRAIT)
        }
        binding.btnLandscapeLeft.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            handleOrientationSelection(OrientationMode.LANDSCAPE_LEFT)
        }
        binding.btnLandscapeRight.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            handleOrientationSelection(OrientationMode.LANDSCAPE_RIGHT)
        }
        binding.btnAutoRotate.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            handleOrientationSelection(OrientationMode.AUTO_ROTATE)
        }

        // Permission Grant Buttons
        binding.btnGrantOverlay.setOnClickListener {
            showOverlayPermissionRationaleDialog()
        }

        // Help Information FAB
        binding.fabHelp.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            showHelpDialog()
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

        // Overlay Opacity Slider
        val currentOpacity = preferencesManager.overlayOpacity
        binding.sliderOverlayOpacity.value = currentOpacity.toFloat()
        binding.tvOpacityValue.text = getString(R.string.pref_opacity_format, currentOpacity)

        binding.sliderOverlayOpacity.addOnChangeListener { _, value, fromUser ->
            val opacity = value.toInt()
            binding.tvOpacityValue.text = getString(R.string.pref_opacity_format, opacity)
            if (fromUser) {
                preferencesManager.overlayOpacity = opacity
                RotationService.updateOverlayPreferences(this)
            }
        }

        // Auto Collapse Switch & Timeout
        val isAutoCollapse = preferencesManager.autoCollapseIdle
        binding.switchAutoCollapse.isChecked = isAutoCollapse
        binding.layoutCollapseTimeout.visibility = if (isAutoCollapse) View.VISIBLE else View.GONE

        binding.switchAutoCollapse.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.autoCollapseIdle = isChecked
            binding.layoutCollapseTimeout.visibility = if (isChecked) View.VISIBLE else View.GONE
            RotationService.updateOverlayPreferences(this)
        }

        // Timeout Chips
        when (preferencesManager.autoCollapseTimeoutSeconds) {
            3 -> binding.chipTimeout3s.isChecked = true
            10 -> binding.chipTimeout10s.isChecked = true
            else -> binding.chipTimeout5s.isChecked = true
        }

        binding.chipGroupTimeout.setOnCheckedStateChangeListener { _, checkedIds ->
            val timeoutSec = when {
                checkedIds.contains(R.id.chipTimeout3s) -> 3
                checkedIds.contains(R.id.chipTimeout10s) -> 10
                else -> 5
            }
            preferencesManager.autoCollapseTimeoutSeconds = timeoutSec
            RotationService.updateOverlayPreferences(this)
        }

        // Snap to Screen Edge Switch
        binding.switchSnapToEdge.isChecked = preferencesManager.snapToEdge
        binding.switchSnapToEdge.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.snapToEdge = isChecked
            RotationService.updateOverlayPreferences(this)
        }
    }

    private fun handleOrientationSelection(mode: OrientationMode) {
        try {
            preferencesManager.lastOrientationMode = mode
            val currentState = RotationService.controllerState.value

            if (currentState.serviceState == ServiceState.STOPPED) {
                if (!PermissionManager.hasOverlayPermission(this)) {
                    showOverlayPermissionRationaleDialog()
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

                binding.btnStartStopService.isEnabled = true
                binding.btnStartStopService.text = getString(R.string.action_start)
                binding.btnStartStopService.setIconResource(R.drawable.ic_play)
                binding.btnStartStopService.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
                binding.btnStartStopService.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnStartStopService.iconTint =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

                binding.btnPauseResumeService.isEnabled = false
                binding.btnPauseResumeService.text = getString(R.string.action_pause)
                binding.btnPauseResumeService.setIconResource(R.drawable.ic_pause)
                binding.btnTogglePanel.isEnabled = false
            }

            ServiceState.RUNNING -> {
                if (state.isPanelHidden) {
                    binding.tvStatusBadge.text = getString(R.string.status_panel_hidden)
                } else {
                    binding.tvStatusBadge.text = getString(R.string.status_running)
                }
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success))

                binding.btnStartStopService.isEnabled = true
                binding.btnStartStopService.text = getString(R.string.action_stop)
                binding.btnStartStopService.setIconResource(R.drawable.ic_stop)
                binding.btnStartStopService.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.danger_container))
                binding.btnStartStopService.setTextColor(ContextCompat.getColor(this, R.color.on_danger_container))
                binding.btnStartStopService.iconTint =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.on_danger_container))

                binding.btnPauseResumeService.isEnabled = true
                binding.btnTogglePanel.isEnabled = true
                binding.btnPauseResumeService.text = getString(R.string.action_pause)
                binding.btnPauseResumeService.setIconResource(R.drawable.ic_pause)
            }

            ServiceState.PAUSED -> {
                binding.tvStatusBadge.text = getString(R.string.status_paused)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.warning))

                binding.btnStartStopService.isEnabled = true
                binding.btnStartStopService.text = getString(R.string.action_stop)
                binding.btnStartStopService.setIconResource(R.drawable.ic_stop)
                binding.btnStartStopService.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.danger_container))
                binding.btnStartStopService.setTextColor(ContextCompat.getColor(this, R.color.on_danger_container))
                binding.btnStartStopService.iconTint =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.on_danger_container))

                binding.btnPauseResumeService.isEnabled = true
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

    private fun checkFirstTimePermissionRationale() {
        if (!PermissionManager.hasOverlayPermission(this) && !preferencesManager.hasSeenPermissionRationale) {
            binding.root.post {
                showOverlayPermissionRationaleDialog()
            }
        }
    }

    private fun showOverlayPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_rationale_title)
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.permission_rationale_message),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            .setPositiveButton(R.string.btn_grant_now) { _, _ ->
                preferencesManager.hasSeenPermissionRationale = true
                PermissionManager.requestOverlayPermission(this)
            }
            .setNegativeButton(R.string.btn_maybe_later) { dialog, _ ->
                preferencesManager.hasSeenPermissionRationale = true
                dialog.dismiss()
            }
            .show()
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.help_title)
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.help_dialog_message),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            .setPositiveButton(R.string.help_dialog_close, null)
            .show()
    }
}
