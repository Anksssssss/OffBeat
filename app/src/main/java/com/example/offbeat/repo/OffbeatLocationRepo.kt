package com.example.offbeat.repo

import android.net.Uri
import android.widget.Toast
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.models.Review
import com.example.offbeat.utils.Result
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object OffbeatLocationRepo {
    private val db = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference

    suspend fun addOffbeatLocation(offbeatDetail: OffbeatDetail): Result<String>{
        return try{
            db.collection("OffBeatLocations")
                .add(offbeatDetail)
                .await()
            Result.Success("Success")
        }catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun uploadImageToFirebaseStorage(imageUri: Uri, getFileExtension: (Uri) -> String?):Result<String>{
        return try{
            val fileReference = storageReference.child(
                "uploads/${System.currentTimeMillis()}.${
                    getFileExtension(imageUri)
                }"
            )
            fileReference.putFile(imageUri).await()
            val downloadUrl = fileReference.downloadUrl.await().toString()
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun addReview(review: Review, docId: String): Result<Unit>{
        return try{
            val offbeatDetailRef = db.collection("OffBeatLocations").document(docId)
            val documnet = offbeatDetailRef.get().await()
            if(documnet.exists()) {
                val offBeatDetail = documnet.toObject(OffbeatDetail::class.java)
                offBeatDetail!!.reviewList.add(0, review)
                offbeatDetailRef.set(offBeatDetail).await()
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Document does not exist"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun fetchOffbeatLocations(
        lastDoc: DocumentSnapshot? = null,
        pageSize:Long = 10,
        filter: String? = null
    ):Result<Pair<List<OffbeatDetail>, DocumentSnapshot?>> {
        return try {
            var query = db.collection("OffBeatLocations")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(pageSize)

            if(filter != null) {
                filter?.let {
                    query = query.whereEqualTo("state", it) // or "city" based on your requirement
                }
            }

            val result = if(lastDoc!= null) {
                query.startAfter(lastDoc).get().await()
            } else {
                query.get().await()
            }
           // val result = db.collection("OffBeatLocations").get().await()
            val locations = mutableListOf<OffbeatDetail>()
            for (document in result) {
                val offBeatDetail = document.toObject(OffbeatDetail::class.java)
                offBeatDetail.offBeatId = document.id
                locations.add(offBeatDetail)
            }
            val lastVisible = result.documents.lastOrNull()
            Result.Success(Pair(locations, lastVisible))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}