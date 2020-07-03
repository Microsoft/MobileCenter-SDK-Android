/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.utils.storage;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unused")
public class SQLiteUtilsAndroidTest {

    /**
     * Context instance.
     */
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    /**
     * Database schema.
     */
    private static ContentValues mSchema;

    /**
     * Test database name.
     */
    private static final String DATABASE_NAME = "test-database";

    @BeforeClass
    public static void setUpClass() {
        sContext = InstrumentationRegistry.getTargetContext();

        /* Create a test schema. */
        mSchema = generateContentValues();
    }

    @After
    public void tearDown() {
        sContext.deleteDatabase(DATABASE_NAME);
    }

    private static ContentValues generateContentValues() {
        ContentValues values = new ContentValues();
        values.put("COL_STRING_NULL", (String) null);
        return values;
    }

    @Test
    public void test() {
        new SQLiteUtils();
        assertNotNull(SQLiteUtils.newSQLiteQueryBuilder());
    }

    @Test
    public void testTableMethods() {
        DatabaseManager databaseManager = new DatabaseManager(sContext, DATABASE_NAME, "test.setMaximumSize", 1, mSchema, mock(DatabaseManager.Listener.class));

        String tableName = java.util.UUID.randomUUID().toString();
        ContentValues contentValues = new ContentValues();
        contentValues.put("someKey", "someValue");
        SQLiteUtils.createTable(databaseManager.getDatabase(), tableName, contentValues);
        assertTrue(checkTableExists(databaseManager, tableName));

        SQLiteUtils.dropTable(databaseManager.getDatabase(), tableName);
        assertFalse(checkTableExists(databaseManager, tableName));
    }

    private boolean checkTableExists(DatabaseManager databaseManager, String tableName) {
        SQLiteQueryBuilder builder = SQLiteUtils.newSQLiteQueryBuilder();
        builder.appendWhere("tbl_name = ?");
        Cursor cursor = databaseManager.getCursor("sqlite_master", builder, new String[]{"tbl_name"}, new String[]{tableName}, null);
        try {
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }
}
