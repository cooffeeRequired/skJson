package cz.coffeerequired.api.requests;

public enum RequestStatus {
    OK("ok"),
    FAILED("failed"),
    UNKNOWN("unknown");

    final String value;

    RequestStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value.toUpperCase();
    }
}
