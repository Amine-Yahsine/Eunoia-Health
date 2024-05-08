package com.example.eunoiahealth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.SetOptions

class ProfileCustomizationActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var saveProfileButton: Button
    private var imageUri: Uri? = null

    companion object {
        const val IMAGE_PICK_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_customization)

        profileImageView = findViewById(R.id.profileImageView)
        usernameEditText = findViewById(R.id.usernameEditText)
        saveProfileButton = findViewById(R.id.saveProfileButton)

        profileImageView.setOnClickListener {
            selectImageFromGallery()
        }

        saveProfileButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            if (username.isNotEmpty()) {
                updateFirestoreUsername(username)
            }
            imageUri?.let { uri ->
                uploadImageToFirebase(uri)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/$uid")
        storageRef.putFile(fileUri).addOnSuccessListener {
            // This is the correct way to get the download URL
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                updateFirestoreUserProfileImage(photoUrl)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateFirestoreUsername(username: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userData = hashMapOf("username" to username)
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFirestoreUserProfileImage(photoUrl: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update("photoUrl", photoUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show()
            }
    }
}
