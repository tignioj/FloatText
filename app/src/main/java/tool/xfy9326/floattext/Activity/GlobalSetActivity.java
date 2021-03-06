package tool.xfy9326.floattext.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tool.xfy9326.floattext.FileSelector.SelectFile;
import tool.xfy9326.floattext.Method.ActivityMethod;
import tool.xfy9326.floattext.Method.FloatManageMethod;
import tool.xfy9326.floattext.Method.IOMethod;
import tool.xfy9326.floattext.R;
import tool.xfy9326.floattext.Service.FloatAdvanceTextUpdateService;
import tool.xfy9326.floattext.Service.FloatNotificationListenerService;
import tool.xfy9326.floattext.Service.FloatTextUpdateService;
import tool.xfy9326.floattext.Service.FloatWindowStayAliveService;
import tool.xfy9326.floattext.Tool.FormatArrayList;
import tool.xfy9326.floattext.Utils.App;
import tool.xfy9326.floattext.Utils.FloatData;
import tool.xfy9326.floattext.Utils.StaticNum;

public class GlobalSetActivity extends AppCompatActivity {
    private PrefFragment pref;

    private static void recoverdata(Context ctx, String path) {
        if (path == null) {
            SelectFile sf = new SelectFile(StaticNum.FLOAT_TEXT_SELECT_RECOVER_FILE, SelectFile.TYPE_ChooseFile);
            sf.setFileType("ftbak");
            sf.start((Activity) ctx);
        } else {
            FloatData fd = new FloatData(ctx);
            if (fd.InputData(path)) {
                final Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
                intent.putExtra("RecoverText", 1);
                FloatManageMethod.restartApplication(ctx, intent);
            } else {
                Toast.makeText(ctx, R.string.recover_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void backupdata(Context ctx) {
        if (((App) ctx.getApplicationContext()).getFloatText().size() == 0) {
            Toast.makeText(ctx, R.string.backup_nofound, Toast.LENGTH_SHORT).show();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", ctx.getResources().getConfiguration().locale);
            String path = Environment.getExternalStorageDirectory().toString() + "/FloatText/Backup/FloatText>" + sdf.format(new Date()) + ".ftbak";
            FloatData fd = new FloatData(ctx);
            if (fd.OutputData(path, ActivityMethod.getVersionCode(ctx))) {
                Toast.makeText(ctx, ctx.getString(R.string.backup_success) + path, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, R.string.backup_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new PrefFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, pref).commit();
        sethome();
    }

    //工具栏和返回按键设置
    private void sethome() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == StaticNum.FLOAT_TEXT_GET_TYPEFACE_PERMISSION) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pref.getTypeFace(pref.findPreference("TextTypeface"));
            } else {
                Toast.makeText(this, R.string.premission_error, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == StaticNum.FLOAT_TEXT_GET_BACKUP_PERMISSION) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                backupdata(this);
            } else {
                Toast.makeText(this, R.string.premission_error, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == StaticNum.FLOAT_TEXT_GET_RECOVER_PERMISSION) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                recoverdata(this, null);
            } else {
                Toast.makeText(this, R.string.premission_error, Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StaticNum.ADVANCE_TEXT_SET) {
            Preference adts = pref.findPreference("AdvanceTextService");
            pref.setADTsum(adts);
        } else if (requestCode == StaticNum.ADVANCE_TEXT_NOTIFICATION_SET) {
            Preference nous = pref.findPreference("NotificationListenerService");
            pref.setNOSsum(nous);
        } else if (requestCode == StaticNum.FLOAT_TEXT_SELECT_RECOVER_FILE) {
            if (data != null) {
                String str = data.getStringExtra("FilePath");
                recoverdata(this, str);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static class PrefFragment extends PreferenceFragment {
        private String default_typeface;
        private int typeface_choice, language_choice;
        private String[] AppNames, PkgNames;
        private boolean[] AppState;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.global_settings);
            ViewSet();
            ServiceViewSet();
            DataViewSet();
        }

        private void ViewSet() {
            //通知栏控制
            CheckBoxPreference notify = (CheckBoxPreference) findPreference("FloatNotification");
            notify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    StayAliveSet(false);
                    StayAliveSet(true);
                    return true;
                }
            });
            //跑马灯模式
            CheckBoxPreference movemethod = (CheckBoxPreference) findPreference("TextMovingMethod");
            movemethod.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    ((App) getActivity().getApplicationContext()).setMovingMethod((boolean) p2);
                    Toast.makeText(getActivity(), R.string.restart_to_apply, Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            //字体
            Preference typeface = findPreference("TextTypeface");
            final SharedPreferences setdata = getActivity().getSharedPreferences("ApplicationSettings", Activity.MODE_PRIVATE);
            default_typeface = setdata.getString("DefaultTTFName", "Default");
            if (default_typeface.equalsIgnoreCase("Default")) {
                default_typeface = getString(R.string.text_default_typeface);
            }
            typeface.setSummary(getString(R.string.xml_global_text_typeface_summary) + default_typeface);
            typeface.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pre) {
                    DataAction(0, pre, StaticNum.FLOAT_TEXT_GET_TYPEFACE_PERMISSION);
                    return true;
                }
            });
            //语言
            Preference language = findPreference("Language");
            final String[] lan_list = getResources().getStringArray(R.array.language_list);
            language_choice = setdata.getInt("Language", 0);
            language.setSummary(getString(R.string.xml_global_language_sum) + lan_list[language_choice]);
            language.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pre) {
                    LanguageSet(pre, lan_list);
                    return true;
                }
            });
            //动态变量刷新时间
            Preference dynamictime = findPreference("DynamicTime");
            dynamictime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    DynamicTimeSet(setdata);
                    return true;
                }
            });
            //实验性功能
            CheckBoxPreference develop = (CheckBoxPreference) findPreference("DevelopMode");
            develop.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    ((App) getActivity().getApplicationContext()).setDevelopMode((boolean) p2);
                    return true;
                }
            });
            //HTML代码功能
            CheckBoxPreference html = (CheckBoxPreference) findPreference("HtmlMode");
            html.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    ((App) getActivity().getApplicationContext()).setHtmlMode((boolean) p2);
                    Toast.makeText(getActivity(), R.string.restart_to_apply, Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            //管理列表单行文字
            CheckBoxPreference hidetext = (CheckBoxPreference) findPreference("ListTextHide");
            hidetext.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    ((App) getActivity().getApplicationContext()).setListTextHide((boolean) p2);
                    return true;
                }
            });
            //窗口过滤器
            Preference filter = findPreference("WinFilter");
            filter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    FilterSet(setdata);
                    return true;
                }
            });
            //文字过滤器
            CheckBoxPreference textfilter = (CheckBoxPreference) findPreference("TextFilter");
            textfilter.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    ((App) getActivity().getApplicationContext()).setTextFilter((boolean) p2);
                    return true;
                }
            });
            //文字过滤器帮助
            Preference textfilterhelp = findPreference("TextFilterHelp");
            textfilterhelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    TextFilterHelpGet(getActivity());
                    return true;
                }
            });
            //通知栏图标设置
            CheckBoxPreference notifyicon = (CheckBoxPreference) findPreference("FloatNotificationIcon");
            notifyicon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    StayAliveSet(false);
                    StayAliveSet(true);
                    return true;
                }
            });
        }

        private void ServiceViewSet() {
            //进程守护服务
            CheckBoxPreference stayalive = (CheckBoxPreference) findPreference("StayAliveService");
            stayalive.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    StayAliveSet((boolean) p2);
                    return true;
                }
            });
            //动态变量服务
            CheckBoxPreference dynamicnum = (CheckBoxPreference) findPreference("DynamicNumService");
            dynamicnum.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p1, Object p2) {
                    DymanicSet((boolean) p2);
                    return true;
                }
            });
            //高级功能服务
            Preference adts = findPreference("AdvanceTextService");
            setADTsum(adts);
            adts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, StaticNum.ADVANCE_TEXT_SET);
                    return true;
                }
            });
            //通知监听服务
            Preference nous = findPreference("NotificationListenerService");
            setNOSsum(nous);
            nous.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        startActivityForResult(intent, StaticNum.ADVANCE_TEXT_NOTIFICATION_SET);
                    }
                    return true;
                }
            });
            //自定义动态变量
            Preference dynamicwordaddon = findPreference("DynamicWordAddon");
            dynamicwordaddon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    DynamicAddonHelpGet(getActivity());
                    return true;
                }
            });
        }

        private void DynamicTimeSet(final SharedPreferences sp) {
            int num = sp.getInt("DynamicReloadTime", 1000);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.dialog_text, null);
            AlertDialog.Builder set = new AlertDialog.Builder(getActivity());
            set.setTitle(R.string.xml_global_dynamicword_reload_time);
            final EditText et = (EditText) view.findViewById(R.id.dialog_text_edittext);
            et.setText(String.valueOf(num));
            et.setKeyListener(new DigitsKeyListener(false, true));
            set.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int i) {
                    String str = et.getText().toString();
                    if (!str.isEmpty()) {
                        int get = Integer.valueOf(str);
                        if (get < 500) {
                            Toast.makeText(getActivity(), R.string.num_err, Toast.LENGTH_SHORT).show();
                        } else {
                            sp.edit().putInt("DynamicReloadTime", get).apply();
                            Toast.makeText(getActivity(), R.string.restart_to_apply, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
            set.setNegativeButton(R.string.cancel, null);
            set.setView(view);
            set.show();
        }

        private void DataViewSet() {
            Preference backup = findPreference("DataBackup");
            backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    DataAction(2, p, StaticNum.FLOAT_TEXT_GET_BACKUP_PERMISSION);
                    return true;
                }
            });
            Preference recover = findPreference("DataRecover");
            recover.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference p) {
                    DataAction(1, p, StaticNum.FLOAT_TEXT_GET_RECOVER_PERMISSION);
                    return true;
                }
            });
        }

        private void DynamicAddonHelpGet(Context ctx) {
            String[] paths = new String[]{"HELPS/Dynamic_Words_Addon_cn.txt", "HELPS/Dynamic_Words_Addon_tw.txt", "HELPS/Dynamic_Words_Addon_en.txt"};
            String path = MultiLanguageSet(ctx, paths);
            String str = IOMethod.readAssets(ctx, path);
            SetHelpDialog(ctx, R.string.xml_global_service_dynamicaddon, str);
        }

        private void TextFilterHelpGet(Context ctx) {
            String[] paths = new String[]{"HELPS/TextFilter_cn.txt", "HELPS/TextFilter_tw.txt", "HELPS/TextFilter_en.txt"};
            String path = MultiLanguageSet(ctx, paths);
            String str = IOMethod.readAssets(ctx, path);
            SetHelpDialog(ctx, R.string.xml_global_text_filter, str);
        }

        private void SetHelpDialog(Context ctx, int title, String str) {
            AlertDialog.Builder help = new AlertDialog.Builder(ctx);
            help.setTitle(title);
            help.setMessage(str);
            help.setPositiveButton(R.string.done, null);
            help.show();
        }

        //语言设置
        private String MultiLanguageSet(Context ctx, String[] arr) {
            String path = arr[2];
            String locale = ctx.getResources().getConfiguration().locale.getCountry();
            if (locale.equals(Locale.SIMPLIFIED_CHINESE.getCountry())) {
                path = arr[0];
            } else if (locale.equals(Locale.TAIWAN.getCountry())) {
                path = arr[1];
            } else if (locale.equals(Locale.ENGLISH.getCountry())) {
                path = arr[2];
            }
            return path;
        }

        private void DataAction(int type, Preference pre, int requestcode) {
            if (Build.VERSION.SDK_INT > 22) {
                if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestcode);
                } else {
                    if (type == 0) {
                        getTypeFace(pre);
                    } else if (type == 1) {
                        recoverdata(getActivity(), null);
                    } else if (type == 2) {
                        backupdata(getActivity());
                    }
                }
            } else {
                if (type == 0) {
                    getTypeFace(pre);
                } else if (type == 1) {
                    recoverdata(getActivity(), null);
                } else if (type == 2) {
                    backupdata(getActivity());
                }
            }
        }

        private void StayAliveSet(boolean b) {
            ((App) getActivity().getApplicationContext()).setStayAliveService(b);
            Intent service = new Intent(getActivity(), FloatWindowStayAliveService.class);
            if (b) {
                getActivity().startService(service);
            } else {
                FloatManageMethod.setWinManager(getActivity());
                getActivity().stopService(service);
            }
        }

        private void DymanicSet(boolean b) {
            ((App) getActivity().getApplicationContext()).setDynamicNumService(b);
            Intent service = new Intent(getActivity(), FloatTextUpdateService.class);
            Intent asservice = new Intent(getActivity(), FloatAdvanceTextUpdateService.class);
            Intent notifyservice = new Intent(getActivity(), FloatNotificationListenerService.class);
            if (b) {
                getActivity().startService(service);
                getActivity().startService(asservice);
                getActivity().startService(notifyservice);
            } else {
                getActivity().stopService(service);
                getActivity().stopService(asservice);
                getActivity().stopService(notifyservice);
            }
        }

        private void FilterSet(final SharedPreferences setdata) {
            final ArrayList<String> FilterApplication = FormatArrayList.StringToStringArrayList(setdata.getString("Filter_Application", "[]"));
            getAppInfo(getActivity(), FilterApplication);
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.xml_global_win_filter)
                    .setMultiChoiceItems(AppNames, AppState, new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface d, int i, boolean b) {
                            AppState[i] = b;
                        }
                    })
                    .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int i) {
                            FilterApplication.clear();
                            for (int a = 0; a < AppState.length; a++) {
                                if (AppState[a]) {
                                    FilterApplication.add(PkgNames[a]);
                                }
                            }
                            ((App) getActivity().getApplicationContext()).getFrameutil().setFilterApplication(FilterApplication);
                            SharedPreferences.Editor ed = setdata.edit();
                            ed.putString("Filter_Application", FilterApplication.toString());
                            ed.apply();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            alert.show();
        }

        private void LanguageSet(final Preference pre, final String[] lan_list) {
            AlertDialog.Builder lan = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.xml_global_language)
                    .setSingleChoiceItems(lan_list, language_choice, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            language_choice = which;
                        }
                    })
                    .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface p1, int p2) {
                            SharedPreferences setdata = getActivity().getSharedPreferences("ApplicationSettings", Activity.MODE_PRIVATE);
                            setdata.edit().putInt("Language", language_choice).apply();
                            FloatManageMethod.LanguageSet(getActivity(), language_choice);
                            pre.setSummary(getString(R.string.xml_global_language_sum) + lan_list[language_choice]);
                            FloatManageMethod.restartApplication(getActivity(), getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName()));
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int i) {
                            SharedPreferences setdata = getActivity().getSharedPreferences("ApplicationSettings", Activity.MODE_PRIVATE);
                            language_choice = setdata.getInt("Language", 0);
                        }
                    });
            lan.show();
        }

        public void getTypeFace(final Preference pre) {
            File path = new File(Environment.getExternalStorageDirectory().toString() + "/FloatText/TTFs");
            if (!path.exists()) {
                //noinspection ResultOfMethodCallIgnored
                path.mkdirs();
            }
            File[] files = path.listFiles();
            int defaultchoice = 0;
            ArrayList<String> ttfs = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                String str = files[i].getName();
                String extra = ActivityMethod.getExtraName(str);
                if (extra.equalsIgnoreCase("ttf")) {
                    String realname = str.substring(0, str.length() - 4);
                    ttfs.add(realname);
                    if (realname.equalsIgnoreCase(default_typeface)) {
                        defaultchoice = i + 1;
                    }
                }
            }
            typeface_choice = defaultchoice;
            ttfs.add(0, getString(R.string.text_default_typeface));
            final String[] ttfname = new String[ttfs.size()];
            for (int i = 0; i < ttfs.size(); i++) {
                ttfname[i] = ttfs.get(i);
            }
            AlertDialog.Builder pathselect = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.text_choose_typeface)
                    .setSingleChoiceItems(ttfname, defaultchoice, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface p1, int p2) {
                            typeface_choice = p2;
                        }
                    })
                    .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface p1, int p2) {
                            SharedPreferences setdata = getActivity().getSharedPreferences("ApplicationSettings", Activity.MODE_PRIVATE);
                            if (typeface_choice == 0) {
                                setdata.edit().putString("DefaultTTFName", "Default").apply();
                                pre.setSummary(getString(R.string.xml_global_text_typeface_summary) + getString(R.string.text_default_typeface));
                                default_typeface = "Default";
                            } else {
                                setdata.edit().putString("DefaultTTFName", ttfname[typeface_choice]).apply();
                                pre.setSummary(getString(R.string.xml_global_text_typeface_summary) + ttfname[typeface_choice]);
                                default_typeface = ttfname[typeface_choice];
                            }
                            FloatManageMethod.restartApplication(getActivity(), getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName()));
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            pathselect.show();
        }

        public void setADTsum(Preference p) {
            Preference filter = findPreference("WinFilterSwitch");
            if (ActivityMethod.isAccessibilitySettingsOn(getActivity())) {
                filter.setEnabled(true);
                p.setSummary(getString(R.string.status) + getString(R.string.on) + "\n" + getString(R.string.xml_global_service_advancetext_sum));
            } else {
                filter.setEnabled(false);
                FloatManageMethod.setWinManager(getActivity());
                p.setSummary(getString(R.string.status) + getString(R.string.off) + "\n" + getString(R.string.xml_global_service_advancetext_sum));
            }
        }

        public void setNOSsum(Preference p) {
            if (ActivityMethod.isNotificationListenerEnabled(getActivity())) {
                p.setSummary(getString(R.string.status) + getString(R.string.on) + "\n" + getString(R.string.xml_global_service_notificationtext_sum));
            } else {
                p.setSummary(getString(R.string.status) + getString(R.string.off) + "\n" + getString(R.string.xml_global_service_notificationtext_sum));
            }
        }

        private void getAppInfo(Context ctx, ArrayList<String> PkgHave) {
            PackageManager pm = ctx.getPackageManager();
            List<PackageInfo> info = pm.getInstalledPackages(0);
            String FloatTextPkgName = ctx.getPackageName();
            ActivityMethod.orderPackageList(ctx, info);
            AppNames = new String[info.size() - 1];
            PkgNames = new String[info.size() - 1];
            AppState = new boolean[info.size() - 1];
            int countnum = 0;
            for (int i = 0; i < info.size(); i++) {
                String pkgname = info.get(i).packageName;
                if (!pkgname.equalsIgnoreCase(FloatTextPkgName)) {
                    AppNames[countnum] = info.get(i).applicationInfo.loadLabel(ctx.getPackageManager()).toString();
                    PkgNames[countnum] = pkgname;
                    AppState[countnum] = PkgHave.contains(pkgname);
                    countnum++;
                }
            }
        }
    }

}
