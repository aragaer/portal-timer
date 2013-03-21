/*
 * Copyright (C) 2013 Ilya Konovalov
 *
 * This file is part of Portal Timer.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
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

	public TimerView(Context context, int j) {
		super(context, null);
		LayoutInflater.from(context).inflate(R.layout.timer, this, true);
		findViewById(R.id.reset).setOnClickListener(this);
		findViewById(R.id.start).setOnClickListener(this);

		mTimer = new Timer(context, j);
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

	public void update() {
		mTimer.refresh();
		mText.setText(mTimer.getFormated());
	}

	public long getTarget() {
		return mTimer.getTarget();
	}
}
