package com.example.sifter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ListView;

import com.google.api.client.util.Data;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by jeffreywongo on 7/30/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "messages.db";
    public static final String TABLE_NAME = "messages_table";
    public static final String MESSAGE_ID = "MessageId";
    public static final String HISTORY_ID = "HistoryId";
    public static final String TIMESTAMP = "Timestamp";
    public static final String SENDER = "Sender";
    public static final String SUBJECT = "Subject";

    // create a private instance of this database to ensure that no other activities can change
    // it
    private static DatabaseHelper databaseInstance;

    // constructor is private because we want to use the same db and not create a new one every time
    // we want access
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * initializes and/or returns a static instance of DataBaseHelper to ensure that the same db
     * can be accessed by multiple activities
     * @param context of Activity
     * @return an instance of DatabaseHelper
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (databaseInstance == null) {
            databaseInstance = new DatabaseHelper(context);
        }
        return databaseInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                MESSAGE_ID + " VARCHAR(200), " +
                HISTORY_ID + " INT, " +
                TIMESTAMP + " DOUBLE PRECISION, " +
                SENDER + " VARCHAR(200), " +
                SUBJECT + " VARCHAR(500)" +
                ");";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Makes information from List of MessagePartHeaders more accessible
     * Organizes the different fields like From and Subject into a hashmap
     * for the specific message
     * @param list List of MessagePartHeaders
     * @return hashmap of fields of the header of an email and their values
     */
    public Hashtable extractTable(List<MessagePartHeader> list) {
        Hashtable table = new Hashtable();
        for (int i = 0; i < list.size(); i++) {
            MessagePartHeader headers = list.get(i);
            String key = headers.getName();
            String value = headers.getValue();
            table.put(key, value);
        }
        return table;
    }

    /*
     * adds a new row to database
     * @param service gmail service
     * @param userId user's gmail address
     * @param message the message to be parsed
     */
    public void addMessage(Gmail service, String userId, Message message) throws IOException {
        List<String> fields = new ArrayList<>();
        // ["To", "From", "Subject"];
        fields.add("To");
        fields.add("From");
        fields.add("Subject");
        Message fullMessage = service.users().messages().get(userId,
                message.getId()).setFormat("full").setMetadataHeaders(fields).execute();

        // accessing all header(s):
        List<MessagePartHeader> part = fullMessage.getPayload().getHeaders();
        // accessing body data:e
        // String body = fullMessage.getPayload().getBody().getData();
        // System.out.println(part);
        String from;
        String subject;
        if (part != null) {

            Hashtable table = extractTable(part);
//            if (table.get("From") == "Facebook") {
            from = "" + table.get("From");
            subject = "" + table.get("Subject");
//            Log.d("MESSAGE", from);
//            Log.d("MESSAGE", subject);

            ContentValues values = new ContentValues();
            values.put(MESSAGE_ID, fullMessage.getId());
            values.put(HISTORY_ID, fullMessage.getHistoryId().intValue());
            values.put(TIMESTAMP, fullMessage.getInternalDate());
            values.put(SENDER, from);
            values.put(SUBJECT, subject);

            // insert contents into database
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            sqLiteDatabase.insert(TABLE_NAME, null, values);
            sqLiteDatabase.close();
        }

        // fill out the columns
    }

    /*
     * deletes a message from the database
     * @param message the message to be deleted
     */
    public void deleteMessage(Message message) {
        String messageId = message.getId();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_NAME + "WHERE " + MESSAGE_ID + " = " +
                messageId + ";");
    }

    /*
     * print info in database into a list
     * @return string of info
     */
    public List<String> makeList() {
        List<String> list = new ArrayList<>();
        Log.d("MESSAGE", "Called makeList()");
        String dbString = "";
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;

        // cursor to point to location of row
        Log.d("MESSAGE", "Instantiate cursor");
        Cursor c = sqLiteDatabase.rawQuery(query, null);
        String[] columnNames = c.getColumnNames();
        // move it to the first row
        c.moveToFirst();
        do {
            Log.d("MESSAGE", "loop through the database to populate listview");
            for (String col : columnNames) {
                if (col.equals(SENDER)) {
                    dbString += c.getString(c.getColumnIndex(col)) + "|";
                } else if (col.equals(SUBJECT)) {
                    dbString += c.getString(c.getColumnIndex(col));
                }
            }
            Log.d("MESSAGE", dbString);
            list.add(dbString);
            dbString = "";
        } while (c.moveToNext());
        c.close();
        return list;
//        String tableString = String.format("Table %s:\n", TABLE_NAME);
//        Cursor allRows  = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
//        tableString += cursorToString(allRows);
//        return tableString;
    }

    /**
     * gets the history id of the most recent message in the database
     * @return the most recent history
     */
    public BigInteger getMostRecentHistId() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor c = sqLiteDatabase.rawQuery(query, null);
        c.moveToLast();
        try {
            String histId = c.getString(c.getColumnIndex(HISTORY_ID));
            c.close();
            return new BigInteger(histId);
        } catch (CursorIndexOutOfBoundsException e) {
            c.close();
            return null;
        }
    }

    /**
     * checks if the database is empty or not
     * @return boolean for if it's empty
     */
    public boolean isEmpty() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor c = sqLiteDatabase.rawQuery(query, null);
        boolean empty = !c.moveToFirst();
        c.close();
        return empty;
    }

//    public String cursorToString(Cursor cursor){
//        String cursorString = "";
//        if (cursor.moveToFirst() ){
//            String[] columnNames = cursor.getColumnNames();
//            for (String name: columnNames)
//                cursorString += String.format("%s ][ ", name);
//            cursorString += "\n";
//            do {
//                for (String name: columnNames) {
//                    cursorString += String.format("%s ][ ",
//                            cursor.getString(cursor.getColumnIndex(name)));
//                }
//                cursorString += "\n";
//            } while (cursor.moveToNext());
//        }
//        return cursorString;
//    }
}
