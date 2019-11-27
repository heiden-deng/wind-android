package com.windchat.client.mvp;


public class BasePresenterImpl<V extends BaseView> implements BasePresenter<V> {
    protected final String TAG = BasePresenterImpl.class.getSimpleName();
    protected V mView;

    @Override
    public void attachView(V view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }
}
