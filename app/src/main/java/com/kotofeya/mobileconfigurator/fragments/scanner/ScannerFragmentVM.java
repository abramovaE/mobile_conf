package com.kotofeya.mobileconfigurator.fragments.scanner;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScannerFragmentVM extends ViewModel {

    private MutableLiveData<String> time = new MutableLiveData<>("");
    public LiveData<String> getTime(){return time;}
    public void setTime(String time){this.time.postValue(time);}

    private MutableLiveData<Integer> _progressBarVisibility = new MutableLiveData<>(View.GONE);
    public LiveData<Integer> getProgressBarVisibility(){return _progressBarVisibility;}
    public void setProgressBarVisibility(int visibility){_progressBarVisibility.postValue(visibility);}

    private MutableLiveData<Integer> _progressTvVisibility = new MutableLiveData<>(View.GONE);
    public LiveData<Integer> getProgressTvVisibility(){return _progressTvVisibility;}
    public void setProgressTvVisibility(int visibility){_progressTvVisibility.postValue(visibility);}

    private MutableLiveData<Integer> _progressBarProgress = new MutableLiveData<>(0);
    public LiveData<Integer> getProgressBarProgress(){return _progressBarProgress;}
    public void setProgressBarProgress(int progress){_progressBarProgress.postValue(progress);}

    private MutableLiveData<String> _progressTvText = new MutableLiveData<>("");
    public LiveData<String> getProgressTvText(){return _progressTvText;}
    public void setProgressTvText(String text){_progressTvText.postValue(text);}

    private MutableLiveData<Integer> _downloadedFilesTvVisibility = new MutableLiveData<>(View.GONE);
    public LiveData<Integer> getDownloadedFilesTvVisibility(){return _downloadedFilesTvVisibility;}
    public void setDownloadedFilesTvVisibility(int visibility){_downloadedFilesTvVisibility.postValue(visibility);}

    private MutableLiveData<String> _downloadedFilesTvText = new MutableLiveData<>("");
    public LiveData<String> getDownloadedFilesTvText(){return _downloadedFilesTvText;}
    public void setDownloadedFilesTvText(String text){_downloadedFilesTvText.postValue(text);}


}
