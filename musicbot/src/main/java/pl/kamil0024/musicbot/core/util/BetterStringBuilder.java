package pl.kamil0024.musicbot.core.util;

import lombok.Getter;

public class BetterStringBuilder {

    @Getter Number lines;
    @Getter StringBuilder stringBuilder;

    public BetterStringBuilder() {
        this.lines = 0;
        this.stringBuilder = new StringBuilder();
    }

    public BetterStringBuilder append(Object obj) {
        stringBuilder.append(obj);
        return this;
    }

    public BetterStringBuilder appendLine(Object obj) {
        stringBuilder.append(obj).append("\n");
        return this;
    }

    public String build() {
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return build();
    }

}
