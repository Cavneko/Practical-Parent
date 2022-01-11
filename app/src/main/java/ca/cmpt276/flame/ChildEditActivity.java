package ca.cmpt276.flame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.FileOutputStream;
import java.io.IOException;

import ca.cmpt276.flame.model.BGMusicPlayer;
import ca.cmpt276.flame.model.Child;
import ca.cmpt276.flame.model.ChildrenManager;

/**
 * ChildEditActivity:
 * Add a new child
 * Rename an existing child
 * Delete a child
 */
public class ChildEditActivity extends AppCompatActivity {
    private static final String EXTRA_CHILD_ID = "EXTRA_CHILD_ID";
    private static final int REQUEST_CODE_CAMERA = 0;
    private static final int REQUEST_CODE_GALLERY = 1;
    private Bitmap childImage = null;
    private Boolean imageNeedsSaving = false;
    private Child clickedChild;
    private String newName;
    private TextView inputName;
    private final ChildrenManager childrenManager = ChildrenManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_edit);
        getDataFromIntent();
        setupToolbar();
        setUpNameSizeLimit();
        fillChildName();
        fillChildImage();
        setupSaveButton();
        setUpEditImageButton();

        if (clickedChild == null) {
            hideDeleteButton();
        } else {
            setupDeleteButton();
        }
    }

    private void getDataFromIntent() {
        long childId = getIntent().getLongExtra(EXTRA_CHILD_ID, Child.NONE);
        clickedChild = childrenManager.getChild(childId);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_edit);

        if(clickedChild == null) {
            toolbar.setTitle(R.string.add_child);
        } else {
            toolbar.setTitle(R.string.edit_child);
        }

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void setUpNameSizeLimit() {
        final int MAX_INPUT_LENGTH = 20;
        inputName = findViewById(R.id.childEdit_editTxtChildName);
        inputName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_INPUT_LENGTH)});
    }

    private void fillChildName() {
        if(clickedChild != null) {
            inputName.setText(clickedChild.getName());
        }
    }

    private void fillChildImage() {
        if(clickedChild != null) {
            childImage = clickedChild.getImageBitmap(this);
        } else {
            childImage = Child.getDefaultImageBitmap(this);
        }
        ImageView imageView = findViewById(R.id.childEdit_child_image_view);
        imageView.setImageBitmap(childImage);
    }

    private void setupSaveButton() {
        Button btn = findViewById(R.id.childEdit_btnSave);

        btn.setOnClickListener(v -> {
            EditText inputName = findViewById(R.id.childEdit_editTxtChildName);
            newName = inputName.getText().toString();
            if(newName.isEmpty()) {
                Toast.makeText(this, getString(R.string.child_name_empty_error), Toast.LENGTH_SHORT).show();
                return;
            }

            if (clickedChild != null) {
                childrenManager.renameChild(clickedChild, newName);
            } else {
                clickedChild = childrenManager.addChild(newName);
            }

            saveChildImage();

            finish();
        });
    }

    private void setUpEditImageButton() {
        ImageButton inputImageBtn = findViewById(R.id.childEdit_changeImageBtn);

        inputImageBtn.setOnClickListener(v ->  {
            if(imageNeedsSaving || (clickedChild != null && clickedChild.hasImage())) {
                showDialogBoxAddRemoveImage();
            } else {
                showDialogBoxGalleryOrCamera();
            }
        });
    }

    private void showDialogBoxAddRemoveImage() {
        new AlertDialog.Builder(ChildEditActivity.this)
                .setTitle(R.string.choose)
                .setPositiveButton(R.string.new_image, ((dialogInterface, i) -> {
                    showDialogBoxGalleryOrCamera();
                }))
                .setNegativeButton(R.string.remove, ((dialogInterface, i) -> {
                    if(clickedChild != null) {
                        childrenManager.removeChildImage(clickedChild, this);
                    }
                    imageNeedsSaving = false;
                    fillChildImage();
                })).show();
    }

    private void showDialogBoxGalleryOrCamera() {
        new AlertDialog.Builder(ChildEditActivity.this)
                .setTitle(R.string.choose)
                .setPositiveButton(R.string.gallery, ((dialogInterface, i) -> {
                    pickImageFromGallery();
                }))
                .setNegativeButton(R.string.camera, ((dialogInterface, i) -> {
                    pickImageFromCamera();
                })).show();
    }
    private void pickImageFromCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, REQUEST_CODE_CAMERA);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    //handle result of picked image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode != RESULT_OK) {
            return;
        }

        Bitmap fullSize = null;
        
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                Bundle extras = imageReturnedIntent.getExtras();
                fullSize = (Bitmap) extras.get("data");
                break;
            case REQUEST_CODE_GALLERY:
                Uri selectedImage = imageReturnedIntent.getData();
                try {
                    fullSize = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    Toast.makeText(this, getResources().getText(R.string.something_wrong_try_again), Toast.LENGTH_SHORT).show();
                }
                break;
        }

        if(fullSize != null) {
            childImage = cropAndDownsizeImage(fullSize);
            imageNeedsSaving = true;

            ImageView childImageView = findViewById(R.id.childEdit_child_image_view);
            childImageView.setImageBitmap(childImage);
        }
    }

    private Bitmap cropAndDownsizeImage(Bitmap fullSize) {
        int size = Math.min(fullSize.getWidth(), fullSize.getHeight());
        int offsetX = 0;
        int offsetY = 0;

        if(fullSize.getWidth() > fullSize.getHeight()) {
            offsetX = (fullSize.getWidth() - size) / 2;
        } else {
            offsetY = (fullSize.getHeight() - size) / 2;
        }

        Bitmap cropped = Bitmap.createBitmap(fullSize, offsetX, offsetY, size, size);

        // down size
        final int IMAGE_DIM = 150;
        return Bitmap.createScaledBitmap(cropped, IMAGE_DIM, IMAGE_DIM, false);
    }

    private void saveChildImage() {
        if(!imageNeedsSaving) {
            return;
        }

        final int IMAGE_QUALITY = 100;

        try {
            FileOutputStream fos = new FileOutputStream(clickedChild.getImageFile(this));
            // Use the compress method on the BitMap object to write image to the OutputStream
            childImage.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fos);
            fos.close();

            childrenManager.setChildHasImage(clickedChild);
        } catch (IOException e) {
            Toast.makeText(this, getResources().getText(R.string.something_wrong_try_again), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideDeleteButton() {
        Button btn = findViewById(R.id.childEdit_btnDelete);
        btn.setVisibility(View.GONE);
    }

    private void setupDeleteButton() {
        Button btn = findViewById(R.id.childEdit_btnDelete);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(v -> {
            new AlertDialog.Builder(ChildEditActivity.this)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.childEditActivity_confirmDeleteMsg)
                    .setPositiveButton(R.string.delete, ((dialogInterface, i) -> {
                        childrenManager.removeChild(clickedChild, this);
                        finish();
                    }))
                    .setNegativeButton(R.string.cancel, null).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BGMusicPlayer.resumeBgMusic();
    }

    protected static Intent makeIntent(Context context, Child child) {
        long childId = Child.NONE;
        if(child != null) {
            childId = child.getId();
        }

        Intent intent = new Intent(context, ChildEditActivity.class);
        intent.putExtra(EXTRA_CHILD_ID, childId);
        return intent;
    }
}