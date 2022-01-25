package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.prefs.GroupPreference
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class GroupRepository @Inject constructor(
    context: Context,
    firestore: FirebaseFirestore,
    private val groupPreference: GroupPreference,
    override val networkService: NetworkService
) : Repository(context) {

    private val specialtiesRef: CollectionReference = firestore.collection("Specialties")
    private val groupsRef: CollectionReference = firestore.collection("Groups")

    fun findBySpecialtyId(specialtyId: String): LiveData<List<CourseGroup>> {
        val groups = MutableLiveData<List<CourseGroup>>()
        groupsRef.whereEqualTo("specialty.id", specialtyId).get()
            .addOnSuccessListener { snapshot: QuerySnapshot ->
                groups.setValue(snapshot.toObjects(CourseGroup::class.java))
            }
            .addOnFailureListener { e: Exception -> Log.d("lol", "err: ", e) }
        return groups
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

//    fun loadGroupPreference(groupId: String?): Completable {
//        return Completable.create { emitter: CompletableEmitter ->
//            groupsRef.whereEqualTo("id", groupId).get()
//                .addOnCompleteListener { task: Task<QuerySnapshot> ->
//                    if (task.isSuccessful) {
//                        for (snapshot in task.result!!) {
//                            groupPreference.saveGroupInfo(groupMapper.docToEntity(snapshot.toObject(GroupDoc::class.java)))
//                        }
//                        emitter.onComplete()
//                    } else {
//                        emitter.onError(task.exception!!)
//                    }
//                }
//        }
//    }

    val yourGroupId: String
        get() = groupPreference.groupId
    val yourGroupName: String
        get() = groupPreference.groupName
}