package com.secureattend.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.secureattend.app.databinding.ActivityLoginBinding
import com.secureattend.app.utils.FirestoreRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.tvError.text = "Please enter email and password"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, SelectLectureActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    binding.tvError.text = e.localizedMessage ?: "Login failed"
                }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // CHANGED: Faculty Dashboard now requires signing in with the SAME
        // email/password fields above, then checks the "teachers" collection
        // before letting them in — no more open door.
        binding.tvFacultyLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.tvError.text = "Enter your faculty email & password above first"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    FirestoreRepository.isTeacher(uid) { isTeacher ->
                        if (isTeacher) {
                            startActivity(Intent(this, FacultyDashboardActivity::class.java))
                            finish()
                        } else {
                            binding.tvError.text = "This account isn't registered as faculty. Register on the website first."
                            auth.signOut()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    binding.tvError.text = e.localizedMessage ?: "Login failed"
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // Skip login if already signed in — send straight to lecture selection.
        if (auth.currentUser != null) {
            startActivity(Intent(this, SelectLectureActivity::class.java))
            finish()
        }
    }
}
