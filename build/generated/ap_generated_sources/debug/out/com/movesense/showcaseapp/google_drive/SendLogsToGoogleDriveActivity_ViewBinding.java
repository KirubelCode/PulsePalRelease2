// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.google_drive;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SendLogsToGoogleDriveActivity_ViewBinding implements Unbinder {
  private SendLogsToGoogleDriveActivity target;

  private View view7f0800c4;

  @UiThread
  public SendLogsToGoogleDriveActivity_ViewBinding(SendLogsToGoogleDriveActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SendLogsToGoogleDriveActivity_ViewBinding(final SendLogsToGoogleDriveActivity target,
      View source) {
    this.target = target;

    View view;
    target.resultTextView = Utils.findRequiredViewAsType(source, R.id.resultTextView, "field 'resultTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.logsFileListView, "field 'logsFileListView' and method 'onItemClick'");
    target.logsFileListView = Utils.castView(view, R.id.logsFileListView, "field 'logsFileListView'", ListView.class);
    view7f0800c4 = view;
    ((AdapterView<?>) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> p0, View p1, int p2, long p3) {
        target.onItemClick(p0, p1, p2, p3);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    SendLogsToGoogleDriveActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.resultTextView = null;
    target.logsFileListView = null;

    ((AdapterView<?>) view7f0800c4).setOnItemClickListener(null);
    view7f0800c4 = null;
  }
}
