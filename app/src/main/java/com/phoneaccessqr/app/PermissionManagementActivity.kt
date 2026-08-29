package com.phoneaccessqr.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.phoneaccessqr.app.database.PhoneAccessQRDatabase
import com.phoneaccessqr.app.models.Permission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PermissionManagementActivity : AppCompatActivity() {

    private lateinit var db: PhoneAccessQRDatabase
    private lateinit var permissionListView: ListView
    private lateinit var permissions: MutableList<Permission>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_management)

        db = PhoneAccessQRDatabase.getDatabase(this)
        permissionListView = findViewById(R.id.permission_list_view)
        permissions = mutableListOf()

        val revokeButton = findViewById<Button>(R.id.revoke_button)
        val refreshButton = findViewById<Button>(R.id.refresh_button)

        loadPermissions()

        revokeButton.setOnClickListener {
            val position = permissionListView.checkedItemPosition
            if (position >= 0 && position < permissions.size) {
                revokePermission(permissions[position])
            } else {
                Toast.makeText(this, "Please select a permission to revoke", Toast.LENGTH_SHORT).show()
            }
        }

        refreshButton.setOnClickListener {
            loadPermissions()
        }
    }

    private fun loadPermissions() {
        CoroutineScope(Dispatchers.Main).launch {
            permissions.clear()
            permissions.addAll(db.permissionDao().getAllPermissions())

            val displayList = permissions.map { 
                "${it.deviceName} - Level: ${it.accessLevel} - ${it.grantedAt}"
            }

            adapter = ArrayAdapter(
                this@PermissionManagementActivity,
                android.R.layout.simple_list_item_1,
                displayList
            )
            permissionListView.adapter = adapter
        }
    }

    private fun revokePermission(permission: Permission) {
        CoroutineScope(Dispatchers.Main).launch {
            db.permissionDao().delete(permission)
            Toast.makeText(
                this@PermissionManagementActivity,
                "Permission revoked for ${permission.deviceName}",
                Toast.LENGTH_SHORT
            ).show()
            loadPermissions()
        }
    }
}
