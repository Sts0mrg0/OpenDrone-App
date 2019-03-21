package at.opendrone.opendrone.settings;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import at.opendrone.opendrone.utils.CustomFontTextView;
import at.opendrone.opendrone.utils.OpenDroneUtils;
import at.opendrone.opendrone.R;
import at.opendrone.opendrone.network.ConnectDisconnectTasks;
import at.opendrone.opendrone.network.OpenDroneFrame;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdjustPIDFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final double P_MAX = 5;
    private static final double I_MAX = 0.5;
    private static final double D_MAX = 100;

    private static final double P_DEFAULT = 1.25;
    private static final double I_DEFAULT = 0.05;
    private static final double D_DEFAULT = 90;

    private static final String TAG = "AdjustPIDy";

    private CustomFontTextView pTextView;
    private CustomFontTextView iTextView;
    private CustomFontTextView dTextView;

    private ConnectDisconnectTasks tasks = ConnectDisconnectTasks.getInstance();

    private Button resetButton;

    private SeekBar pSeekbar;
    private SeekBar iSeekbar;
    private SeekBar dSeekbar;

    private View view;

    public AdjustPIDFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_adjust_pid, container, false);
        findViews();
        setSeekbarListeners();
        setInitalText();
        return view;
    }

    private void setSeekbarListeners(){
        pSeekbar.setOnSeekBarChangeListener(this);
        iSeekbar.setOnSeekBarChangeListener(this);
        dSeekbar.setOnSeekBarChangeListener(this);
    }

    private void findViews(){
        pTextView = view.findViewById(R.id.adjustPIDPTextView);
        iTextView = view.findViewById(R.id.adjustPIDITextView);
        dTextView = view.findViewById(R.id.adjustPIDDTextView);

        pSeekbar = view.findViewById(R.id.seekbarP);
        iSeekbar = view.findViewById(R.id.seekbarI);
        dSeekbar = view.findViewById(R.id.seekbarD);

        resetButton = view.findViewById(R.id.adjustPIDResetButton);

        resetButton.setOnClickListener(v -> setInitalText());
    }

    private void sendMessage(String val, int code){
        try{
            OpenDroneFrame f = new OpenDroneFrame((byte)1, new String[] {val}, new int[]{code});
            tasks.sendMessage(f.toString());
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage(), ex);
        }

    }


    private void setInitalText(){
        int val = (int)(P_DEFAULT/P_MAX*1000);
        Log.i(TAG, val+" P");
        setSeekbarText(pTextView, getString(R.string.adjust_pid_p), val*P_MAX);
        pSeekbar.setProgress(val);
        val = (int)(I_DEFAULT/I_MAX*1000);
        Log.i(TAG, val+" I");
        setSeekbarText(iTextView, getString(R.string.adjust_pid_i), val*I_MAX);
        iSeekbar.setProgress(val);
        val = (int)(D_DEFAULT/D_MAX*1000);
        Log.i(TAG, val+" D");
        setSeekbarText(dTextView, getString(R.string.adjust_pid_d), val*D_MAX);
        dSeekbar.setProgress(val);

    }

    private void setSeekbarText(TextView txtView, String format, double value){
        txtView.setText(String.format(format, value+""));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        double val = progress/1000.0;
        TextView txtView = null;
        String format = "";
        int code = 0;
        switch(seekBar.getId()){
            case R.id.seekbarP:
                format = getString(R.string.adjust_pid_p);
                txtView = pTextView;
                val = (double)Math.round(val * P_MAX * 1000)/1000.0;
                code = OpenDroneUtils.CODE_PID_P;
                break;
            case R.id.seekbarI:
                format = getString(R.string.adjust_pid_i);
                txtView = iTextView;
                val = (double)Math.round(val * I_MAX * 1000)/1000.0;
                code = OpenDroneUtils.CODE_PID_I;
                break;
            case R.id.seekbarD:
                format = getString(R.string.adjust_pid_d);
                txtView = dTextView;
                val = (double)Math.round(val * D_MAX * 1000)/1000.0;
                code = OpenDroneUtils.CODE_PID_D;
                break;
            default:
                return;
        }
        setSeekbarText(txtView, format, val);
        sendMessage(val+"", code);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
