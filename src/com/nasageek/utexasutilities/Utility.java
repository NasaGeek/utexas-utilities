
package com.nasageek.utexasutilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

public class Utility {

    private static String sID = null;
    private static final String INSTALLATION = "UUID";

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void commit(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

    public static void updateBusStops(Context con) throws IOException {
        new StopParseTask(con).execute();
    }

    // TODO: check resulting files against regex to confirm they are correct?
    static class StopParseTask extends AsyncTask<Void, Void, Void> {
        String errorText;
        Context con;

        public StopParseTask(Context con) {
            this.con = con;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(con, "Fetching stops", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... v) {

            // 675, 684, 685 were retired on 1/7/14
            int[] routes = {
                    640, 641, 642, 651, 652, 653, 656, 661, 663, 670, 671, 672, 680, 681
            };

            // FileOutputStream fos = con.openFileOutput("stops",
            // Context.MODE_PRIVATE);
            File folder = con.getDir("stops", Context.MODE_PRIVATE);

            DefaultHttpClient client = ConnectionHelper.getThreadSafeClient();
            for (int route : routes) {
                // BufferedOutputStream bos = new
                // BufferedOutputStream(con.openFileOutput(route+"_stops.txt",
                // Context.MODE_PRIVATE));

                HttpGet hget = new HttpGet("http://www.capmetro.org/schedulemap-ut.aspx?f1="
                        + route + "&map=stops");
                String pagedata = "";

                try {
                    HttpResponse response = client.execute(hget);
                    pagedata = EntityUtils.toString(response.getEntity());
                } catch (Exception e) {
                    errorText = "Connection to CapMetro failed.";
                    cancel(true);
                    e.printStackTrace();
                    return null;
                }
                File stopsfile = new File(folder, route + "_stops.txt");
                BufferedOutputStream bos = null;

                try {
                    bos = new BufferedOutputStream(new FileOutputStream(stopsfile));
                } catch (FileNotFoundException e) {
                    errorText = "Stop files could not be created.";
                    cancel(true);
                    e.printStackTrace();
                    return null;
                }
                // out of date as of 7/27/2013
                // Pattern scriptPattern =
                // Pattern.compile("<script type=\"text/javascript\">.*?/\\* <!\\[CDATA\\[ \\*/.*function initMap()",
                // Pattern.DOTALL);
                Pattern scriptPattern = Pattern.compile("var markers = \\[(.*?)\\];",
                        Pattern.DOTALL);
                Matcher scriptMatcher = scriptPattern.matcher(pagedata);
                if (scriptMatcher.find()) {
                    String script = scriptMatcher.group(1);

                    // out of date as of 9/1/2013
                    // Pattern coordPattern =
                    // Pattern.compile("(30\\.\\d*);(-97\\.\\d*);");
                    Pattern coordPattern = Pattern
                            .compile("lat: (3[0-9]\\.\\d+), lng: (-9[0-9]\\.\\d+),");
                    Matcher coordMatcher = coordPattern.matcher(script);

                    Pattern namePattern = Pattern.compile("Stop Name - (.*?)</b>");
                    Matcher nameMatcher = namePattern.matcher(script);

                    Pattern idPattern = Pattern.compile("Stop ID - (\\d*)");
                    Matcher idMatcher = idPattern.matcher(script);

                    try {

                        while (coordMatcher.find() && nameMatcher.find() && idMatcher.find()) {
                            bos.write((coordMatcher.group(1) + ", " + coordMatcher.group(2) + "\t")
                                    .getBytes());
                            bos.write((nameMatcher.group(1) + "\t").getBytes());
                            bos.write((idMatcher.group(1) + "\n").getBytes());
                        }
                        bos.flush();
                        bos.close();

                    } catch (IOException e) {
                        errorText = "IO Error.";
                        cancel(true);
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    errorText = "Stop data could not be found on CapMetro.";
                    cancel(true);
                    return null;
                }
            }
            return null;
        }

        // TODO: progressdialog!
        @Override
        protected void onCancelled(Void result) {
            Toast.makeText(con, errorText, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO: will need to set a flag here so campus map knows where to
            // fetch the new stops
            // (private data folder instead of assets)
            Toast.makeText(con, "Stops have been updated.", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * this method copies the ViewPager method of the same name (perhaps I
     * should just use reflection?) to generate the necessary fragment tags for
     * the purpose of restoring them
     */
    public static String makeFragmentName(int viewId, int position) {
        return "android:switcher:" + viewId + ":" + position;
    }
}
