package com.example.proyecto

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.SetOptions

data class UserProfile(
    val nombre: String = "",
    val email: String = "",
    val peso: String = "",
    val altura: String = "",
    val edad: String = "",
    val objetivo: String = Objetivo.MANTENER.name,
    val alimentos: List<String> = emptyList(),
    val updatedAt: Timestamp? = null
)

data class PuntuacionData(
    val id: String = "",
    val puntuacion: Int = 0,
    val comentario: String = "",
    val imagenUri: String = "",
    val timestamp: Timestamp? = null,
    val userId: String = ""
)

object AuthRepository {
    private val auth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }

    private fun currentUserId(): String? = auth.currentUser?.uid

    fun signUp(email: String, password: String, nombre: String, onComplete: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = currentUserId()
                if (uid == null) {
                    onComplete(false, "No se pudo crear el usuario")
                    return@addOnSuccessListener
                }
                val userData = mapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "peso" to "",
                    "altura" to "",
                    "edad" to "",
                    "objetivo" to Objetivo.MANTENER.name,
                    "alimentos" to emptyList<String>(),
                    "updatedAt" to Timestamp.now()
                )
                firestore.collection("usuarios").document(uid)
                    .set(userData)
                    .addOnSuccessListener { onComplete(true, "Cuenta creada con éxito") }
                    .addOnFailureListener { e -> onComplete(false, e.localizedMessage ?: "Error al guardar usuario") }
            }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage ?: "Error al crear cuenta") }
    }

    fun signIn(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onComplete(true, "Sesión iniciada") }
            .addOnFailureListener { e -> 
                val code = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: ""
                onComplete(false, code) 
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun saveProfile(state: AlimentacionState, onComplete: (Boolean, String) -> Unit) {
        val uid = currentUserId()
        if (uid == null) {
            onComplete(false, "Debes iniciar sesión para guardar los datos")
            return
        }
        val profileData = mapOf(
            "nombre" to state.nombre,
            "email" to state.email,
            "peso" to state.peso,
            "altura" to state.altura,
            "edad" to state.edad,
            "objetivo" to state.objetivo.name,
            "alimentos" to state.alimentos.toList(),
            "updatedAt" to Timestamp.now()
        )
        firestore.collection("usuarios").document(uid)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, "Datos guardados con éxito") }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage ?: "Error al guardar") }
    }

    fun savePuntuacion(
        puntuacion: Int,
        comentario: String,
        imagenUri: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val uid = currentUserId()
        if (uid == null) {
            onComplete(false, "Inicia sesión para puntuar")
            return
        }
        val data = mapOf(
            "userId" to uid,
            "puntuacion" to puntuacion,
            "comentario" to comentario,
            "imagenUri" to imagenUri,
            "timestamp" to Timestamp.now()
        )
        firestore.collection("puntuaciones")
            .add(data)
            .addOnSuccessListener { onComplete(true, "¡Puntuación guardada!") }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage ?: "Error al guardar") }
    }

    fun getPuntuaciones(onResult: (List<PuntuacionData>) -> Unit) {
        val uid = currentUserId()
        if (uid == null) {
            onResult(emptyList())
            return
        }
        // Eliminamos .orderBy para evitar problemas de índices en Firestore
        firestore.collection("puntuaciones")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    PuntuacionData(
                        id = doc.id,
                        puntuacion = doc.getLong("puntuacion")?.toInt() ?: 0,
                        comentario = doc.getString("comentario") ?: "",
                        imagenUri = doc.getString("imagenUri") ?: "",
                        timestamp = doc.getTimestamp("timestamp"),
                        userId = doc.getString("userId") ?: ""
                    )
                }.sortedByDescending { it.timestamp?.seconds ?: 0L } // Ordenamos en el móvil

                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun deletePuntuacion(id: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("puntuaciones").document(id)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun loadProfile(onResult: (UserProfile?) -> Unit) {
        val uid = currentUserId()
        if (uid == null) {
            onResult(null)
            return
        }
        firestore.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }
                val profile = UserProfile(
                    nombre = document.getString("nombre") ?: "",
                    email = document.getString("email") ?: "",
                    peso = document.getString("peso") ?: "",
                    altura = document.getString("altura") ?: "",
                    edad = document.getString("edad") ?: "",
                    objetivo = document.getString("objetivo") ?: Objetivo.MANTENER.name,
                    alimentos = document.get("alimentos") as? List<String> ?: emptyList(),
                    updatedAt = document.getTimestamp("updatedAt")
                )
                onResult(profile)
            }
            .addOnFailureListener { _ -> onResult(null) }
    }
}
