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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.records.data.RecordContract;
import com.example.android.records.data.RecordContract.RecordEntry;

import java.io.File;
import java.io.InputStream;

/**
 * Allows user to create a new record or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the record album image data loader
     */
    public static final int IMAGE_GALLERY_REQUEST = 20;
    /** Identifier for the record data loader */
    private static final int EXISTING_RECORD_LOADER = 0;
    /**
     * Identifier for the record album image URI loader
     */
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";
    final Context mContext = this;
    /**
     * Content URI for the existing record cover image(null if it's a new record)
     */
    private Uri mImageUri;
    private String imagePath;
    private Bitmap image;
    /** Content URI for the existing record (null if it's a new record) */
    private Uri mCurrentRecordUri;

    /** EditText field to enter the album name */
    private EditText mAlbumNameEditText;

    /** EditText field to enter the record's band name */
    private EditText mBandNameEditText;

    /** EditText field to enter the Record quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the Record price */
    private EditText mPriceEditText;

    /** ImageView field to insert the Record Cover */
    private ImageView mRecordCover;

    /**
     * EditText field to enter the Record supplier Name
     */
    private EditText mContactNameEditText;

    /**
     * EditText field to enter the Record supplier Email
     */
    private EditText mContactEmailEditText;

    private Button mAddImage;


    /** Boolean flag that keeps track of whether the record has been edited (true) or not (false) */
    private boolean mRecordHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mRecordHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mRecordHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new record or editing an existing one.
        Intent intent = getIntent();
        mCurrentRecordUri = intent.getData();

        // If the intent DOES NOT contain a record content URI, then we know that we are
        // creating a new record.
        if (mCurrentRecordUri == null) {
            // This is a new record, so change the app bar to say "Add a Record"
            setTitle(getString(R.string.editor_activity_title_new_pet));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a record that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing record, so change app bar to say "Edit Record"
            setTitle(getString(R.string.editor_activity_title_edit_record));

            // Initialize a loader to read the record data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_RECORD_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mAlbumNameEditText = (EditText) findViewById(R.id.edit_album_name);
        mBandNameEditText = (EditText) findViewById(R.id.edit_band_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mRecordCover = (ImageView) findViewById(R.id.edit_image_cover);
        mContactNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mContactEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mAddImage = (Button) findViewById(R.id.add_image);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mAlbumNameEditText.setOnTouchListener(mTouchListener);
        mBandNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mRecordCover.setOnTouchListener(mTouchListener);
        mContactNameEditText.setOnTouchListener(mTouchListener);
        mContactEmailEditText.setOnTouchListener(mTouchListener);


        //Open camera when you press on image
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Invoke an implicit intent to open the photo gallery
                Intent openPhotoGallery = new Intent(Intent.ACTION_PICK);

                //Where do we find the data?
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);


                String pictureDirectoryPath = pictureDirectory.getPath();

                //Get the Uri rapresentation
                Uri data = Uri.parse(pictureDirectoryPath);

                //Set the data and type
                openPhotoGallery.setDataAndType(data, "image/*");

                //We will invoke this activity and get something back from it
                startActivityForResult(openPhotoGallery, IMAGE_GALLERY_REQUEST);

            }

        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            //if we are here our request was succesfull
            if (requestCode == IMAGE_GALLERY_REQUEST) {

                //if we are here we hearing back from the image gallery

                //this is the address of the image on the sd cards
                Uri mImageUri = data.getData();

                String imagePath = mImageUri.toString();

                //Declare a stream to read the data from the card
                InputStream inputStream;

                try {
                    //We are getting an input stream based on the Uri of the image
                    inputStream = getContentResolver().openInputStream(mImageUri);

                    //Get a bitmap from the stream
                    Bitmap image = BitmapFactory.decodeStream(inputStream);

                    //Show the image to the user
                    mRecordCover.setImageBitmap(image);

                } catch (Exception e) {
                    e.printStackTrace();

                    //Show the user a Toast mewssage that the Image is not available
                    Toast.makeText(EditorActivity.this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


    /**
     * Get user input from editor and save record into database.
     */
    private void saveRecord() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String albumNameString = mAlbumNameEditText.getText().toString().trim();
        String bandNameString = mBandNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mContactNameEditText.getText().toString().trim();
        String supplierEmailString = mContactEmailEditText.getText().toString().trim();

        String albumCoverString = imagePath;


        // Check if this is supposed to be a new record
        // and check if all the fields in the editor are blank
        if (mCurrentRecordUri == null &&
                TextUtils.isEmpty(albumNameString) && TextUtils.isEmpty(bandNameString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString) &&
                //TextUtils.isEmpty(albumCoverString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString)) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and record attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(RecordContract.RecordEntry.COLUMN_ALBUM_NAME, albumNameString);
        values.put(RecordEntry.COLUMN_BAND_NAME, bandNameString);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(RecordEntry.COLUMN_QUANTITY, quantity);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(RecordEntry.COLUMN_PRICE, price);
        values.put(RecordEntry.COLUMN_RECORD_COVER, albumCoverString);
        values.put(RecordEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(RecordEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString );


        // Determine if this is a new or existing pet by checking if mCurrentRecordUri is null or not
        if (mCurrentRecordUri == null) {
            // This is a NEW pet, so insert a new record into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(RecordEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentRecordUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentRecordUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentRecordUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentRecordUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveRecord();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mRecordHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the record hasn't changed, continue with handling back button press
        if (!mRecordHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all record attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                RecordEntry._ID,
                RecordContract.RecordEntry.COLUMN_ALBUM_NAME,
                RecordEntry.COLUMN_BAND_NAME,
                RecordContract.RecordEntry.COLUMN_QUANTITY,
                RecordEntry.COLUMN_PRICE,
                RecordEntry.COLUMN_RECORD_COVER,
                RecordEntry.COLUMN_SUPPLIER_NAME,
                RecordEntry.COLUMN_SUPPLIER_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentRecordUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int albumNameColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_ALBUM_NAME);
            int bandNameColumnIndex = cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_BAND_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_RECORD_COVER);
            int supplierNameColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(RecordEntry.COLUMN_SUPPLIER_EMAIL);


            // Extract out the value from the Cursor for the given column index
            String albumName = cursor.getString(albumNameColumnIndex);
            String bandName = cursor.getString(bandNameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String cover = cursor.getString(imageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);


            // Update the views on the screen with the values from the database
            mAlbumNameEditText.setText(albumName);
            mBandNameEditText.setText(bandName);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));

            mImageUri = Uri.parse(cover);
            mRecordCover.setImageBitmap(image);

            mContactNameEditText.setText(supplierName);
            mContactNameEditText.setText(supplierEmail);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mAlbumNameEditText.setText("");
        mBandNameEditText.setText("");
        mQuantityEditText.setText("0");
        mPriceEditText.setText("0£");
        mRecordCover.setImageResource(R.mipmap.add_record_cover);
        mContactNameEditText.setText("");
        mContactEmailEditText.setText("");}

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteRecord();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteRecord() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentRecordUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentRecordUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentRecordUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}