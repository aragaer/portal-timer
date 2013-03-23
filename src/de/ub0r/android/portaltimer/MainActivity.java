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
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends Activity {
	private static final String TAG = "portal-timer/ma";
	public static final String INGRESS_PACKAGE = "com.nianticproject.ingress";

	@SuppressLint("HandlerLeak")
	private class UpdateHandler extends Handler {
		@Override
		public void dispatchMessage(final Message msg) {
			for (int j = 0; j < Timer.TIMER_IDS.length; j++) {
				mTimerViews[j].update();
			}
		}
	}

	private class UpdateThread extends Thread {
		@Override
		public void run() {
			while (mThread == this) {
				mHandler.sendEmptyMessage(0);
				long t = 0;
				for (int j = 0; j < Timer.TIMER_IDS.length; j++) {
					t = Math.max(t, mTimerViews[j].getTarget());
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

	private TimerView mTimerViews[];

	UpdateHandler mHandler = null;
	UpdateThread mThread = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate()");
		mHandler = new UpdateHandler();

		if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
				&& PreferenceManager.getDefaultSharedPreferences(this)
						.getBoolean("start_ingress", false)) {
			try {
				Intent i = getPackageManager().getLaunchIntentForPackage(
						INGRESS_PACKAGE);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
				UpdateReceiver.trigger(this);
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
		Log.d(TAG, "onResume()");
		initTimers();
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

	private void initTimers() {
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mTimerViews = new TimerView[Timer.TIMER_IDS.length];
		LinearLayout list = (LinearLayout) findViewById(R.id.list);
		for (int j = 0; j < mTimerViews.length; j++) {
			mTimerViews[j] = new TimerView(this, j);
			mTimerViews[j].setGravity(Gravity.CENTER);
			list.addView(mTimerViews[j], lp);
		}
	}
}
