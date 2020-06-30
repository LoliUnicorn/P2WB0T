package pl.kamil0024.core.util;

import lombok.Getter;

@Getter
@Deprecated
public class Response {
    private long delta;
    private final boolean valid;

    Response(long delta, boolean valid) {
        this.delta = delta;
        this.valid = valid;
    }

    Response(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return String.format("%s {delta=%d,valid=%s}", super.toString(), delta, valid);
    }
}