package tool.xfy9326.floattext.Method;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.floattext.FileSelector.SelectFile;
import tool.xfy9326.floattext.FloatManage;
import tool.xfy9326.floattext.R;
import tool.xfy9326.floattext.Service.FloatAdvanceTextUpdateService;
import tool.xfy9326.floattext.Service.FloatNotificationListenerService;
import tool.xfy9326.floattext.Service.FloatTextUpdateService;
import tool.xfy9326.floattext.Service.FloatUpdateService;
import tool.xfy9326.floattext.Service.FloatWindowStayAliveService;
import tool.xfy9326.floattext.Setting.FloatTextSetting;
import tool.xfy9326.floattext.Setting.FloatWebSetting;
import tool.xfy9326.floattext.Tool.GithubUpdateCheck;
import tool.xfy9326.floattext.Utils.App;
import tool.xfy9326.floattext.Utils.FloatData;
import tool.xfy9326.floattext.Utils.FloatTextUtils;
import tool.xfy9326.floattext.Utils.StaticNum;
import tool.xfy9326.floattext.View.FloatLinearLayout;
import tool.xfy9326.floattext.View.FloatTextView;
import tool.xfy9326.floattext.View.ListViewAdapter;

public class FloatManageMethod {
    private static boolean waitdoubleclick = false;
    private static Handler waithandle;
    private static Runnable waitrun;

    //删除下载的文件(一天前)
    public static void DeleteDownloadFile() {
        long time = System.currentTimeMillis();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FloatText/Download/");
        if (file.isDirectory() && file.exists()) {
            File[] apks = file.listFiles();
            for (File apk : apks) {
                if (time - apk.lastModified() > 1000 * 60 * 60 * 24) {
                    //noinspection ResultOfMethodCallIgnored
                    apk.delete();
                }
            }
        }
    }

    //自动检测更新
    public static void AutoCheckUpdate(Context ctx) {
        if (ActivityMethod.isNetworkAvailable(ctx)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences spa = ctx.getSharedPreferences("ApplicationSettings", Context.MODE_PRIVATE);
            if (sp.getBoolean("AutoCheckUpdate", false)) {
                if ((System.currentTimeMillis() - spa.getLong("LastUpdateCheckTime", 0) >= 1000 * 60 * 60)) {
                    spa.edit().putLong("LastUpdateCheckTime", System.currentTimeMillis()).apply();
                    GithubUpdateCheck gu = new GithubUpdateCheck(ctx);
                    gu.setProjectData("XFY9326", "FloatText");
                    gu.setMarketDownload(true, "http://www.coolapk.com/apk/tool.xfy9326.floattext");
                    gu.showUpdateInfoDialog(false);
                }
            }
        }
    }

    //通知栏更新
    public static void UpdateNotificationCount(Context ctx) {
        NotificationCompat.Builder notification = ((App) ctx.getApplicationContext()).getNotification();
        if (notification != null) {
            RemoteViews rv = ((App) ctx.getApplicationContext()).getRemoteview();
            if (rv != null) {
                NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                rv.setTextViewText(R.id.textview_notification_win_count, ctx.getString(R.string.notification_win_count, String.valueOf(FloatManageMethod.getWinCount(ctx))));
                notification.setContent(rv);
                nm.notify(StaticNum.ONGONING_NOTIFICATION_ID, notification.build());
            }
        }
    }

    //获取窗口数量
    public static int getWinCount(Context ctx) {
        App utils = (App) ctx.getApplicationContext();
        return utils.getTextutil().getTextShow().size();
    }

