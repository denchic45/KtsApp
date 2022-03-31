package com.denchic45.kts.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class EditTextTransformer : ObservableTransformer<CharSequence, String> {
    override fun apply(upstream: Observable<CharSequence>): ObservableSource<String> {
        return upstream
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { obj: CharSequence -> obj.toString() }
            .debounce(700, TimeUnit.MILLISECONDS)
    }
}