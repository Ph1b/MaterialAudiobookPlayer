package de.ph1b.audiobook.model;

import android.support.annotation.NonNull;

import net.jcip.annotations.Immutable;

@Immutable
public class Chapter {

    private static final String TAG = Chapter.class.getSimpleName();
    @NonNull
    private final String path;
    private final int duration;
    @NonNull
    private final String name;

    public Chapter(Chapter that) {
        this.path = that.path;
        this.duration = that.duration;
        this.name = that.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof Chapter) {
            Chapter that = (Chapter) o;
            return this.path.equals(that.path) && this.name.equals(that.name) && this.duration == that.duration;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = PRIME + path.hashCode();
        result = PRIME * result + duration;
        result = PRIME * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return TAG + "[" +
                "path=" + path +
                ",duration=" + duration +
                ",name=" + name +
                "]";
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    @NonNull
    public String getPath() {
        return path;
    }
}