    //隐藏和显示全部
    public static void ShoworHideAllWin(Context ctx, boolean showwin, boolean notifycontrolmode) {
        App utils = (App) ctx.getApplicationContext();
        FloatTextUtils textutils = utils.getTextutil();
        ArrayList<Boolean> showFloat = textutils.getShowFloat();
        if (showFloat.size() > 0) {
            for (int i = 0; i < showFloat.size(); i++) {
                if (!notifycontrolmode || textutils.getNotifyControl().get(i)) {
                    showFloat.set(i, showwin);
                    FloatLinearLayout floatLinearLayout = utils.getFrameutil().getFloatlinearlayout().get(i);
                    floatLinearLayout.setShowState(showwin);
                }
            }

            ListViewAdapter adapter = utils.getListviewadapter();
            FloatData dat = new FloatData(ctx);
            dat.savedata();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    //隐藏和显示单个窗口
    //显示返回true
    @SuppressWarnings("UnusedReturnValue")
    public static boolean ShoworHideWin(Context ctx, int index) {
        boolean result = true;
        App utils = (App) ctx.getApplicationContext();
        FloatTextUtils textutils = utils.getTextutil();
        ArrayList<Boolean> showFloat = textutils.getShowFloat();
        if (index >= 0 && index < showFloat.size() && showFloat.size() != 0) {
            boolean iShowFloat = showFloat.get(index);
            iShowFloat = !showFloat.set(index, !iShowFloat);
            FloatLinearLayout floatLinearLayout = utils.getFrameutil().getFloatlinearlayout().get(index);
            floatLinearLayout.setShowState(iShowFloat);

            result = iShowFloat;

            ListViewAdapter adapter = utils.getListviewadapter();
            FloatData dat = new FloatData(ctx);
            dat.savedata();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        }
        return result;
    }

    //锁定和解锁全部
    public static void LockorUnlockAllWin(Context ctx, boolean lockwin, boolean notifycontrolmode) {
        App utils = (App) ctx.getApplicationContext();
        FloatTextUtils textutils = utils.getTextutil();
        ArrayList<Boolean> lock = textutils.getLockPosition();
        if (lock.size() > 0) {
            ArrayList<String> position = textutils.getPosition();
            for (int i = 0; i < lock.size(); i++) {
                if (!notifycontrolmode || textutils.getNotifyControl().get(i)) {
                    FloatLinearLayout fll = utils.getFrameutil().getFloatlinearlayout().get(i);
                    fll.setPositionLocked(lockwin);
                    lock.set(i, lockwin);
                    if (lockwin) {
                        position.set(i, fll.getPosition());
                    }
                }
            }

            ListViewAdapter adapter = utils.getListviewadapter();
            FloatData dat = new FloatData(ctx);
            dat.savedata();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    //锁定或解锁单个窗口
    //锁定返回true
    public static boolean LockorUnlockWin(Context ctx, int index) {
        boolean result = false;
        App utils = (App) ctx.getApplicationContext();
        FloatTextUtils textutils = utils.getTextutil();
        ArrayList<Boolean> lock = textutils.getLockPosition();
        ArrayList<String> position = textutils.getPosition();
        if (index >= 0 && index < lock.size() && lock.size() != 0) {
            FloatLinearLayout fll = utils.getFrameutil().getFloatlinearlayout().get(index);
            if (fll.getPositionLocked()) {
                fll.setPositionLocked(false);
                lock.set(index, false);
                result = false;
            } else {
                fll.setPositionLocked(true);
                lock.set(index, true);
                position.set(index, fll.getPosition());
                result = true;
            }

            FloatData dat = new FloatData(ctx);
            dat.savedata();
            ListViewAdapter adapter = utils.getListviewadapter();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        }
        return result;
    }

    //导入或导出的权限检查
    public static void TextFileSolve(Activity ctx, int type, int requestcode) {
        if (Build.VERSION.SDK_INT > 22) {
            if (ctx.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ctx.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestcode);
            } else {
                if (type == 0) {
                    FloatManageMethod.exporttxt(ctx);
                } else if (type == 1) {
                    FloatManageMethod.selectFile(ctx);
                }
            }
        } else {
            if (type == 0) {
                FloatManageMethod.exporttxt(ctx);
            } else if (type == 1) {
                FloatManageMethod.selectFile(ctx);
            }
        }
    }

    //根Activity判断
    public static void RootTask(Activity act) {
        if (!act.isTaskRoot()) {
            act.finish();
        }
    }

    //重启应用
    public static void restartApplication(Context ctx, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (Build.VERSION.SDK_INT >= 21) {
            ctx.startActivity(intent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ctx.startActivity(intent);
        }
        System.exit(0);
    }

    //设置加载窗口
    public static AlertDialog setLoadingDialog(Context ctx) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View layout = inflater.inflate(R.layout.dialog_loading, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(ctx)
                .setView(layout)
                .setCancelable(false);
        return dialog.create();
    }

    //导入文本
    public static boolean importtxt(Context ctx, String path) {
        File file = new File(path);
        App utils = (App) ctx.getApplicationContext();
        String[] lines = IOMethod.readfile(file);
        ArrayList<String> fixline = new ArrayList<>();
        for (String line : lines) {
            if (!Arrays.toString(lines).isEmpty() && !line.replaceAll("\\s+", "").equalsIgnoreCase("") && !line.replace(" ", "").replace("\n", "").equals("")) {
                fixline.add(line);
            }
        }
        if (fixline.size() != 0 && fixline.size() < 100) {
            ArrayList<String> data = utils.getTextData();
            for (int a = 0; a < fixline.size(); a++) {
                data.add(fixline.get(a));
            }
            closeAllWin(ctx);
            FloatData dat = new FloatData(ctx);
            dat.savedata();
            dat.getSaveArrayData();
            dat.savedata();
            return true;
        } else {
            return false;
        }
    }

    //输出文本
    public static void exporttxt(Context ctx) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", ctx.getResources().getConfiguration().locale);
        String path = Environment.getExternalStorageDirectory().toString() + "/FloatText/Export/FloatText>" + sdf.format(new Date()) + ".txt";
        String str = "";
        ArrayList<String> str_arr = ((App) ctx.getApplicationContext()).getTextData();
        for (int i = 0; i < str_arr.size(); i++) {
            str += str_arr.get(i) + "\n";
        }
        if (!str.replace("\n", "").replaceAll("\\s+", "").equalsIgnoreCase("")) {
            IOMethod.writefile(path, str);
            Toast.makeText(ctx, ctx.getString(R.string.text_export_success) + path, Toast.LENGTH_LONG).show();
        } else {
            FloatManage.snackshow((Activity) ctx, ctx.getString(R.string.text_export_error));
        }
    }

    //选择文件
    public static void selectFile(Activity ctx) {
        Toast.makeText(ctx, R.string.text_import_notice, Toast.LENGTH_LONG).show();
        SelectFile sf = new SelectFile(StaticNum.FLOAT_TEXT_IMPORT_CODE, SelectFile.TYPE_ChooseFile);
        sf.start(ctx);
    }

    //关闭所有窗口
    private static void closeAllWin(Context ctx) {
        WindowManager wm = ((App) ctx.getApplicationContext()).getFloatwinmanager();
        ArrayList<FloatLinearLayout> layout = ((App) ctx.getApplicationContext()).getFrameutil().getFloatlinearlayout();
        ArrayList<Boolean> show = ((App) ctx.getApplicationContext()).getTextutil().getShowFloat();
        for (int i = 0; i < layout.size(); i++) {
            if (show.get(i)) {
                wm.removeView(layout.get(i));
            }
        }
    }

    //动态变量列表
    public static void showDList(final Activity ctx) {
        if (((App) ctx.getApplicationContext()).DynamicNumService) {
            final String[] dynamiclist = ctx.getResources().getStringArray(R.array.floatsetting_dynamic_list);
            String[] dynamicname = ctx.getResources().getStringArray(R.array.floatsetting_dynamic_name);
            String[] result = new String[dynamiclist.length + 1];
            for (int i = 0; i < dynamiclist.length; i++) {
                result[i] = "<" + dynamiclist[i] + ">" + "\n" + dynamicname[i];
            }
            result[dynamiclist.length] = ctx.getString(R.string.dynamic_num_tip);
            AlertDialog.Builder list = new AlertDialog.Builder(ctx)
                    .setTitle(R.string.dynamic_list_title)
                    .setItems(result, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int i) {
                            if (i != dynamiclist.length) {
                                ClipboardManager clip = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                                if (((App) ctx.getApplicationContext()).HtmlMode) {
                                    clip.setText("#" + dynamiclist[i] + "#");
                                } else {
                                    clip.setText("<" + dynamiclist[i] + ">");
                                }
                                Toast.makeText(ctx, R.string.copy_ok, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            list.show();
        } else {
            FloatManage.snackshow(ctx, ctx.getString(R.string.dynamicservice_no_open));
        }
    }

    //权限提示
    @TargetApi(Build.VERSION_CODES.M)
    private static void notifypermission(final Activity ctx) {
        if (!Settings.canDrawOverlays(ctx)) {
            ctx.runOnUiThread(new Runnable() {
                public void run() {
                    CoordinatorLayout cl = (CoordinatorLayout) ctx.findViewById(R.id.FloatManage_MainLayout);
                    Snackbar.make(cl, R.string.no_premission, Snackbar.LENGTH_SHORT).setAction(R.string.get_premission, new OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + ctx.getPackageName()));
                            ctx.startActivity(intent);
                        }
                    }).setActionTextColor(Color.RED).show();

                }
            });
        }
    }

    //权限判断与悬浮窗重现
    public static Thread PrepareSave(Activity ctx, Handler han) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(ctx)) {
                FloatManageMethod.askforpermission(ctx, StaticNum.RESHOW_PERMISSION_RESULT_CODE);
            } else {
                FloatManageMethod.delayaskforpermission(ctx);
                if (((App) ctx.getApplicationContext()).getTextData().size() > 0) {
                    return FloatManageMethod.Reshow(ctx, han);
                } else {
                    FloatData fd = new FloatData(ctx);
                    fd.savedata();
                }
            }
        } else {
            if (((App) ctx.getApplicationContext()).getTextData().size() > 0) {
                return FloatManageMethod.Reshow(ctx, han);
            } else {
                FloatData fd = new FloatData(ctx);
                fd.savedata();
            }
        }
        return null;
    }

    //关闭应用
    public static void ShutDown(Context ctx) {
        FloatManageMethod.stopservice(ctx);
        App utils = (App) ctx.getApplicationContext();
        utils.setGetSave(false);
        utils.setFloatReshow(true);
        utils.setStartShowWin(false);
        FloatManageMethod.closeAllWin(ctx);
        System.gc();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                System.exit(0);
            }
        }, 100);
    }

    //关闭应用控制(SnackBar)
    public static void SnackShow_CloseApp(final Activity ctx) {
        if (waitdoubleclick) {
            ctx.finish();
            ShutDown(ctx);
        } else {
            CoordinatorLayout cl = (CoordinatorLayout) ctx.findViewById(R.id.FloatManage_MainLayout);
            Snackbar.make(cl, R.string.exit_title, Snackbar.LENGTH_LONG).setAction(R.string.back_to_launcher, new OnClickListener() {
                public void onClick(View v) {
                    ctx.moveTaskToBack(true);
                }
            }).setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar bar, int i) {
                    waitdoubleclick = false;
                    if (waithandle != null && waitrun != null) {
                        waithandle.removeCallbacks(waitrun);
                    }
                    super.onDismissed(bar, i);
                }
            }).
                    setActionTextColor(Color.RED).show();
            waitdoubleclick = true;
            waithandle = new Handler();
            waitrun = new Runnable() {
                public void run() {
                    waitdoubleclick = false;
                    waithandle = null;
                    waitrun = null;
                }
            };
            waithandle.postDelayed(waitrun, 2200);
        }
    }

    //设置WindowManager
    public static void setWinManager(Context ctx) {
        WindowManager wm = (WindowManager) ctx.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        ((App) ctx.getApplicationContext()).setFloatwinmanager(wm);
    }

    //首次请求权限
    public static void first_ask_for_premission(final Context ctx) {
        SharedPreferences setdata = ctx.getSharedPreferences("ApplicationSettings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor setedit = setdata.edit();
        if (!setdata.getBoolean("FirstUse_AskForPremission", false)) {
            AlertDialog.Builder asp = new AlertDialog.Builder(ctx)
                    .setTitle(R.string.ask_for_premission)
                    .setMessage(R.string.ask_for_premission_alert)
                    .setNeutralButton(R.string.open_application_set, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int i) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + ctx.getPackageName()));
                            ctx.startActivity(intent);
                        }
                    })
                    .setPositiveButton(R.string.done, null);
            asp.show();
            setedit.putBoolean("FirstUse_AskForPremission", true);
            setedit.apply();
        }
    }

    //准备文件夹
    public static void preparefolder() {
        String typeface_path = Environment.getExternalStorageDirectory().toString() + "/FloatText/TTFs";
        File typeface = new File(typeface_path);
        if (!typeface.exists()) {
            //noinspection ResultOfMethodCallIgnored
            typeface.mkdirs();
        }
    }

    //延迟请求权限
    private static void delayaskforpermission(final Activity act) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                FloatManageMethod.notifypermission(act);
            }
        }, 2000);
    }

    //请求权限
    @TargetApi(Build.VERSION_CODES.M)
    private static void askforpermission(final Activity act, int code) {
        final int askcode = code;
        AlertDialog.Builder dialog = new AlertDialog.Builder(act)
                .setTitle(R.string.ask_for_premission)
                .setMessage(act.getString(R.string.ask_for_premisdion_msg) + act.getString(R.string.reshow_msg))
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface p1, int p2) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setData(Uri.parse("package:" + act.getPackageName()));
                        act.startActivityForResult(intent, askcode);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface p1, int p2) {
                        act.finish();
                        System.exit(0);
                    }
                })
                .setCancelable(false);
        dialog.show();
    }

    //读取并且设置应用所有数据
    public static Thread getSaveData(final Context ctx, final App utils, final SharedPreferences spdata, final Handler han) {
        return new Thread() {
            public void run() {
                FloatData dat = new FloatData(ctx);
                dat.getSaveArrayData();
                utils.setMovingMethod(spdata.getBoolean("TextMovingMethod", false));
                utils.setStayAliveService(spdata.getBoolean("StayAliveService", true));
                utils.setDynamicNumService(spdata.getBoolean("DynamicNumService", false));
                utils.setDevelopMode(spdata.getBoolean("DevelopMode", false));
                utils.setHtmlMode(spdata.getBoolean("HtmlMode", true));
                utils.setListTextHide(spdata.getBoolean("ListTextHide", false));
                utils.setTextFilter(spdata.getBoolean("TextFilter", false));
                han.obtainMessage(0).sendToTarget();
            }
        };
    }

    //重现悬浮窗
    public static Thread Reshow(final Activity ctx, final Handler han) {
        return new Thread() {
            public void run() {
                final App utils = (App) ctx.getApplicationContext();
                final FloatTextUtils textutils = utils.getTextutil();
                ctx.runOnUiThread(new Runnable() {
                    public void run() {
                        Reshow_Create(ctx, utils, textutils, textutils.getTextShow(), textutils.getShowFloat(), textutils.getLockPosition(), textutils.getPosition(), textutils.getTextMove());
                        utils.getListviewadapter().notifyDataSetChanged();
                    }
                });
                if (han != null) {
                    han.obtainMessage(1).sendToTarget();
                }
            }
        };
    }

    public static void Reshow_Create(Context ctx, App utils, FloatTextUtils textutils, ArrayList<String> Text, ArrayList<Boolean> Show, ArrayList<Boolean> Lock, ArrayList<String> Position, ArrayList<Boolean> Move) {
        if (Text.size() != 0) {
            WindowManager wm = utils.getFloatwinmanager();
            for (int i = 0; i < Text.size(); i++) {
                FloatTextView fv = FloatTextSettingMethod.CreateFloatView(ctx, textutils, i);
                FloatLinearLayout fll = FloatTextSettingMethod.CreateLayout(ctx, i);
                fll.changeShowState(Show.get(i));
                String[] ptemp = new String[]{"100", "150"};
                if (Lock.get(i)) {
                    ptemp = Position.get(i).split("_");
                    fll.setAddPosition(Float.parseFloat(ptemp[0]), Float.parseFloat(ptemp[1]));
                }
                WindowManager.LayoutParams layout = FloatTextSettingMethod.CreateFloatLayout(ctx, wm, fv, fll, Float.parseFloat(ptemp[0]), Float.parseFloat(ptemp[1]), textutils, i);
                fll.setFloatLayoutParams(layout);
                fll.setPositionLocked(Lock.get(i));
                fll.setTop(textutils.getAutoTop().get(i));
                if (utils.getMovingMethod()) {
                    fv.setMoving(Move.get(i), 0);
                } else {
                    fv.setMoving(Move.get(i), 1);
                    if (Move.get(i)) {
                        fll.setShowState(false);
                        fll.setShowState(true);
                    }
                }
                FloatTextSettingMethod.savedata(ctx, fv, fll, Text.get(i), layout);
            }
        }
    }

    //字体文件检测
    public static void floattext_typeface_check(Context ctx, boolean alert) {
        SharedPreferences setdata = ctx.getSharedPreferences("ApplicationSettings", Activity.MODE_PRIVATE);
        String filename = setdata.getString("DefaultTTFName", "Default");
        if (filename.equalsIgnoreCase("Default")) {
            return;
        } else {
            String typeface_path = Environment.getExternalStorageDirectory().toString() + "/FloatText/TTFs/" + filename + ".ttf";
            String typeface_path2 = Environment.getExternalStorageDirectory().toString() + "/FloatText/TTFs/" + filename + ".TTF";
            File f1 = new File(typeface_path);
            File f2 = new File(typeface_path2);
            if (f1.exists() || f2.exists()) {
                return;
            }
        }
        setdata.edit().putString("DefaultTTFName", "Default").apply();
        if (alert) {
            FloatManage.snackshow((Activity) ctx, ctx.getString(R.string.text_typeface_err));
        }
    }

    //语言设置 数据
    public static void LanguageInit(Activity ctx) {
        SharedPreferences setdata = ctx.getSharedPreferences("ApplicationSettings", Activity.MODE_PRIVATE);
        int lan = setdata.getInt("Language", 0);
        LanguageSet(ctx, lan);
    }

    //语言设置 分类
    public static void LanguageSet(Activity ctx, int i) {
        Resources resource = ctx.getResources();
        Configuration config = resource.getConfiguration();
        switch (i) {
            case 0:
                config.locale = Locale.getDefault();
                break;
            case 1:
                config.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case 2:
                config.locale = Locale.TAIWAN;
                break;
            case 3:
                config.locale = Locale.ENGLISH;
                break;
        }
        ctx.getBaseContext().getResources().updateConfiguration(config, null);
    }

    //启动服务
    public static void startservice(Context ctx) {
        if (((App) ctx.getApplicationContext()).getStayAliveService()) {
            Intent service = new Intent(ctx, FloatWindowStayAliveService.class);
            ctx.startService(service);
            if (ActivityMethod.isAccessibilitySettingsOn(ctx)) {
                Intent asservice = new Intent(ctx, FloatAdvanceTextUpdateService.class);
                ctx.startService(asservice);
            }
            if (ActivityMethod.isNotificationListenerEnabled(ctx)) {
                Intent notifyservice = new Intent(ctx, FloatNotificationListenerService.class);
                ctx.startService(notifyservice);
            }
        }
    }

    //关闭服务
    private static void stopservice(Context ctx) {
        Intent service = new Intent(ctx, FloatWindowStayAliveService.class);
        ctx.stopService(service);
        Intent floatservice = new Intent(ctx, FloatTextUpdateService.class);
        ctx.stopService(floatservice);
        Intent asservice = new Intent(ctx, FloatAdvanceTextUpdateService.class);
        ctx.stopService(asservice);
        Intent notifyservice = new Intent(ctx, FloatNotificationListenerService.class);
        ctx.stopService(notifyservice);
        Intent downloadservice = new Intent(ctx, FloatUpdateService.class);
        ctx.stopService(downloadservice);
    }

    //用户新建悬浮窗时的提示
    public static void addFloatWindow(final Activity ctx, final ArrayList<String> FloatDataName) {
        if (((App) ctx.getApplicationContext()).getDevelopMode()) {
            String[] type = ctx.getResources().getStringArray(R.array.floatmanage_choose);
            AlertDialog.Builder choose = new AlertDialog.Builder(ctx)
                    .setTitle(R.string.choose_float_type)
                    .setItems(type, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                Intent intent = new Intent(ctx, FloatTextSetting.class);
                                intent.putExtra("EditID", FloatDataName.size());
                                ctx.startActivityForResult(intent, StaticNum.FLOATTEXT_RESULT_CODE);
                            } else if (which == 1) {
                                Intent intent = new Intent(ctx, FloatWebSetting.class);
                                ctx.startActivity(intent);
                            }
                        }
                    });
            choose.show();
        } else {
            Intent intent = new Intent(ctx, FloatTextSetting.class);
            intent.putExtra("EditID", FloatDataName.size());
            ctx.startActivityForResult(intent, StaticNum.FLOATTEXT_RESULT_CODE);
        }
    }

}
