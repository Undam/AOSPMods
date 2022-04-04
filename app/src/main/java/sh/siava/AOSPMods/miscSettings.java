package sh.siava.AOSPMods;

import com.topjohnwu.superuser.Shell;

import java.util.regex.Pattern;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class miscSettings implements IXposedModPack {

    @Override
    public void updatePrefs(String...Key) {
        if(XPrefs.Xprefs == null) return; //it won't be null. but anyway...

        if(Key.length == 0)
        {
            //we are at startup
            updateSysUITuner();
            updateWifiCell();
            updateGSansOverride();
        }
        else
        {
            //we know what has changed
            switch(Key[0])
            {
                case "sysui_tuner":
                    updateSysUITuner();
                    break;
                case "wifi_cell":
                    updateWifiCell();
                    break;
                case "gsans_override":
                    updateGSansOverride();
                    break;
            }
        }
    }

    private void updateWifiCell() {
        Boolean WifiCellEnabled = XPrefs.Xprefs.getBoolean("wifi_cell", false);

        try {
            String currentTiles = Shell.su("settings get secure sysui_qs_tiles").exec().getOut().get(0);

            boolean hasWifi = Pattern.matches(getPattern("wifi"), currentTiles);
            boolean hasCell = Pattern.matches(getPattern("cell"), currentTiles);
            boolean hasInternet = Pattern.matches(getPattern("internet"), currentTiles);

            boolean providerModel;

            if (WifiCellEnabled) {
                providerModel = false;
                if (!hasCell) {
                    currentTiles = String.format("cell,%s", currentTiles);
                }
                if (!hasWifi) {
                    currentTiles = String.format("wifi,%s", currentTiles);
                }
                currentTiles = currentTiles.replaceAll(getPattern("internet"), "$2$3$5"); //remove intrnet

            } else {
                providerModel = true;

                currentTiles = currentTiles.replaceAll(getPattern("cell"), "$2$3$5"); //remove cell
                currentTiles = currentTiles.replaceAll(getPattern("wifi"), "$2$3$5"); //remove wifi

                if (!hasInternet) {
                    currentTiles = "internet," + currentTiles;
                }
            }

            Shell.su("settings put global settings_provider_model " + providerModel + "; settings put secure sysui_qs_tiles \"" + currentTiles + "\"").exec();
        }catch (Exception ignored){}
    }

    private static String getPattern(String tile)
    {
        return String.format("^(%s,)(.+)|(.+)(,%s)(,.+|$)",tile,tile);
    }

    private void updateSysUITuner() {
        Boolean SysUITunerEnabled = XPrefs.Xprefs.getBoolean("sysui_tuner", false);
        String mode = (SysUITunerEnabled) ? "enable" : "disable";

        try {
            Shell.su("pm " + mode + " com.android.systemui/.tuner.TunerActivity").exec();
        }catch(Exception ignored){}
    }

    private void updateGSansOverride() {
        Boolean GSansOverrideEnabled = XPrefs.Xprefs.getBoolean("gsans_override", false);
        try {

            if (GSansOverrideEnabled){
                Shell.su(String.format("cp %s/data/fontz/GSans/*.ttf %s/system/fonts/ && cp %s/data/productz/etc/fonts_customization.xml.NEW %s/system/product/etc/fonts_customization.xml",XPrefs.MagiskRoot, XPrefs.MagiskRoot, XPrefs.MagiskRoot, XPrefs.MagiskRoot)).exec();
            } else {
                Shell.su(String.format("rm -rf %s/system/fonts/*.ttf && cp %s/data/productz/etc/fonts_customization.xml.OLD %s/system/product/etc/fonts_customization.xml", XPrefs.MagiskRoot, XPrefs.MagiskRoot, XPrefs.MagiskRoot)).exec();
            }
        }catch(Exception ignored){}
    }

    @Override
    public String getListenPack() {
        return "com.android.systemui";
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        return;
    }
}