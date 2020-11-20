package ru.ijo42.rbirb.model;

public enum PicType {
    PNG, GIF;

    public static PicType parseOf(String str) {
        if (str.endsWith("gif"))
            return GIF;
        else
            return PNG;
    }

    @Override
    public String toString() {
        return "." + this.name().toLowerCase();
    }
}
