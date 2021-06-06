package me.limansky.beanpuree;

public class WeirdBean {
    int w;
    int x;
    int y;
    int z;

    public int getW() {
        return w;
    }

    public void setW(long w) {
        this.w = Long.valueOf(w).intValue();
    }

    public void setW(int a, int b) {
        w = a * b;
    }

    public int getX(int y) {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    int getZ() {
        return z;
    }

    int setZ() {
        return z;
    }
}
