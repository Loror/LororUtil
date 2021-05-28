package com.loror.lororUtil.http.api.chain;

import com.loror.lororUtil.http.api.Observable;

/**
 * Date: 2020/6/4 8:58
 * Description: Create By Loror
 */
public class ObserverChain {

    private ObserverChain.Builder builder;

    public interface OnErrorCollection {
        void onError(Throwable throwable);
    }

    public interface OnComplete {
        void onComplete();
    }

    public static class Builder {
        private OnErrorCollection onErrorCollection;
        private OnComplete onComplete;

        public Builder setOnErrorCollection(OnErrorCollection onErrorCollection) {
            this.onErrorCollection = onErrorCollection;
            return this;
        }

        public Builder setOnComplete(OnComplete onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        protected OnComplete getOnComplete() {
            return onComplete;
        }

        protected OnErrorCollection getOnErrorCollection() {
            return onErrorCollection;
        }

        public ObserverChain build() {
            ObserverChain chain = new ObserverChain();
            chain.builder = this;
            return chain;
        }
    }

    public <T> ChainNode<T> begin(Observable<T> observable) {
        return new ChainNode<T>(builder, observable);
    }
    
    private ChainNode<?> chainNode;

    public <T> ObserverChain add(Observable<T> observable, ChainNode.OnResult<T> onResult) {
        if (chainNode == null) {
            chainNode = new ChainNode<T>(builder, observable).onResult(onResult);
        } else {
            chainNode = chainNode.next(observable).onResult(onResult);
        }
        return this;
    }
    
    public void execute() {
        if (chainNode != null) {
            chainNode.load();
        }
    }

}
