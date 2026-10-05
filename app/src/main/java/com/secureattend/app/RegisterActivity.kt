package com.secureattend.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.secureattend.app.databinding.ActivityRegisterBinding
import com.secureattend.app.model.Student
import com.secureattend.app.utils.BiometricAuthHelper
import com.secureattend.app.utils.DeviceUtils
import com.secureattend.app.utils.FirestoreRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Check the phone actually has a fingerprint enrolled BEFORE letting
        // them register, since the whole app depends on it.
        if (!BiometricAuthHelper.isFingerprintAvailable(this as FragmentActivity)) {
            binding.tvError.text =
                "No fingerprint enrolled on this device. Please add a fingerprint in " +
                "Settings > Security before registering."
            binding.btnRegister.isEnabled = false
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val roll = binding.etRoll.text.toString().trim()
            val prn = binding.etPrn.text.toString().trim()
            val className = binding.etClass.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val mobile = binding.etMobile.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || roll.isEmpty() || email.isEmpty() || password.length < 6) {
                binding.tvError.text = "Please fill all fields (password min 6 characters)"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val student = Student(
                        uid = uid,
                        name = name,
                        rollNumber = roll,
                        prnNumber = prn,
                        className = className,
                        email = email,
                        mobile = mobile,
                        deviceId = DeviceUtils.getDeviceId(this)
                    )
                    FirestoreRepository.saveStudent(student) { success ->
                        if (success) {
                            startActivity(Intent(this, MarkAttendanceActivity::class.java))
                            finish()
                        } else {
                            binding.tvError.text = "Failed to save student profile"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    binding.tvError.text = e.localizedMessage ?: "Registration failed"
                }
        }
    }
}
