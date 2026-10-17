package com.example.internhub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "intern_hub.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_INTERNSHIPS = "internships";

    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_COMPANY = "company";
    public static final String COL_DOMAIN = "domain";
    public static final String COL_SKILLS = "required_skills";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_LOCATION = "location";
    public static final String COL_WORK_MODE = "work_mode";
    public static final String COL_STIPEND = "stipend";
    public static final String COL_DEADLINE = "deadline";
    public static final String COL_APPLICATION_URL = "application_url";
    public static final String COL_STATUS = "status";
    public static final String COL_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createInternshipsTable =
                "CREATE TABLE IF NOT EXISTS " + TABLE_INTERNSHIPS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_TITLE + " TEXT NOT NULL, " +
                        COL_COMPANY + " TEXT NOT NULL, " +
                        COL_DOMAIN + " TEXT NOT NULL, " +
                        COL_SKILLS + " TEXT NOT NULL, " +
                        COL_DESCRIPTION + " TEXT, " +
                        COL_LOCATION + " TEXT, " +
                        COL_WORK_MODE + " TEXT, " +
                        COL_STIPEND + " TEXT, " +
                        COL_DEADLINE + " TEXT, " +
                        COL_APPLICATION_URL + " TEXT, " +
                        COL_STATUS + " TEXT NOT NULL DEFAULT 'approved', " +
                        COL_CREATED_AT + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ")";

        db.execSQL(createInternshipsTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        // Preserve existing records.
        // Add explicit database migrations here when the schema changes.
        onCreate(db);
    }

    public long insertInternship(
            String title,
            String company,
            String domain,
            String skills,
            String description,
            String location,
            String workMode,
            String stipend,
            String deadline,
            String applicationUrl
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_TITLE, title.trim());
        values.put(COL_COMPANY, company.trim());
        values.put(COL_DOMAIN, domain.trim());
        values.put(COL_SKILLS, skills.trim());
        values.put(COL_DESCRIPTION, description);
        values.put(COL_LOCATION, location);
        values.put(COL_WORK_MODE, workMode);
        values.put(COL_STIPEND, stipend);
        values.put(COL_DEADLINE, deadline);
        values.put(COL_APPLICATION_URL, applicationUrl);
        values.put(COL_STATUS, "approved");

        return db.insert(TABLE_INTERNSHIPS, null, values);
    }

    public Cursor getApprovedInternships() {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_INTERNSHIPS,
                null,
                COL_STATUS + " = ?",
                new String[]{"approved"},
                null,
                null,
                COL_CREATED_AT + " DESC"
        );
    }
}