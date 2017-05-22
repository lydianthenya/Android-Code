package com.example.android.faidabank;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

import static android.R.id.list;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "FAIDA";
    ImageView imageViewSearch;
    private ListView listView;
    EditText edtFname, edtOname, edtCustID, edtNatID, edtMNumber, edtAddress;
    FloatingActionButton edit_submit;
   String search,selector;
    boolean canSave= false;
    int db_id=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        search= getIntent().getStringExtra("search");
        selector=getIntent().getStringExtra("selector");
        imageViewSearch = (ImageView) findViewById(R.id.backdrop);
       edtFname = (EditText) findViewById(R.id.txtFetchName);
        edtOname = (EditText) findViewById(R.id.txtFetchOtherNames);
        edtCustID = (EditText) findViewById(R.id.txtfetchCustomerID);
        edtNatID = (EditText) findViewById(R.id.fetchNationalID);
        edtAddress = (EditText) findViewById(R.id.fetchAddress);
        edtMNumber = (EditText) findViewById(R.id.fetchMobileNumber);
        edit_submit= (FloatingActionButton) findViewById(R.id.edit_submit);

        edtOname.setEnabled(false);
        edtFname.setEnabled(false);
        edtAddress.setEnabled(false);
        edtMNumber.setEnabled(false);
        edtNatID.setEnabled(false);
        edtCustID.setEnabled(false);

        edit_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtFname.isEnabled()){
                    edtOname.setEnabled(true);
                    edtFname.setEnabled(true);
                    edtAddress.setEnabled(true);
                    edtMNumber.setEnabled(true);
                    edtNatID.setEnabled(true);
                    edtCustID.setEnabled(true);
                    edit_submit.setImageResource(android.R.drawable.ic_menu_save);
                    canSave=true;
                }

                if(canSave){

                   save_updates();
                }
            }
        });

        search();
    }
    private boolean validate() {

        boolean valid = true;

        String name = edtFname.getText().toString().trim();
        String oname = edtOname.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String customerID = edtCustID.getText().toString().trim();
        String mobilenumber = edtMNumber.getText().toString().toString();
        String nationalID = edtNatID.getText().toString().trim();


        if (name.isEmpty()) {
            edtFname.setError("enter a valid name");
            valid = false;
        } else {
            edtFname.setError(null);
        }
        if (oname.isEmpty()) {
            edtOname.setError("enter other Names");
            valid = false;
        } else {
            edtOname.setError(null);
        }
        if (mobilenumber.isEmpty()) {
            edtMNumber.setError("enter Mobile Number");
            valid = false;
        } else {
            edtMNumber.setError(null);
        }
        if (address.isEmpty()) {
            edtAddress.setError("enter a valid Address");
            valid = false;
        } else {
            edtAddress.setError(null);
        }
        if (customerID.isEmpty()) {
            edtCustID.setError("enter a valid email address");
            valid = false;
        } else {
            edtCustID.setError(null);
        }

        if (nationalID.isEmpty() || edtNatID.length() < 8 || nationalID.length() > 20) {
            edtNatID.setError("Invalid National ID");
            valid = false;
        } else {
            edtNatID.setError(null);
        }
        return valid;
    }

    public void save_updates() {
        String name = edtFname.getText().toString().trim();
        String oname = edtOname.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String customerID = edtCustID.getText().toString().trim();
        String mobilenumber = edtMNumber.getText().toString().toString();
        String nationalID = edtNatID.getText().toString().trim();

        if (!validate()) {
            Toast.makeText(this, "Please fill up respective fields", Toast.LENGTH_LONG).show();
            return;


        } else {


            RequestParams params = new RequestParams();
            params.put("fname", name);
            params.put("oname", oname);
            params.put("address", address);
            params.put("customer_id", customerID);
            params.put("mobile_number", mobilenumber);
            params.put("nationalid", nationalID);
            params.put("id",db_id);


            AsyncHttpClient client = new AsyncHttpClient();
            String url = Constants.BASE_URL + "updateCustomer.php";
            client.post(url, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "failed To upload");
                    throwable.printStackTrace();
                    Toast.makeText(SearchActivity.this, "Failed to update. Try again", Toast.LENGTH_SHORT).show();

                }


                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Log.d(TAG, "Success");

                    Toast.makeText(SearchActivity.this, responseString, Toast.LENGTH_SHORT).show();
                    if (responseString.contains("success")){
                        edtOname.setEnabled(false);
                        edtFname.setEnabled(false);
                        edtAddress.setEnabled(false);
                        edtMNumber.setEnabled(false);
                        edtNatID.setEnabled(false);
                        edtCustID.setEnabled(false);
                        edit_submit.setImageResource(android.R.drawable.ic_menu_edit);
                    }

                }
            });


        }

    }

    private void search() {

        AsyncHttpClient client = new AsyncHttpClient();
        String url = Constants.BASE_URL + "search.php";
        RequestParams params = new RequestParams();
        params.put("search", search);
        params.put("selector", selector);
        Log.d(TAG,selector+" "+search);
        client.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "failed To upload");
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                Log.d(TAG, "Success  "+responseString);
                try {
                    JSONArray array = new JSONArray(responseString);
                    JSONObject obj = array.getJSONObject(0);
                    String photo = obj.getString("customer_photo");
                     Picasso.with(getApplicationContext()).load(Constants.BASE_URL + photo).into(imageViewSearch);
                    /*get the user details and set text*/
                    String fname = obj.getString("fname");
                    String oname = obj.getString("oname");
                    String nationalid = obj.getString("national_id");
                    String address = obj.getString("address");
                    String customerid = obj.getString("customer_id");
                    String mobile_number = obj.getString("mobile_number");
                    db_id=obj.getInt("id");
                    //HashMap<String, String> custdetails = new HashMap<String, String>();

                    edtOname.setText(oname);
                    edtAddress.setText(address);
                    edtNatID.setText(nationalid);
                    edtCustID.setText(customerid);
                    edtMNumber.setText(mobile_number);
                    edtFname.setText(fname);



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


}
