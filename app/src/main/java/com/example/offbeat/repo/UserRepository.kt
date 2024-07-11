package com.example.offbeat.repo

import android.util.Log
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.models.User
import com.example.offbeat.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


object UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User Creation Failed")
            val uid = user.uid
            val newUser = User(uid, name, email)
            saveuserToFireStore(newUser)
            auth.signOut()
            Result.Success(newUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun saveuserToFireStore(user: User) {
        db.collection("users").document(user.id)
            .set(user).await()
        Log.d("SignUp", "User data added to Firestore")
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Login  Failed")
            val uid = user.uid
            val userDoc = db.collection("users").document(uid).get().await()
            val loggedInUser =
                userDoc.toObject(User::class.java) ?: throw Exception("User Not Found")
            Result.Success(loggedInUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun addOffbeaLocationUploadedByUser(
        userId: String,
        offBeatDetail: OffbeatDetail
    ): Result<String> {
        return try {
            db.collection("users").document(userId).collection("OffBeatLocations")
                .add(offBeatDetail)
                .await()
            Result.Success("Offbeat location added to user collection")
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

}