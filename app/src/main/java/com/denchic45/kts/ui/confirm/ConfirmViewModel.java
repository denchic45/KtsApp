package com.denchic45.kts.ui.confirm;

import androidx.lifecycle.ViewModel;

import com.denchic45.kts.rx.bus.RxBusConfirm;

public class ConfirmViewModel extends ViewModel {

    public void onNegativeClick() {
        RxBusConfirm.getInstance().postEvent(false);
    }

    public void onPositiveClick() {
        RxBusConfirm.getInstance().postEvent(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        RxBusConfirm.getInstance().completeEvent();
    }
}
