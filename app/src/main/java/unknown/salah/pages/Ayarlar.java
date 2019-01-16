package unknown.salah.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
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
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import unknown.salah.DBHelper.DBHelper;
import unknown.salah.MainActivity;
import unknown.salah.Model.NamazModel;
import unknown.salah.R;
import unknown.salah.pages.GDrive.ApiClientAsyncTask;
import unknown.salah.pages.GDrive.GetFileFromGD;

public class Ayarlar extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private DBHelper db;
    private List<NamazModel> namazInfos;
    private Fragment fragment = null;

    public static final String NAMAZ_COLUMN_TARIH = "namaz_tarih";
    public static final String NAMAZ_COLUMN_VAKIT = "namaz_vakit";
    public static final String NAMAZ_COLUMN_VAKIT_DEGER = "namaz_vakit_deger";

    private static final int REQUEST_CODE_SELECT = 102;

    private static final String TAG = "google_drive_namaz";
    private static final String SELECTED_DATE = "selectedDate";
    private GoogleApiClient mGoogleApiClient;
    private boolean fileOperation = false;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private String impNamazJson;

    private DriveId driveId;


    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.fragment_ayarlar);
        db = new DBHelper(getApplicationContext());

        MaterialRippleLayout saveDrive = (MaterialRippleLayout) findViewById(R.id.saveDrive);
        MaterialRippleLayout importDB = (MaterialRippleLayout) findViewById(R.id.importDB);

        saveDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileOperation = true;
                getBackupNamazInfos();
                try {
                    impNamazJson = prepJSONforDrive();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                buildGoogleApiClient();

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
        });

        importDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildGoogleApiClient();
                Intent intent = new Intent(getApplicationContext(), GetFileFromGD.class);
                startActivity(intent);
            }
        });
    }

    private void getBackupNamazInfos() {
        Cursor cursor = db.getAllNamazInfo();
        cursor.moveToFirst();
        namazInfos = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                namazInfos.add(new NamazModel(cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_TARIH)),
                        cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT)),
                        cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT_DEGER))));
                cursor.moveToNext();
            }
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_data_DB), Toast.LENGTH_SHORT).show();
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    private String prepJSONforDrive() throws JSONException {
        JSONObject[] nmzJSONArr = new JSONObject[namazInfos.size()];

        StringBuilder nmzBuilder = new StringBuilder();

        for (int i = 0; i < namazInfos.size(); i++) {
            JSONObject nmz = new JSONObject();

            nmz.put("NAMAZ_TARIH", namazInfos.get(i).getNamazTarih());
            nmz.put("NAMAZ_VAKIT", namazInfos.get(i).getNamazVakit());
            nmz.put("NAMAZ_VAKIT_DEGER", namazInfos.get(i).getNerdeKildi());
            nmzJSONArr[i] = nmz;

            nmzBuilder.append(nmzJSONArr[i].toString() + "\n");
        }
        return nmzBuilder.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "In onStart() - connecting...");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
           mGoogleApiClient.disconnect();
        }
        super.onPause();
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
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connection succeed");
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

    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {

                    if (result.getStatus().isSuccess()) {

                        if (fileOperation) {

                            CreateFileOnGoogleDrive(result);

                        }
                    }
                }
            };

    /**
     * Create a file in root folder using MetadataChangeSet object.
     *
     * @param result
     */
    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result) {

        final DriveContents driveContents = result.getDriveContents();

        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                try {
                    writer.write(impNamazJson);
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(getResources().getString(R.string.app_name))
                        .setMimeType("text/plain")
                        .setStarred(true).build();

                if (null == driveId.asDriveFolder()) {
                    DriveFile selectedFile = Drive.DriveApi.getFile(mGoogleApiClient, driveId);
                    new EditContentsAsyncTask(Ayarlar.this).execute(selectedFile);
                } else {
                    DriveFolder folder = driveId.asDriveFolder();
                    folder.createFile(mGoogleApiClient, changeSet, driveContents).
                                setResultCallback(fileCallback);
                }
            }
        }.start();
    }

    public class EditContentsAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {

        public EditContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            DriveFile file = args[0];
            try {
                DriveApi.DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                getBackupNamazInfos();
                try {
                    impNamazJson = prepJSONforDrive();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DriveContents driveContents = driveContentsResult.getDriveContents();
                OutputStream outputStream = driveContents.getOutputStream();
                outputStream.write(impNamazJson.getBytes());
                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null).await();
                return status.getStatus().isSuccess();
            } catch (IOException e) {
                Log.e(TAG, "IOException while appending to the output stream", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(getApplication(), getResources().getString(R.string.gdrive_dosya_yazma), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getApplication(), getResources().getString(R.string.gdrive_yazma), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Handle result of Created file
     */
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    if (result.getStatus().isSuccess()) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.drive_upload_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.drive_upload_fail), Toast.LENGTH_SHORT).show();
                    }
                }
            };

    private void buildGoogleApiClient() {
        Log.i(TAG, "Building the client");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "in onActivityResult() - triggered on pressing Select");
        switch (requestCode) {
            case REQUEST_CODE_SELECT:
                if (resultCode == RESULT_OK) {
                    driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.i(TAG, "Selected folder's Resource ID: " + driveId.getResourceId());

                    Drive.DriveApi.newDriveContents(mGoogleApiClient)
                            .setResultCallback(driveContentsCallback);
                }
                finish();
                break;
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "in onActivityResult() - resolving connection, connecting...");
                    mGoogleApiClient.connect();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
