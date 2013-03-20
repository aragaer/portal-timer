/*
 * Copyright (C) 2013 Felix Bechstein
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class MainActivity extends Activity {
	private static final String TAG = "portal-timer/ma";
	public static final String INGRESS_PACKAGE = "com.nianticproject.ingress";
	private static final int N_TIMERS = Timer.TIMER_ALL.length;

	@SuppressLint("HandlerLeak")
	private class UpdateHandler extends Handler {
		@Override
		public void dispatchMessage(final Message msg) {
			for (int i = 0; i < N_TIMERS; i++)
				mTimerViews[i].update();
		}
	}

	private class UpdateThread extends Thread {
		@Override
		public void run() {
			while (mThread == this) {
				mHandler.sendEmptyMessage(0);
				long t = 0;
				for (int i = 0; i < N_TIMERS; i++) {
					final long target = mTimerViews[i].getTarget();
					if (target > t)
						t = target;
				}
				long d = (t > SystemClock.elapsedRealtime() ? 1000 : 5000);
				try {
					sleep(d);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	private final TimerView[] mTimerViews = new TimerView[N_TIMERS];
	private static final int[] TimerViewIds = { R.id.timer0, R.id.timer1, R.id.timer2 };

	UpdateHandler mHandler = null;
	UpdateThread mThread = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		for (int i = 0; i < N_TIMERS; i++) {
			mTimerViews[i] = (TimerView) findViewById(TimerViewIds[i]);
			mTimerViews[i].setTimerKey(Timer.TIMER_ALL[i]);
		}

		mHandler = new UpdateHandler();

		if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
				&& PreferenceManager.getDefaultSharedPreferences(this)
						.getBoolean("start_ingress", false)) {
			try {
				startActivity(getPackageManager().getLaunchIntentForPackage(
						INGRESS_PACKAGE));
				finish();
			} catch (NullPointerException e) {
				Log.e(TAG, "unable to launch intent", e);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "unable to launch intent", e);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mThread = new UpdateThread();
		mThread.start();
		UpdateReceiver.trigger(this);
	}

	@Override
	protected void onPause() {
		UpdateThread t = mThread;
		mThread = null;
		t.interrupt();
		super.onPause();
	}
}
