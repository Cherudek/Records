/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.records;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.records.data.RecordContract;
import com.example.android.records.data.RecordContract.RecordEntry;

/**
 * {@link RecordCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of record data as its data source. This adapter knows
 * how to create list items for each row of record data in the {@link Cursor}.
 */
public class RecordCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link RecordCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public RecordCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the record data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current record can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView albumNameTextView = (TextView) view.findViewById(R.id.album_name);
        TextView bandNameTextView = (TextView) view.findViewById(R.id.band_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantiy);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        // Find the columns of the record attributes that we're interested in
        int albumNameColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_ALBUM_NAME);
        int bandNameColumnIndex = cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_BAND_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_QUANTITY);
        int priceNameColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_PRICE);

        // Read the record attributes from the Cursor for the current record
        String albumName = cursor.getString(albumNameColumnIndex);
        String bandName = cursor.getString(bandNameColumnIndex);
        String quantity = cursor.getString(quantityColumnIndex);
        String price = cursor.getString(priceNameColumnIndex);

        // Update the TextViews with the attributes for the current record
        albumNameTextView.setText(albumName);
        bandNameTextView.setText(bandName);
        quantityTextView.setText(quantity);
        priceTextView.setText(price);

    }
}
