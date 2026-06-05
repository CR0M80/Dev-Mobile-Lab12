package com.example.localisation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.localisation.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestQueue: RequestQueue
    
    // CONFIGURATION SERVEUR
    private val SERVER_IP = "192.168.100.28"
    private val INSERT_URL = "http://$SERVER_IP/localisation/createPosition.php"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Permission GPS requise pour le tracking", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        
        requestQueue = Volley.newRequestQueue(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnMap.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permissions)
        } else {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000) // 30 secondes
            .setMinUpdateDistanceMeters(10f)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateUI(location.latitude, location.longitude)
                    sendToServer(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun updateUI(lat: Double, lon: Double) {
        binding.tvLat.text = "Latitude: %.5f".format(lat)
        binding.tvLon.text = "Longitude: %.5f".format(lon)
        binding.tvTime.text = "Dernier ping: ${SimpleDateFormat("HH:mm:ss", Locale.FRANCE).format(Date())}"
    }

    private fun sendToServer(lat: Double, lon: Double) {
        val request = object : StringRequest(
            Request.Method.POST, INSERT_URL,
            { _ -> /* Succès silencieux pour plus de fluidité */ },
            { error -> Toast.makeText(this, "Erreur Serveur: ${error.message}", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["latitude"] = lat.toString()
                params["longitude"] = lon.toString()
                params["date"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                params["imei"] = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                return params
            }
        }
        requestQueue.add(request)
    }
}