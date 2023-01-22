/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static cz.coffee.utils.ErrorHandler.Level.ERROR;
import static cz.coffee.utils.ErrorHandler.Level.WARNING;
import static cz.coffee.utils.ErrorHandler.sendMessage;

@SuppressWarnings("unused")
public class HTTPHandler {

    final Map<String, String> connectionProperty = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    boolean doOutput = true;
    boolean doInput = true;
    int responseCode;
    int timeout = 5500;
    STATUS status = STATUS.UNKNOWN;
    METHOD method = METHOD.GET;
    HttpsURLConnection currentConnection;
    InputStream inputStream;
    URL currentUrl;
    URL inputURL;

    /**
     * Constructor for HTTPHandler
     *
     * @param url           URL what to be executed
     * @param requestMethod Methods like GET,POST...
     */
    public HTTPHandler(URL url, String requestMethod) {
        this.inputURL = url;
        this.method = METHOD.valueOf(requestMethod);
    }

    /**
     * Constructor for HTTPHandler
     *
     * @param stringURL     StringURL what to be executed
     * @param requestMethod Methods like GET,POST...
     */
    public HTTPHandler(String stringURL, String requestMethod) {
        try {
            this.inputURL = new URL(stringURL);
            this.method = METHOD.valueOf(requestMethod);
        } catch (MalformedURLException malformedURLException) {
            sendMessage(malformedURLException.getCause(), WARNING);
        }
    }


    /**
     * Constructor for HTTPHandler without Request Methods settings.
     *
     * @param stringURL StringURL what to be executed
     */
    public HTTPHandler(String stringURL) {
        try {
            this.inputURL = new URL(stringURL);
            this.method = METHOD.GET;
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
        }
    }

    private STATUS parseCode(int i) {
        if (i >= 200 && i <= 299) {
            return STATUS.OK;
        } else if (i == 301) {
            return STATUS.REDIRECTED;
        } else if (i == 404) {
            return STATUS.NOT_FOUND;
        } else if (i == 500) {
            return STATUS.SERVER_ERROR;
        } else {
            return STATUS.NOT_OK;
        }
    }

    /**
     * This is function what allows you to set the input Header data. for example
     * <p> Authorization Token .... </p>
     *
     * <code>setProperty("Authorization", "Token ....")</code>
     *
     * @param key   Key
     * @param value Value
     * @return self
     */
    public HTTPHandler setProperty(String key, String value) {
        connectionProperty.put(key, value);
        return this;
    }

    /**
     * This is function what allows you to set the input Header data. for example
     * <p> Authorization Token .... </p>
     *
     * <pre>
     *     <code>
     * keys = List.of("Authorization", "Accept");
     * values = List.of("Token ...", "json/github+");
     * setProperty(keys, values);
     *      </code>
     * </pre>
     *
     * @param keys   List of String {@link List} of keys
     * @param values List of String {@link List} of values
     * @return self
     */
    public HTTPHandler setProperty(List<String> keys, List<String> values) {
        for (int i = 0; keys.size() > i; i++) {
            connectionProperty.put(keys.get(i), values.get(i));
        }
        return this;
    }

