package ca.cmpt276.flame.model;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import ca.cmpt276.flame.R;

/**
 * Child represents a single child. A hash map of Child objects are managed by the
 * ChildrenManager class. Each child is given an ID so it can be uniquely referenced by
 * other classes. If they are instead referred to by index (ex. in a FlipHistoryEntry), then
 * history "breaks" when a Child is deleted. If we store a Child object in a FlipHistoryEntry,
 * then we'll get multiple copies of the object when we restore state from SharedPreferences
 * (one in ChildManager and a separate one in each FlipHistoryEntry) which breaks renaming.
 */
public class Child {
    public static final long NONE = 0L;
    private final long id;
    private String name;
    private boolean hasImage;

    protected Child(String name) {
        id = ChildrenManager.getInstance().getNextChildId();
        setName(name);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name must be non-empty");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean hasImage() {
        return hasImage;
    }

    protected void setHasImage() {
        this.hasImage = true;
    }

    protected void removeImage(Context context) {
        this.getImageFile(context).delete();
        this.hasImage = false;
    }

    public File getImageFile(Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("childImageDir", Context.MODE_PRIVATE);
        return new File(directory, this.getId() + "profile.jpg");
    }

    public Bitmap getImageBitmap(Context context) {
        Bitmap childImage = null;

        if(this.hasImage) {
            try {
                File f = this.getImageFile(context);
                childImage = BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                // use default image below
            }
        }

        if(childImage == null) {
            childImage = getDefaultImageBitmap(context);
        }

        return childImage;
    }

    public static Bitmap getDefaultImageBitmap(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_child);
    }
}
