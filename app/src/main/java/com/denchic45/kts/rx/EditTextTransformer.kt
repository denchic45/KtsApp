package com.denchic45.kts.rx;


import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditTextTransformer implements ObservableTransformer<CharSequence, String> {

    @Override
    public @NonNull ObservableSource<String> apply(@NonNull Observable<CharSequence> upstream) {
        return upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString)
                .debounce(700, TimeUnit.MILLISECONDS);
    }
}
