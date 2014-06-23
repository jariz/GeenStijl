package io.jari.geenstijl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;

/**
 * InternalBrowserMovementMethod is a custom LinkMovementMethod that handles links in our own browser instead of the system one.
 */
public class InternalBrowserMovementMethod extends LinkMovementMethod {

    public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer, android.view.MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {
                String url = link[0].getURL();
                widget.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url), widget.getContext(), Browser.class));
                return true;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }
}