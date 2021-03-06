package com.moez.QKSMS.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class SidebandDBSource {

    public static final String MESSAGE_SENT = "1";
    public static final String MESSAGE_UNSENT = "0";

    public static final String UW_MESSAGE_IS_SPAM = "SPAM,";
    public static final String UW_MESSAGE_IS_SCAM = "SCAM,";
    public static final String UW_MESSAGE_IS_FRAUD = "FRAUD,";
    public static final String UW_MESSAGE_IS_OK = "OK,";
    public static final String UW_MESSAGE_IS_UNKNOWN = "unknown,";

    // Database fields
    private SQLiteDatabase database;
    private MessageSidebandDBHelper dbHelper;

    //full column list of sms_sideband_db
    private String[] allColumnsSideband = { MessageSidebandDBHelper.SIDEBAND_COLUMN_ID,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_ADDRESSEE,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_SMISHING_LABEL,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_IS_EMAIL,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_EMAIL_FROM,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_EMAIL_BODY,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_ORIGIN_ADDRESS};

    //full column list of sms_privacy_db
    private String[] allColumnsPrivacy = { MessageSidebandDBHelper.PRIVACY_COLUMN_ID,
            MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID};

    public SidebandDBSource(Context context) {
        dbHelper = new MessageSidebandDBHelper(context);
    }


    //database managment calls
    public void openWrite() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void openRead() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    //Create SMS/MMS entry in sideband DB with basic parameters
    public Boolean createNewMessageSidebandDBEntry(String messagedb_id, long thread_id, String addressee) {
        ContentValues values = new ContentValues();
        addressee = stripChars(addressee);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID, messagedb_id);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SMISHING_LABEL,
                getConversationSmishingLabelByThreadID(thread_id));
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID, thread_id);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_ADDRESSEE, addressee);
        if(getThreadIsPrivate(thread_id)) {
            values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, MESSAGE_SENT);
        }
        openWrite();
        long insertId = database.insert(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, values);
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                allColumnsSideband, MessageSidebandDBHelper.SIDEBAND_COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        cursor.close();
        close();
        return true;
    }

    //Create SMS entry in sideband DB with extra SMS header information
    public Boolean createNewMessageSidebandDBEntry(String messagedb_id, long thread_id, String address, boolean is_email, String email_from, String email_body, String orgin_address) {
        ContentValues values = new ContentValues();
        address = stripChars(address);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID, messagedb_id);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SMISHING_LABEL,
                getConversationSmishingLabelByThreadID(thread_id));
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID, thread_id);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_ADDRESSEE, address);
        if(getThreadIsPrivate(thread_id)) {
            values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, MESSAGE_SENT);
        }
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_IS_EMAIL, is_email);
        if(email_from!=null)
            values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_EMAIL_FROM, email_from);
        if(email_body!=null)
            values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_EMAIL_BODY, email_body);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_ORIGIN_ADDRESS, orgin_address);
        openWrite();
        long insertId = database.insert(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, values);
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                allColumnsSideband, MessageSidebandDBHelper.SIDEBAND_COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        cursor.close();
        close();
        return true;
    }

    public String getMessageSidebandDbEntryByThreadID(long thread_id, String field) {
        String [] columns = { field };
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = { Long.toString(thread_id) };
        String returnval = "";

        openRead();
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                columns, where, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            returnval = cursor.getString(cursor.getColumnIndex(field));
        }
        cursor.close();
        close();
        return returnval;
    }


    public String getMessageSidebandDBEntryByArg(String messagedb_id, String field) {

        String [] columns = { field };
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID + "=?";
        String [] whereArgs = { messagedb_id};
        String returnval = "";

        openRead();
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                            columns, where, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            returnval = cursor.getString(cursor.getColumnIndex(field));
        }
        cursor.close();
        close();
        return returnval;

    }

    public int setConversationSidebandDBEntryByThreadID(long thread_id, String field, String newVal) {
        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(field,newVal);



        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};


        int returnval = -1;

        openWrite();

        returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                dataToUpdate, where, whereArgs);

        close();
        return returnval;
    }


    // TODO: We want to be careful that we don't update individual messages for something like
    //      the smishing label, so make this method more specific.
    public int setMessageSidebandDBEntryByArg(String messagedb_id, String field, String newVal) {

        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(field,newVal);
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID + "=?";
        String [] whereArgs = { messagedb_id};
        int returnval = -1;

        openWrite();

        returnval += database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                     dataToUpdate, where, whereArgs);

        close();
        return returnval;

    }


    /**
     * Return the smishing_marked_as label from the sideband DB for a given thread ID.
     * precondition: This method assumes all messages in the sideband DB with a given
     *  thread_id will have the same label.
     *  If the thread_id does not exist yet in the database, return an empty string.
     */
    // TODO: see getMessageSidebandDbEntryByThreadID as a good example.
    public String getConversationSmishingLabelByThreadID(long thread_id) {
        String [] columns = { MessageSidebandDBHelper.SIDEBAND_COLUMN_SMISHING_LABEL};
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};
        Cursor myCursor;

        // the label to return.
        String returnval = "";

        openRead();
        myCursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                columns, where, whereArgs, null, null, null);

        // Try to move to the first entry.
        if (myCursor.moveToFirst()) {
            // Get the label from the smishing_label column.
            returnval = myCursor.getString(myCursor.getColumnIndex(
                    MessageSidebandDBHelper.SIDEBAND_COLUMN_SMISHING_LABEL));
        }

        myCursor.close();
        close();

        return returnval;
    }

    //sms_privacy_db accessors
    public int clearPrivacyDBEntry(long thread_id) {

        String where = MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};
        int returnval;

        openRead();
        returnval = database.delete(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, where, whereArgs);
        close();

        //mark all messages to the addressee as unsent, meaning they will be pushed up on next update
        markAllThreadIDMsgUnsent(thread_id);

        return returnval;

    }


    public int setPrivacyDBEntry(long thread_id) {

        ContentValues values = new ContentValues();
        values.put(MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID, thread_id);
        int returnval;

        //Insert this thread into the database or ignore it if is already there (only 1 _real_ column in table so only conflict is itself)
        openWrite();
        returnval = (int)database.insertWithOnConflict(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        close();

        //mark all messages from this user as sent.  Effectively making them private
        markAllThreadIDMsgSent(thread_id);

        return returnval;

    }


    public boolean getThreadIsPrivate(long thread_id) {

        String [] columns = { MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID};
        String where = MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};
        Cursor myCursor;
        boolean returnval;

        openRead();
        myCursor = database.query(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, columns, where, whereArgs, null, null, null);


        returnval = myCursor.moveToFirst();
        myCursor.close();
        close();

        // TODO: Why is this code here?
        Cursor myCursor1;
        boolean returnval1;

        openRead();
        myCursor1 = database.rawQuery("Select * from " + MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB,  null);
        returnval1 = myCursor.moveToFirst();
        myCursor1.close();
        close();

        return returnval;
    }


    private int markAllThreadIDMsgSent(long thread_id) {
        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, MESSAGE_SENT);
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = { Long.toString(thread_id) };
        int returnval;

        openWrite();

        returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                dataToUpdate, where, whereArgs);

        close();
        return returnval;
    }

    private int markAllThreadIDMsgUnsent (long thread_id) {

        //Prep for update
        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, MESSAGE_UNSENT);
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};

        int returnval;

        openWrite();
        returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                dataToUpdate, where, whereArgs);
        close();

        return returnval;
    }

    private String stripChars (String str) {
        str = str.replace(" ","");
        str = str.replace("-","");
        str = str.replace("(","");
        str = str.replace(")","");

        return str;
    }
}