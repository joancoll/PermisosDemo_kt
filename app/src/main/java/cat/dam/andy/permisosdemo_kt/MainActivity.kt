package cat.dam.andy.permisosdemo_kt

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity

data class PermissionInfo(
    val name: String,
    val permission: String,
    val grantedMessage: String,
    val rationale: String
)

class MainActivity : AppCompatActivity() {

    private val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED
    private lateinit var activityResultLauncher: ActivityResultLauncher<String>

    private val permissionsList = listOf(
        PermissionInfo(
            "Camera Access",
            Manifest.permission.CAMERA,
            "Camera Permission granted",
            "Camera access is required for taking pictures."
        ),
        PermissionInfo(
            "Contacts Access",
            Manifest.permission.WRITE_CONTACTS,
            "Contacts Permission granted",
            "Contact access is required for managing contacts."
        ),
        // Afegeix més permisos si és necessari
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermissions()
        initListeners()
    }

    private fun initPermissions() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    showToast("Permission granted")
                } else {
                    showToast("Permission denied")
                    handlePermissionDenied()
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handlePermissionDenied() {
        val permanentDeniedPermissions = mutableListOf<String>()

        for (permissionInfo in permissionsList) {
            if (checkSelfPermission(permissionInfo.permission) != PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(
                    permissionInfo.permission
                )
            ) {
                permanentDeniedPermissions.add(permissionInfo.permission)
            }
        }

        if (permanentDeniedPermissions.isNotEmpty()) {
            showPermanentDeniedDialog(permanentDeniedPermissions)
        }
    }

    private fun showPermanentDeniedDialog(permanentDeniedPermissions: List<String>) {
        val deniedPermissionNames = permissionsList
            .filter { it.permission in permanentDeniedPermissions }
            .joinToString(", ") { it.name }
        AlertDialog.Builder(this)
            .setTitle("Permission denied")
            .setMessage("Permissions « $deniedPermissionNames » were permanently denied. You need to go to Permission settings to allow them. Thanks")
            .setPositiveButton("Go to settings") { _, _ ->
                goToAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun goToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")

        // Si estem en Android 11 o versions posteriors, obrim la configuració de l'aplicació directament
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent.action = Settings.ACTION_APP_USAGE_SETTINGS
        }

        startActivity(intent)
    }

    private fun initListeners() {
        val llButtonsContainer = findViewById<LinearLayout>(R.id.ll_buttons_container)
        for (permissionInfo in permissionsList) {
            val button = createButton(permissionInfo)
            llButtonsContainer.addView(button)
            // Afegir un marge entre els botons (exemple: 16dp)
            val marginInDp = 16
            val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
            val layoutParams = button.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = marginInPx
        }
    }

    private fun createButton(permissionInfo: PermissionInfo): Button {
        val button = Button(this)
        button.text = permissionInfo.name
        button.setOnClickListener {
            handlePermissionButton(permissionInfo)
        }
        // Pots personalitzar més propietats del botó segons les teves necessitats
        return button
    }

    private fun handlePermissionButton(permissionInfo: PermissionInfo) {
        if (checkSelfPermission(permissionInfo.permission) == PERMISSION_GRANTED) {
            showToast(permissionInfo.grantedMessage)
        } else {
            // Comprova si ja s'ha mostrat la informació de la necessitat del permís
            if (!shouldShowRequestPermissionRationale(permissionInfo.permission)) {
                // Si no cal explicació, sol·licita el permís directament
                activityResultLauncher.launch(permissionInfo.permission)
            } else {
                // Altrament, mostra la informació de la necessitat del permís abans de sol·licitar-lo
                showRationaleDialog(permissionInfo)
            }
        }
    }

    private fun showRationaleDialog(permissionInfo: PermissionInfo) {
        AlertDialog.Builder(this)
            .setTitle("Permission required")
            .setMessage(permissionInfo.rationale)
            .setPositiveButton("OK") { _, _ ->
                // Sol·licita el permís després d'informar l'usuari de la necessitat
                activityResultLauncher.launch(permissionInfo.permission)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}
