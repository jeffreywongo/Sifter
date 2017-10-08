package com.example.sifter;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.ListHistoryResponse;


import java.io.IOException;
import java.lang.Thread;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.math.BigInteger;
import java.util.prefs.PreferenceChangeEvent;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by jeffreywongo on 7/27/2017.
 */

public class GmailListener extends IntentService {

    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

//    private com.google.api.services.gmail.Gmail mService = new com.google.api.services.gmail.Gmail.Builder(
//            transport, jsonFactory, mCredential)
//            .setApplicationName("Gmail API Android Quickstart")
//            .build();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    TextView mOutputText;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY };

    private GoogleAccountCredential mCredential;
    private String userId;
    private ResultReceiver rec;

    private DatabaseHelper database = DatabaseHelper.getInstance(this);

    public GmailListener() {
        super("GmailListener");
    }

    private NotificationCompat.Builder notification;
    private static final int uniqueId = 1234;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MESSAGE", "Background service started");
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
        mCredential = GmailActivity.mCredential;
        if (mCredential  == null) {
            mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        } else {
            Log.d("MESSAGE", "got mCredential from GmailActivity");
            mCredential = GmailActivity.mCredential;
        }
//        mCredential = GmailActivity.mCredential;
//        if (mCredential.getSelectedAccount() != )
//        GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(
//                this, Arrays.asList(SCOPES))
//                .setBackOff(new ExponentialBackOff());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MESSAGE", "onStartCommand called");
        super.onStartCommand(intent, flags, startId);
         /*
         used this if statement here because when the user opens the app while the service is
         still running, GmailActivity calls this method but onHandleIntent is not called
         this will repopulate the list in that case
         */
        if (rec != null) {
            // use this receiver to fill listview in GmailActiity
            fillListView();
        }
        return START_REDELIVER_INTENT;
    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d("MESSAGE", "ondestroy!");
//        Intent broadcastIntent = new Intent(this, ServiceRestarter.class);
//        sendBroadcast(broadcastIntent);
//    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        Log.d("MESSAGE", "onHandleIntent started");
        // get the receiver and do stuff
        rec = workIntent.getParcelableExtra("receiver");
        // use this receiver to fill listview in GmailActiity
        fillListView();
        // get the userId and do stuff
        userId = workIntent.getStringExtra("userId");
        if (mCredential.getSelectedAccountName() != userId) {
            mCredential.setSelectedAccountName(userId);
        }
        try {
            Gmail service = getGmailService();
            poll(service, userId, "INBOX");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    public Gmail getGmailService() throws IOException {
        return new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Gmail API Android Quickstart")
                .build();
    }

    /**
     * Makes information from List of MessagePartHeaders more accessible
     * Organizes the different fields like From and Subject into a hashmap
     * for the specific message
     * @param list List of MessagePartHeaders
     * @return hashmap of fields of the header of an email and their values
     */
    public Hashtable<String, String> extractTable(List<MessagePartHeader> list) {
        Hashtable<String, String> table = new Hashtable<>();
        for (int i = 0; i < list.size(); i++) {
            MessagePartHeader headers = list.get(i);
            String key = headers.getName();
            String value = headers.getValue();
            table.put(key, value);
        }
        return table;
    }

    /**
     * Gets a list of messages from user's account
     * @param user email address of user
     * @param labels list of labels of messages to get
     * @return list of messages
     */
    public List<Message> getMessageInfo(String user, List<String> labels)
            throws IOException {
        Gmail service = getGmailService();
        ListMessagesResponse listMessages =
                service.users().messages().list(user).setLabelIds(labels).execute();
        //        if (messages.size() == 0) {
//            System.out.println("No messages found");
//        }
        return listMessages.getMessages();
    }

    /**
     * returns a list of message updates to a mailbox
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param startHistoryId Only return Histories at or after startHistoryId.
     * @throws IOException
     * @return a list of new messages
     */
    public List<Message> listHistory(Gmail service, String userId, String labelId, BigInteger startHistoryId)
            throws IOException {
        try {
            // list of messages to be updated and returned
            List<Message> newMessages = new ArrayList<>();
            // list of histories
            List<History> histories = new ArrayList<History>();
            // list to limit the types of messages to get history of
            List<String> messageAdded = new ArrayList<>();
            messageAdded.add("messageAdded");
            ListHistoryResponse response =
                    service.users().history().list(userId).
                            setLabelId(labelId).setHistoryTypes(messageAdded).
                            setStartHistoryId(startHistoryId).execute();
            // if (response.isEmpty()) {
            //     return newMessages;
            // }
            histories.addAll(response.getHistory());
            for (History history : histories) {
                List<Message> messages = history.getMessages();
                for (Message message : messages) {
                    // if (!newMessages.contains(message)) {
                    //     newMessages.add(message);
                    // }
                    newMessages.add(message);
                }
            }
            return newMessages;
        } catch (NullPointerException e) {
            List<Message> empty = new ArrayList<>();
            return empty;
        } /*catch (IOException e) {
            // return listHistory(service, userId, labelId, startHistoryId);
            List<Message> empty = new ArrayList<>();
            return empty;
        }*/
    }

    public void fillListView() {
        if (!database.isEmpty()) {
            Log.d("MESSAGE", "database is not empty so populate listview");
            List<String> dbinfo = database.makeList();
            for (String info : dbinfo) {
//                Log.d("MESSAGE", info);
                Bundle bundle = new Bundle();
                bundle.putString("messageInfo", info);
                Log.d("MESSAGE", "sent message in bundle");
                rec.send(Activity.RESULT_OK, bundle);
            }
        }
    }

    /**
     * polls the mail folder for new messages and adds them to a database
     * @param service the gmail service to use
     * @param userId your gmail account username
     * @param labelId the mailbox you want to search
     */
    public void poll(Gmail service, String userId, String labelId)
            throws IOException {
        Log.d("MESSAGE", "Polling mailbox");
        List<String> labels = new ArrayList();
        labels.add(labelId);
        // list the messages in the mailbox
        List<Message> messages = getMessageInfo(userId, labels);
        // get the most recent message
        // if there's nothing to parse then keep searching until there is
        Log.d("MESSAGE", "Try to get the most recent message");
        while (messages.isEmpty()) {
            messages = getMessageInfo(userId, labels);
        }
        Log.d("MESSAGE", "Got the most recent message");
        Message newest = messages.get(0);
        Message fullNewest = service.users().messages().get(userId,
                newest.getId()).execute();
        BigInteger firstId = fullNewest.getHistoryId();
        // check database to see if there is a stored history id. If so use that
        if (database.getMostRecentHistId() != null) {
            Log.d("MESSAGE", "Got most recent histId from db");
            firstId = database.getMostRecentHistId();
        }
        // initialize list of message history
        List<Message> newMessages = new ArrayList<>();
//        System.out.println(histId);
        while (true) {
            Log.d("MESSAGE", "While loop started");
            try {
                newMessages = listHistory(service, userId, labelId, firstId);
                if (newMessages.isEmpty()) {
                    Log.d("MESSAGE", "No new message");
                    continue;
                }
                if (newMessages.get(newMessages.size() - 1) != newest) {
                    Log.d("MESSAGE", "there is a new message");
                    int count = 0;
                    for (int i = 0; i < newMessages.size(); i++) {
                        Message message = newMessages.get(i);
                        Log.d("MESSAGE", message.getId());
                        // extract information from the message for now
                        // I'll narrow it down to social media sites later
//                        String messageInfo = getSocialMedia(service, userId, message);

                        // add message info to the database and print it to log
                        database.addMessage(service, userId, message);
                        Log.d("MESSAGE", "message added to database");
//                        Log.d("MESSAGE", database.toString());
                        // add the message to GmailActivity ArrayList
                        List<String> fields = new ArrayList<>();
                        // ["To", "From", "Subject"];
                        fields.add("To");
                        fields.add("From");
                        fields.add("Subject");
                        Message fullMessage = service.users().messages().get(userId,
                                message.getId()).setFormat("full").setMetadataHeaders(fields).execute();
                        List<MessagePartHeader> part = fullMessage.getPayload().getHeaders();
                        Hashtable table = extractTable(part);
                        String info = table.get("From") + "|" +
                                table.get("Subject");
                        String subject;
                        String sender;
                        if (info.contains("<")) {
                            sender = info.substring(0, info.indexOf("<") - 1);
                            subject = info.substring(info.indexOf("|") + 1, info.length());
                        } else {
                            sender = info.substring(0, info.indexOf("|") - 1);
                            subject = info.substring(info.indexOf("|") + 1, info.length());
                        }
                        // create notification contents
                        notification.setSmallIcon(R.drawable.icon);
                        notification.setWhen(System.currentTimeMillis());
                        notification.setContentTitle(subject);
                        notification.setContentText(sender);
                        // create intent for when the notification is clicked
                        Intent intent = new Intent(this, GmailActivity.class);
                        // this pending intent allows phone to open the app
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notification.setContentIntent(pendingIntent);
                        // build notification and issue it
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // send the message information in a bundle to the ResultReceiver callback
                        // in GmailActivity
                        Bundle bundle = new Bundle();
                        bundle.putString("messageInfo", info);
                        rec.send(Activity.RESULT_OK, bundle);
                        manager.notify(uniqueId, notification.build());
                    }
                    newest = newMessages.get(newMessages.size() - 1);
                    fullNewest = service.users().messages().get(userId,
                            newest.getId()).setFormat("metadata").execute();
                    firstId = fullNewest.getHistoryId();
                }
            } catch (IOException e) {
                newMessages = listHistory(service, userId, labelId, firstId);
                continue;
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
