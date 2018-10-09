import java.io.Serializable;

public class MediaFile implements Serializable{

    private String fileName;
    private int fileLength;
    private int value;
    private Object fileDownloader;

    public MediaFile(String fileName, int fileLength) {
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileLength() {
        return fileLength;
    }

    public int getValue() {
        return value;
    }

    public Object getFileDownloader() {
        return fileDownloader;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setFileDownloader(Object fileDownloader) {
        this.fileDownloader = fileDownloader;
    }
}
