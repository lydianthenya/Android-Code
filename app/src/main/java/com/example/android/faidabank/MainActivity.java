package com.example.android.faidabank;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends AppCompatActivity {
    EditText search;
    RadioButton radioCustom, radioName, radioCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // radioCustom =(RadioButton) findViewById(R.id.radioNationalId);
        radioName = (RadioButton) findViewById(R.id.radioName);
        radioCustomer = (RadioButton) findViewById(R.id.radioCustomerId);

        search = (EditText) findViewById(R.id.edtSearch);

    }

    public void searchuser(View view) {
        String searchTerm= search.getText().toString();
        String selector="national_id";

        if (radioName.isChecked()){
            selector="fname";
        }
        else if (radioCustomer.isChecked()){
            selector="customer_id";
        }
        Intent x=new Intent(this,SearchActivity.class);
        x.putExtra("search",searchTerm);
        x.putExtra("selector",selector);
        startActivity(x);
    }

    public void adduser(View view) {
        Intent i = new Intent(this, CaptureScreen.class);
        startActivity(i);

    }
}
