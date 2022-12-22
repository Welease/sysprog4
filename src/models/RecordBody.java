package models;

public class RecordBody {
    private String len;
    private String body;

    public RecordBody(String body) {
        this.body = body.toUpperCase();
        int l = body.length();
        this.len = Integer.toHexString(l).toUpperCase();
        if (len.length() % 2 != 0)
            this.len = "0" + len;
    }

    public RecordBody(String len, String body) {
        this.body = body.toUpperCase();
        this.len = len.toUpperCase();
        if (this.len.length() % 2 != 0)
            this.len = "0" + len;
    }

    public String getLen() {
        return len;
    }

    public String getBody() {
        return body;
    }
}
