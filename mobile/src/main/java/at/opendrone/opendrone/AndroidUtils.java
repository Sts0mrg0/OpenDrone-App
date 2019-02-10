package at.opendrone.opendrone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class AndroidUtils {
    /**
     * hides Keyboard when touching out of TextView
     * @param activity The Parent Activity of the View
     * @param view the view from onFocusChange(View v, boolean hasFocus) Method
     */
    public static void hideKeyboard(Activity activity, View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity, View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, 0);
    }

    /**
     * hides Keyboard when touching out of TextView
     * you have to set
     * android:clickable="true"
     * android:focusableInTouchMode="true"
     * in ParentView (e.g.: Constraint Layout)
     *
     * @param editText The Edittext you are currently in
     * @param activity The Parent Activity of the View
     **/
    public static void hideKeyboard(EditText editText, final Activity activity){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(activity,v);
                }else{
                    showKeyboard(activity,v);
                }
            }
        });
    }

    /**
     * hides Keyboard when touching out of TextView
     * you have to set
     * android:clickable="true"
     * android:focusableInTouchMode="true"
     * in ParentView (e.g.: Constraint Layout)
     *
     * @param recView The RecyclerView that should trigger hideKeyboard
     * @param activity The Parent Activity of the View
     **/
    @SuppressLint("ClickableViewAccessibility")
    public static void hideKeyboard(RecyclerView recView, final Activity activity){
        ItemClickSupport.addTo(recView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                hideKeyboard(activity, v);
            }
        });
    }

    private void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
