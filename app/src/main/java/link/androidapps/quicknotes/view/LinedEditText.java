package link.androidapps.quicknotes.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by PKamenov on 12.12.15.
 */
public class LinedEditText extends EditText {
	private Paint paint;
	private Rect rect;

	public LinedEditText(Context context) {
		super(context);
		setLines(1);
		init();
	}

	public LinedEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LinedEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		rect = new Rect();
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.GRAY);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int numberOfLines = getLineCount();

		for (int i = 0; i < numberOfLines; i++) {
			int baseline = getLineBounds(i, rect);
			canvas.drawLine(rect.left, baseline + 3, rect.right, baseline + 3, paint);
		}

		super.onDraw(canvas);
	}
}
