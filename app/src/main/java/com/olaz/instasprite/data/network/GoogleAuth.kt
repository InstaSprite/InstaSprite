package com.olaz.instasprite.data.network

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.olaz.instasprite.BuildConfig
import com.olaz.instasprite.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoogleAuth {

    companion object {
        fun doGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
            login: (String, String) -> Unit,
            onError: (String) -> Unit = {}
        ) {
            val credentialManager = CredentialManager.Companion.create(context)

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()
            scope.launch {
                try {
                    val result = credentialManager.getCredential(context, request)
                    when (result.credential) {
                        is CustomCredential -> {
                            if (result.credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.Companion.createFrom(result.credential.data)
                                val googleTokenId = googleIdTokenCredential.idToken
                                val authCredential =
                                    GoogleAuthProvider.getCredential(googleTokenId, null)
                                val user =
                                    Firebase.auth.signInWithCredential(authCredential).await().user
                                user?.let {
                                    if (it.isAnonymous.not()) {
                                        val userName = it.displayName ?: it.email ?: "User"
                                        login.invoke(userName, googleTokenId)
                                    }
                                }
                            }
                        }

                        else -> {
                            onError("Unsupported credential type")
                        }
                    }
                } catch (e: NoCredentialException) {
                    launcher?.launch(getIntent())
                } catch (e: GetCredentialCancellationException) {
                    onError(context.getString(R.string.auth_cancelled_by_user))
                } catch (e: GetCredentialException) {
                    e.printStackTrace()
                    onError("Failed: ${e.message ?: "Unknown error"}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError("Failed: ${e.message ?: "Unknown error"}")
                }
            }
        }

        private fun getIntent(): Intent {
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context): CredentialOption {
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(BuildConfig.GOOGLE_WEBCLIENT_ID)
                .build()
        }
    }
}