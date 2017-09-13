package me.limansky.beanpuree;

public class BeanWithPrivate {
    private int a;
    private String s;
    private long l;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    protected String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public long getL() {
        return l;
    }

    private void setL(long l) {
        this.l = l;
    }
}
