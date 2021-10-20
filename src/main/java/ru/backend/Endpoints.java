package ru.backend;

public class Endpoints {
    public static final String UPLOAD_IMAGE = "/image";
    public static final String UPLOAD_FILE = "/upload";
    public static final String UPDATE_FILE = "/image/{imageDeleteHash}";
    public static final String DELETE_FILE = "/image/{imageDeleteHash}";
    public static final String GET_FILE = "/image/{imageHash}";
    public static final String FAVORITE_FILE = "/image/{imageHash}/favorite";
}
