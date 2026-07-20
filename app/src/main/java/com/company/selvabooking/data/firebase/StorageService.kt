package com.company.selvabooking.data.firebase

import android.net.Uri
import com.company.selvabooking.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageService(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadImage(
        uri: Uri,
        folder: String,
        fileName: String = UUID.randomUUID().toString()
    ): Result<String> = try {
        val ref = storage.reference
            .child(folder)
            .child("$fileName.jpg")
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadHotelImage(uri: Uri): Result<String> {
        return uploadImage(uri, Constants.STORAGE_HOTELES)
    }

    suspend fun uploadRoomImage(uri: Uri): Result<String> {
        return uploadImage(uri, Constants.STORAGE_HABITACIONES)
    }

    suspend fun uploadProfileImage(uri: Uri, userId: String): Result<String> {
        return uploadImage(uri, Constants.STORAGE_PERFILES, userId)
    }

    suspend fun deleteImage(url: String): Result<Unit> = try {
        storage.getReferenceFromUrl(url).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
