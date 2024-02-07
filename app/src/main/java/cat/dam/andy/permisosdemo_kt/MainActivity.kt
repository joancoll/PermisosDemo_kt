package cat.dam.andy.permisosdemo_kt

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val context: Context = this
    private var permissionManager = PermissionManager(context)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermissions()
        initListeners()
    }

    private fun initPermissions() {
        permissionManager.addPermission(
            Manifest.permission.CAMERA,
            getString(R.string.cameraPermissionInfo),
            getString(R.string.cameraPermissionNeeded),
            getString(R.string.cameraPermissionDenied),
            getString(R.string.cameraPermissionThanks),
            getString(R.string.cameraPermissionSettings)
        )
        permissionManager.addPermission(
            Manifest.permission.WRITE_CONTACTS,
            getString(R.string.contactsPermissionInfo),
            getString(R.string.contactsPermissionNeeded),
            getString(R.string.contactsPermissionDenied),
            getString(R.string.contactsPermissionThanks),
            getString(R.string.contactsPermissionSettings)
        )
    }

    private fun initListeners() {
        val llButtonsContainer = findViewById<LinearLayout>(R.id.ll_buttons_container)
        for (permissionData in permissionManager.getAllNeededPermissions()) {
            if (permissionData.permission != null && permissionData.permissionInfo != null) {
                val button = createButton(
                    permissionData,
                    permissionData.permissionInfo!!
                )
                llButtonsContainer.addView(button)
                // Afegir un marge entre els botons (exemple: 16dp)
                val marginInDp = 16
                val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
                val layoutParams = button.layoutParams as LinearLayout.LayoutParams
                layoutParams.topMargin = marginInPx
            }
        }
    }

    private fun createButton(permissionData:PermissionManager.PermissionData, permissionInfo:String): Button {
        val button = Button(this)
        button.text = permissionInfo
        button.setOnClickListener {
            if (!permissionManager.hasPermission(permissionData.permission!!)) {
                permissionManager.askForThisPermission(permissionData.permission!!)
            }
            else {
                //Toast per indicar que ja té el permís
                Toast.makeText(
                    context, permissionData.permissionGrantedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // Pots personalitzar més propietats del botó segons les teves necessitats
        return button
    }

}
