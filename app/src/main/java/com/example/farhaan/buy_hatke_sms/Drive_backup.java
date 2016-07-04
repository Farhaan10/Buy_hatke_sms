package com.example.farhaan.buy_hatke_sms;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.ParentReference;

/**
 * Created by Farhaan on 02-07-2016.
 */

public class Drive_backup extends Activity {
    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final int REQUEST_AUTHORIZATION = 2;
    int i = 0;
    private boolean updatefolder = false;
    String str = "";

    private static Drive service;
    private GoogleAccountCredential credential;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        long timeMillis = smsInboxCursor.getColumnIndex("date");
        Date date = new Date(timeMillis);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
        String dateText = format.format(date);

        if(indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        do {
            str += smsInboxCursor.getString(indexAddress) + "\n" + smsInboxCursor.getString(indexBody) + "at" + dateText + "\n\n";
        } while (smsInboxCursor.moveToNext());

        // had to update this part to a collection of String objects instead of a single String obj ref
        // also tested updating DRIVE to DRIVE_FILE both seem to work fine though the more specific the better
        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(new String[] {DriveScopes.DRIVE}));
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);

        // check readme for other changes

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                        saveFileToDrive();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    saveFileToDrive();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }
                break;
        }
    }

    private void saveFileToDrive() {
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    updatefolder = i != 0;

                    showToast("Start process");

                    File folder = null;
                    if(!updatefolder)
                        folder = createBackupFolder();

                    File body = new File();
                    body.setTitle("Sms Backup No " + i);
                    body.setMimeType("text/plain");
                    body.setDescription("Backup sms");

                    if(updatefolder)
                        body.setParents(Arrays.asList(new ParentReference().setId("appdata")));
                    else body.setParents(Arrays.asList(new ParentReference().setId(folder.getId())));

                    ByteArrayContent content = ByteArrayContent.fromString("text/plain", str);
                    File file = service.files().insert(body, content).execute();

                    if (file != null) {
                        showToast("backup uploaded: " + file.getTitle());
                        //startCameraIntent();
                        Intent intent = new Intent(Drive_backup.this, MainActivity.class);
                        startActivity(intent);
                    }
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private File createBackupFolder() {

                System.out.println("Inside createBackupFolder");
                File folder = null;
                try {
                    folder = _createFolder();
                } catch (UserRecoverableAuthIOException e) {
                    Intent intent = e.getIntent();
                    startActivityForResult(intent, REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    Log.e("MainActivity", "Error in create folder", e);
                }
                return folder;
            }
        });
        t.start();
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .build();
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File _createFolder() throws UserRecoverableAuthIOException,
            IOException {

        System.out.println("Inside _createFolder");
        if (updatefolder)
            return null;

        File folder = _isFolderExists();
        if (folder != null)
            return folder;

        File body = new File();
        body.setTitle("backup");
        body.setMimeType("application/vnd.google-apps.folder");

        File file = service.files().insert(body).execute();
        return file;

    }

    private File _isFolderExists() throws UserRecoverableAuthIOException,
            IOException {

        System.out.println("Inside _isFolderExists");
        Files.List request = service
                .files()
                .list()
                .setQ("mimeType = '" + "application/vnd.google-apps.folder" + "' and title = '"
                        + "backup" + "' ");

        FileList files = request.execute();

        if (files != null) {
            for (File file : files.getItems()) {
                return file;
            }
        }
        return null;
    }
}