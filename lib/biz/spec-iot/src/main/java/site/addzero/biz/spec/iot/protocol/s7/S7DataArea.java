package site.addzero.biz.spec.iot.protocol.s7;

/**
 * Common S7 data areas.
 */
public enum S7DataArea {

    DB("DB"),
    I("I"),
    Q("Q"),
    M("M"),
    V("V");

    private final String code;

    S7DataArea(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
