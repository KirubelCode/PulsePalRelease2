// Generated code from Butter Knife. Do not modify!
package com.movesense.showcaseapp.section_01_movesense.tests;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.movesense.showcaseapp.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MemoryDiagnosticActivity_ViewBinding implements Unbinder {
  private MemoryDiagnosticActivity target;

  @UiThread
  public MemoryDiagnosticActivity_ViewBinding(MemoryDiagnosticActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MemoryDiagnosticActivity_ViewBinding(MemoryDiagnosticActivity target, View source) {
    this.target = target;

    target.responseTextView = Utils.findRequiredViewAsType(source, R.id.response_textView, "field 'responseTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MemoryDiagnosticActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.responseTextView = null;
  }
}