    /**
     * make a connection to the given URL
     */
    public void connect() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) inputURL.openConnection();
            connection.setRequestMethod(method.toString().toUpperCase());
            if (!connectionProperty.isEmpty()) {
                for (Map.Entry<String, String> properties : connectionProperty.entrySet()) {
                    connection.setRequestProperty(properties.getKey(), properties.getValue());
                }
            }
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(doOutput);
            connection.setDoInput(doInput);
            connection.connect();

            responseCode = connection.getResponseCode();
            status = parseCode(responseCode);
            currentConnection = connection;
            currentUrl = inputURL;
            inputStream = connection.getInputStream();

        } catch (IOException e) {
            sendMessage(e.getCause(), WARNING);
        }
    }

    /**
     * Setter for inputData
     *
     * @param doInput you can enable or disable input to the connection
     * @return {@link HTTPHandler}
     */
    public HTTPHandler allowInput(boolean doInput) {
        this.doInput = doInput;
        return this;
    }

    /**
     * Setter for outputData
     *
     * @param doOutput you can enable or disable output data from the connection
     * @return {@link HTTPHandler}
     */
    public HTTPHandler allowOutput(boolean doOutput) {
        this.doOutput = doOutput;
        return this;
    }

    /**
     * Getter for Status of connection
     *
     * @return connection status converted to {@link String}
     */
    public String getStatus() {
        return status.toString().toLowerCase();
    }

    /**
     * You can download the file by passing the new File inside a params.
     * You can use this function only when you're a connected to the given URL/server.
     *
     * @param file specified input location and extension of your download file
     * @return returned true/false depends on download status
     */
    public boolean download(File file) {
        try (BufferedInputStream in = new BufferedInputStream(getInputStream());
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fos.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException ioException) {
            sendMessage(ioException.getCause(), WARNING);
            return false;
        }
        return true;
    }

    /**
     * Website response's body
     *
     * @param jsonEncoded true/false depends on website response content.
     * @return the content of body of given URL
     */
    public Object getContents(boolean... jsonEncoded) {
        boolean jsonEncoded_ = false;
        if (jsonEncoded != null && jsonEncoded.length > 0) {
            if (jsonEncoded[0]) {
                jsonEncoded_ = true;
            }
        }
        if (responseCode == 200) {
            if (Objects.equals(status, STATUS.OK)) {
                if (currentUrl != null && currentConnection != null) {
                    StringBuilder sb = new StringBuilder();
                    Scanner sc = null;
                    try {
                        sc = new Scanner(currentUrl.openStream());
                        while (sc.hasNext())
                            sb.append(sc.nextLine());
                    } catch (IOException ioException) {
                        sendMessage(ioException.getCause(), WARNING);
                        return null;
                    } finally {
                        assert sc != null;
                        sc.close();
                    }
                    return !jsonEncoded_ ? sb.toString() : JsonParser.parseString(sb.toString());
                }
            }
        }
        return null;
    }

    /**
     * Website response's headers
     *
     * @param jsonEncode true/false depends on you, basically what type of Headers you need.
     * @return website response Headers.
     */
    public Object getHeaders(boolean... jsonEncode) {
        boolean jsonEncoded_ = false;
        if (jsonEncode != null && jsonEncode.length > 0) {
            if (jsonEncode[0]) {
                jsonEncoded_ = true;
            }
        }
        if (currentConnection != null && currentUrl != null) {
            Map<String, List<String>> headersMap = currentConnection.getHeaderFields();
            Set<String> headersKey = headersMap.keySet();
            HashMap<String, String> headers = new HashMap<>();
            for (String h : headersKey) {
                if (jsonEncoded_) {
                    JsonArray array = new JsonArray();
                    for (String value : headersMap.get(h)) {
                        headers.put(h, value);
                    }
                } else {
                    headers.put(h, Arrays.toString(headersMap.get(h).toArray()));
                }
            }
            if (jsonEncoded_) {
                return gson.toJsonTree(headers);
            }
            return headers;
        }
        return null;
    }

    /**
     * Returning the {@link Integer} response code of the website
     *
     * @return {@link Integer} response code
     */
    public int getResponse() {
        return responseCode;
    }

    /**
     * Given InputStream of the given and connected URL
     *
     * @return {@link InputStream}
     */
    public InputStream getInputStream() {
        try {
            return currentConnection.getInputStream();
        } catch (IOException e) {
            sendMessage(e.getCause(), ERROR);
        }
        return null;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public enum METHOD {
        GET,
        POST,
        PUT,
        DELETE,
        CONNECT,
        HEAD,
        PATCH
    }

    enum STATUS {
        OK(200),
        NOT_OK(505),
        REDIRECTED(301),
        NOT_FOUND(404),
        SERVER_ERROR(500),
        UNKNOWN(0);

        STATUS(int code) {
        }

    }


}
