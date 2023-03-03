package com.example.antilight;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private EditText mIpAddressEditText;
    private ListView mIpAddressListView;
    private List<String> mIpAddressList;
    private IpAddressListAdapter mListAdapter;


    private static final String IP_ADDRESS_WORK_TAG = "ip_address_work_tag";
    private static final long IP_ADDRESS_WORK_INTERVAL = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIpAddressEditText = findViewById(R.id.ip_address_edit_text);
        mIpAddressListView = findViewById(R.id.ip_address_list_view);
        mIpAddressList = new ArrayList<>();
        mListAdapter = new IpAddressListAdapter(this, mIpAddressList);
        mIpAddressListView.setAdapter(mListAdapter);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest ipAddressWorkRequest = new PeriodicWorkRequest.Builder(
                IpAddressWorker.class,
                IP_ADDRESS_WORK_INTERVAL,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .addTag(IP_ADDRESS_WORK_TAG)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                IP_ADDRESS_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                ipAddressWorkRequest
        );
    }

        Button addIpAddressButton = findViewById(R.id.add_ip_address_button);
        addIpAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = mIpAddressEditText.getText().toString();
                if (!ipAddress.isEmpty()) {
                    mIpAddressList.add(ipAddress);
                    mListAdapter.notifyDataSetChanged();
                    mIpAddressEditText.setText("");
                }
            }
        });
    }
}
public class IpAddressWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        List<String> ipAddressList = getIpAddressListFromSharedPreferences();
        for (String ipAddress : ipAddressList) {
            String pingResponse = getPingResponse(ipAddress);
            mListAdapter.setPingResponse(ipAddress, pingResponse);
            sendAlertIfNecessary(ipAddress, pingResponse);
        }
        return Result.success();
    }

    private List<String> getIpAddressListFromSharedPreferences() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.shared_preferences_file_key),
                Context.MODE_PRIVATE
        );
        String ipAddressListString = sharedPreferences.getString(
                getString(R.string.ip_address_list_key),
                null
        );
        if (ipAddressListString == null) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(ipAddressListString.split(","));
        }
    }

    private String getPingResponse(String ipAddress) {
        // Ping the IP address and return the response
    }

    private void sendAlertIfNecessary(String ipAddress, String pingResponse) {
        // Send an alert if the ping response indicates a problem
    }
}


public class IpAddressListAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mIpAddressList;
    private Map<String, String> mPingResponseMap;

    public IpAddressListAdapter(Context context, List<String> ipAddressList) {
        mContext = context;
        mIpAddressList = ipAddressList;
        mPingResponseMap = new HashMap<>();
    }

    public void setPingResponse(String ipAddress, String pingResponse) {
        mPingResponseMap.put(ipAddress, pingResponse);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mIpAddressList.size();
    }

    @Override
    public Object getItem(int position) {
        return mIpAddressList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, parent, false);
        }

        String ipAddress = mIpAddressList.get(position);
        String pingResponse = mPingResponseMap.get(ipAddress);

        TextView ipAddressTextView = view.findViewById(R.id.ip_address_list_view);
        ipAddressTextView.setText(ipAddress);

        TextView pingResponseTextView = view.findViewById(R.id.pingResult);
        if (pingResponse != null) {
            pingResponseTextView.setText(pingResponse);
        } else {
            pingResponseTextView.setText(R.string.ping_response_unknown);
        }

        return view;
    }
}

