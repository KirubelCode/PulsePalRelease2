package com.movesense.showcaseapp.custom_matchers;


import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.section_03_dfu.ScannedDevicesAdapter;
import com.movesense.showcaseapp.section_01_movesense.MovesenseAdapter;

import org.hamcrest.Matcher;

public class RecyclerViewByTextNameMacher {

    /**
     * Custom matcher for RecyclerView text
     *
     * @param text
     * @return
     */
    public static Matcher<RecyclerView.ViewHolder> withHolderTimeView(final String text) {
        return new BoundedMatcher<RecyclerView.ViewHolder, MovesenseAdapter.ViewHolder>(MovesenseAdapter.ViewHolder.class) {

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("No ViewHolder found with text: " + text);
            }

            @Override
            protected boolean matchesSafely(MovesenseAdapter.ViewHolder viewHolder) {
                TextView timeViewText = viewHolder.itemView.findViewById(R.id.movesense_name);
                if (timeViewText == null) {
                    return false;
                }
                return timeViewText.getText().toString().equalsIgnoreCase(text);
            }

        };
    }


    /**
     * Custom matcher for RecyclerView text
     *
     * @param text
     * @return
     */
    public static Matcher<RecyclerView.ViewHolder> withDfuHolderTimeView(final String text) {
        return new BoundedMatcher<RecyclerView.ViewHolder, ScannedDevicesAdapter.DeviceViewHolder>(ScannedDevicesAdapter.DeviceViewHolder.class) {

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("No ViewHolder found with text: " + text);
            }

            @Override
            protected boolean matchesSafely(ScannedDevicesAdapter.DeviceViewHolder viewHolder) {
                TextView timeViewText = viewHolder.itemView.findViewById(R.id.movesense_name);
                if (timeViewText == null) {
                    return false;
                }
                return timeViewText.getText().toString().equalsIgnoreCase(text);
            }

        };
    }
}
