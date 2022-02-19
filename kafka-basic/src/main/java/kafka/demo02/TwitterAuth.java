package kafka.demo02;

public enum TwitterAuth {
    AUTH_KEY("xU4tKcF2rbv10eG7q3q3NPsKT")
    , AUTH_SECRET("2CrF4iVIAFgfT7Dnm4jx0jWBX5Y2qijCnopfRZrEOUrY9JiNkb")
    , AUTH_TOKEN("1494768720228478978-yaGT9xAdxlJEGpcbvjVbeWfP5k8H5z")
    , AUTH_TOKEN_SECRET("yvfTriSUTdAkAD7TEgDxhKZdiuv3FOUPxWERDh19KIavG");

    private String value;

    TwitterAuth(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
