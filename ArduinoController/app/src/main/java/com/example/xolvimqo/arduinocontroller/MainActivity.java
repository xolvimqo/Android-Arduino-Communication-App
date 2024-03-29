package com.example.xolvimqo.arduinocontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";

    // declare buttons and text inputs
    private Button buttonPin09, buttonPin10, buttonPin11;
    private EditText editTextIPAddress, editTextPortNumber;

    /* shared preferences objects used to save the IP address and port so that the user doesn't
    * have to type them next time
    * */
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // assign buttons
        buttonPin09 = (Button)findViewById(R.id.buttonPin09);
        buttonPin10 = (Button)findViewById(R.id.buttonPin10);
        buttonPin11 = (Button)findViewById(R.id.buttonPin11);

        // assign text inputs
        editTextIPAddress = (EditText)findViewById(R.id.editTextIPAddress);
        editTextPortNumber = (EditText)findViewById(R.id.editTextPortNumber);

        // set button listener (this class)
        buttonPin09.setOnClickListener(this);
        buttonPin10.setOnClickListener(this);
        buttonPin11.setOnClickListener(this);

        /* get the IP address and port number from the last time the user used the app,
        * put an empty string "" is this is the first time.
        * */
        editTextIPAddress.setText(sharedPreferences.getString(PREF_IP, ""));
        editTextPortNumber.setText(sharedPreferences.getString(PREF_PORT, ""));
    }

    @Override
    public void onClick(View view) {
        // get the pin number
        String parameterValue = "";
        // get the ip address
        String ipAddress = editTextIPAddress.getText().toString().trim();
        // get the port number
        String portNumber = editTextPortNumber.getText().toString().trim();

        // save the IP address and port for the next time the app is used
        editor.putString(PREF_IP, ipAddress); // set the ip address value to save
        editor.putString(PREF_PORT, portNumber); // set the port number to save
        editor.commit(); // save the IP and PORT

        // get the pin number from the button that was clicked
        if(view.getId() == buttonPin09.getId()){
            parameterValue = "09";
        } else if(view.getId() == buttonPin10.getId()){
            parameterValue = "10";
        } else if(view.getId() == buttonPin11.getId()){
            parameterValue = "11";
        }

        // execute HTTP request
        if(ipAddress.length() > 0 && portNumber.length() > 0){
            new HttpRequestAsyncTask(view.getContext(), parameterValue, ipAddress, portNumber, "pin").execute();
        }
    }

    public String sendOkHTTPRequest(String parameterValue, String ipAddress, String portNumber, String parameterName) {
        String serverResponse = "ERROR";

        try{
            // create an HTTP client
//            OkHttpClient httpClient = new OkHttpClient();
            // create an HTTP client with timeout settings
            OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
            // define the URL e.g. http://myIPAddress:myport/?pin=13 (to toggle pin 13 for example)
            URI website = new URI("http://" + ipAddress + ":" + portNumber + "/?"
                    + parameterName + "=" + parameterValue);
            Request httpRequest = new Request.Builder().url(HttpUrl.get(website)).build();
            Response httpResponse = httpClient.newCall(httpRequest).execute();
            // get the ip address server's reply
            InputStream content = null;
            content = httpResponse.body().byteStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
            serverResponse = bufferedReader.readLine();
            // close the connection
            content.close();
        } catch (ClientProtocolException e) {
            // HTTP error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            // IO error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (URISyntaxException e) {
            serverResponse = e.getMessage();
            e.printStackTrace();
        }
        return serverResponse;
    }

    /*
     * Description: Send an HTTP Get request to a specified ip address and port.
     * Also send a parameter "parameterName" with the value of "parameterValue".
     * @param parameterValue the pin number to toggle
     * @param ipAddress the ip address to send the request to
     * @param portNumber the port number of the ip address
     * @param parameterName
     * @return The ip address' reply text, or an ERROR message is it fails to receive one
     */
    public String sendRequest(String parameterValue, String ipAddress, String portNumber, String parameterName){
        String serverResponse = "ERROR";

        try {
            HttpClient httpClient = new DefaultHttpClient(); // create an HTTP client
            // define the URL e.g. http://myIPAddress:myport/?pin=13 (to toggle pin 13 for example)
            URI website = new URI("http://" + ipAddress + ":" + portNumber + "/?"
                    + parameterName + "=" + parameterValue);
            HttpGet getRequest = new HttpGet(); // create an HTTP GET object
            getRequest.setURI(website); // set the URL of the GET request
            HttpResponse response = httpClient.execute(getRequest); // execute the request
            // get the ip address server's reply
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
            serverResponse = bufferedReader.readLine();
            // close the connection
            content.close();
        } catch (ClientProtocolException e) {
            // HTTP error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            // IO error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // URL syntax error
            serverResponse = e.getMessage();
            e.printStackTrace();
        }
        // return the server's reply/response text
        return serverResponse;
    }

    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {
        // declare variables needed
        private String requestReply, ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        private String parameter;
        private String parameterValue;

        /* Description: The asyncTask class constructor. Assigns the values used in its other methods
         * @param context the application context, needed to create the dialog
         * @param parameterValue the pin number to toggle
         * @param ipAddress the ip address to send the request to
         * @param portNumber the port number of the ip address
         */
        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber, String parameter){
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From IP Address:")
                    .setCancelable(true)
                    .create();

            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.parameter = parameter;
        }

        /* Name doInBackground
         * Description: Sends the request to the ip address
         * @param voids
         * @return
         */
        @Override
        protected  Void doInBackground(Void... voids){
            alertDialog.setMessage("Data sent, waiting for reply from server...");
            if(!alertDialog.isShowing()){
                alertDialog.show();
            }
            requestReply = sendOkHTTPRequest(parameterValue, ipAddress, portNumber, parameter);
            return null;
        }

        /* Name: onPostExecute
         * Description: This function is executed after the HTTP request returns from the ip address.
         * The function sets the dialog's message with the reply text from the server and display the dialog
         * if it's not displayed already (in case it was closed by accident);
         * @param aVoid void parameter
         */
        @Override
        protected  void onPostExecute(Void aVoid){
            alertDialog.setMessage(requestReply);
            if(!alertDialog.isShowing()){
                alertDialog.show();
            }
        }

        /* Name: onPreExecute
         * Description: This function is executed before the HTTP request is sent to ip address.
         * The function will set the dialog's message and display the dialog.
         */
        @Override
        protected void onPreExecute() {
            alertDialog.setMessage("Sending data to server, please wait...");
            if(!alertDialog.isShowing()){
                alertDialog.show();
            }
        }
    }
}
