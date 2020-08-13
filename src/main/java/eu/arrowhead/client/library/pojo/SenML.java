package eu.arrowhead.client.library.pojo;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Objects;

/**
 * Sensor Measurement Lists (SenML)
 * https://tools.ietf.org/html/rfc8428
 */
public class SenML implements Serializable {
    private String bn;
    private int bt;
    private String bu;
    private double bv;
    private double bs;
    private int bver;
    private String n;
    private String u;
    private double v;
    private String vs;
    private boolean vb;
    private String vd;
    private double s;
    private long t;
    private long ut;

    /**
     * empty constructor
     */
    public SenML() {
        this.t = System.currentTimeMillis();
    }

    /**
     * minimal required fields
     * @param n
     * @param u
     * @param v
     */
    public SenML(String n, String u, double v) {
        this.n = n;
        this.u = u;
        this.v = v;
    }

    /**
     * minimal required fields
     * @param n
     * @param u
     * @param vs
     */
    public SenML(String n, String u, String vs) {
        this.n = n;
        this.u = u;
        this.vs = vs;
    }

    /**
     * minimal required fields
     * @param n
     * @param u
     * @param vb
     */
    public SenML(String n, String u, boolean vb) {
        this.n = n;
        this.u = u;
        this.vb = vb;
    }

    /**
     * full constructor
     * @param bn
     * @param bt
     * @param bu
     * @param bv
     * @param bs
     * @param bver
     * @param n
     * @param u
     * @param v
     * @param vs
     * @param vb
     * @param vd
     * @param s
     * @param t
     * @param ut
     */
    public SenML(String bn, int bt, String bu, double bv, double bs, int bver, String n, String u, double v,
                 String vs, boolean vb, String vd, double s, long t, long ut) {
        this.bn = bn;
        this.bt = bt;
        this.bu = bu;
        this.bv = bv;
        this.bs = bs;
        this.bver = bver;
        this.n = n;
        this.u = u;
        this.v = v;
        this.vs = vs;
        this.vb = vb;
        this.vd = vd;
        this.s = s;
        this.t = t;
        this.ut = ut;
    }

    public String getBn() {
        return bn;
    }

    public void setBn(String bn) {
        this.bn = bn;
    }

    public int getBt() {
        return bt;
    }

    public void setBt(int bt) {
        this.bt = bt;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
    }

    public double getBv() {
        return bv;
    }

    public void setBv(double bv) {
        this.bv = bv;
    }

    public double getBs() {
        return bs;
    }

    public void setBs(double bs) {
        this.bs = bs;
    }

    public int getBver() {
        return bver;
    }

    public void setBver(int bver) {
        this.bver = bver;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public String getVs() {
        return vs;
    }

    public void setVs(String vs) {
        this.vs = vs;
    }

    public boolean isVb() {
        return vb;
    }

    public void setVb(boolean vb) {
        this.vb = vb;
    }

    public String getVd() {
        return vd;
    }

    public void setVd(String vd) {
        this.vd = vd;
    }

    public double getS() {
        return s;
    }

    public void setS(double s) {
        this.s = s;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public long getUt() {
        return ut;
    }

    public void setUt(long ut) {
        this.ut = ut;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SenML senML = (SenML) o;
        return Double.compare(senML.bv, bv) == 0 &&
                Double.compare(senML.bs, bs) == 0 &&
                bver == senML.bver &&
                Double.compare(senML.v, v) == 0 &&
                vb == senML.vb &&
                Double.compare(senML.s, s) == 0 &&
                t == senML.t &&
                ut == senML.ut &&
                Objects.equals(bn, senML.bn) &&
                Objects.equals(bt, senML.bt) &&
                Objects.equals(bu, senML.bu) &&
                Objects.equals(n, senML.n) &&
                Objects.equals(u, senML.u) &&
                Objects.equals(vs, senML.vs) &&
                Objects.equals(vd, senML.vd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bn, bt, bu, bv, bs, bver, n, u, v, vs, vb, vd, s, t, ut);
    }
}
