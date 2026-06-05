package com.example.localisation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.localisation.databinding.ActivityMapsBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuration indispensable pour osmdroid
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()

        binding.fabBack.setOnClickListener {
            finish()
        }
    }

    private fun setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        
        val mapController = binding.map.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(33.5731, -7.5898) // Casablanca par défaut
        mapController.setCenter(startPoint)
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}
