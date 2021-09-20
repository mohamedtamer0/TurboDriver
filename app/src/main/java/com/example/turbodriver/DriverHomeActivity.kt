package com.example.turbodriver


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.turbodriver.databinding.ActivityDriverHomeBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.nav_header_driver_home.*
import kotlinx.android.synthetic.main.nav_header_driver_home.view.*


class DriverHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDriverHomeBinding
    private lateinit var navView: NavigationView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDriverHome.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        init()
    }

    private fun init() {
        //when clicked on items Navigation
        navView.setNavigationItemSelectedListener {
            if(it.itemId == R.id.nav_exit)
            {
                val builder= AlertDialog.Builder(this,R.style.AlertDialog)
                builder.apply {
                    setTitle("Sign out")
                    setMessage("Do you want to sign out?")
                    setNegativeButton("CANCEL", {dialogInterface, _ -> dialogInterface.dismiss() })
                    setPositiveButton("SIGN OUT"){dialogInterface, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent= Intent(this@DriverHomeActivity, SplashScreenActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }.setCancelable(false)

                    val  dialog= builder.create()
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(resources.getColor(android.R.color.holo_red_dark))

                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(resources.getColor(R.color.colorAccent))
                    }
                    dialog.show()
                }
            }

            true
        }

        //the data for the user
        val headerView= navView.getHeaderView(0)
        val txt_name= headerView.findViewById<View>(R.id.txt_name) as TextView
        val txt_phone= headerView.findViewById<View>(R.id.txt_phone) as TextView
        val txt_star= headerView.findViewById<View>(R.id.txt_star) as TextView


        txt_name.setText(Common.buildWelcomeMessage())
        txt_phone.setText(Common.currentUser!!.phoneNumber)
        txt_star.setText(StringBuilder().append(Common.currentUser!!.rating))


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}