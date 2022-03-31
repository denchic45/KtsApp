package com.denchic45.kts.data.repository

import com.denchic45.kts.data.getDataFlow
import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.DomainModel
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.*

class FindByContainsNameRepositoryImpl<T : DomainModel, D : DocModel>
    (
    private val collectionRef: CollectionReference,
    private val onSave: suspend (List<D>) -> Unit,
    private val docClazz: Class<D>,
    private val map: (List<D>) -> List<T>
) : FindByContainsNameRepository<T> {

    override fun findByContainsName(text: String): Flow<List<T>> {
        return collectionRef.whereArrayContains("searchKeys", text.lowercase(Locale.getDefault()))
            .getDataFlow { it.toObjects(docClazz) }
            .onEach { onSave(it) }
            .map { subjectDocs ->
                map(subjectDocs)
            }
    }

}

inline fun <reified T : DomainModel, reified D : DocModel> findByContainsName(
    collectionRef: CollectionReference,
    noinline onSave: suspend (List<D>) -> Unit,
    noinline map: (List<D>) -> List<T>
): FindByContainsNameRepositoryImpl<T, D> {
    return FindByContainsNameRepositoryImpl(
        collectionRef = collectionRef,
        onSave = onSave,
        docClazz = D::class.java,
        map = map
    )
}