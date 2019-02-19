package at.opendrone.opendrone;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private Switch profiSwitch;
    private EditText maxHeight;
    private Spinner language;
    private View view;
    private SharedPreferences sp;
    private boolean proMode;
    private static final String TAG = "SETTINGSY";

    String[] supportedLanguages = new String[]{"English","English","English"};

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        findView();
        return view;
    }

    private void findView(){
        profiSwitch = view.findViewById(R.id.switchProfiMode);
        maxHeight = view.findViewById(R.id.valMaxHeight);
        language = view.findViewById(R.id.spinner_Language);
        sp = getActivity().getSharedPreferences("at.opendrone.opendrone", Context.MODE_PRIVATE);

        configureProMode();
        configureLanguageSpinner();
        addListeners();
    }

    private void addListeners(){
        profiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(OpenDroneUtils.SP_SETTINGS_PROMODE, isChecked).apply();
                if(isChecked){
                    proMode = true;
                    maxHeight.setEnabled(true);
                }else{
                    proMode = false;
                    maxHeight.setEnabled(false);
                }
                ((MainActivity)getActivity()).initNavView();

            }
        });

        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String l = (String) parent.getItemAtPosition(position);
                sp.edit().putString(OpenDroneUtils.SP_SETTINGS_LANGUAGE, l).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        maxHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    int i = Integer.parseInt(s.toString());
                    if(i > 1000){
                        Toast.makeText(getContext(),"You can only fly up to 1000m!",Toast.LENGTH_LONG);
                        maxHeight.setText(""+sp.getInt(OpenDroneUtils.SP_SETTINGS_MAXHEIGHT,0));
                        maxHeight.setSelection(maxHeight.getText().length());
                    }else if( i < 0){
                        Toast.makeText(getContext(),"You can only fly above the ground!",Toast.LENGTH_LONG);
                        maxHeight.setText(""+sp.getInt(OpenDroneUtils.SP_SETTINGS_MAXHEIGHT,0));
                        maxHeight.setSelection(maxHeight.getText().length());
                    }else{
                        sp.edit().putInt(OpenDroneUtils.SP_SETTINGS_MAXHEIGHT,i).apply();
                    }
                }catch(NumberFormatException ex){
                    Log.e(TAG,ex.getMessage(),ex);
                }
            }
        });
    }

    private void configureProMode(){
        proMode = sp.getBoolean(OpenDroneUtils.SP_SETTINGS_PROMODE,false);
        profiSwitch.setChecked(proMode);
        maxHeight.setEnabled(proMode);
        maxHeight.setText(""+sp.getInt(OpenDroneUtils.SP_SETTINGS_MAXHEIGHT,0));

    }

    private void configureLanguageSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.array_Languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        language.setAdapter(adapter);

        String preselected = sp.getString(OpenDroneUtils.SP_SETTINGS_LANGUAGE, "English");
        int preselectedPos = 0;

        for(int i = 0; i < adapter.getCount(); i++){
            if(adapter.getItem(i).equals(preselected)){
                preselectedPos = i;
                break;
            }
        }

        language.setSelection(preselectedPos);

    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtils.hideKeyboard(maxHeight, getActivity());
    }
}
