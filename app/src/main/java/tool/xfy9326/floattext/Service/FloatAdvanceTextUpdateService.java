package tool.xfy9326.floattext.Service;

import android.content.*;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;
import java.util.List;
import tool.xfy9326.floattext.Activity.GlobalSetActivity;
import tool.xfy9326.floattext.Method.FloatManageMethod;
import tool.xfy9326.floattext.R;
import tool.xfy9326.floattext.Utils.StaticString;
import tool.xfy9326.floattext.Method.ActivityMethod;

public class FloatAdvanceTextUpdateService extends AccessibilityService
{
	private String currentactivity;
	private String notifymes;
	private String toasts;
	private String oldactivity = "";
	private boolean sentrule = false;

	@Override
	public void onCreate()
	{
		currentactivity = toasts = notifymes = getString(R.string.loading);
		FloatManageMethod.setWinManager(this);
		super.onCreate();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event)
	{
		int eventType = event.getEventType();
        switch (eventType)
		{
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
				List<CharSequence> text = event.getText();
				if (!text.isEmpty())
				{
					String str = text.toString();
					if (str != "[]")
					{
						str = str.substring(1, str.length() - 1);
						sentrule = getRule();
						if (sentrule)
						{
							if (event.getClassName().toString().contains("Toast"))
							{
								toasts = str;
							}
							else
							{
								notifymes = str;
							}
						}
						else
						{
							if (event.getClassName().toString().contains("Toast"))
							{
								toasts = str;
							}
						}
						sendmes();
					}
				}
				break;
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
				if (event.getClassName() != null)
				{
					currentactivity = event.getClassName().toString();
					sendmes();
				}
				break;
		}
	}

	@Override
	public void onInterrupt()
	{
	}

	private boolean getRule()
	{
		if (Build.VERSION.SDK_INT < 18 || !ActivityMethod.isNotificationListenerEnabled(this))
		{
			return true;
		}
		return false;
	}

	private void sendmes()
	{
		Intent intent = new Intent();
		intent.setAction(StaticString.TEXT_ADVANCE_UPDATE_ACTION);
		intent.putExtra("CurrentActivity", currentactivity);
		if (sentrule)
		{
			intent.putExtra("NotifyMes", notifymes);
		}
		intent.putExtra("Toasts", toasts);
		sendBroadcast(intent);
		updateactivity();
	}

	private void updateactivity()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FloatAdvanceTextUpdateService.this);
		if (sp.getBoolean("WinFilterSwitch", false))
		{
			if (!oldactivity.equalsIgnoreCase(currentactivity))
			{
				Intent intent = new Intent();
				intent.setAction(StaticString.ACTIVITY_CHANGE_ACTION);
				intent.putExtra("CurrentActivity", currentactivity);
				sendBroadcast(intent);
				oldactivity = currentactivity;
			}
		}
	}

}
