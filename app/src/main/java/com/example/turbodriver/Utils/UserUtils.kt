package com.example.turbodriver.Utils

import android.view.View
import com.example.turbodriver.Common
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserUtils {

    fun updateUser(
        view: View?,
        updateData:Map<String,Any>
    ) {
        FirebaseDatabase.getInstance()
            .getReference(Common.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener {
                Snackbar.make(view!!,it.message!!,Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener {
                Snackbar.make(view!!,"Update Information Success",Snackbar.LENGTH_LONG).show()
            }

    }

}