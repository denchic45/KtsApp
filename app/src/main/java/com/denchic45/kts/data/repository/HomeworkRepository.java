//package com.example.kts.data.repository;
//
//import android.app.Application;
//import android.util.Log;
//
//import com.example.kts.data.DataBase;
//import com.example.kts.data.dao.HomeworkDao;
//import com.example.kts.data.model.entity.Homework;
//import com.example.kts.data.prefs.GroupPreference;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import io.reactivex.rxjava3.core.Completable;
//
//public class HomeworkRepository {
//
//    private final HomeworkDao homeworkDao;
//    private final CollectionReference homeworkRef;
//    private final GroupPreference groupPreference;
//
//    public HomeworkRepository(Application application) {
//        groupPreference = new GroupPreference(application);
//        DataBase dataBase = DataBase.newInstance(application);
//        FirebaseFirestore firestore = FirebaseFirestore.newInstance();
//        homeworkRef = firestore.collection("Group").document(groupPreference.getGroupUuid()).collection("Homework");
//        homeworkDao = dataBase.homeworkDao();
//
//    }
//
//    public Completable loadHomeworkOfGroup() {
//        return Completable.create(emitter -> homeworkRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                homeworkDao.insertList(task.getResult().toObjects(Homework.class));
//                emitter.onComplete();
//            } else {
//                emitter.onError(task.getException());
//            }
//        }));
//    }
//}
