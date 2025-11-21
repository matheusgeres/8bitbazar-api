package br.com.eightbitbazar.application.port.out;

import java.io.InputStream;

public interface ImageStorage {

    String upload(String filename, InputStream inputStream, String contentType, long size);

    void delete(String filename);

    String getUrl(String filename);
}
