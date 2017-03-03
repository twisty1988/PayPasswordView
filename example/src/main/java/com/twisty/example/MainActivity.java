package com.twisty.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.twisty.ppv.PayPasswordView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final PayPasswordView ppv = (PayPasswordView) findViewById(R.id.ppv);
        ppv.setOnInputDoneListener(new PayPasswordView.OnInputDoneListener() {
            @Override
            public void onInputDone(String res) {
                Toast.makeText(MainActivity.this, res, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
