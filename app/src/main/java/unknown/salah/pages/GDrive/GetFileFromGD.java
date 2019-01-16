package unknown.salah.pages.GDrive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import unknown.salah.DBHelper.DBHelper;
import unknown.salah.MainActivity;
import unknown.salah.R;

public class GetFileFromGD extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private DBHelper db;
    private static final String TAG = "google_drive_namaz";
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_SELECT = 102;
    private static final int REQUEST_CODE_RESOLUTION = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        Log.i(TAG, "Building the client");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "In onStart() - connecting...");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            Log.i(TAG, "In onStop() - disConnecting...");
            mGoogleApiClient.disconnect();
        }
        super.onPause();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "in onConnected() - connected");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{DriveFolder.MIME_TYPE, "text/plain"})
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(
                    intentSender, REQUEST_CODE_SELECT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.i(TAG, "Unable to send intent", e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        switch (i) {
            case 1:
                Log.i(TAG, "Connection suspended - Cause: " + "Service disconnected");
                break;
            case 2:
                Log.i(TAG, "Connection suspended - Cause: " + "Connection lost");
                break;
            default:
                Log.i(TAG, "Connection suspended - Cause: " + "Unknown");
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed - result: " + result.toString());
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        try {
            Log.i(TAG, "trying to resolve the Connection failed error...");
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.i(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "in onActivityResult() - triggered on pressing Select");
        switch (requestCode) {
            case REQUEST_CODE_SELECT:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.i(TAG, "Selected folder's Resource ID: " + driveId.getResourceId());

                    DriveFile selectedFile = Drive.DriveApi.getFile(mGoogleApiClient, driveId);

                    selectedFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(contentsOpenedCallback);


                }
                break;
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "in onActivityResult() - resolving connection, connecting...");
                    mGoogleApiClient.connect();

                }
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }

    ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(getApplication(), getResources().getString(R.string.gdrive_dosya_acma), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DriveContents contents = result.getDriveContents();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                    List<String> nmzInfos = new ArrayList<String>();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            nmzInfos.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (nmzInfos != null) {
                        Log.i(TAG, "List will include namaz Info");
                        contents.discard(mGoogleApiClient);

                        db = new DBHelper(getApplicationContext());
                        try {
                            Cursor cursor;
                            for (int i = 0; i < nmzInfos.size(); i++) {
                                JSONObject jsonParams = new JSONObject(nmzInfos.get(i));

                                cursor = db.getInfoAll(jsonParams.getInt("NAMAZ_TARIH"),jsonParams.getInt("NAMAZ_VAKIT"));

                                if (cursor.moveToFirst() && cursor.getCount() > 0) {
                                    db.updateNamazVakitDeger(jsonParams.getInt("NAMAZ_TARIH"), jsonParams.getInt("NAMAZ_VAKIT"), jsonParams.getInt("NAMAZ_VAKIT_DEGER"));
                                } else {
                                    db.insertNamaz(jsonParams.getInt("NAMAZ_TARIH"), jsonParams.getInt("NAMAZ_VAKIT"), jsonParams.getInt("NAMAZ_VAKIT_DEGER"));
                                }
                            }
                            Log.i(TAG, "in onActivityResult() - JSON parse");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            };
}
