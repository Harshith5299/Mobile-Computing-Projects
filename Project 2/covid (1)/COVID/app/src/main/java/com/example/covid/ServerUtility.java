package com.example.covid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ServerUtility {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpURLConnection;
    private OutputStream outputStream;
    private PrintWriter printWriter;

    public ServerUtility(String requestURL, String charset)
            throws IOException {

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoOutput(true); // indicates POST method
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        outputStream = httpURLConnection.getOutputStream();
        printWriter = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                    true);
    }

    public void addFile(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        printWriter.append("--")
                .append(boundary)
                .append(LINE_FEED);
        printWriter.append("Content-Disposition: form-data; name=\"")
                .append(fieldName)
                .append("\"; filename=\"")
                .append(fileName)
                .append("\"")
                .append(LINE_FEED);
        printWriter.append("Content-Type: ")
                .append(URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        printWriter.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        printWriter.append(LINE_FEED);
        printWriter.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        printWriter.append(LINE_FEED);
        printWriter.flush();
    }

    public List<String> finishRequest() throws IOException {
        List<String> response = new ArrayList<String>();

        printWriter.append(LINE_FEED).flush();
        printWriter.append("--")
                .append(boundary)
                .append("--")
                .append(LINE_FEED);
        printWriter.close();

        int status = httpURLConnection.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpURLConnection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpURLConnection.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }
}
