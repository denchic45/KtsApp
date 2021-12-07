package com.denchic45.kts.rx

import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import org.reactivestreams.Publisher

class AsyncTransformer<T> : ObservableTransformer<T, T>, SingleTransformer<T, T>, FlowableTransformer<T, T>, MaybeTransformer<T, T> {
    override fun apply(observable: Observable<T>): ObservableSource<T> {
        return observable
            .subscribeOn(Schedulers.io())
            .distinct()
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun apply(single: Single<T>): SingleSource<T> {
        return single
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun apply(maybe: Maybe<T>): MaybeSource<T> {
        return maybe
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun apply(flowable: Flowable<T>): Publisher<T> {
        return flowable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

class AsyncCompletableTransformer() : CompletableTransformer {
    override fun apply(completable: Completable): CompletableSource {
        return completable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}