package ins.com.ins_project.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ins.com.ins_project.R;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";
    private Button button;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        button= (Button) view.findViewById(R.id.gotowifi);
        mContext = getActivity();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, ins.com.ins_project.Home.Wifi.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
