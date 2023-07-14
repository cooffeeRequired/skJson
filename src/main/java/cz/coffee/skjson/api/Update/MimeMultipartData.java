package cz.coffee.skjson.api.Update;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("all")
public class MimeMultipartData {

    public static class Builder {

        private String boundary;
        private Charset charset = StandardCharsets.UTF_8;
        private final List<MimedFile> files = new ArrayList<>();
        private final Map<String, String> texts = new LinkedHashMap<>();

        private Builder() {
            this.boundary = new BigInteger(128, new Random()).toString();
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder withBoundary(String boundary) {
            this.boundary = boundary;
            return this;
        }

        public Builder addFile(String name, Path path, String mimeType) {
            this.files.add(new MimedFile(name, path, mimeType));
            return this;
        }

        public Builder addText(String name, String text) {
            texts.put(name, text);
            return this;
        }

        public MimeMultipartData build() throws IOException {
            MimeMultipartData mimeMultipartData = new MimeMultipartData();
            mimeMultipartData.boundary = boundary;

            var newline = "\r\n".getBytes(charset);
            var byteArrayOutputStream = new ByteArrayOutputStream();
            for (var f : files) {
                byteArrayOutputStream.write(("--" + boundary).getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"" + f.name + "\"; filename=\"" + f.path.getFileName() + "\"").getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(("Content-Type: " + f.mimeType).getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(Files.readAllBytes(f.path));
                byteArrayOutputStream.write(newline);
            }
            for (var entry: texts.entrySet()) {
                byteArrayOutputStream.write(("--" + boundary).getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(entry.getValue().getBytes(charset));
                byteArrayOutputStream.write(newline);
            }
            byteArrayOutputStream.write(("--" + boundary + "--").getBytes(charset));

            mimeMultipartData.bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(byteArrayOutputStream.toByteArray());
            return mimeMultipartData;
        }

        public class MimedFile {

            public final String name;
            public final Path path;
            public final String mimeType;

            public MimedFile(String name, Path path, String mimeType) {
                this.name = name;
                this.path = path;
                this.mimeType = mimeType;
            }
        }
    }

    private String boundary;
    private HttpRequest.BodyPublisher bodyPublisher;

    private MimeMultipartData() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public HttpRequest.BodyPublisher getBodyPublisher() throws IOException {
        return bodyPublisher;
    }

    public String getContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

}