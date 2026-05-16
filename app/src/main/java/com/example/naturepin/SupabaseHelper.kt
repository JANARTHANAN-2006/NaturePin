package com.example.naturepin

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object SupabaseHelper {
    private val client = SupabaseManager.getClient()
    private val scope = CoroutineScope(Dispatchers.Main)

    fun getCurrentUser(): UserInfo? = client.auth.currentUserOrNull()

    fun signOut() {
        scope.launch { client.auth.signOut() }
    }

    fun signUpWithEmail(emailStr: String, passwordStr: String, callback: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                client.auth.signUpWith(Email) {
                    email = emailStr
                    password = passwordStr
                }
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }

    fun signInWithEmail(emailStr: String, passwordStr: String, callback: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                client.auth.signInWith(Email) {
                    email = emailStr
                    password = passwordStr
                }
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }

    fun getUserProfile(uid: String, callback: (UserProfile?) -> Unit) {
        scope.launch {
            try {
                // Be defensive: if the table accidentally contains multiple rows for the same uid,
                // decodeSingleOrNull would fail. We pick the first match.
                val profiles = withContext(Dispatchers.IO) {
                    client.postgrest["users"].select {
                        filter { eq("uid", uid) }
                    }.decodeList<UserProfile>()
                }
                callback(profiles.firstOrNull())
            } catch (e: Exception) {
                Log.e("SupabaseHelper", "getUserProfile failed: ${e.message}", e)
                callback(null)
            }
        }
    }

    fun getAllPosts(callback: (List<Post>) -> Unit) {
        scope.launch {
            try {
                val posts = withContext(Dispatchers.IO) {
                    client.postgrest["posts"].select().decodeList<Post>()
                }
                callback(posts)
            } catch (e: Exception) {
                callback(emptyList())
            }
        }
    }

    fun getUserPosts(uid: String, callback: (List<Post>) -> Unit) {
        scope.launch {
            try {
                val posts = withContext(Dispatchers.IO) {
                    client.postgrest["posts"].select {
                        filter { eq("user_uid", uid) }
                    }.decodeList<Post>()
                }
                callback(posts)
            } catch (e: Exception) {
                callback(emptyList())
            }
        }
    }

    fun savePost(post: Post, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    client.postgrest["posts"].insert(post)
                }
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    client.postgrest["posts"].delete {
                        filter { eq("id", postId) }
                    }
                }
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun uploadPostImages(resolver: ContentResolver, uris: Array<String>, callback: (List<String>) -> Unit) {
        scope.launch {
            val urls = mutableListOf<String>()
            for (uriStr in uris) {
                if (uriStr.isEmpty()) {
                    urls.add("")
                    continue
                }
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        resolver.openInputStream(Uri.parse(uriStr))?.readBytes()
                    }
                    if (bytes != null) {
                        val fileName = "${UUID.randomUUID()}.jpg"
                        val bucket = client.storage["images"]
                        bucket.upload(fileName, bytes) {
                            upsert = true
                        }
                        urls.add(bucket.publicUrl(fileName))
                    } else {
                        urls.add("")
                    }
                } catch (e: Exception) {
                    urls.add("")
                }
            }
            callback(urls)
        }
    }

    fun uploadProfilePicture(resolver: ContentResolver, uriStr: String, callback: (String?) -> Unit) {
        scope.launch {
            try {
                if (uriStr.isEmpty()) {
                    callback(null)
                    return@launch
                }
                val bytes = withContext(Dispatchers.IO) {
                    resolver.openInputStream(Uri.parse(uriStr))?.readBytes()
                } ?: run {
                    Log.e("SupabaseHelper", "uploadProfilePicture: openInputStream returned null for $uriStr")
                    callback(null)
                    return@launch
                }

                val fileName = "avatars/${UUID.randomUUID()}.jpg"
                val bucket = client.storage["images"]
                bucket.upload(fileName, bytes) {
                    upsert = true
                }
                callback(bucket.publicUrl(fileName))
            } catch (e: Exception) {
                Log.e("SupabaseHelper", "uploadProfilePicture failed: ${e.message}", e)
                callback(null)
            }
        }
    }

    fun checkIfUserExists(uid: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.postgrest["users"].select {
                        filter { eq("uid", uid) }
                    }
                }
                callback(response.data != "[]")
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun getUserDetails(uid: String, callback: (String) -> Unit) {
        getUserProfile(uid) { profile ->
            callback(profile?.username ?: "User")
        }
    }

    fun saveUser(uid: String, username: String, email: String, bio: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                val user = UserProfile(uid, username, email, bio, "")
                withContext(Dispatchers.IO) {
                    client.postgrest["users"].insert(user)
                }
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun updateUserProfile(uid: String, username: String, bio: String, profilePicUrl: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                val email = client.auth.currentUserOrNull()?.email ?: ""

                val exists = withContext(Dispatchers.IO) {
                    client.postgrest["users"].select {
                        filter { eq("uid", uid) }
                    }.decodeList<UserProfile>().isNotEmpty()
                }

                withContext(Dispatchers.IO) {
                    if (exists) {
                        client.postgrest["users"].update({
                            set("username", username)
                            set("bio", bio)
                            set("profile_pic_url", profilePicUrl)
                        }) {
                            filter { eq("uid", uid) }
                        }
                    } else {
                        // If a row was never created (or got deleted), create it now.
                        client.postgrest["users"].insert(UserProfile(uid, username, email, bio, profilePicUrl))
                    }
                }

                callback(true)
            } catch (e: Exception) {
                Log.e("SupabaseHelper", "updateUserProfile failed: ${e.message}", e)
                callback(false)
            }
        }
    }

    fun befriend(currentUid: String, otherUid: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    client.postgrest["friendships"].insert(FriendshipRow(user_uid1 = currentUid, user_uid2 = otherUid, status = "accepted"))
                }
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun getMyFriends(currentUid: String, callback: (Set<String>) -> Unit) {
        scope.launch {
            try {
                val rows = withContext(Dispatchers.IO) {
                    client.postgrest["friendships"].select {
                        filter {
                            eq("user_uid1", currentUid)
                            eq("status", "accepted")
                        }
                    }.decodeList<FriendshipRow>()
                }
                callback(rows.map { it.user_uid2 }.toSet())
            } catch (e: Exception) {
                callback(emptySet())
            }
        }
    }

    fun isBefriended(currentUid: String, otherUid: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                val rows = withContext(Dispatchers.IO) {
                    client.postgrest["friendships"].select {
                        filter {
                            eq("user_uid1", currentUid)
                            eq("user_uid2", otherUid)
                            eq("status", "accepted")
                        }
                    }.decodeList<FriendshipRow>()
                }
                callback(rows.isNotEmpty())
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class UserProfile(
    val uid: String,
    val username: String,
    val email: String,
    val bio: String = "",
    val profile_pic_url: String = ""
)

@kotlinx.serialization.Serializable
data class FriendshipRow(
    val id: String? = null,
    val user_uid1: String,
    val user_uid2: String,
    val status: String = "accepted"
)
