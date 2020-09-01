/*
 * MIT License
 *
 * Copyright (c) 2020 kyngs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cz.kyngs.duinotools.statusupdater;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.kyngs.logger.LogManager;
import cz.kyngs.logger.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Updater implements Runnable {

    public static final Logger LOGGER;
    public static final Gson GSON;

    static {
        LOGGER = new LogManager()
                .createLogger(true);
        GSON = new Gson();
    }

    private final String apiKey;
    private final Object ipLock;
    private final HttpClient httpClient;
    private String ip;
    private int port;

    public Updater(String[] args) throws IOException {

        ipLock = new Object();
        if (args.length != 1) {
            throw new IllegalArgumentException("INVALID ARGUMENTS!");
        }

        apiKey = args[0];

        Map.Entry<String, Integer> startData = getServerIP();

        ip = startData.getKey();
        port = startData.getValue();

        httpClient = HttpClients.createDefault();

        Thread ipUpdater = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    break;
                }
                Map.Entry<String, Integer> data;
                try {
                    data = getServerIP();
                } catch (IOException e) {
                    LOGGER.warn("Failed to retrieve server IP, is GitHub down?");
                    continue;
                }

                synchronized (ipLock) {
                    ip = data.getKey();
                    port = data.getValue();
                }

            }

        }, "IP Updater");
        ipUpdater.setDaemon(false);
        ipUpdater.start();

        Thread thread = new Thread(this, "Main loop");
        thread.start();
    }

    private static Map.Entry<String, Integer> getServerIP() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/revoxhere/duino-coin/gh-pages/serverip.txt").openStream()));
        String IP = reader.readLine();
        int port = Integer.parseInt(reader.readLine());
        return new EntryImpl<>(IP, port);
    }

    public static void main(String[] args) {
        try {
            new Updater(args);
        } catch (Exception e) {
            LOGGER.error("Error occurred while initializing!", e);
            System.exit(1);
        }
    }

    @SuppressWarnings("BusyWait")
    public void run() {
        while (true) {
            try {
                Thread.sleep(1500);

                boolean serverStatus = retrieveServerStatus();

                sendApiRequest(Status.byBool(serverStatus), "0y426sbfp90y");
                Thread.sleep(1500);
                Status statisticsStatus = retrieveStatisticsStatus();
                sendApiRequest(statisticsStatus, "ym6bpq3nq67b");
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                LOGGER.warn("UH OH Some error occurred while updating stats!");
                e.printStackTrace();
            }
        }
    }

    private void sendApiRequest(Status status, String compID) throws IOException {

        LOGGER.info("Sending API request");

        String command = String.format("https://api.statuspage.io/v1/pages/hh0g5sfyc1h0/components/%s?api_key=%s", compID, apiKey);

        HttpPatch httpPatch = new HttpPatch(command);

        Map<String, String> props = getComponentProps(compID);

        StringEntity stringEntity = new StringEntity(String.format("{\n  \"component\": {\n    \"description\": \"%s\",\n    \"status\": \"%s\",\n    \"name\": \"%s\",\n    \"only_show_if_degraded\": %s,\n    \"showcase\": %s\n  }\n}", props.get("description"), status.name().toLowerCase(), props.get("name"), props.get("only_show_if_degraded"), props.get("showcase")));

        stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        httpPatch.setEntity(stringEntity);

        HttpResponse httpResponse = httpClient.execute(httpPatch);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        String line = bufferedReader.readLine();

        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line);
            line = bufferedReader.readLine();
        }

        httpPatch.releaseConnection();

        LOGGER.info(String.format("SENT! %s", sb.toString()));

    }

    private Map<String, String> getComponentProps(String compID) throws IOException {
        return GSON.fromJson(new InputStreamReader(new URL(String.format("https://api.statuspage.io/v1/pages/hh0g5sfyc1h0/components/%s?api_key=%s", compID, apiKey)).openStream()), new TypeToken<Map<String, String>>() {
        }.getType());
    }

    private Status retrieveStatisticsStatus() throws ParseException {
        try {
            URL url = new URL("https://raw.githubusercontent.com/revoxhere/duco-statistics/master/api.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = reader.readLine();
            StringBuilder sb = new StringBuilder();

            while (line != null) {
                sb.append(line).append("\n");
                line = reader.readLine();
            }

            Map<String, String> data = GSON.fromJson(sb.toString(), new TypeToken<Map<String, String>>() {
            }.getType());

            String time = data.get("Last update");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

            TimeZone timeZone = TimeZone.getTimeZone(time.substring(18, 23));

            ZonedDateTime lastApiUpdate = simpleDateFormat.parse(time.substring(0, 16)).toInstant().atZone(timeZone.toZoneId());
            ZonedDateTime now = Instant.now().atZone(timeZone.toZoneId());
            Duration duration = Duration.between(lastApiUpdate, now);

            long seconds = duration.getSeconds();

            if (seconds > TimeUnit.MINUTES.toSeconds(40)) return Status.MAJOR_OUTAGE;
            if (seconds > TimeUnit.MINUTES.toSeconds(20)) return Status.PARTIAL_OUTAGE;

        } catch (IOException e) {
            return Status.MAJOR_OUTAGE;
        }
        return Status.OPERATIONAL;
    }

    private boolean retrieveServerStatus() {
        String ip;
        int port;

        synchronized (ipLock) {
            ip = this.ip;
            port = this.port;
        }

        try {
            Socket socket = new Socket(ip, port);
            byte[] buf = new byte[3];
            socket.getInputStream().read(buf);
            String s = new String(buf);
            if (s.contentEquals("SSH")) {
                socket.close();
                return false;
            }
            socket.close();
        } catch (ConnectException | UnknownHostException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


}
