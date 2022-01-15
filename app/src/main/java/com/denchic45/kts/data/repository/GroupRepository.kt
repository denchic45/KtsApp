package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.GroupMapper
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.TimestampPreference
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import javax.inject.Inject

class GroupRepository @Inject constructor(
    context: Context,
    firestore: FirebaseFirestore,
    private val groupPreference: GroupPreference,
    private val timestampPreference: TimestampPreference,
    private val groupMapper: GroupMapper, override val networkService: NetworkService
) : Repository(context) {

    private val specialtiesRef: CollectionReference = firestore.collection("Specialties")
    private val groupsRef: CollectionReference = firestore.collection("Groups")

    fun findBySpecialtyUuid(specialtyUuid: String?): LiveData<List<Group>> {
        val groupsByUuid = MutableLiveData<List<Group>>()
        groupsRef.whereEqualTo("specialty.uuid", specialtyUuid).get()
            .addOnSuccessListener { snapshot: QuerySnapshot ->
                groupsByUuid.setValue(
                    snapshot.toObjects(
                        Group::class.java
                    )
                )
            }
            .addOnFailureListener { e: Exception? -> Log.d("lol", "err: ", e) }
        return groupsByUuid
    }

    val allSpecialties: MutableLiveData<List<Specialty>>
        get() {
            val allSpecialties = MutableLiveData<List<Specialty>>()
            addListenerRegistration("specials") {
                specialtiesRef.addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.d("lol", "onEvent error: ", error)
                    }
                    Log.d("lol", "getAllSpecialties size: " + snapshot!!.size())
                    allSpecialties.setValue(snapshot.toObjects(Specialty::class.java))
                }
            }
            return allSpecialties
        }

    fun loadGroupPreference(groupUuid: String?): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            groupsRef.whereEqualTo("id", groupUuid).get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        for (snapshot in task.result!!) {
                            groupPreference.saveGroupInfo(groupMapper.docToEntity(snapshot.toObject(GroupDoc::class.java)))
                        }
                        emitter.onComplete()
                    } else {
                        emitter.onError(task.exception!!)
                    }
                }
        }
    }

    val yourGroupId: String
        get() = groupPreference.groupId
    val yourGroupName: String
        get() = groupPreference.groupName
}