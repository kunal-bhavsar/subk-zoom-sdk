package com.subk.zoomsdksample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.subk.zoomsdk.TextGenerator;

import org.w3c.dom.Text;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.subk.zoomsdksample.R.layout.activity_main);

        ((TextView)findViewById(R.id.txt_temp)).setText(TextGenerator.generate(20));
    }
}