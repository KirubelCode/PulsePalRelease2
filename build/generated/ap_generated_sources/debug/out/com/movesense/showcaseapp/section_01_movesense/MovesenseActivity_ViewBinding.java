// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.section_01_movesense;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MovesenseActivity_ViewBinding implements Unbinder {
  private MovesenseActivity target;

  @UiThread
  public MovesenseActivity_ViewBinding(MovesenseActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MovesenseActivity_ViewBinding(MovesenseActivity target, View source) {
    this.target = target;

    target.mMovesenseRecyclerView = Utils.findRequiredViewAsType(source, R.id.movesense_recyclerView, "field 'mMovesenseRecyclerView'", RecyclerView.class);
    target.mMovesenseInfoTv = Utils.findRequiredViewAsType(source, R.id.movesense_infoTv, "field 'mMovesenseInfoTv'", TextView.class);
    target.mMovesenseProgressBar = Utils.findRequiredViewAsType(source, R.id.movesense_progressBar, "field 'mMovesenseProgressBar'", ProgressBar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MovesenseActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mMovesenseRecyclerView = null;
    target.mMovesenseInfoTv = null;
    target.mMovesenseProgressBar = null;
  }
}
