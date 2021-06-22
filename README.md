# CircularProgress
### Create XML file
```xml
<com.widget.CircularProgressBar
	android:id="@+id/circularProgressbar"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone"
        app:backgroundColor="@color/bgColor"
        app:progressbarColor="@color/progressColor"
        app:strokeCap="round"
        app:strokeWidth="2dp"
        tools:visibility="visible" />
```
### Laod in Activity or Fragment
```Kotlin
fun loadProgressbar(){
 plpCircularProgressbar.setVisibility(View.VISIBLE);
 plpCircularProgressbar.setProgress(0f);
 plpCircularProgressbar.setProgressDuration(duration);
 plpCircularProgressbar.setProgressWithAnimation(100, null);
}  
```


