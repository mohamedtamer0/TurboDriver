package com.example.turbodriver.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources

import android.os.Bundle
import android.os.Looper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.turbodriver.Common
import com.example.turbodriver.R
import com.example.turbodriver.databinding.FragmentHomeBinding
import com.firebase.geofire.GeoFire
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.google.firebase.database.FirebaseDatabase


import com.firebase.geofire.GeoLocation








class HomeFragment : Fragment(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var mapFragment: SupportMapFragment

    //Location
    private lateinit var locationRequest: LocationRequest
    private  var locationCallback: LocationCallback?=null
    private   var fusedLocationProviderClient: FusedLocationProviderClient?=null


    //Online System
    private lateinit var onlineRef:DatabaseReference
    private lateinit var currentUserRef:DatabaseReference
    private lateinit var driversLocationRef:DatabaseReference
    private lateinit var geoFire:GeoFire

    private val onlineValueEventListener = object :ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                currentUserRef.onDisconnect().removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(),error.message,Snackbar.LENGTH_LONG).show()
        }

    }








    override fun onResume() {
        super.onResume()
        registerOnlineSystem()
    }

    private fun registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener)
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }


    private fun init() {

        onlineRef= FirebaseDatabase.getInstance().getReference().child(".info/connected")
        driversLocationRef= FirebaseDatabase.getInstance().getReference(Common.DRIVERS_LOCATION_REFERENCES)
        currentUserRef= FirebaseDatabase.getInstance().getReference(Common.DRIVERS_LOCATION_REFERENCES).child(
            FirebaseAuth.getInstance().currentUser!!.uid
        )

        geoFire= GeoFire(driversLocationRef)

        registerOnlineSystem()

        locationRequest= LocationRequest()
        locationRequest.apply {
            this.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            this.setFastestInterval(3000)
            this.interval= 5000
            this.setSmallestDisplacement(10f)
        }

        //take last location
        locationCallback=object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                //set the location on map
                val newPos= LatLng(locationResult!!.lastLocation.latitude,locationResult.lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,18f))

                //set the location to the geoFire
                //Update location
                geoFire
                    .setLocation(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        GeoLocation(
                            locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude
                        )
                    ){key: String?, error: DatabaseError? ->
                        if (error != null)
                        {
                            Snackbar.make(mapFragment.requireView(),error.message, Snackbar.LENGTH_LONG).show()
                        }
                        else
                        {
                            Snackbar.make(mapFragment.requireView(),"You 're online!", Snackbar.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(requireContext())

        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
        }
        else
        {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),12)
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    //Enable btn first
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {
                        fusedLocationProviderClient!!.lastLocation
                            .addOnFailureListener {
                                Toast.makeText(context!!, it.message, Toast.LENGTH_LONG).show()

                            }.addOnSuccessListener {
                                val userLatLng = LatLng(it.latitude, it.longitude)
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userLatLng,
                                        18f
                                    )
                                )
                            }
                        true

                    }


                    //LayOut
                    val locationButton = ( mapFragment.requireView()
                        .findViewById<View>("1".toInt())!!
                        .parent!! as View).findViewById<View>("2".toInt())
                    val params = locationButton.layoutParams as RelativeLayout.LayoutParams
                    params.addRule(RelativeLayout.ALIGN_TOP,0)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE)
                    params.bottomMargin = 50


                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        context!!,
                        "Permission " + p0!!.permissionName + " was denied",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

            }).check()

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.uber_maps_style
                )
            )
            if (!success) {
                Log.e("Tamer_Error", "style error")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Tamer_Error", e.message!!)
        }


    }


    override fun onDestroy() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        if (locationCallback == null) {
            geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        }
        onlineRef.removeEventListener(onlineValueEventListener)
        super.onDestroy()
    }

}