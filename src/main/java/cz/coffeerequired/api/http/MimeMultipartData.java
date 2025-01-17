package cz.coffeerequired.api.http;

import com.google.errorprone.annotations.RestrictedApi;
import com.google.gson.JsonElement;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


@SuppressWarnings("all")
public class MimeMultipartData {

    private String boundary;
    @Getter
    private HttpRequest.BodyPublisher bodyPublisher;

    private MimeMultipartData() {
    }

    @RestrictedApi(link = "", explanation = "Only for setting charset")
    public static Builder newBuilder() {
        return new Builder();
    }

    @RestrictedApi(link = "", explanation = "Only for setting boundary")
    public String getContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Getter
    public enum FileType {
        TEXT_PLAIN("text/plain"),
        APPLICATION_JSON("application/json"),
        IMAGE_PNG("image/png"),
        IMAGE_JPEG("image/jpeg"),
        APPLICATION_PDF("application/pdf"),
        AUTOMATIC(""); // Automatically detect MIME type

        private final String mimeType;

        FileType(String mimeType) {
            this.mimeType = mimeType;
        }

        public static String detectMimeType(Path path) {
            try {
                String mimeType = Files.probeContentType(path);
                return mimeType != null ? mimeType : "application/octet-stream";
            } catch (IOException e) {
                return "application/octet-stream"; // Default MIME type if detection fails
            }
        }
    }

    public static class Builder {

        private final List<MimedFile> files = new ArrayList<>();
        private final Map<String, String> texts = new LinkedHashMap<>();
        private String boundary;
        private Charset charset = StandardCharsets.UTF_8;
        private String jsonContent; // Additional JSON content

        private Builder() {
            this.boundary = UUID.randomUUID().toString();
        }

        @RestrictedApi(link = "", explanation = "Only for setting charset")
        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        @RestrictedApi(link = "", explanation = "Only for setting boundary")
        public Builder withBoundary(String boundary) {
            this.boundary = boundary;
            return this;
        }

        public Builder addFile(String name, Path path, FileType fileType) {
            String mimeType = fileType == FileType.AUTOMATIC
                    ? FileType.detectMimeType(path)
                    : fileType.getMimeType();
            this.files.add(new MimedFile(name, path, mimeType));
            return this;
        }

        public Builder addText(String name, String text) {
            texts.put(name, text);
            return this;
        }

        public Builder addContent(String jsonBody) {
            this.jsonContent = jsonBody;
            return this;
        }

        public Builder addContent(JsonElement jsonBody) {
            this.jsonContent = jsonBody.toString();
            return this;
        }

        public MimeMultipartData build() throws IOException {
            MimeMultipartData mimeMultipartData = new MimeMultipartData();
            mimeMultipartData.boundary = boundary;

            var newline = "\r\n".getBytes(charset);
            var byteArrayOutputStream = new ByteArrayOutputStream();

            // Add JSON content (body)
            if (jsonContent != null) {
                byteArrayOutputStream.write(("--" + boundary).getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write("Content-Disposition: form-data; name=\"jsonBody\"".getBytes(charset)); // Minimal metadata
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write("Content-Type: application/json".getBytes(charset)); // Explicitly JSON type
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(jsonContent.getBytes(charset)); // JSON body
                byteArrayOutputStream.write(newline);
            }

            // Add text parts
            for (var entry : texts.entrySet()) {
                byteArrayOutputStream.write(("--" + boundary).getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").getBytes(charset));
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(newline);
                byteArrayOutputStream.write(entry.getValue().getBytes(charset));
                byteArrayOutputStream.write(newline);
            }

            // Add file parts
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

            // Final boundary
            byteArrayOutputStream.write(("--" + boundary + "--").getBytes(charset));
            byteArrayOutputStream.write(newline);

            mimeMultipartData.bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(byteArrayOutputStream.toByteArray());
            return mimeMultipartData;
        }

        private record MimedFile(String name, Path path, String mimeType) {
        }
    }
}
