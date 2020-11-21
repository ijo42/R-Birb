package ru.ijo42.rbirb.model;

public enum PicType {
    /*
     * PNG: any static pictures converts to PNG
     * GIF: .gif file that has more that one frame
     */
    PNG, GIF;

    @Override
    public String toString() {
        return "." + this.name().toLowerCase();
    }
}
