package com.example.sifter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class OtherMailsActivity extends AppCompatActivity {

    TextView mOutputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_mails);
//        new GetMails(MainActivity.getEmail(), MainActivity.getPassowrd());
        new GetMails().execute();
        mOutputText = (TextView) findViewById(R.id.outputView);
        mOutputText.setText(MainActivity.getEmail() + " " + MainActivity.getPassowrd());
    }

    private class GetMails extends AsyncTask<Void, Void, List<String>>{

        private ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressBar2);
        private Exception mLastError;

        TextView mOutputText = (TextView) findViewById(R.id.outputView);
        String user = MainActivity.getEmail();
        String password = MainActivity.getPassowrd();
        String status = "";

//        GetMails(String user, String password) {
//            this.user = user;
//            this.password = password;
////            check("imap.mail.yahoo.com", "imap", user, password);
//            mOutputText.setText("working");
//        }

        public List<String> check(String host, String storeType, String user, String password)
        throws IOException{
            user = this.user;
            password = this.password;
            Folder folder = null;
            Store store = null;
            List<String> msgInfos = new ArrayList<String>();
            try {
//                Properties props = System.getProperties();
//                props.setProperty("mail.store.protocol", "imaps");
                Properties props = new Properties();

                final String finalUser = user;
                final String finalPassword = password;
                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(finalUser, finalPassword);
                    }
                });
                // session.setDebug(true);
                store = session.getStore("imaps");
                store.connect(host, user, password);

//                Folder[] f = store.getDefaultFolder().list();
//                for(Folder fd:f)
//                    System.out.println(">> "+fd.getName());

                folder = store.getFolder("Unwanted");
      /* Others GMail folders :
       * [Gmail]/All Mail   This folder contains all of your Gmail messages.
       * [Gmail]/Drafts     Your drafts.
       * [Gmail]/Sent Mail  Messages you sent to other people.
       * [Gmail]/Spam       Messages marked as spam.
       * [Gmail]/Starred    Starred messages.
       * [Gmail]/Trash      Messages deleted from Gmail.
       */
                folder.open(Folder.READ_ONLY);
                status = "opened folder";
                Message messages[] = folder.getMessages();

                for (int i = messages.length-1, n = -1; i > n; i--) {
                    Message message = messages[i];
                    String info = "Email Number " + (i + 1) + "\nSubject: " +
                            message.getSubject() + "\nFrom: " + message.getFrom()[0];
                    msgInfos.add(info);
//                    Object msgContent = message.getContent();
//
//                    String content = msgContent.toString();
//                    if (msgContent instanceof Multipart) {
//                        Multipart multipart = (Multipart) msgContent;
//                        for (int j = 0; j < multipart.getCount(); j++) {
//                            BodyPart bodyPart = multipart.getBodyPart(j);
//                            content = bodyPart.getContent().toString();
//                        }
//                    }
//                    // System.out.println("Text: " + getLink(message.getContent().toString()));
//                    System.out.println("Text: " + getLink(content));
//                    // break;

                }

                //close the store and folder objects
                folder.close(false);
                store.close();
                return msgInfos;

            } catch (NoSuchProviderException e) {
                e.printStackTrace();
                List<String> error = new ArrayList<>();
                error.add(e.getMessage());
                return error;
            } catch (MessagingException e) {
                e.printStackTrace();
                List<String> error = new ArrayList<>();
                error.add(e.getMessage());
                return error;
            } catch (Exception e) {
                e.printStackTrace();
                List<String> error = new ArrayList<>();
                error.add(e.getMessage());
                return error;
            }

        }

        public String getLink(String str) {
            int start = str.indexOf("https://");
            int end = str.indexOf("/", start+8);
            String link = "";
            if (start == -1 || end == -1) {
                link = "No link";
            } else {
                link = str.substring(start, end);
            }
            return link;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                List<String> msgs = check("imap.mail.yahoo.com", "imap", user, password);
//                List<String> msgs = new ArrayList<>();
                if (!msgs.isEmpty()) {
                    return msgs;
                } else {
                    List<String> test = new ArrayList<>();
                    test.add("failed");
                    return test;
                }
//                List<String> test = new ArrayList<>();
//                test.add("works");
//                return test;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return (List<String>) e;
            }
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText(status);
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.setVisibility(View.INVISIBLE);
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using JavaMail API:");
//                mOutputText.setText(TextUtils.join("\n", output));
                ListAdapter myAdapter = new ArrayAdapter<String>(OtherMailsActivity.this, android.R.layout.simple_list_item_1, output);
                ListView list = (ListView) findViewById(R.id.notifications);
                list.setAdapter(myAdapter);
            }
        }
    }
}
