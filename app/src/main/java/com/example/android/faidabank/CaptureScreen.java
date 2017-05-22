package com.example.android.faidabank;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class CaptureScreen extends AppCompatActivity {
    private static final String TAG = "FaidaBank";
    ImageView customerphoto;
    private ProgressDialog progressDialog;
    static int PICK_IMAGE_REQUEST = 2000;
    EditText inputFname, inputOtherName, inputAddress, inputNationalId, inputMobileNumber, inputCustomerID;
    String imgPath = "";
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_screen);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FAIDA", refreshedToken);

        inputAddress = (EditText) findViewById(R.id.txtAddress);
        inputFname = (EditText) findViewById(R.id.txtFname);
        inputNationalId = (EditText) findViewById(R.id.txtID);
        inputMobileNumber = (EditText) findViewById(R.id.txtNumber);
        inputCustomerID = (EditText) findViewById(R.id.txtCustid);
        inputOtherName = (EditText) findViewById(R.id.txtonames);
        customerphoto = (ImageView) findViewById(R.id.customerPhoto);
        progressDialog = new ProgressDialog(this);
    }

    public void show_gallery(View view) {
        showFileChooser();
       /* String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FAIDA", refreshedToken);
        */
//        Intent intent = new Intent(this, SearchActivity.class);
//        startActivity(intent);
    }


    //STEP 2 Display gallery to allow the user to choose the photo
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //STEP 3 Display the selected image on the image view and set the path
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                customerphoto.setImageBitmap(bitmap);
                imgPath = getPath(filePath);
                Log.d("PATH", imgPath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private boolean validate() {

        boolean valid = true;

        String name = inputFname.getText().toString().trim();
        String oname = inputOtherName.getText().toString().trim();
        String address = inputAddress.getText().toString().trim();
        String customerID = inputCustomerID.getText().toString().trim();
        String mobilenumber = inputMobileNumber.getText().toString().toString();
        String nationalID = inputNationalId.getText().toString().trim();


        if (name.isEmpty()) {
            inputFname.setError("enter valid name");
            valid = false;
        } else {
            inputFname.setError(null);
        }
        if (oname.isEmpty()) {
            inputOtherName.setError("enter other name");
            valid = false;
        } else {
            inputOtherName.setError(null);
        }
        if (mobilenumber.isEmpty()) {
            inputMobileNumber.setError("enter Mobile Number");
            valid = false;
        } else {
            inputMobileNumber.setError(null);
        }
        if (address.isEmpty()) {
            inputAddress.setError("enter a valid Address");
            valid = false;
        } else {
            inputAddress.setError(null);
        }
        if (customerID.isEmpty()) {
            inputCustomerID.setError("enter a valid customer id");
            valid = false;
        } else {
            inputCustomerID.setError(null);
        }

        if (nationalID.isEmpty() || inputNationalId.length() < 8 || nationalID.length() > 20) {
            inputNationalId.setError("Invalid National ID");
            valid = false;
        } else {
            inputNationalId.setError(null);
        }
        return valid;
    }


    public void process(View view) {
        String name = inputFname.getText().toString().trim();
        String oname = inputOtherName.getText().toString().trim();
        String address = inputAddress.getText().toString().trim();
        final String customerID = inputCustomerID.getText().toString().trim();
        String mobilenumber = inputMobileNumber.getText().toString().toString();
        String nationalID = inputNationalId.getText().toString().trim();

        if (!validate()) {
            Toast.makeText(this, "Please fill up respective fields", Toast.LENGTH_LONG).show();
            return;


        } else {
            progressDialog.setMessage("Registering Please Wait...");

            File myFile = new File(imgPath);
            RequestParams params = new RequestParams();
            params.put("fname", name);
            params.put("oname", oname);
            params.put("address", address);
            params.put("customer_id", customerID);
            params.put("mobile_number", mobilenumber);
            params.put("nationalid", nationalID);


            try {
                params.put("fileToUpload", myFile);
            } catch (FileNotFoundException e) {
            }
          if(!imgPath.isEmpty()) {
              progressDialog.show();
              progressDialog.setCancelable(false);
              AsyncHttpClient client = new AsyncHttpClient();
              String url = Constants.BASE_URL + "addCustomer.php";
              client.post(url, params, new TextHttpResponseHandler() {
                  @Override
                  public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                      Log.d(TAG, "failed To upload");
                      throwable.printStackTrace();
                      Toast.makeText(CaptureScreen.this, "failed", Toast.LENGTH_SHORT).show();
                      progressDialog.dismiss();
                  }


                  @Override
                  public void onSuccess(int statusCode, Header[] headers, String responseString) {
                      Log.d(TAG, "Success " + responseString);
                      progressDialog.dismiss();
                      if (responseString.contains("success")) {
                          Toast.makeText(CaptureScreen.this, "Uploaded Succesfully", Toast.LENGTH_SHORT).show();
                          inputAddress.setText("");
                          inputCustomerID.setText("");
                          inputFname.setText("");
                          inputMobileNumber.setText("");
                          inputNationalId.setText("");
                          inputOtherName.setText("");
                          customerphoto.setImageResource(R.drawable.plus);
                          imgPath="";
                      }

                  }
              });
          }else {
              Toast.makeText(this, "You must attach an image. Tap the image to add", Toast.LENGTH_SHORT).show();
          }

        }

    }
}