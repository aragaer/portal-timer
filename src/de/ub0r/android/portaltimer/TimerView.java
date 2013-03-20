package de.ub0r.android.portaltimer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TimerView extends RelativeLayout implements OnClickListener {
	private Timer mTimer;
	private TextView mText;

	public TimerView(Context context) {
		this(context, null);
	}

	public TimerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.timer, this, true);
		findViewById(R.id.reset).setOnClickListener(this);
		findViewById(R.id.start).setOnClickListener(this);

		mTimer = new Timer(context);
		mText = (TextView) findViewById(R.id.timer);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reset:
			mTimer.reset(getContext());
			break;
		case R.id.start:
			mTimer.start(getContext());
			break;
		}
		update();
	}

	public void setTimerKey(final String key) {
		mTimer.setKey(key);
	}

	public void update() {
		mTimer.refresh();
		mText.setText(mTimer.getFormated());
	}

	public long getTarget() {
		return mTimer.getTarget();
	}
}
