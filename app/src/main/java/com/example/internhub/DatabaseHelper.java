package com.example.internhub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "intern_hub.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_INTERNSHIPS = "internships";

    public static final String COL_ID = "id";
    public static final String COL_CLOUD_ID = "cloud_id";
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
                        COL_CLOUD_ID + " TEXT, " +
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
        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_internships_cloud_id " +
                        "ON " + TABLE_INTERNSHIPS + "(" + COL_CLOUD_ID + ")"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        // Add the cloud ID column to older databases.
        if (oldVersion < 3) {

            db.execSQL(
                    "ALTER TABLE " + TABLE_INTERNSHIPS +
                            " ADD COLUMN " + COL_CLOUD_ID + " TEXT"
            );

            db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_internships_cloud_id " +
                            "ON " + TABLE_INTERNSHIPS +
                            "(" + COL_CLOUD_ID + ")"
            );
        }

        // Remove the old local-only demo internship.
        if (oldVersion < 4) {

            db.delete(
                    TABLE_INTERNSHIPS,
                    COL_CLOUD_ID + " IS NULL AND " +
                            COL_TITLE + " = ? AND " +
                            COL_COMPANY + " = ?",
                    new String[]{
                            "Android Development Intern",
                            "CareerLink Demo Co."
                    }
            );
        }
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

    public long upsertCloudInternship(
            String cloudId,
            String title,
            String company,
            String domain,
            String skills,
            String description,
            String location,
            String workMode,
            String stipend,
            String deadline,
            String applicationUrl,
            String status
    ) {
        if (cloudId == null || cloudId.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_CLOUD_ID, cloudId);
        values.put(COL_TITLE, title == null ? "" : title);
        values.put(COL_COMPANY, company == null ? "" : company);
        values.put(COL_DOMAIN, domain == null ? "" : domain);
        values.put(COL_SKILLS, skills == null ? "" : skills);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_LOCATION, location);
        values.put(COL_WORK_MODE, workMode);
        values.put(COL_STIPEND, stipend);
        values.put(COL_DEADLINE, deadline);
        values.put(COL_APPLICATION_URL, applicationUrl);
        values.put(
                COL_STATUS,
                status == null ? "approved" : status
        );

        // Find this cloud record locally.
        // Also match the demo record if it has no cloud ID yet.
        Cursor cursor = db.query(
                TABLE_INTERNSHIPS,
                new String[]{COL_ID},
                COL_CLOUD_ID + " = ? OR (" +
                        COL_CLOUD_ID + " IS NULL AND " +
                        COL_TITLE + " = ? AND " +
                        COL_COMPANY + " = ?)",
                new String[]{
                        cloudId,
                        title == null ? "" : title,
                        company == null ? "" : company
                },
                null,
                null,
                null,
                "1"
        );

        long existingId = -1;

        try {
            if (cursor.moveToFirst()) {
                existingId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_ID)
                );
            }
        } finally {
            cursor.close();
        }

        if (existingId != -1) {

            // Update the existing local record.
            int rowsUpdated = db.update(
                    TABLE_INTERNSHIPS,
                    values,
                    COL_ID + " = ?",
                    new String[]{String.valueOf(existingId)}
            );

            return rowsUpdated > 0 ? existingId : -1;
        }

        // Insert a new local record.
        return db.insert(
                TABLE_INTERNSHIPS,
                null,
                values
        );
    }

    public int deleteCloudInternshipsNotIn(List<String> activeCloudIds) {

        SQLiteDatabase db = getWritableDatabase();

        // Only target records linked to Firestore.
        // Preserve local-only records whose cloud_id is NULL.
        String cloudRecordCondition =
                COL_CLOUD_ID + " IS NOT NULL AND "
                        + COL_CLOUD_ID + " != ''";

        // If no approved cloud records exist, remove only cloud-linked rows.
        if (activeCloudIds == null || activeCloudIds.isEmpty()) {
            return db.delete(
                    TABLE_INTERNSHIPS,
                    cloudRecordCondition,
                    null
            );
        }

        // Build placeholders for the active Firestore document IDs.
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < activeCloudIds.size(); i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }

        String selection = cloudRecordCondition
                + " AND " + COL_CLOUD_ID
                + " NOT IN (" + placeholders + ")";

        String[] selectionArgs =
                activeCloudIds.toArray(new String[0]);

        return db.delete(
                TABLE_INTERNSHIPS,
                selection,
                selectionArgs
        );
    }
}